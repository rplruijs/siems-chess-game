package siem.chess.application.repositories

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import siem.chess.ChessApplication
import siem.chess.TestChessApplication
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.queryside.ChessGameState
import siem.chess.domain.queryside.GameState

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ChessApplication::class, TestChessApplication::class])
class ChessGameStateRepositoryTest {

    @Autowired
    lateinit var chessGameStateRepository: ChessGameStateRepository


    @Test
    fun `retrieveGameState should return  the gameState of a chessGame with a given gameId` () {
        //Given
        val gameId = "18"
        val gameState = ChessGameState(
            gameId = gameId,
            gameState = GameState.STARTED,
            currentTurn = PieceColor.WHITE,
            turnNumber = 1,
            castlingShortStillPossibleByWhite = true,
            castlingLongStillPossibleByWhite = true,
            castlingShortStillPossibleByBlack = true,
            castlingLongStillPossibleByBlack = true,
            settling = boardTextualOpeningSettling()
        )

        givenGameState(gameState)

        //When
        val result = chessGameStateRepository.retrieveGameState(gameId)
        //Then
        Assertions.assertThat(gameState).isEqualTo(result)

        //When
        givenGameState(result.copy(castlingShortStillPossibleByBlack = false))
        val resultAfterUpsert = chessGameStateRepository.retrieveGameState(gameId)
        //Then
        Assertions.assertThat(result.copy(castlingShortStillPossibleByBlack = false)).isEqualTo(resultAfterUpsert)
    }

    private fun givenGameState(chessGameState: ChessGameState) {
        chessGameStateRepository.upsertGameState(chessGameState)
    }
}