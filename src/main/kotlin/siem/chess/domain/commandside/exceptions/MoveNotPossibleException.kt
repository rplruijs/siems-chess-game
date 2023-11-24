package siem.chess.domain.commandside.exceptions

import siem.chess.domain.commandside.board.Square

class MoveNotPossibleException(from: Square, to: Square) : Exception("no move possible from $from to $to")
