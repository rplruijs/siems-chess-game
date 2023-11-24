package siem.chess.domain.commandside.exceptions

import siem.chess.domain.commandside.board.Position

class PieceNotFoundAtPositionException(from: Position) : Exception("No piece located at $from " )
