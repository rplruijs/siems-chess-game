package siem.chess.application.repositories

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import siem.chess.domain.queryside.ChessGameLog
import siem.chess.domain.queryside.ChessGameMove
import siem.chess.domain.queryside.LogMessage
import siem.chess.domain.queryside.LogMessageType
import java.sql.Types
import java.time.LocalDateTime

@Component
class ChessGameMoveRepository(val jdbcClient: JdbcClient) {

    fun insertChessGameMove(chessGameMove: ChessGameMove) {
        val sql = "insert into chessgamemove (id, executionTime, fromPosition, toPosition, settling) values(:id,:executionTime, :fromPosition, :toPosition, :settling)"

        this.jdbcClient.sql(sql)
            .param("id",chessGameMove.gameId)
            .param("executionTime",chessGameMove.executionTime, Types.TIMESTAMP)
            .param("fromPosition", chessGameMove.from)
            .param("toPosition" ,chessGameMove.to)
            .param("settling", chessGameMove.settling)
            .update()
    }

    fun selectAllChessGameMoves(gameId: String) : List<ChessGameMove> {
        val sql = "select * from chessgamemove where id = :gameId"

        return  this.jdbcClient.sql(sql)
            .param("gameId", gameId)
            .query { rs, _ ->
                ChessGameMove(
                    gameId = gameId,
                    executionTime = rs.getTimestamp("executionTime").toLocalDateTime(),
                    from = rs.getString("fromPosition"),
                    to = rs.getString("toPosition"),
                    settling = rs.getString("settling")

                )
            }.list()

    }
}