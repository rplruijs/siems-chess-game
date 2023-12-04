package siem.chess.domain.commandside.board

import siem.chess.domain.commandside.board.constants.ChessPiece
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.commandside.board.constants.PieceType
import siem.chess.domain.commandside.exceptions.NoSuchPieceAtSquareException
import siem.chess.domain.commandside.gamestatus.Move
import kotlin.math.max
import kotlin.math.min

object MoveDeterminer {

    fun possibleMoves(from: Square, board: Board ): List<Move> {

        val fromPosition = from.position
        val chessPiece = from.piece ?: throw NoSuchPieceAtSquareException(from)
        val squares = board.squares

        fun movePossibilities(targets: List<Square>): List<Move> {
            return targetsToMoves(targets, fromPosition, chessPiece, squares)
        }

        fun movePawnPossibilities(color: PieceColor): List<Move> {
            val captureTargets = pawnCaptureTargets(from.position, squares, color)

            val movesSoFar = pawnMovementTargetsToMoves(pawnMovementTargets(from.position, squares, color),  fromPosition, chessPiece, squares)
                .plus(pawnCaptureTargetsToMoves(captureTargets,  fromPosition, chessPiece))

            return if (board.lastMove != null) {
                movesSoFar.plus(enPassantCaptureTargetsToMove(captureTargets, fromPosition, chessPiece, lastMove = board.lastMove))
            } else {
                movesSoFar
            }
        }

        fun moveKnightPossibilities(): List<Move> {
            val possibleTargets = knightTargets(from.position, squares)
            return knightTargetsToMoves(possibleTargets, fromPosition, chessPiece)
        }

        return when (from.piece.type) {
            PieceType.KING   -> movePossibilities(allTargets(1, from.position, squares))
            PieceType.QUEEN  -> movePossibilities(allTargets(from = from.position, squares = squares))
            PieceType.ROOK   -> movePossibilities(lateralTargets(from = from.position, squares = squares))
            PieceType.BISHOP -> movePossibilities(diagonalTargets(from = from.position, squares = squares))
            PieceType.PAWN   -> movePawnPossibilities(from.piece.color)
            PieceType.KNIGHT -> moveKnightPossibilities()
        }
    }

    private fun targetsToMoves(possibleTargets: List<Square>, from: Position, chessPiece: ChessPiece, squares: List<Square>): List<Move> {

        return possibleTargets.mapNotNull { it ->
            val range = rangeDeterminer(from, it.position, squares)
            val move = range.drop(1).all { it.piece == null }
            val capture = range.last.containsPieceOfOppositeColor(chessPiece) && noPiecesBetween(range)

            when {
                move -> toMove(from = from, to = it.position, piece = chessPiece)
                capture -> toMove(
                    from = from,
                    to = it.position,
                    piece = chessPiece,
                    chessPieceCapturedOn = range.last
                )

                else -> null
            }
        }
    }

    private fun knightTargetsToMoves(possibleTargets: List<Square>, from: Position, chessPiece: ChessPiece): List<Move> {
        return possibleTargets.map {
            val capture = it.piece != null

            when {
                capture -> toMove(from = from, to = it.position, piece = chessPiece, chessPieceCapturedOn = it)
                else    -> toMove(from = from, to = it.position, piece = chessPiece)
            }
        }
    }

    private fun pawnMovementTargetsToMoves(possibleMovements: List<Square>, from: Position, chessPiece: ChessPiece, squares: List<Square>): List<Move> {

        return possibleMovements.mapNotNull { it ->
            val range = rangeDeterminer(from, it.position, squares)
            val move = range.drop(1).all { it.piece == null }

            when {
                move -> toMove(from = from, to = it.position, piece = chessPiece)
                else -> null
            }
        }
    }

    private fun pawnCaptureTargetsToMoves(
        possibleCaptures: List<Square>,
        from: Position,
        chessPiece: ChessPiece): List<Move> {
        return possibleCaptures
            .filter {
                it.containsPieceOfOppositeColor(chessPiece)
            }
            .map { toMove(from = from, to = it.position, piece = chessPiece, chessPieceCapturedOn = it) }
    }
    private fun enPassantCaptureTargetsToMove (
        possibleCaptures: List<Square>,
        from: Position,
        chessPiece: ChessPiece,
        lastMove: Move) : List<Move> {

        val enPassantSquare = possibleCaptures.find { enPassantCaptureIsPossible(it, lastMove) }

        return if (enPassantSquare != null) {
            listOf(toMove(from = from, to = enPassantSquare.position, piece = chessPiece, chessPieceCapturedOn = Square(position = lastMove.to, piece = lastMove.piece)))
        } else {
            emptyList()
        }
    }

    private fun enPassantCaptureIsPossible(to: Square, lastMove: Move?): Boolean {
        return if (lastMove == null) {
            false
        } else {
            (to.isEmpty()
                    && pawnMoveFromStartPositionDistanceTwo(lastMove)
                    && lastMove.to.verticalDistance(to.position) == 1
                    && lastMove.to.sameColumn(to.position))
        }
    }

    private fun pawnMoveFromStartPositionDistanceTwo(move: Move): Boolean {
        return when (move.piece.type) {
            PieceType.PAWN -> move.from.isStartPositionPawn(move.piece.color) && move.from.verticalDistance(move.to) == 2
            else           -> false
        }
    }

    private fun rangeDeterminer(from: Position, to: Position, squares: List<Square>): List<Square> {
        val indexData = indexDeterminer(from, to)

        val result = when {
            from.sameRow(to)      -> squares.subList(indexData.first, indexData.second + 1)
            from.sameColumn(to)   -> (indexData.first..indexData.second step 8).toList().map { squares[it] }
            from.sameDiagonal(to) -> {

                val first = squares[indexData.first].position
                val second = squares[indexData.second].position

                val stepSize = when {
                    first.left(second) -> 9
                    else -> 7
                }
                (indexData.first..indexData.second step stepSize).toList().map { squares[it] }
            }

            else -> emptyList()
        }

        return if (indexData.third) {
            result.reversed()
        } else {
            result
        }
    }

    private fun indexDeterminer(from: Position, to: Position): Triple<Int, Int, Boolean> {
        val indexFrom     = (from.row.index * 8) + from.column.index
        val toIndex       = (to.row.index * 8) + to.column.index
        val shouldReverse = indexFrom > toIndex

        return Triple(min(indexFrom, toIndex), max(indexFrom, toIndex), shouldReverse)
    }

    private fun toMove(from: Position, to: Position, piece: ChessPiece, chessPieceCapturedOn: Square? = null): Move {
        return Move(from = from, to = to, piece = piece, chessPieceCapturedOn = chessPieceCapturedOn)
    }

    private fun allTargets(limit: Int = 8, from: Position, squares: List<Square>): List<Square> {
        return lateralTargets(limit, from, squares).plus(diagonalTargets(limit, from, squares))
    }

    private fun lateralTargets(limit: Int = 8, from: Position, squares: List<Square>): List<Square> {
        return verticalTargets(limit, from, squares).plus(horizontalTargets(limit, from, squares))
    }

    private fun verticalTargets(limit: Int = 0, from: Position, squares: List<Square>): List<Square> {
        val allSquaresExceptFrom = squares.filter { it.position != from }

        return allSquaresExceptFrom
            .filter { it.position.sameColumn(from) }
            .filter { it.position.verticalDistance(from) <= limit }
    }

    private fun horizontalTargets(limit: Int = 8, from: Position, squares: List<Square>): List<Square> {
        val allSquaresExceptFrom = squares.filter { it.position != from }

        return allSquaresExceptFrom
            .filter { it.position.sameRow(from) }
            .filter { it.position.horizontalDistance(from) <= limit }
    }

    private fun diagonalTargets(limit: Int = 8, from: Position, squares: List<Square>): List<Square> {
        val allSquaresExceptFrom = squares.filter { it.position != from }

        return allSquaresExceptFrom
            .filter { it.position.sameDiagonal(from) }
            .filter { it.position.diagonalDistance(from) <= limit }
    }

    private fun knightTargets(from: Position, squares: List<Square>): List<Square> {
        return squares.filter { from.isKnightJump(it.position) }
    }

    private fun pawnMovementTargets(from: Position, squares: List<Square>, color: PieceColor): List<Square> {
        val forwardMovementsSquares = squares.filter {
            when (color) {
                PieceColor.BLACK -> it.position.below(from)
                PieceColor.WHITE -> it.position.above(from)
            }
        }

        val maxVerticalDistance = when {
            from.isStartPositionPawn(color) -> 2
            else                            -> 1
        }

        return verticalTargets(maxVerticalDistance, from,  forwardMovementsSquares)
    }

    private fun pawnCaptureTargets(from: Position, squares: List<Square>, color: PieceColor): List<Square> {
        val forwardMovementsSquares = squares.filter {
            when (color) {
                PieceColor.BLACK -> it.position.below(from)
                PieceColor.WHITE -> it.position.above(from)
            }
        }
        return diagonalTargets(1, from, forwardMovementsSquares)
    }
}