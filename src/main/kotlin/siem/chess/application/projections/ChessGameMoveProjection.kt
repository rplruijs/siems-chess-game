package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import siem.chess.application.repositories.ChessGameMoveRepository
import siem.chess.domain.*
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
    fun handle(event: ShortCastlingAppliedEvent) {
        val castlingType = toCastlingTypeShort(event.pieceColor)
        saveCastlingMoves(event.gameId, event.dateTime, event.boardTextual, castlingType)
    }

    @EventHandler
    fun handle(event: LongCastlingAppliedEvent) {
        val castlingType = toCastlingTypeLong(event.pieceColor)
        saveCastlingMoves(event.gameId, event.dateTime, event.boardTextual, castlingType)
    }

    private fun saveCastlingMoves(gameId: String, dateTime: LocalDateTime, boardTextual: String, castlingType: CastlingType) {
        val kingChessGameMove = ChessGameMove(
            gameId,
            dateTime,
            castlingType.kingMove.from.toString(),
            castlingType.kingMove.to.toString(),
            boardTextual
        )
        val rookChessGameMove = ChessGameMove(
            gameId,
            dateTime,
            castlingType.rookMove.from.toString(),
            castlingType.rookMove.to.toString(),
            boardTextual
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

    private fun toCastlingTypeShort(color: PieceColor): CastlingType {
        return when (color) {
            PieceColor.WHITE -> CastlingType.SHORT_WHITE
            PieceColor.BLACK -> CastlingType.SHORT_BLACK
        }
    }

    private fun toCastlingTypeLong(color: PieceColor): CastlingType {
        return when (color) {
            PieceColor.WHITE -> CastlingType.LONG_WHITE
            PieceColor.BLACK -> CastlingType.LONG_BLACK
        }
    }
}