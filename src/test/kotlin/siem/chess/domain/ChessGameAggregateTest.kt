package siem.chess.domain

import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siem.chess.domain.commandside.ChessGameAggregate
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.constants.D2
import siem.chess.domain.commandside.board.constants.D4
import siem.chess.domain.commandside.board.constants.WHITE_PAWN
import java.time.LocalDateTime

class ChessGameAggregateTest {

    private lateinit var fixture: AggregateTestFixture<ChessGameAggregate>

    @BeforeEach
    fun setUp() {
        fixture = AggregateTestFixture(ChessGameAggregate::class.java)
    }


    @Test
    fun `start game command should should emit the proper event `() {

        val now = LocalDateTime.now()
        fixture.givenNoPriorActivity()
            .`when`(StartGameCommand("42", now, "Siem", "Remco"))
            .expectEvents(GameStartedEvent(
                gameId = "42",
                now,
                whitePlayer = "Siem",
                blackPlayer = "Remco",
                boardTextual = boardTextualOpeningSettling()  )
            )
    }


    @Test
    fun `the famous d2-d4 opening should apply in the correct event` () {

        val now = LocalDateTime.now()
        val toBeBoardTextual = """
            WHITE_ROOK,WHITE_KNIGHT,WHITE_BISHOP,WHITE_KING,WHITE_QUEEN,WHITE_BISHOP,WHITE_KNIGHT,WHITE_ROOK,
            WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,X,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,
            X,X,X,X,X,X,X,X,
            X,X,X,WHITE_PAWN,X,X,X,X,
            X,X,X,X,X,X,X,X,
            X,X,X,X,X,X,X,X,
            BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,
            BLACK_ROOK,BLACK_KNIGHT,BLACK_BISHOP,BLACK_KING,BLACK_QUEEN,BLACK_BISHOP,BLACK_KNIGHT,BLACK_ROOK
            
        """.trimIndent().replace(Regex("[\\r\\n]"), "")

        fixture.givenCommands(StartGameCommand("42", now, "Siem", "Remco"))
            .`when`(MoveChessPieceCommand("42", now, D2, D4))
            .expectEvents(ChessPieceMovedEvent("42", now, WHITE_PAWN, D2, D4, toBeBoardTextual))
    }
}