package siem.chess.domain

import org.assertj.core.api.Assertions.*
import org.junit.Test
import siem.chess.domain.commandside.board.MoveDeterminer
import siem.chess.domain.commandside.board.Square
import siem.chess.domain.commandside.board.constants.*
import siem.chess.domain.commandside.board.generateChessBoardWithOpeningSettling
import siem.chess.domain.commandside.board.generateChessBoardWithSpecificSettling
import siem.chess.domain.commandside.gamestatus.GameStateDeterminer
import siem.chess.domain.commandside.gamestatus.Move

class BoardTest {

    @Test
    fun generateChessBoardWithOpeningSettling_should_generate_the_correct_opening_board() {
        val result = generateChessBoardWithOpeningSettling()

        assertThat(result.squares)
            .hasSize(64)
    }


    @Test
    fun white_rook_movements_one_way_block_by_white_pawn() {
        val settling = mapOf(
            D1 to WHITE_KING,
            G2 to WHITE_PAWN,
            E2 to WHITE_ROOK,

            D8 to BLACK_KING,
            E6 to BLACK_ROOK
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val actualResult = MoveDeterminer.possibleMoves(Square(E2, WHITE_ROOK), chessBoard)
        val expectedResult = setOf(
            Move(from= E2, to= E1, piece = WHITE_ROOK),
            Move(from= E2, to= F2, piece = WHITE_ROOK),
            Move(from= E2, to= D2, piece = WHITE_ROOK),
            Move(from= E2, to= C2, piece = WHITE_ROOK),
            Move(from= E2, to= B2, piece = WHITE_ROOK),
            Move(from= E2, to= A2, piece = WHITE_ROOK),
            Move(from= E2, to= E3, piece = WHITE_ROOK),
            Move(from= E2, to= E4, piece = WHITE_ROOK),
            Move(from= E2, to= E5, piece = WHITE_ROOK),
            Move(from= E2, to= E6, piece = WHITE_ROOK, chessPieceCaptured = BLACK_ROOK),
        )

        assertThat(expectedResult)
            .hasSameElementsAs(actualResult)
    }

    @Test
    fun possible_moves_king() {

        val settling = mapOf(
            D1 to WHITE_KING,
            G2 to WHITE_PAWN,
            E2 to WHITE_ROOK,

            D8 to BLACK_KING,
            E6 to BLACK_ROOK
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)

        val actualResult = MoveDeterminer.possibleMoves(Square(D1, WHITE_KING), chessBoard)
        val expectedResult = setOf(
            Move(from = D1, to = C1, piece = WHITE_KING),
            Move(from = D1, to = C2, piece = WHITE_KING),
            Move(from = D1, to = E1, piece = WHITE_KING),
            Move(from = D1, to = D2, piece = WHITE_KING)
        )


        assertThat(expectedResult)
              .hasSameElementsAs(actualResult)
    }

    @Test
    fun possible_moves_king_with_capture() {

        val settling = mapOf(
            D1 to WHITE_KING,
            G2 to WHITE_PAWN,
            E2 to WHITE_ROOK,

            D8 to BLACK_KING,
            E6 to BLACK_ROOK,
            D2 to BLACK_PAWN
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)

        val actualResult = MoveDeterminer.possibleMoves(Square(D1, WHITE_KING), chessBoard).toSet()
        val expectedResult = setOf(
                                   Move(from = D1, to = C1, piece = WHITE_KING),
                                   Move(from = D1, to = C2, piece = WHITE_KING),
                                   Move(from = D1, to = E1, piece = WHITE_KING),
                                   Move(from = D1, to = D2, piece = WHITE_KING, chessPieceCaptured = ChessPiece(
                                       PieceType.PAWN, PieceColor.BLACK)
                                   )
        )

        assertThat(actualResult).hasSameElementsAs(expectedResult)
    }

    @Test
    fun possible_moves_knight_with_capture() {

        val settling = mapOf(
            D1 to WHITE_KING,
            G2 to WHITE_PAWN,
            E2 to WHITE_KNIGHT,
            D8 to BLACK_KING,
            E6 to BLACK_ROOK,
            D2 to BLACK_PAWN,
            C3 to BLACK_PAWN
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)

        val actualResult = MoveDeterminer.possibleMoves(Square(E2, WHITE_KNIGHT), chessBoard).toSet()

        val expectedResult = setOf(
            Move(from = E2, to = C1, piece = WHITE_KNIGHT, chessPieceCaptured = null),
                                   Move(from = E2, to = G1, piece = WHITE_KNIGHT, chessPieceCaptured = null),
                                   Move(from = E2, to = C3, piece = WHITE_KNIGHT, chessPieceCaptured = ChessPiece(
                                       PieceType.PAWN, PieceColor.BLACK)
                                   ),
                                   Move(from = E2, to = G3, piece = WHITE_KNIGHT, chessPieceCaptured = null),
                                   Move(from = E2, to = F4, piece = WHITE_KNIGHT, chessPieceCaptured = null),
                                   Move(from = E2, to = D4, piece = WHITE_KNIGHT, chessPieceCaptured = null)
        )

        assertThat(expectedResult)
            .hasSameElementsAs(actualResult)
    }

    @Test
    fun possible_moves_pawn_with_capture_and_at_start_position() {
        val settling = mapOf(
            C7 to BLACK_PAWN,
            D8 to BLACK_KING,
            D6 to WHITE_PAWN,
            D1 to WHITE_KING
        )
        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val actualResult = MoveDeterminer.possibleMoves(Square(C7, BLACK_PAWN), chessBoard)

        val expectedResult = setOf(
            Move(from = C7, to = D6, piece = BLACK_PAWN, chessPieceCaptured = WHITE_PAWN),
            Move(from = C7, to = C5, piece = BLACK_PAWN),
            Move(from = C7, to = C6, piece =  BLACK_PAWN)
        )

        assertThat(expectedResult)
            .hasSameElementsAs(actualResult)
    }

    @Test
    fun possible_moves_pawn_from_start_position_two_jump_not_possible() {
        val settling = mapOf(
            C2 to WHITE_PAWN,
            E1 to WHITE_KING,

            C3 to BLACK_PAWN,
            B3 to BLACK_BISHOP,
            D1 to WHITE_KING
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val actualResult = MoveDeterminer.possibleMoves(Square(C2, WHITE_PAWN), chessBoard)

        val expectedResult = listOf(
            Move(from = C2, to = B3, piece = WHITE_PAWN, chessPieceCaptured = BLACK_BISHOP)
        )

        assertThat(expectedResult)
            .hasSameElementsAs(actualResult)
    }

    @Test
    fun possible_moves_bishop_with_captures() {
        val settling = mapOf(
            C7 to BLACK_BISHOP,
            D8 to BLACK_KING,

            B6 to WHITE_PAWN,
            D1 to WHITE_KING,
            H6 to WHITE_BISHOP,
            F4 to WHITE_KNIGHT
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val actualResult = MoveDeterminer.possibleMoves(Square(C7, BLACK_BISHOP), chessBoard)

        val expectedResult = setOf(
            Move(from = C7, to = B8, piece = BLACK_BISHOP),
            Move(from = C7, to = D6, piece = BLACK_BISHOP),
            Move(from = C7, to = E5, piece = BLACK_BISHOP),
            Move(from = C7, to = F4, piece = BLACK_BISHOP, chessPieceCaptured = WHITE_KNIGHT),
            Move(from = C7, to = B6, piece = BLACK_BISHOP, chessPieceCaptured = WHITE_PAWN),
        )

        assertThat(expectedResult)
            .hasSameElementsAs(actualResult)
    }

    @Test
    fun possible_move_rook_with_captures() {
        val settling = mapOf(
            C7 to BLACK_ROOK,
            B7 to BLACK_KING,
            B6 to WHITE_PAWN,
            D1 to WHITE_KING,
            A7 to WHITE_BISHOP,
            C4 to WHITE_KNIGHT
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val actualResult = MoveDeterminer.possibleMoves(Square(C7, BLACK_ROOK), chessBoard)

        val expectedResult = setOf(
            Move(from = C7, to = D7, piece = BLACK_ROOK),
            Move(from = C7, to = E7, piece = BLACK_ROOK),
            Move(from = C7, to = F7, piece = BLACK_ROOK),
            Move(from = C7, to = G7, piece = BLACK_ROOK),
            Move(from = C7, to = H7, piece = BLACK_ROOK),
            Move(from = C7, to = C8, piece = BLACK_ROOK),
            Move(from = C7, to = C6, piece = BLACK_ROOK),
            Move(from = C7, to = C5, piece = BLACK_ROOK),
            Move(from = C7, to = C4, piece = BLACK_ROOK, chessPieceCaptured = WHITE_KNIGHT)
        )

        assertThat(expectedResult)
            .hasSameElementsAs(actualResult)
    }

    @Test
    fun check_by_knight() {
        val settling = mapOf(
            A1 to WHITE_ROOK,
            D1 to WHITE_KING,
            E2 to WHITE_PAWN,

            C3 to BLACK_KNIGHT,
            G4 to BLACK_BISHOP,
            D8 to BLACK_KING
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val check = GameStateDeterminer.check(PieceColor.WHITE, chessBoard)
        assertThat(check).isTrue()
    }

    @Test
    fun no_check_pawn_protection() {
        val settling = mapOf(
            A1 to WHITE_ROOK,
            D1 to WHITE_KING,
            E2 to WHITE_PAWN,

            B3 to BLACK_KNIGHT,
            G4 to BLACK_BISHOP,
            D8 to BLACK_KING
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val check = GameStateDeterminer.check(PieceColor.WHITE, chessBoard)
        assertThat(check).isFalse()
    }

    @Test
    fun check_mate_false_still_option_to_move() {
        val settling = mapOf(
            A1 to WHITE_ROOK,
            D1 to WHITE_KING,
            E2 to WHITE_PAWN,

            D5 to BLACK_QUEEN,
            B3 to BLACK_KNIGHT,
            G4 to BLACK_BISHOP,
            D8 to BLACK_KING
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val checkMate = GameStateDeterminer.checkMate(PieceColor.WHITE, chessBoard)
        assertThat(checkMate).isFalse()
    }

    @Test
    fun check_mate() {
        val settling = mapOf(
            A1 to WHITE_ROOK,
            D1 to WHITE_KING,
            E2 to WHITE_PAWN,
            C2 to WHITE_PAWN,

            D5 to BLACK_QUEEN,
            B2 to BLACK_PAWN,
            B3 to BLACK_KNIGHT,
            G3 to BLACK_BISHOP,
            D8 to BLACK_KING
        )

        val chessBoard = generateChessBoardWithSpecificSettling(settling)
        val checkMate = GameStateDeterminer.checkMate(PieceColor.WHITE, chessBoard)
        assertThat(checkMate).isTrue()
    }


    @Test
    fun correct_move_should_change_the_board_correct() {
        val settling = mapOf(
            D1 to WHITE_KING,
            D2 to WHITE_PAWN,
            F2 to WHITE_BISHOP,

            D8 to BLACK_KING,
            D7 to BLACK_PAWN
        )
        val chessBoard =  generateChessBoardWithSpecificSettling(settling)
        val actualResult = chessBoard.moveChessPiece(WHITE_PAWN, D2, D4)

        val settlingAfterMove = mapOf(
            D1 to WHITE_KING,
            D4 to WHITE_PAWN,
            F2 to WHITE_BISHOP,

            D8 to BLACK_KING,
            D7 to BLACK_PAWN
        )

        val expectedResult = generateChessBoardWithSpecificSettling(settlingAfterMove)
            .copy(lastMove = Move(WHITE_PAWN, from = D2, to = D4))

        assertThat(expectedResult).isEqualTo(actualResult)
    }
}