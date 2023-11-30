package siem.chess.domain.commandside.gamestatus

import siem.chess.domain.commandside.board.Position
import siem.chess.domain.commandside.board.Square
import siem.chess.domain.commandside.board.constants.ChessPiece
import siem.chess.domain.commandside.board.constants.PieceColor

data class Move (
    val piece: ChessPiece,
    val from: Position,
    val to: Position,
    val chessPieceCapturedOn: Square? = null
)

data class GameStatus (
    val check: Boolean,
    val checkMate: Boolean,
    val winner: PieceColor? = null
)


