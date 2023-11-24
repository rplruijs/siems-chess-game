package siem.chess.domain.commandside.board

import siem.chess.domain.commandside.board.MoveDeterminer.possibleMoves
import siem.chess.domain.commandside.board.constants.*
import siem.chess.domain.commandside.exceptions.MoveNotPossibleException
import siem.chess.domain.commandside.exceptions.PieceNotFoundAtPositionException
import siem.chess.domain.commandside.gamestatus.GameStateDeterminer
import siem.chess.domain.commandside.gamestatus.GameStatus
import siem.chess.domain.commandside.gamestatus.Move

data class Board(val squares: List<Square>, val lastMove: Move? = null, val gameStatus: GameStatus? = null) {

    fun moveChessPiece(chessPiece: ChessPiece, from: Position, to: Position): Board {
        return moveChessPiece(turn = chessPiece.color, from = from, to = to)
            .copy(lastMove = Move(chessPiece, from, to))
    }

    fun setCheck(): Board {
        return this.copy(gameStatus = this.gameStatus?.copy(check = true))
    }

    fun setCheckMate(): Board {
        return this.copy(gameStatus = this.gameStatus?.copy(checkMate = true))
    }

    fun pieceOfColorOnPosition(color: PieceColor, position: Position): Boolean {
        return this.squares.find { it.position == position && it.piece?.color == color} != null
    }

    private fun moveChessPiece(turn: PieceColor, from: Position, to: Position): Board {

        val chessPiece = getChessPiece(from)
        if (chessPiece?.color != turn) {
            throw PieceNotFoundAtPositionException(from)
        }

        val fromSquare = getChessSquare(from)
        val toSquare = getChessSquare(to)

        val move =
            possibleMoves(fromSquare, this)
                .find { it.to == to } ?: throw MoveNotPossibleException(fromSquare, toSquare)


        fun convert(iter: Square): Square {
            when (iter.position) {
                from -> return iter.removeChessPiece()
                to -> return iter.placeChessPiece(move.piece)
                else -> return iter
            }
        }
        return Board(squares = this.squares.map { iter -> convert(iter) },
            lastMove = Move(piece = chessPiece, from = fromSquare.position, to = toSquare.position, chessPieceCaptured = toSquare.piece)
        )
    }


    fun resultOfMove(from: Position, to: Position): Board {

        val chessPiece = getChessPiece(from)?: throw PieceNotFoundAtPositionException(from)

        val toBeBoard = moveChessPiece(chessPiece, from, to)
        val check = GameStateDeterminer.check(chessPiece.color.opposite(), toBeBoard)
        val checkMate = GameStateDeterminer.checkMate(chessPiece.color.opposite(), toBeBoard)
        val winner = if (checkMate) chessPiece.color else null

        return toBeBoard.copy(gameStatus = GameStatus(check, checkMate, winner))
    }

    private fun getChessSquare(position: Position): Square {
        return squares.first { it.position == position }
    }

    fun allSquaresWith(color: PieceColor): List<Square> {
        return squares.filter { it.piece?.color == color }
    }

    private fun getChessPiece(position: Position): ChessPiece? {
        return squares.find { it.position == position }?.piece
    }
}


fun generateChessBoardWithOpeningSettling(): Board {
    return generateChessBoardWithSettling(startPositionsWithChessPiece)
}

fun boardTextualOpeningSettling(): String {
    return textualRepresentation(generateChessBoardWithOpeningSettling().squares)
}

fun generateChessBoardWithSpecificSettling(settling: Map<Position, ChessPiece>): Board {
    return generateChessBoardWithSettling(settling)
}



private fun generateChessBoardWithSettling(settling: Map<Position, ChessPiece>): Board {
    val squares = allPositions
        .map { Square(it, settling[it]) }
    return Board(squares)
}


fun textualRepresentation(squares: List<Square>): String {
    return squares.map {
        when (it.piece) {
            null -> "X"
            else -> "${it.piece.color.name}_${it.piece.type.name}"
        }
    }.joinToString(",")
}

fun toSquares(textualRepresentation: String): List<Square> {

    return textualRepresentation.split(",").mapIndexed { index, s ->
        val position = allPositions.first{it.column.index == (index % 8) && it.row.index == (index / 8) }
        val chessPiece = when(s) {
            "X"  -> null
            else -> {
                val pieceElements = s.split("_")
                val pieceColor = pieceElements[0]
                val pieceType = pieceElements[1]
                ChessPiece(PieceType.valueOf(pieceType), PieceColor.valueOf(pieceColor))
            }
        }
        Square(position, chessPiece )
    }
}