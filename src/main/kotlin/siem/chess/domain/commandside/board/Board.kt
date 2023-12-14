package siem.chess.domain.commandside.board

import siem.chess.domain.CastlingType
import siem.chess.domain.commandside.board.MoveDeterminer.possibleMoves
import siem.chess.domain.commandside.board.constants.*
import siem.chess.domain.commandside.exceptions.MoveNotPossibleException
import siem.chess.domain.commandside.exceptions.PieceNotFoundAtPositionException
import siem.chess.domain.commandside.exceptions.WrongSettlingForShortCastlingException
import siem.chess.domain.commandside.gamestatus.GameStateDeterminer
import siem.chess.domain.commandside.gamestatus.GameStatus
import siem.chess.domain.commandside.gamestatus.Move

data class Board(val squares: List<Square>,
                 val lastMove: Move? = null,
                 val gameStatus: GameStatus? = null) {

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
        return this.squares.find { it.position == position && it.piece?.color == color } != null
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
            return when (iter.position) {
                from -> iter.removeChessPiece()
                to -> iter.placeChessPiece(move.piece)
                else -> iter
            }
        }
        return Board(
            squares = this.squares.map { iter -> convert(iter) },
            lastMove = Move(
                piece = chessPiece,
                from = fromSquare.position,
                to = toSquare.position,
                chessPieceCapturedOn = toSquare
            )
        )
    }

    fun resultOfMove(from: Position, to: Position): Board {

        val chessPiece = getChessPiece(from) ?: throw PieceNotFoundAtPositionException(from)

        val toBeBoard = moveChessPiece(chessPiece, from, to)
        val check = GameStateDeterminer.check(chessPiece.color.opposite(), toBeBoard)
        val checkMate = GameStateDeterminer.checkMate(chessPiece.color.opposite(), toBeBoard)
        val winner = if (checkMate) chessPiece.color else null

        return toBeBoard.copy(gameStatus = GameStatus(check, checkMate, winner))
    }

    fun castlingShort(color: PieceColor): Board {
        val castlingType = when(color) {
            PieceColor.WHITE -> CastlingType.SHORT_WHITE
            PieceColor.BLACK -> CastlingType.SHORT_BLACK
        }

        if (correctPositionsForShortCastling(color)) {
            val intermediateBoard = move(castlingType.kingMove.piece, this.squares, castlingType.kingMove.from, castlingType.kingMove.to)
            return move(castlingType.rookMove.piece, intermediateBoard.squares, castlingType.rookMove.from, castlingType.rookMove.to)
        } else {
            throw WrongSettlingForShortCastlingException(color)
        }
    }

    fun castlingLong(color: PieceColor): Board {
        val castlingType = when(color) {
            PieceColor.WHITE -> CastlingType.LONG_WHITE
            PieceColor.BLACK -> CastlingType.LONG_BLACK
        }

        if (correctPositionsForLongCastling(color)) {
            val intermediateBoard = move(castlingType.kingMove.piece, this.squares, castlingType.kingMove.from, castlingType.kingMove.to)
            return move(castlingType.rookMove.piece, intermediateBoard.squares, castlingType.rookMove.from, castlingType.rookMove.to)
        } else {
            throw WrongSettlingForShortCastlingException(color)
        }
    }

    private fun move(chessPiece: ChessPiece, squares: List<Square>, from: Position, to: Position): Board {

        val fromSquare = getChessSquare(from)
        val toSquare = getChessSquare(to)

        val toBeSquares = squares.map {
            when (it) {
                fromSquare -> it.removeChessPiece()
                toSquare -> it.placeChessPiece(chessPiece)
                else -> it
            }
        }
        return Board(squares = toBeSquares, lastMove = Move(chessPiece, from, to))
    }


    private fun correctPositionsForShortCastling(color: PieceColor): Boolean {
        return when(color) {
            PieceColor.WHITE -> getChessPiece(E1) == WHITE_KING && getChessPiece(H1) == WHITE_ROOK && emptySquaresAt(F1, G1)
            PieceColor.BLACK -> getChessPiece(E8) == BLACK_KING && getChessPiece(H8) == BLACK_ROOK && emptySquaresAt(F8, G8)
        }
    }

    private fun correctPositionsForLongCastling(color: PieceColor): Boolean {
        return when(color) {
            PieceColor.WHITE -> getChessPiece(E1) == WHITE_KING && getChessPiece(A1) == WHITE_ROOK && emptySquaresAt(B1, C1, D1)
            PieceColor.BLACK -> getChessPiece(E8) == BLACK_KING && getChessPiece(A8) == BLACK_ROOK && emptySquaresAt(B8, C8, D8)
        }
    }

    private fun emptySquaresAt(vararg squares: Position): Boolean {
        return squares.all { getChessSquare(it).isEmpty() }
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