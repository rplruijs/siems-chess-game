package siem.chess.domain.queryside

import java.time.LocalDateTime


data class ChessGame(val gameId: String, val startDate: LocalDateTime, val endDate: LocalDateTime, val playerPlayingWhite: String, val playerPlayingBlack: String)

data class ChessGameMove(val gameId: String, val executionTime: LocalDateTime, val from: String, val to: String, val settling: String)

data class ChessGameLog(val gameId: String, val entries: List<LogMessage>)


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