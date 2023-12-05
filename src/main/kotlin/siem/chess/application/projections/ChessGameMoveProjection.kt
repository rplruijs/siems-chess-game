package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import siem.chess.adapter.`in`.rest.CastlingMove
import siem.chess.application.repositories.ChessGameMoveRepository
import siem.chess.domain.CastlingAppliedEvent
import siem.chess.domain.CastlingType
import siem.chess.domain.ChessMoveQuery
import siem.chess.domain.ChessPieceMovedEvent
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.queryside.ChessGameMove
import java.time.LocalDateTime

@Component
class ChessGameMoveProjection(val queryUpdateEmitter: QueryUpdateEmitter,
                              val chessGameMoveRepository: ChessGameMoveRepository  ) {

    @EventHandler
    fun handle(event: ChessPieceMovedEvent) {
        val chessGameMove =
            ChessGameMove(event.gameId, event.dateTime, event.from.toString(), event.to.toString(), event.boardTextual)
        saveAndEmit(chessGameMove)
    }

    @EventHandler
    fun handle(event: CastlingAppliedEvent) {
        val castlingType = toCastlingType(event.castlingMove, event.pieceColor)
        val kingChessGameMove = ChessGameMove(
            event.gameId,
            event.dateTime,
            castlingType.kingMove.from.toString(),
            castlingType.kingMove.to.toString(),
            event.boardTextual
        )
        val rookChessGameMove = ChessGameMove(
            event.gameId,
            event.dateTime,
            castlingType.rookMove.from.toString(),
            castlingType.rookMove.to.toString(),
            event.boardTextual
        )
        saveAndEmit(kingChessGameMove)
        saveAndEmit(rookChessGameMove)
    }

    private fun saveAndEmit(chessGameMove: ChessGameMove) {
        chessGameMoveRepository.insertChessGameMove(chessGameMove)
        queryUpdateEmitter.emit(
            ChessMoveQuery::class.java,
            { query: ChessMoveQuery -> chessGameMove.gameId == query.gameId },
            chessGameMove
        )
    }

    @QueryHandler
    fun handle(query: ChessMoveQuery): ChessGameMove {
        return ChessGameMove(query.gameId, LocalDateTime.now(), "init", "init", boardTextualOpeningSettling())
    }

    private fun toCastlingType(castlingMove: CastlingMove, pieceColor: PieceColor): CastlingType {
        return when (castlingMove) {

            CastlingMove.CASTLING_SHORT -> {
                when (pieceColor) {
                    PieceColor.WHITE -> CastlingType.SHORT_WHITE
                    PieceColor.BLACK -> CastlingType.SHORT_BLACK
                }
            }

            CastlingMove.CASTLING_LONG -> {
                when (pieceColor) {
                    PieceColor.WHITE -> CastlingType.LONG_WHITE
                    PieceColor.BLACK -> CastlingType.LONG_BLACK
                }
            }
        }
    }


}