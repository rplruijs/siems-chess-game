package siem.chess.domain.queryside

import java.time.LocalDateTime


data class ChessGame(val gameId: String, val startDate: LocalDateTime, val endDate: LocalDateTime, val playerPlayingWhite: String, val playerPlayingBlack: String)

data class ChessGameMove(val gameId: String, val executionTime: LocalDateTime, val from: String, val to: String, val settling: String)

data class ChessGameLog(val gameId: String, val entries: List<LogMessage>)


data class LogMessage(val gameId: String, val logTime: LocalDateTime, val logMessageType: LogMessageType, val message: String)



val BLACK = "text-black"
val WHITE = "text-white"
val RED = "text-red"

enum class LogMessageType(color:String) {
    GAME_STARTED(BLACK),
    MOVE_BY_WHITE(WHITE),
    MOVE_BY_BLACK(BLACK),
    WRONG_MOVE_BY_BLACK(RED),
    WRONG_MOVE_BY_WHITE(RED),
    CHECK_BY_WHITE(WHITE),
    CHECK_BY_BLACK(BLACK),
    CHECK_MATE_BY_WHITE(WHITE),
    SHORT_CASTLING_WHITE(WHITE),
    SHORT_CASTLING_BLACK(BLACK),
    LONG_CASTLING_WHITE(WHITE),
    LONG_CASTLING_BLACK(BLACK),
    WRONG_SHORT_CASTLING_WHITE(RED),
    WRONG_SHORT_CASTLING_BLACK(RED),
    WRONG_LONG_CASTLING_WHITE(RED),
    WRONG_LONG_CASTLING_BLACK(RED),
    CHECK_MATE_BY_BLACK(BLACK),
}