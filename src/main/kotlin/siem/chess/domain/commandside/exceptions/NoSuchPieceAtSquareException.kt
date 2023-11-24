package siem.chess.domain.commandside.exceptions

import siem.chess.domain.commandside.board.Square

class NoSuchPieceAtSquareException(from: Square) : Exception("No piece located at square $from" )


