package siem.chess.domain.commandside.exceptions

import siem.chess.domain.CastlingType

class WrongSettlingForCastlingException(castlingType: CastlingType) : Exception("Castling of type $castlingType not possible because of wrong settling" )
