package siem.chess.domain.commandside.gamestatus

import siem.chess.domain.commandside.board.constants.ChessPiece
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.commandside.board.constants.PieceType
import siem.chess.domain.commandside.board.Board
import siem.chess.domain.commandside.board.MoveDeterminer

object GameStateDeterminer {

    fun check(color: PieceColor, board: Board): Boolean {
        return board.allSquaresWith(color.opposite())
            .flatMap { MoveDeterminer.possibleMoves(it, board) }
            .any { it.chessPieceCaptured == ChessPiece(PieceType.KING, color) }
    }

    fun checkMate(color: PieceColor, board: Board): Boolean {
        return when {
            check(color, board) -> return !movePossibleToAttackCheck(color, board)
            else -> false
        }
    }

    private fun movePossibleToAttackCheck(color: PieceColor, board: Board): Boolean {
        return board.allSquaresWith(color)
            .flatMap { MoveDeterminer.possibleMoves(it, board) }
            .map { board.moveChessPiece(it.piece, it.from, it.to) }
            .any { !check(color, it) }
    }
}