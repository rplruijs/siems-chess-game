package siem.chess.domain.queryside

import java.time.LocalDateTime


data class ChessGame(val gameId: String, val startDate: LocalDateTime, val endDate: LocalDateTime, val playerPlayingWhite: String, val playerPlayingBlack: String)

data class ChessGameMove(val gameId: String, val executionTime: LocalDateTime, val from: String, val to: String, val settling: String)

data class ChessGameLog(val gameId: String, val entries: List<LogMessage>)


data class LogMessage(val gameId: String, val logTime: LocalDateTime, val logMessageType: LogMessageType, val message: String)

enum class LogMessageType {
    GAME_STARTED,
    MOVE_BY_WHITE,
    MOVE_BY_BLACK,
    WRONG_MOVE_BY_BLACK,
    WRONG_MOVE_BY_WHITE,
    CHECK_BY_WHITE,
    CHECK_BY_BLACK,
    CHECK_MATE_BY_WHITE,
    SHORT_CASTLING_WHITE,
    SHORT_CASTLING_BLACK,
    LONG_CASTLING_WHITE,
    LONG_CASTLING_BLACK,
    CHECK_MATE_BY_BLACK
}