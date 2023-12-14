package siem.chess.adapter.`in`.rest

import siem.chess.adapter.`in`.rest.exceptions.InvalidChessMoveException
import siem.chess.adapter.`in`.rest.exceptions.InvalidPositionException
import siem.chess.adapter.`in`.rest.exceptions.WrongSpecialMoveException
import siem.chess.domain.commandside.board.Column
import siem.chess.domain.commandside.board.Position
import siem.chess.domain.commandside.board.Row

fun parseChessMove(move: String): Pair<Position, Position> {
    return when(move.length) {
        4   -> {
            Pair(parseChessPosition(move.subSequence(0, 2).toString()),
                 parseChessPosition(move.subSequence(2, 4).toString())
            )
        }
        5   -> {
            Pair(parseChessPosition(move.subSequence(0, 2).toString()),
                 parseChessPosition(move.subSequence(3, 5).toString()))
        }
        else -> throw InvalidChessMoveException("invalid input $move")
    }
}

fun parseChessPosition(input: String): Position {
    if (input.length != 2) {
        return throw InvalidPositionException(input)
    }

    val columnChar = input[0].uppercaseChar()
    val rowChar = input[1]

    val column = when (columnChar) {
        'A' -> Column.A
        'B' -> Column.B
        'C' -> Column.C
        'D' -> Column.D
        'E' -> Column.E
        'F' -> Column.F
        'G' -> Column.G
        'H' -> Column.H
        else -> throw InvalidPositionException(input)
    }

    val row = when (rowChar) {
        '1' -> Row.ONE
        '2' -> Row.TWO
        '3' -> Row.THREE
        '4' -> Row.FOUR
        '5' -> Row.FIVE
        '6' -> Row.SIX
        '7' -> Row.SEVEN
        '8' -> Row.EIGHT
        else -> throw InvalidPositionException(input)
    }

    return Position(column, row)
}

fun parseCastlingMove(input: String): CastlingMove {
    val inputNormalized = input.replace(Regex("[\\r\\n\\s]"), "")
         .lowercase()
    return CastlingMove.values().find { it.expectedInput == inputNormalized} ?: throw WrongSpecialMoveException("Wrong special move $input")
}

enum class CastlingMove(val expectedInput: String){CASTLING_SHORT("castlingshort"), CASTLING_LONG("castlinglong")}