package siem.chess.adapter.`in`.rest

import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.commandside.board.toSquares
import siem.chess.domain.queryside.ActorType
import siem.chess.domain.queryside.ChessGameState
import siem.chess.domain.queryside.LogLevel
import siem.chess.domain.queryside.LogMessage
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Component
class HtmlSnippetComposer(val engine: TemplateEngine) {

     fun toHtmlChessLogTable(log: List<LogMessage>): HtmlSnippet {
        val context = Context()
        context.setVariable("log", log.map{convert(it)})
        return engine.process("chess-game-log", context).replace(Regex("[\\r\\n]"), "")
    }

    fun toHtmlChessBoard(settling: String): HtmlSnippet {
        val context = Context()
        context.setVariable("squares", toSquares(settling))
        return engine.process("chess-board-standalone", context).replace(Regex("[\\r\\n]"), "")
    }


    fun toTurnForm(chessGameState: ChessGameState): HtmlSnippet {
        val context = Context()
        context.setVariable("turn", convert(chessGameState))
        return engine.process("chess-game-turn-form", context).replace(Regex("[\\r\\n]"), "")
    }

    private fun convert(logMessage: LogMessage) : ViewLogMessage {
        return ViewLogMessage(
            time = logMessage.logTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)),
            message = logMessage.message,
            color = when (logMessage.logLevel) {
                LogLevel.INFO -> {
                    when (logMessage.causedBy) {
                        ActorType.WHITE_PLAYER -> "text-white"
                        ActorType.BLACK_PLAYER -> "text-black"
                        ActorType.SYSTEM       -> "text-grey-300"
                    }
                }
                LogLevel.WARN  -> "text-orange-500"
                LogLevel.ERROR -> "text-red-500"
            }
        )
    }

    private fun convert(chessGameState: ChessGameState): TurnFormViewModel {

        return TurnFormViewModel(
            message = when(chessGameState.currentTurn) {
                PieceColor.WHITE -> "White's turn"
                PieceColor.BLACK -> "Black's turn"
            },
            showLongCastlingButton = when(chessGameState.currentTurn) {
                PieceColor.WHITE -> chessGameState.castlingLongStillPossibleByWhite
                PieceColor.BLACK -> chessGameState.castlingLongStillPossibleByBlack
            },
            showShortCastlingButton = when(chessGameState.currentTurn) {
                PieceColor.WHITE -> chessGameState.castlingShortStillPossibleByWhite
                PieceColor.BLACK -> chessGameState.castlingShortStillPossibleByBlack
            }
        )

    }

    data class ViewLogMessage(val time: String, val message: String, val color: String)
    data class TurnFormViewModel(val message: String, val showShortCastlingButton: Boolean, val showLongCastlingButton: Boolean)
}