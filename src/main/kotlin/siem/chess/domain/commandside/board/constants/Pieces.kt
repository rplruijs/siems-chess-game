package siem.chess.domain.commandside.board.constants

enum class PieceType(val representation: String) {
    KING("♔"),
    QUEEN("♕"),
    ROOK("♖"),
    BISHOP("♗"),
    KNIGHT("♘"),
    PAWN("♙")
}

enum class PieceColor{WHITE, BLACK;

    fun opposite(): PieceColor {
        return when {
            this == WHITE -> BLACK
            else          -> WHITE
        }
    }
}

val WHITE_ROOK = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
val WHITE_KNIGHT = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
val WHITE_BISHOP = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
val WHITE_KING = ChessPiece(PieceType.KING, PieceColor.WHITE)
val WHITE_QUEEN = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
val WHITE_PAWN = ChessPiece(PieceType.PAWN, PieceColor.WHITE)

val BLACK_ROOK = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
val BLACK_KNIGHT = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
val BLACK_BISHOP = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
val BLACK_KING = ChessPiece(PieceType.KING, PieceColor.BLACK)
val BLACK_QUEEN = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
val BLACK_PAWN = ChessPiece(PieceType.PAWN, PieceColor.BLACK)


data class ChessPiece(val type: PieceType, val color: PieceColor) {
    override fun toString(): String {
        return color.toString() + "_" + "$type"
    }
}