package siem.chess.domain.queryside

import siem.chess.domain.commandside.board.constants.PieceColor
import java.time.LocalDateTime


data class ChessGame(val gameId: String,
                     val startDate: LocalDateTime,
                     val endDate: LocalDateTime,
                     val playerPlayingWhite: String,
                     val playerPlayingBlack: String)

data class ChessGameMove(val gameId: String,
                         val executionTime: LocalDateTime,
                         val from: String,
                         val to: String,
                         val settling: String)

data class ChessGameLog(val gameId: String,
                        val entries: List<LogMessage>)

data class ChessGameState(val gameId: String,
                          val gameState: GameState,
                          val currentTurn: PieceColor,
                          val turnNumber: Int,
                          val castlingShortStillPossibleByWhite: Boolean,
                          val castlingLongStillPossibleByWhite: Boolean,
                          val castlingShortStillPossibleByBlack: Boolean,
                          val castlingLongStillPossibleByBlack: Boolean,
                          val settling: String) {

    fun castlingNotPossibleFor(color: PieceColor): ChessGameState {
        return when(color) {
            PieceColor.WHITE -> this.copy(castlingShortStillPossibleByWhite = false, castlingLongStillPossibleByWhite = false)
            PieceColor.BLACK -> this.copy(castlingShortStillPossibleByBlack = false, castlingLongStillPossibleByBlack = false)
        }
    }


    fun castlingShortNotPossible(color: PieceColor): ChessGameState {
        return when(color) {
            PieceColor.WHITE -> this.copy(castlingShortStillPossibleByWhite = false)
            PieceColor.BLACK -> this.copy(castlingShortStillPossibleByBlack = false)
        }
    }

    fun castlingLongNotPossible(color: PieceColor): ChessGameState {
        return when(color) {
            PieceColor.WHITE -> this.copy(castlingLongStillPossibleByWhite = false)
            PieceColor.BLACK -> this.copy(castlingLongStillPossibleByBlack = false)
        }
    }

    fun defaultChessGameStateFlip(boardTextual: String): ChessGameState {
        return this.copy(
            currentTurn = this.currentTurn.opposite(),
            turnNumber = this.turnNumber + 1,
            settling = boardTextual
        )
    }

    fun endedGame(): ChessGameState {
        return this.copy(gameState = GameState.ENDED)
    }
}


data class LogMessage(val gameId: String,
                      val logTime: LocalDateTime,
                      val message: String,
                      val causedBy: ActorType,
                      val logMessageType: LogMessageType,
                      val logLevel: LogLevel =  LogLevel.INFO)

enum class LogMessageType {
    GAME_STARTED,
    MOVE,
    WRONG_MOVE,
    CHECK,
    CHECK_MATE,
    SHORT_CASTLING,
    LONG_CASTLING,
    WRONG_SHORT_CASTLING,
    WRONG_LONG_CASTLING
}

enum class LogLevel {
    INFO, WARN, ERROR
}

enum class ActorType {
    SYSTEM, WHITE_PLAYER, BLACK_PLAYER
}

enum class GameState {
    CREATED, STARTED, PAUSED, ENDED
}
