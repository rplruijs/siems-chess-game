package siem.chess.application.repositories

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import siem.chess.domain.queryside.*
import java.sql.Types

@Component
class ChessGameInfoLogRepository(val jdbcClient: JdbcClient) {

    fun getChessGameLog(gameId: String): ChessGameLog {
        val sql = "select * from chessgameinfolog where gameId = :gameId"

        val logMessages = this.jdbcClient.sql(sql)
            .param("gameId", gameId)
            .query { rs, _ ->
                LogMessage(
                    gameId = gameId,
                    logTime = rs.getTimestamp("logTime").toLocalDateTime(),
                    message = rs.getString("logMessage"),
                    causedBy = ActorType.valueOf(rs.getString("causedBy")),
                    logMessageType = LogMessageType.valueOf(rs.getString("logMessageType")),
                    logLevel = LogLevel.valueOf(rs.getString("logLevel"))

                )
            }.list()
        return ChessGameLog(gameId, logMessages)
    }

    fun insertLogMessage(logMessage: LogMessage) {

        val sql = "insert into chessgameinfolog (gameId, logTime, logMessage, causedBy, logMessageType, logLevel) values (:gameId, :logTime, :logMessage, :causedBy, :logMessageType, :logLevel)"

        this.jdbcClient.sql(sql)
            .param("gameId", logMessage.gameId)
            .param("logTime", logMessage.logTime)
            .param("logMessage", logMessage.message)
            .param("causedBy", logMessage.causedBy, Types.OTHER)
            .param("logMessageType", logMessage.logMessageType, Types.OTHER)
            .param("logLevel", logMessage.logLevel, Types.OTHER)
            .update()
    }
}