package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import siem.chess.application.repositories.ChessGameInfoLogRepository
import siem.chess.domain.*
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.queryside.ChessGameLog
import siem.chess.domain.queryside.LogMessage
import siem.chess.domain.queryside.LogMessageType

@Component
class ChessGameInfoLogProjection(val queryUpdateEmitter: QueryUpdateEmitter,
                                 val chessGameInfoLogRepository: ChessGameInfoLogRepository) {

    @EventHandler
    fun handle(event: GameStartedEvent) {
        handleLogMessage(toLogMessage(event))
    }

    @EventHandler
    fun handle(event: ChessPieceMovedEvent) {
        handleLogMessage(toLogMessage(event))
    }

    @EventHandler
    fun handle(event: CheckEvent) {
        handleLogMessage(toLogMessage(event))
    }

    @EventHandler
    fun handle(event: GameEndedByCheckMateEvent) {
        handleLogMessage(toLogMessage(event))
    }

    @EventHandler
    fun handle(event: MoveNotPossibleByWrongTargetEvent) {
        handleLogMessage(toLogMessage(event))
    }

    @EventHandler
    fun handle(event: CastlingAppliedEvent) {
        handleLogMessage(toLogMessage(event))
    }

    @EventHandler
    fun handle(event: CastlingNotPossibleEvent) {
        handleLogMessage(toLogMessage(event))
    }

    @QueryHandler
    fun handle(chessGameLogInfoQuery: ChessGameLogInfoQuery): ChessGameLog {
        return chessGameInfoLogRepository.getChessGameLog(chessGameLogInfoQuery.gameId)
    }

    private fun handleLogMessage(logMessage: LogMessage) {

        val currentLogEntries = chessGameInfoLogRepository.getChessGameLog(logMessage.gameId).entries
        val toBeChessGameLog = ChessGameLog(gameId = logMessage.gameId, entries = currentLogEntries + listOf(logMessage))

        queryUpdateEmitter.emit(
            ChessGameLogInfoQuery::class.java,
            { query: ChessGameLogInfoQuery -> logMessage.gameId ==  query.gameId },
            toBeChessGameLog
        )
        chessGameInfoLogRepository.insertLogMessage(logMessage)
    }

    private fun toLogMessage(event: Any): LogMessage {
        return when(event) {
            is GameStartedEvent -> LogMessage(event.gameId, event.dateTime,LogMessageType.GAME_STARTED,  "Game started between ${event.whitePlayer} playing with white against ${event.blackPlayer} playing with black")
            is ChessPieceMovedEvent  -> {
                val move = "${event.chessPiece} -> ${event.from} - ${event.to}"
                when(event.chessPiece.color) {
                    PieceColor.WHITE -> LogMessage(event.gameId, event.dateTime, LogMessageType.MOVE_BY_WHITE,  move)
                    PieceColor.BLACK -> LogMessage(event.gameId, event.dateTime, LogMessageType.MOVE_BY_BLACK, move)
                }
            }
            is CheckEvent -> {
                when(event.check) {
                    PieceColor.WHITE -> LogMessage(event.gameId, event.dateTime, LogMessageType.CHECK_BY_BLACK, "Check by black")
                    PieceColor.BLACK -> LogMessage(event.gameId, event.dateTime, LogMessageType.CHECK_BY_WHITE, "Check by white")
                }
            }
            is GameEndedByCheckMateEvent -> {
                when(event.winner) {
                    PieceColor.WHITE -> LogMessage(event.gameId, event.dateTime, LogMessageType.CHECK_MATE_BY_WHITE, "Game won by white")
                    PieceColor.BLACK -> LogMessage(event.gameId, event.dateTime, LogMessageType.CHECK_MATE_BY_BLACK, "Game won by black")
                }
            }
            is MoveNotPossibleByWrongTargetEvent -> {

                val logMessageType = when(event.chessPiece.color) {
                    PieceColor.WHITE -> LogMessageType.WRONG_MOVE_BY_WHITE
                    PieceColor.BLACK -> LogMessageType.WRONG_MOVE_BY_BLACK
                }
                val wrongMoveMessage = "Not possible to move ${event.chessPiece} from ${event.from} to ${event.to}"
                LogMessage(event.gameId, event.dateTime, logMessageType,  wrongMoveMessage)
            }
            is CastlingAppliedEvent -> {
                LogMessage(event.gameId, event.dateTime, toLogMessageType(event.castlingType), toLogMessage(event.castlingType))
            }
            is CastlingNotPossibleEvent -> {
                LogMessage(event.gameId, event.dateTime, toLogMessageTypeWrong(event.castlingType), toLogMessageWrong(event.castlingType))
            }

            else                     -> throw IllegalStateException("Unsupported event")
        }
    }

    private fun toLogMessageType(castlingType: CastlingType) : LogMessageType {
        return when(castlingType) {
            CastlingType.SHORT_WHITE -> LogMessageType.SHORT_CASTLING_WHITE
            CastlingType.SHORT_BLACK -> LogMessageType.SHORT_CASTLING_BLACK
            CastlingType.LONG_WHITE  -> LogMessageType.LONG_CASTLING_WHITE
            CastlingType.LONG_BLACK  -> LogMessageType.LONG_CASTLING_BLACK
        }
    }
    private fun toLogMessage(castlingType: CastlingType): String {
        return when(castlingType) {
            CastlingType.SHORT_WHITE -> "Short castling done by white"
            CastlingType.SHORT_BLACK -> "Short castling done by black"
            CastlingType.LONG_WHITE  -> "Long castling done by white"
            CastlingType.LONG_BLACK  -> "Long castling done by black"
        }
    }

    private fun toLogMessageTypeWrong(castlingType: CastlingType) : LogMessageType {
        return when(castlingType) {
            CastlingType.SHORT_WHITE -> LogMessageType.WRONG_SHORT_CASTLING_WHITE
            CastlingType.SHORT_BLACK -> LogMessageType.WRONG_SHORT_CASTLING_BLACK
            CastlingType.LONG_WHITE  -> LogMessageType.WRONG_LONG_CASTLING_WHITE
            CastlingType.LONG_BLACK  -> LogMessageType.WRONG_LONG_CASTLING_BLACK
        }
    }
    private fun toLogMessageWrong(castlingType: CastlingType): String {
        return when(castlingType) {
            CastlingType.SHORT_WHITE -> "Wrong short castling done by white"
            CastlingType.SHORT_BLACK -> "Wrong short castling done by black"
            CastlingType.LONG_WHITE  -> "Wrong long castling done by white"
            CastlingType.LONG_BLACK  -> "Wring long castling done by black"
        }
    }
}