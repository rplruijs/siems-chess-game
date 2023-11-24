package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import siem.chess.application.repositories.ChessGameInfoLogRepository
import siem.chess.domain.*
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.queryside.LogMessageType
import siem.chess.domain.queryside.ChessGameLog
import siem.chess.domain.queryside.LogMessage

@Component
class ChessGameInfoLogProjection(val queryUpdateEmitter: QueryUpdateEmitter,
                                 val chessGameInfoLogRepository: ChessGameInfoLogRepository,
                                 val engine: TemplateEngine) {

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


                val move = "${event.from} - ${event.to}"
                LogMessage(event.gameId, event.dateTime, LogMessageType.WRONG_MOVE_BY_WHITE,  move)
            }

            else                     -> throw IllegalStateException("Unsupported event")
        }
    }


}