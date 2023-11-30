package siem.chess.domain.commandside.board

import siem.chess.domain.commandside.board.constants.ChessPiece


data class Square(val position: Position, val piece: ChessPiece? = null) {
    fun containsPieceOfOppositeColor(other: ChessPiece): Boolean {
        return if (piece == null) {
            false
        } else {
            piece.color != other.color
        }
    }

    fun left(other: Square): Boolean {
        return this.position.left(other.position)
    }

    fun isEmpty(): Boolean {
        return piece == null
    }

    fun removeChessPiece(): Square {
        return this.copy(piece = null)
    }

    fun placeChessPiece(piece: ChessPiece): Square {
        return this.copy(piece = piece)
    }
}

fun noPiecesBetween(squares: List<Square>): Boolean {
    return if (squares.size > 2) {
        squares.drop(1).dropLast(1).all { it.piece == null }
    } else {
        true
    }
}