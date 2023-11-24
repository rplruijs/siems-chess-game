import org.assertj.core.api.Assert
import org.assertj.core.api.Assertions
import org.junit.Test
import siem.chess.domain.commandside.board.constants.C1
import siem.chess.domain.commandside.board.constants.WHITE_KING
import siem.chess.domain.commandside.board.generateChessBoardWithOpeningSettling
import siem.chess.domain.commandside.board.textualRepresentation
import siem.chess.domain.commandside.board.toSquares

class BoardTest {


    @Test
    fun position_toString() {
        println(C1.toString());
    }

    @Test
    fun piece_toString() {
        println(WHITE_KING)

    }


    @Test
    fun textual_representation() {
        val settling = generateChessBoardWithOpeningSettling()
        println( textualRepresentation(settling.squares) )
    }



    @Test
    fun transitive_textual_representation() {
        val settling = generateChessBoardWithOpeningSettling()

        val settlingTextual = textualRepresentation(settling.squares)
        val settlingTransitive = toSquares(settlingTextual)

        Assertions.assertThat(settling.squares).isEqualTo(settlingTransitive)

    }
}