package siem.chess.domain.commandside.exceptions

import siem.chess.domain.commandside.board.Square

class MoveNotPossibleException(val from: Square, val to: Square) : Exception("no move possible from $from to $to")
