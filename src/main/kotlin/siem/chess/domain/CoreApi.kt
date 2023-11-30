package siem.chess.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier
import siem.chess.domain.commandside.board.Position
import siem.chess.domain.commandside.board.constants.ChessPiece
import siem.chess.domain.commandside.board.constants.PieceColor
import java.time.LocalDateTime

/***
 * Commands
 *
 */
data class StartGameCommand(@TargetAggregateIdentifier val gameId:String, val dateTime: LocalDateTime, val whitePlayer: String, val blackPlayer: String)
data class MoveChessPieceCommand (@TargetAggregateIdentifier val gameId:String, val dateTime: LocalDateTime, val from: Position, val to: Position)

data class CastlingCommand(@TargetAggregateIdentifier val gameId: String, val dateTime: LocalDateTime, val castlingType: CastlingType)

enum class CastlingType{SHORT_WHITE, LONG_WHITE, SHORT_BLACK, LONG_BLACK}

/***
 * Events
 *
 */
data class GameStartedEvent(val gameId: String, val dateTime: LocalDateTime, val whitePlayer: String, val blackPlayer: String, val boardTextual: String)
data class ChessPieceMovedEvent(val gameId: String, val dateTime: LocalDateTime, val chessPiece: ChessPiece, val from: Position, val to: Position, val boardTextual: String)
data class CastlingAppliedEvent(val gameId: String, val dateTime: LocalDateTime, val castlingType: CastlingType, val boardTextual: String)
data class CheckEvent(val gameId: String, val dateTime: LocalDateTime, val check: PieceColor, val boardTextual: String)
data class GameEndedByCheckMateEvent(val gameId: String, val dateTime: LocalDateTime, val winner: PieceColor, val boardTextual: String)

data class MoveAttemptByWrongPlayerEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor, val from: Position, val to: Position)
data class MoveNotPossibleByMissingChessPieceEvent(val gameId: String, val dateTime: LocalDateTime, val from: Position, val to: Position)
data class MoveNotPossibleByWrongTargetEvent(val gameId: String, val dateTime: LocalDateTime, val chessPiece: ChessPiece, val from: Position, val to: Position)

data class CastlingNotPossibleEvent(val gameId: String, val castlingType: CastlingType)




/***
 * Queries
 *
 */
data class ChessMoveQuery(val gameId: String)

data class ChessGameLogInfoQuery(val gameId: String)



