package siem.chess.application.repositories

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import siem.chess.ChessApplication
import siem.chess.TestChessApplication
import siem.chess.domain.queryside.ChessGameMove
import java.time.LocalDateTime

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ChessApplication::class, TestChessApplication::class])
class ChessGameMoveRepositoryTest {

    @Autowired
    lateinit var chessGameMoveRepository: ChessGameMoveRepository

    @Test
    fun `selectAllChessGameMoves should return the moves from a game with gameId only` () {

        //Given
        val now = LocalDateTime.now().withNano(0)

        val d2d4Move = ChessGameMove(
                gameId = "42",
                executionTime = now,
                from = "D2",
                to = "D4",
                settling = """
                                    WHITE_ROOK,WHITE_KNIGHT,WHITE_BISHOP,WHITE_KING,WHITE_QUEEN,WHITE_BISHOP,WHITE_KNIGHT,WHITE_ROOK,
                                    WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,X,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,
                                    X,X,X,X,X,X,X,X,
                                    X,X,X,WHITE_PAWN,X,X,X,X,
                                    X,X,X,X,X,X,X,X,
                                    X,X,X,X,X,X,X,X,
                                    BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,
                                    BLACK_ROOK,BLACK_KNIGHT,BLACK_BISHOP,BLACK_KING,BLACK_QUEEN,BLACK_BISHOP,BLACK_KNIGHT,BLACK_ROOK
                                   """

        )

        val d2d4DifferentGame = d2d4Move.copy(gameId = "78")

        givenMove(d2d4Move)
        givenMove(d2d4DifferentGame)

        //When
        val checkGameMoves = chessGameMoveRepository.selectAllChessGameMoves("42")

        //Then
        assertThat(checkGameMoves).hasSameElementsAs(listOf(
            d2d4Move
        ))
    }

    private fun givenMove(chessGameMove: ChessGameMove) {
        chessGameMoveRepository.insertChessGameMove(chessGameMove)
    }

}