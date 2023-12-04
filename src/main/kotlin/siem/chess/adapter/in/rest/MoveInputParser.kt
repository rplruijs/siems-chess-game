package siem.chess.adapter.`in`.rest

import siem.chess.domain.commandside.board.Column
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.commandside.board.Position
import siem.chess.domain.commandside.board.Row

fun parseChessPosition(input: String): Position? {
    if (input.length != 2) {
        return null
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
        else -> return null
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
        else -> return null
    }

    return Position(column, row)
}

fun parsePieceColor(color: String): PieceColor? {
    return when(color.uppercase()) {
        "WHITE" -> PieceColor.WHITE
        "BLACK" -> PieceColor.BLACK
        else   -> null
    }
}

val CASTLING_MOVES = setOf("castling long, castling short")