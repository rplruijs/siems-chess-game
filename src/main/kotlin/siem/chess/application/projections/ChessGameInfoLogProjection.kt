package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
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
    fun handle(event: LongCastlingAppliedEvent) {
        handleLogMessage(toInfoLogMessage(event))
    }

    @EventHandler
    fun handle(event: ShortCastlingAppliedEvent) {
        handleLogMessage(toInfoLogMessage(event))
    }

    @EventHandler
    fun handle(event: ShortCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent) {
        handleLogMessage(toErrorLogMessage(event))
    }

    @EventHandler
    fun handle(event: LongCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent) {
        handleLogMessage(toErrorLogMessage(event))
    }

    @EventHandler
    fun handle(event: LongCastlingNotPossibleBecauseWrongSettlingEvent){
        handleLogMessage(toErrorLogMessage(event))
    }

    @EventHandler
    fun handle(event: ShortCastlingNotPossibleBecauseWrongSettlingEvent){
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
            is ShortCastlingAppliedEvent -> {
                LogMessage(gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.SHORT_CASTLING,
                    causedBy = toActorType(event.pieceColor),
                    message = toCastlingInfoLogMessage(event.pieceColor, "Short")
                )
            }
            is LongCastlingAppliedEvent -> {
                LogMessage(gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.LONG_CASTLING,
                    causedBy = toActorType(event.pieceColor),
                    message = toCastlingInfoLogMessage(event.pieceColor, "Long"))
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
            is ShortCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent -> {
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.WRONG_SHORT_CASTLING,
                    causedBy = toActorType(event.pieceColor),
                    message = toCastlingPiecesAlreadyMovedMessage(event.pieceColor, "short"),
                    logLevel = LogLevel.ERROR)
            }
            is LongCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent -> {
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.WRONG_LONG_CASTLING,
                    causedBy = toActorType(event.pieceColor),
                    message = toCastlingPiecesAlreadyMovedMessage(event.pieceColor, "long"),
                    logLevel = LogLevel.ERROR)
            }
            is ShortCastlingNotPossibleBecauseWrongSettlingEvent -> {
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.WRONG_SHORT_CASTLING,
                    causedBy = toActorType(event.pieceColor),
                    message = toCastlingIncorrectSettlingMessage(event.pieceColor, "short"),
                    logLevel = LogLevel.ERROR)
            }
            is LongCastlingNotPossibleBecauseWrongSettlingEvent -> {
                LogMessage(
                    gameId = event.gameId,
                    logTime = event.dateTime,
                    logMessageType = LogMessageType.WRONG_SHORT_CASTLING,
                    causedBy = toActorType(event.pieceColor),
                    message = toCastlingIncorrectSettlingMessage(event.pieceColor, "long"),
                    logLevel = LogLevel.ERROR)
            }

            else                     -> throw IllegalStateException("Unsupported event")
        }
    }

    private fun toCastlingInfoLogMessage(color: PieceColor, castlingType: String): String {
        return "$castlingType castling applied by ${color.name}."
    }

    private fun toCastlingPiecesAlreadyMovedMessage(color: PieceColor, castlingType: String): String {
        return  "Castling $castlingType not possible because the related pieces are moved already."
    }


    private fun toCastlingIncorrectSettlingMessage(color: PieceColor, castlingType: String): String {
        return "The current setting of ${color} is not correct for applying castling ${castlingType}."
    }



}