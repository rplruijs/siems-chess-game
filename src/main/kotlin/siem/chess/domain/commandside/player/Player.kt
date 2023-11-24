package siem.chess.domain.commandside.player

import siem.chess.domain.commandside.board.constants.PieceColor

data class Player(val name: String, val pieceColor: PieceColor)