package siem.chess.application.repositories

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import siem.chess.domain.queryside.ChessGameLog
import siem.chess.domain.queryside.LogMessage
import siem.chess.domain.queryside.LogMessageType
import java.sql.Types

@Component
class ChessGameInfoLogRepository(val jdbcClient: JdbcClient) {

    fun getChessGameLog(gameId: String): ChessGameLog {
        val sql = "select * from chessgameinfolog where id = :gameId"

        val logMessages = this.jdbcClient.sql(sql)
            .param("gameId", gameId)
            .query { rs, _ ->
                LogMessage(
                    gameId = gameId,
                    logTime = rs.getTimestamp("logTime").toLocalDateTime(),
                    message = rs.getString("logMessage"),
                    logMessageType = LogMessageType.valueOf(rs.getString("logMessageType"))

                )
            }.list()
        return ChessGameLog(gameId, logMessages)
    }

    fun insertLogMessage(logMessage: LogMessage) {
        val sql = "insert into chessgameinfolog (id, logTime, logMessage, logMessageType) values (:gameId, :logTime, :logMessage, :logMessageType)"

        this.jdbcClient.sql(sql)
            .param("gameId", logMessage.gameId)
            .param("logTime", logMessage.logTime)
            .param("logMessage", logMessage.message)
            .param("logMessageType", logMessage.logMessageType, Types.OTHER)
            .update()
    }
}