package siem.chess.domain.commandside.gamestatus

import siem.chess.domain.commandside.board.constants.ChessPiece
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.commandside.board.Position

data class Move (
    val piece: ChessPiece,
    val from: Position,
    val to: Position,
    val chessPieceCaptured: ChessPiece? = null
)

data class GameStatus (
    val check: Boolean,
    val checkMate: Boolean,
    val winner: PieceColor? = null
)


