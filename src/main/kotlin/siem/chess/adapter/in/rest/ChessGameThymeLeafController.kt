package siem.chess.adapter.`in`.rest

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Controller
import org.springframework.ui.Model

import org.springframework.web.bind.annotation.*
import siem.chess.domain.commandside.board.generateChessBoardWithOpeningSettling
import siem.chess.domain.queryside.ChessGame
import java.util.concurrent.CompletableFuture

@Controller
@RequestMapping("/view/chess-games")
class ChessGameThymeLeafController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

    @GetMapping
    fun chessGames(): CompletableFuture<List<ChessGame>> {
        return queryGateway.query("allChessGames", "allChessGames", ResponseTypes.multipleInstancesOf(ChessGame::class.java));
    }

    @GetMapping("/board")
    fun chessBoard(model: Model): String {
        model.addAttribute("chessBoard", generateChessBoardWithOpeningSettling())
        return "chess-board"
    }
}