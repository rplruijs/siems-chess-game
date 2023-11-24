package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import siem.chess.domain.GameEndedByCheckMateEvent
import siem.chess.domain.GameStartedEvent
import siem.chess.domain.queryside.ChessGame
import java.sql.Types

@Component
class ChessGameProjection(val jdbcClient: JdbcClient) {

    @EventHandler
    fun handle(event: GameStartedEvent) {
        val sql = "insert into chessgame (id, startTime, whitePlayer, blackPlayer) values(:id, :startTime, :whitePlayer, :blackPlayer)"

        this.jdbcClient.sql(sql)
            .param("id", event.gameId)
            .param("startTime", event.dateTime, Types.TIMESTAMP)
            .param("whitePlayer", event.whitePlayer)
            .param("blackPlayer", event.blackPlayer)
            .update()
    }

    @EventHandler
    fun handle(event: GameEndedByCheckMateEvent) {
        val sql = "update chessgame set endDate = :endDate WHERE id = :gameId"
        this.jdbcClient.sql(sql)
            .param("id", event.gameId)
            .param("endDate", event.dateTime, Types.TIMESTAMP)
            .update()
    }

    @QueryHandler(queryName = "allChessGames")
    fun allChessGames(): List<ChessGame> {
        val sql = "select * from chessgame"

        return this.jdbcClient.sql(sql)
            .query { rs, _ ->
                ChessGame(
                    rs.getString("id"),
                    rs.getTimestamp("startDate").toLocalDateTime(),
                    rs.getTimestamp("endDate").toLocalDateTime(),
                    rs.getString("whitePlayer"),
                    rs.getString("blackPlayer")
                )
            }.list()
    }
}