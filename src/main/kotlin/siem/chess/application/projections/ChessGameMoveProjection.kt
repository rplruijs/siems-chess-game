package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import siem.chess.application.repositories.ChessGameMoveRepository
import siem.chess.domain.CastlingAppliedEvent
import siem.chess.domain.ChessMoveQuery
import siem.chess.domain.ChessPieceMovedEvent
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.queryside.ChessGameMove
import java.time.LocalDateTime

@Component
class ChessGameMoveProjection(val queryUpdateEmitter: QueryUpdateEmitter,
                              val chessGameMoveRepository: ChessGameMoveRepository  ) {

    @EventHandler
    fun handle(event: ChessPieceMovedEvent) {
        val chessGameMove = ChessGameMove(event.gameId, event.dateTime, event.from.toString(), event.to.toString(), event.boardTextual)
        chessGameMoveRepository.insertChessGameMove(chessGameMove)
        queryUpdateEmitter.emit(
            ChessMoveQuery::class.java,
            { query: ChessMoveQuery -> event.gameId ==  query.gameId },
            chessGameMove
        )
    }


    @EventHandler
    fun handle(event: CastlingAppliedEvent) {

    }

    @QueryHandler
    fun handle(query: ChessMoveQuery): ChessGameMove {
        return ChessGameMove(query.gameId, LocalDateTime.now(),"init", "init", boardTextualOpeningSettling())
    }

}