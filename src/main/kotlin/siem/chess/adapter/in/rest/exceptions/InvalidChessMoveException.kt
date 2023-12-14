package siem.chess.adapter.`in`.rest.exceptions

class InvalidChessMoveException(cause: String) : Exception(cause)
class InvalidPositionException(input: String) : Exception("Invalid position $input")
