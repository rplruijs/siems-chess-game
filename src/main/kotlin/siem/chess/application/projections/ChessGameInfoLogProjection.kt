package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import siem.chess.adapter.`in`.rest.CastlingMove
import siem.chess.application.repositories.ChessGameInfoLogRepository
import siem.chess.domain.*
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.queryside.*

@Component
class ChessGameInfoLogProjection(val queryUpdateEmitter: QueryUpdateEmitter,
                                 val chessGameInfoLogRepository: ChessGameInfoLogRepository) {

    @EventHandler
    fun handle(event: GameStartedEvent) {
        handleLogMessage(toInfoLogMessage(event))
    }

    @EventHandler
    fun handle(event: ChessPieceMovedEvent) {
        handleLogMessage(toInfoLogMessage(event))
    }

    @EventHandler
    fun handle(event: CheckEvent) {
        handleLogMessage(toInfoLogMessage(event))
    }

    @EventHandler
    fun handle(event: GameEndedByCheckMateEvent) {
        handleLogMessage(toInfoLogMessage(event))
    }

    @EventHandler
    fun handle(event: MoveNotPossibleByWrongTargetEvent) {
        handleLogMessage(toErrorLogMessage(event))
    }

    @EventHandler
    fun handle(event: MoveAttemptByWrongPlayerEvent ) {
        handleLogMessage(toErrorLogMessage(event))
    }


    @EventHandler
    fun handle(event: CastlingAppliedEvent) {
        handleLogMessage(toInfoLogMessage(event))
    }

    @EventHandler
    fun handle(event: CastlingNotPossibleEvent) {
        handleLogMessage(toErrorLogMessage(event))
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

    private fun toInfoLogMessage(event: Any): LogMessage {
        return when(event) {
            is GameStartedEvent -> LogMessage(
                gameId = event.gameId,
                logTime =  event.dateTime,
                logMessageType = LogMessageType.GAME_STARTED,
                causedBy = ActorType.SYSTEM,
                message = "Game started between ${event.whitePlayer} playing with white against ${event.blackPlayer} playing with black",
                )
            is ChessPieceMovedEvent  -> {
                val move = "${event.chessPiece} -> ${event.from} - ${event.to}"
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.MOVE,
                    causedBy = toActorType(event.chessPiece.color),
                    message = move)
            }

            is CheckEvent -> {
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.CHECK,
                    causedBy = toActorType(event.check.opposite()),
                    message = "Check")
            }
            is GameEndedByCheckMateEvent -> {
                LogMessage(gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.CHECK_MATE,
                    causedBy = toActorType(event.winner),
                    message = "Game ended by check mate. Winner is ${event.winner}")

            }
            is CastlingAppliedEvent -> {
                LogMessage(gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = toLogMessageType(event.castlingMove),
                    causedBy = toActorType(event.pieceColor),
                    message = toInfoLogMessage(event.castlingMove))
            }
            else                     -> throw IllegalStateException("Unsupported event")
        }
    }

    private fun toActorType(color: PieceColor?): ActorType {
        return when(color) {
            PieceColor.WHITE -> ActorType.WHITE_PLAYER
            PieceColor.BLACK -> ActorType.BLACK_PLAYER
            else             -> ActorType.SYSTEM
        }
    }

    private fun toErrorLogMessage(event: Any): LogMessage {
        return when(event) {
            is MoveNotPossibleByWrongTargetEvent -> {
                val wrongMoveMessage = "Not possible to move ${event.chessPiece} from ${event.from} to ${event.to}"
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.WRONG_MOVE,
                    causedBy = toActorType(event.chessPiece.color),
                    message = wrongMoveMessage,
                    logLevel = LogLevel.ERROR)
            }
            is MoveAttemptByWrongPlayerEvent -> {
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.WRONG_MOVE,
                    causedBy = toActorType(event.pieceColor),
                    message = "${event.pieceColor.opposite()} turn please",
                    logLevel = LogLevel.ERROR)
            }
            is CastlingNotPossibleEvent -> {
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = toLogMessageType(event.castlingMove),
                    causedBy = toActorType(event.pieceColor),
                    message =toLogMessageWrong(event.castlingMove),
                    logLevel = LogLevel.ERROR)
            }
            else                     -> throw IllegalStateException("Unsupported event")
        }
    }

    private fun toLogMessageType(castlingMove: CastlingMove) : LogMessageType {
        return  when(castlingMove) {
            CastlingMove.CASTLING_SHORT -> LogMessageType.SHORT_CASTLING
            CastlingMove.CASTLING_LONG  -> LogMessageType.LONG_CASTLING
        }
    }
    private fun toInfoLogMessage(castlingMove: CastlingMove): String {
        return when(castlingMove) {
            CastlingMove.CASTLING_SHORT -> "Short castling applied"
            CastlingMove.CASTLING_LONG  -> "Long castling applied"
        }
    }

    private fun toLogMessageWrong(castlingMove: CastlingMove): String {
        return when(castlingMove) {
            CastlingMove.CASTLING_SHORT -> "Short castling not possible with current settling"
            CastlingMove.CASTLING_LONG  -> "Long castling not possible with current settling"
        }
    }
}