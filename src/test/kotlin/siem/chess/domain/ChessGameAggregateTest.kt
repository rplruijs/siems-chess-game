package siem.chess.domain

import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siem.chess.domain.commandside.ChessGameAggregate
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.constants.*
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
    fun `castling short by white should be result in correct event when state of the board is castling proof`() {
        val now = LocalDateTime.now()
        val toBeBoardTextual = """
            WHITE_ROOK,WHITE_KNIGHT,WHITE_BISHOP,WHITE_QUEEN,X,WHITE_ROOK,WHITE_KING,X,
            WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,X,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,
            X,X,X,WHITE_BISHOP,X,X,X,WHITE_KNIGHT,
            X,X,X,X,WHITE_PAWN,X,X,X,
            X,X,X,X,BLACK_PAWN,X,X,X,
            BLACK_PAWN,X,X,BLACK_PAWN,X,X,X,X,
            X,BLACK_PAWN,BLACK_PAWN,X,X,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,
            BLACK_ROOK,BLACK_KNIGHT,BLACK_BISHOP,BLACK_QUEEN,BLACK_KING,BLACK_BISHOP,BLACK_KNIGHT,BLACK_ROOK
            
        """.trimIndent().replace(Regex("[\\r\\n]"), "")


        fixture.givenCommands(
            startCommand,
            MoveChessPieceCommand(gameId, now, E2, E4),
            MoveChessPieceCommand(gameId, now, E7, E5),
            MoveChessPieceCommand(gameId, now, F1, D3),
            MoveChessPieceCommand(gameId, now, D7, D6),
            MoveChessPieceCommand(gameId, now, G1, H3),
            MoveChessPieceCommand(gameId, now, A7, A6),
        ).`when`(ShortCastlingCommand(gameId, now))
            .expectEvents(ShortCastlingAppliedEvent(gameId, now, PieceColor.WHITE, toBeBoardTextual))
    }


    @Test
    fun `the famous d2-d4 opening should apply the correct event` () {
        val toBeBoardTextual = """
            WHITE_ROOK,WHITE_KNIGHT,WHITE_BISHOP,WHITE_QUEEN,WHITE_KING,WHITE_BISHOP,WHITE_KNIGHT,WHITE_ROOK,
            WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,X,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,WHITE_PAWN,
            X,X,X,X,X,X,X,X,
            X,X,X,WHITE_PAWN,X,X,X,X,
            X,X,X,X,X,X,X,X,
            X,X,X,X,X,X,X,X,
            BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,BLACK_PAWN,
            BLACK_ROOK,BLACK_KNIGHT,BLACK_BISHOP,BLACK_QUEEN,BLACK_KING,BLACK_BISHOP,BLACK_KNIGHT,BLACK_ROOK
            
        """.trimIndent().replace(Regex("[\\r\\n]"), "")

        fixture.givenCommands(startCommand)
            .`when`(d2d4MOve)
            .expectEvents(ChessPieceMovedEvent("42", now, WHITE_PAWN, D2, D4, toBeBoardTextual))
    }

    @Test
    fun `It should not be possiblethe same player making multiple moves in a row`() {
        fixture.givenCommands(startCommand, d2d4MOve)
            .`when`(e2e4MOve)
            .expectEvents(MoveAttemptByWrongPlayerEvent(gameId = gameId, dateTime = now, PieceColor.WHITE, E2, E4))

    }

    companion object {
        val now = LocalDateTime.now()
        val gameId = "42"
        val startCommand = StartGameCommand(gameId, now, "Siem", "Remco")
        val d2d4MOve = MoveChessPieceCommand(gameId, now, D2, D4)
        val e2e4MOve = MoveChessPieceCommand(gameId, now, E2, E4)
    }
}