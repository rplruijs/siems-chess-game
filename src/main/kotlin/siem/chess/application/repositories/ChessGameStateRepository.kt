package siem.chess.application.repositories

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.queryside.ChessGameState
import java.sql.Types

@Component
class ChessGameStateRepository(val jdbcClient: JdbcClient) {

    fun upsertGameState(chessGameState: ChessGameState) {
        val sql = """
                    insert into chess_game_state values(:gameId,
                                                     :currentTurn,
                                                     :turnNumber,
                                                     :castlingShortStillPossibleByWhite,
                                                     :castlingLongStillPossibleByWhite,
                                                     :castlingShortStillPossibleByBlack,
                                                     :castlingLongStillPossibleByBlack,
                                                     :settling) ON CONFLICT(gameId) DO UPDATE SET 
                                                         currentTurn = :currentTurn,
                                                         turnNumber = :turnNumber,
                                                         castlingShortStillPossibleByWhite = :castlingShortStillPossibleByWhite,
                                                         castlingLongStillPossibleByWhite = :castlingLongStillPossibleByWhite,
                                                         castlingShortStillPossibleByBlack = :castlingShortStillPossibleByBlack,
                                                         castlingLongStillPossibleByBlack = :castlingLongStillPossibleByBlack,
                                                         settling = :settling

                """

        this.jdbcClient.sql(sql)
            .param("gameId", chessGameState.gameId)
            .param("currentTurn", chessGameState.currentTurn, Types.OTHER)
            .param("turnNumber", chessGameState.turnNumber, Types.INTEGER)
            .param("castlingShortStillPossibleByWhite", chessGameState.castlingShortStillPossibleByWhite, Types.BOOLEAN)
            .param("castlingLongStillPossibleByWhite", chessGameState.castlingLongStillPossibleByWhite, Types.BOOLEAN)
            .param("castlingShortStillPossibleByBlack", chessGameState.castlingShortStillPossibleByBlack, Types.BOOLEAN)
            .param("castlingLongStillPossibleByBlack", chessGameState.castlingLongStillPossibleByBlack, Types.BOOLEAN)
            .param("settling", chessGameState.settling)
            .update()
    }

    fun retrieveGameState(gameId: String): ChessGameState {
        val sql = "select * from chess_game_state where gameId = :gameId"

        return this.jdbcClient.sql(sql)
            .param("gameId", gameId)
            .query { rs, _ ->
                ChessGameState(
                    gameId = gameId,
                    currentTurn = PieceColor.valueOf(rs.getString("currentTurn")),
                    turnNumber = rs.getInt("turnNumber"),
                    castlingShortStillPossibleByWhite = rs.getBoolean("castlingShortStillPossibleByWhite"),
                    castlingLongStillPossibleByWhite = rs.getBoolean("castlingLongStillPossibleByWhite"),
                    castlingShortStillPossibleByBlack = rs.getBoolean("castlingShortStillPossibleByBlack"),
                    castlingLongStillPossibleByBlack = rs.getBoolean("castlingLongStillPossibleByBlack"),
                    settling = rs.getString("settling")

                )
            }.single()
    }
}