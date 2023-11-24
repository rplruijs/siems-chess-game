package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import siem.chess.application.repositories.ChessGameMoveRepository
import siem.chess.domain.ChessMoveQuery
import siem.chess.domain.ChessPieceMovedEvent
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.toSquares
import siem.chess.domain.queryside.ChessGameMove
import java.sql.Types
import java.time.LocalDateTime

@Component
class ChessGameMoveProjection(val queryUpdateEmitter: QueryUpdateEmitter,
                              val engine: TemplateEngine,
                              val chessGameMoveRepository: ChessGameMoveRepository  ) {

    @EventHandler
    fun handle(event: ChessPieceMovedEvent) {
        val chessGameMove = ChessGameMove(event.gameId, event.dateTime, event.from.toString(), event.to.toString(), toHtmlChessBoard(event.boardTextual))
        chessGameMoveRepository.insertChessGameMove(chessGameMove)
        queryUpdateEmitter.emit(
            ChessMoveQuery::class.java,
            { query: ChessMoveQuery -> event.gameId ==  query.gameId },
            chessGameMove
        )
    }

    @QueryHandler
    fun handle(query: ChessMoveQuery): ChessGameMove {
        return ChessGameMove(query.gameId, LocalDateTime.now(),"init", "init", toHtmlChessBoard(
            boardTextualOpeningSettling()
        ))
    }

    fun toHtmlChessBoard(settling: String): String {
        val context = Context()
        context.setVariable("squares", toSquares(settling))
        return engine.process("chess-board-standalone", context).replace(Regex("[\\r\\n]"), "")
    }
}