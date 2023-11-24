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

/***
 * Events
 *
 */
data class GameStartedEvent(val gameId: String, val dateTime: LocalDateTime, val whitePlayer: String, val blackPlayer: String, val boardTextual: String)
data class MoveAttemptByWrongPlayerEvent(val gameId: String, val dateTime: LocalDateTime, val pieceColor: PieceColor, val from: Position, val to: Position)
data class ChessPieceMovedEvent(val gameId: String, val dateTime: LocalDateTime, val chessPiece: ChessPiece, val from: Position, val to: Position, val boardTextual: String)
data class MoveNotPossibleByMissingChessPieceEvent(val gameId: String, val dateTime: LocalDateTime, val from: Position, val to: Position)
data class MoveNotPossibleByWrongTargetEvent(val gameId: String, val dateTime: LocalDateTime, val chessPiece: ChessPiece, val from: Position, val to: Position)
data class CheckEvent(val gameId: String, val dateTime: LocalDateTime, val check: PieceColor, val byPiece: ChessPiece)
data class GameEndedByCheckMateEvent(val gameId: String, val dateTime: LocalDateTime, val winner: PieceColor)




/***
 * Queries
 *
 */
data class ChessMoveQuery(val gameId: String)

data class ChessGameLogInfoQuery(val gameId: String)



