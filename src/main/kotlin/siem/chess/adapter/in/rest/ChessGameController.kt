package siem.chess.adapter.`in`.rest

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Controller

import org.springframework.web.bind.annotation.*
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import reactor.core.publisher.Flux

import siem.chess.adapter.`in`.rest.exceptions.InvalidChessMoveException

import siem.chess.domain.*
import siem.chess.domain.commandside.board.Position
import siem.chess.domain.queryside.ChessGame
import siem.chess.domain.queryside.ChessGameLog
import siem.chess.domain.queryside.ChessGameMove
import siem.chess.domain.queryside.LogMessage

import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Controller
@RequestMapping("/api/chess-games")
@ResponseBody
class ChessGameController(val commandGateway: CommandGateway, val queryGateway: QueryGateway, val engine: TemplateEngine) {

    @PostMapping("/start")
    fun start(@RequestParam whitePlayer: String, @RequestParam blackPlayer: String, response: HttpServletResponse): String {
        val gameId = UUID.randomUUID().toString()
        commandGateway.send<StartGameCommand>(StartGameCommand(
            gameId = gameId,
            dateTime = LocalDateTime.now(),
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer)
        )
        addGameIdAsCookie(gameId, response)
        return "$whitePlayer speelt met wit tegen $blackPlayer met zwart."
    }

    @PostMapping("/move")
    fun move(@RequestParam from: String, @RequestParam to: String, request: HttpServletRequest) {

        val from: Position = parseChessPosition(from) ?: throw InvalidChessMoveException("from position $from is invalid")
        val to: Position = parseChessPosition(to)   ?: throw InvalidChessMoveException("to position $to is invalid")

        commandGateway.send<MoveChessPieceCommand>(
            MoveChessPieceCommand(gameId = extractGameIdFromCookie(request),
                dateTime = LocalDateTime.now(),
                from = from,
                to = to)
        )
    }

    @GetMapping("/updates/move")
    fun updates(request: HttpServletRequest) : Flux<ServerSentEvent<HtmlSnippet>> {
        val query = queryGateway.subscriptionQuery(
            ChessMoveQuery(gameId = extractGameIdFromCookie(request)),
            ResponseTypes.instanceOf(ChessGameMove::class.java),
            ResponseTypes.instanceOf(ChessGameMove::class.java))

        return query.initialResult()
            .concatWith( query.updates())
                .map { ServerSentEvent.builder(it.settling).build() }

    }

    @GetMapping("/updates/log")
    fun moveUpdates(request: HttpServletRequest) : Flux<ServerSentEvent<HtmlSnippet>> {
        val query = queryGateway.subscriptionQuery(
            ChessGameLogInfoQuery(gameId = extractGameIdFromCookie(request)),
            ResponseTypes.instanceOf(ChessGameLog::class.java),
            ResponseTypes.instanceOf(ChessGameLog::class.java))

        return query.initialResult()
            .concatWith( query.updates())
            .map { ServerSentEvent.builder(toHtmlChessLogTable(it.entries)).build() }
    }

    private fun toHtmlChessLogTable(log: List<LogMessage>): HtmlSnippet {
        val context = Context()
        context.setVariable("log", log)
        return engine.process("chess-game-log", context).replace(Regex("[\\r\\n]"), "")
    }

    @GetMapping
    fun chessGames(): CompletableFuture<List<ChessGame>> {
        return queryGateway.query("allChessGames", "allChessGames", ResponseTypes.multipleInstancesOf(ChessGame::class.java));
    }

    private fun addGameIdAsCookie(gameId: String, response: HttpServletResponse) {
        val cookie = Cookie("gameId", gameId)
        cookie.maxAge = 3600 // Set the cookie's max age in seconds
        cookie.secure = true // Set whether the cookie should only be sent over secure connections
        cookie.isHttpOnly = true // Set whether the cookie should be accessible only through HTTP requests
        cookie.path = "/" // Set the cookie's path

        // Add the cookie to the HTTP response
        response.addCookie(cookie)
    }

    private fun extractGameIdFromCookie(request: HttpServletRequest): String {
        return request.cookies.find { it.name == "gameId"}?.value ?: throw InvalidChessMoveException("Unable to extract gameId")
    }
}

typealias HtmlSnippet =  String