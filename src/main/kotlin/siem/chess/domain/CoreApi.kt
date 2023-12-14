package siem.chess.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import siem.chess.domain.commandside.board.Position
import siem.chess.domain.commandside.board.constants.*
import siem.chess.domain.commandside.gamestatus.Move
import java.time.LocalDateTime

/***
 * Commands
 *
 */
data class StartGameCommand(@TargetAggregateIdentifier val gameId:String, val dateTime: LocalDateTime, val whitePlayer: String, val blackPlayer: String)
data class MoveChessPieceCommand (@TargetAggregateIdentifier val gameId:String, val dateTime: LocalDateTime, val from: Position, val to: Position)

data class ShortCastlingCommand(@TargetAggregateIdentifier val gameId: String, val dateTime: LocalDateTime)
data class LongCastlingCommand(@TargetAggregateIdentifier val gameId: String, val dateTime: LocalDateTime)

enum class CastlingType(val kingMove: Move, val rookMove: Move){
   SHORT_WHITE (kingMove = Move(piece = WHITE_KING, from=E1, to=G1), rookMove = Move(piece = WHITE_ROOK, H1, F1)),
   LONG_WHITE  (kingMove = Move(piece = WHITE_KING, from=E1, to=C1), rookMove = Move(piece = WHITE_ROOK, A1, D1)),
   SHORT_BLACK (kingMove = Move(piece = BLACK_KING, from=E8, to=G8), rookMove = Move(piece = BLACK_ROOK, H8, F8)),
   LONG_BLACK  (kingMove = Move(piece = BLACK_KING, from=E8, to=C8), rookMove = Move(piece = BLACK_ROOK, A8, D8));

   fun isShortCastling(): Boolean {
      return this == SHORT_WHITE || this == SHORT_BLACK
   }

   fun isLongCastling(): Boolean {
      return this == LONG_WHITE || this == LONG_BLACK
   }
}

/***
 * Events
 *
 */
data class GameStartedEvent(val gameId: String, val dateTime: LocalDateTime, val whitePlayer: String, val blackPlayer: String, val boardTextual: String)
data class ChessPieceMovedEvent(val gameId: String, val dateTime: LocalDateTime, val chessPiece: ChessPiece, val from: Position, val to: Position, val boardTextual: String)
data class ShortCastlingAppliedEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor, val boardTextual: String)
data class LongCastlingAppliedEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor, val boardTextual: String)
data class CheckEvent(val gameId: String, val dateTime: LocalDateTime, val check: PieceColor, val boardTextual: String)
data class GameEndedByCheckMateEvent(val gameId: String, val dateTime: LocalDateTime, val winner: PieceColor, val boardTextual: String)

data class MoveAttemptByWrongPlayerEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor, val from: Position, val to: Position)
data class MoveNotPossibleByMissingChessPieceEvent(val gameId: String, val dateTime: LocalDateTime, val from: Position, val to: Position)
data class MoveNotPossibleByWrongTargetEvent(val gameId: String, val dateTime: LocalDateTime, val chessPiece: ChessPiece, val from: Position, val to: Position)

data class ShortCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor)
data class LongCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor)

data class ShortCastlingNotPossibleBecauseWrongSettlingEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor)
data class LongCastlingNotPossibleBecauseWrongSettlingEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor)

data class ShortCastlingNotPossibleAnyMoreEvent(val gameId: String, val dateTime: LocalDateTime, val color: PieceColor)

data class LongCastlingNotPossibleAnyMoreEvent(val gameId: String, val dateTime: LocalDateTime, val color: PieceColor)


/***
 * Queries
 *
 */
data class ChessMoveQuery(val gameId: String)

data class ChessGameLogInfoQuery(val gameId: String)
data class ChessGameTurnQuery(val gameId: String)



