package siem.chess.domain.commandside.exceptions

import siem.chess.domain.CastlingType
import siem.chess.domain.commandside.board.constants.PieceColor

class WrongSettlingForShortCastlingException(color: PieceColor) : Exception("Short castling with $color is not possible because of wrong settling" )
class WrongSettlingForLongCastlingException(color: PieceColor) : Exception("Long castling with $color is not possible because of wrong settling" )
