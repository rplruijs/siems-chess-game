package siem.chess.domain.commandside.board

import siem.chess.domain.commandside.board.constants.PieceColor

data class Position(val column: Column, val row: Row) {

    fun sameRow(other: Position): Boolean {
        return this.row == other.row
    }

    fun sameColumn(other: Position): Boolean {
        return this.column == other.column
    }

    fun verticalDistance(other: Position): Int {
        return Math.abs(this.row.index - other.row.index)
    }

    fun horizontalDistance(other: Position): Int {
        return Math.abs(this.column.index - other.column.index)
    }

    fun diagonalDistance(other: Position): Int {
        return horizontalDistance(other)
    }

    fun sameDiagonal(to: Position): Boolean {
        return verticalDistance(to) == horizontalDistance(to)
    }

    fun isKnightJump(to: Position): Boolean {
        return  (horizontalDistance(other = to) == 2 && verticalDistance(to) == 1) ||
                (horizontalDistance(other = to) == 1 && verticalDistance(to) == 2)
    }

    fun below(other: Position): Boolean {
        return this.row.index < other.row.index
    }

    fun above(other: Position): Boolean {
        return this.row.index > other.row.index
    }

    fun isStartPositionPawn(color: PieceColor): Boolean {
        return when (color) {
            PieceColor.BLACK -> this.row.index == 6
            PieceColor.WHITE -> this.row.index == 1
        }
    }

    fun left(other: Position): Boolean {
        return this.column.index < other.column.index
    }

    fun right(other: Position): Any {
        return this.column.index > other.column.index
    }

    override fun toString(): String {
        return this.column.toString() + (this.row.index + 1)
    }
}

enum class Row(val index: Int) {ONE(0), TWO(1), THREE(2), FOUR(3), FIVE(4), SIX(5), SEVEN(6), EIGHT(7)}
enum class Column(val index: Int) {A(0), B(1), C(2), D(3),  E(4),  F(5),  G(6), H(7)}


