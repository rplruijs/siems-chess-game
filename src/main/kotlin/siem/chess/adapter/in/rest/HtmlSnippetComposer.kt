package siem.chess.adapter.`in`.rest

import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import siem.chess.domain.commandside.board.toSquares
import siem.chess.domain.queryside.LogMessage

@Component
class HtmlSnippetComposer(val engine: TemplateEngine) {

     fun toHtmlChessLogTable(log: List<LogMessage>): HtmlSnippet {
        val context = Context()
        context.setVariable("log", log)
        return engine.process("chess-game-log", context).replace(Regex("[\\r\\n]"), "")
    }

    fun toHtmlChessBoard(settling: String): String {
        val context = Context()
        context.setVariable("squares", toSquares(settling))
        return engine.process("chess-board-standalone", context).replace(Regex("[\\r\\n]"), "")
    }
}