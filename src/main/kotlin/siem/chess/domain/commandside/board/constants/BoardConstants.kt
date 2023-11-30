package siem.chess.domain.commandside.board.constants

import siem.chess.domain.commandside.board.Column
import siem.chess.domain.commandside.board.Position
import siem.chess.domain.commandside.board.Row

val A1: Position = Position(Column.A, Row.ONE)
val A2: Position = Position(Column.A, Row.TWO)
val A3: Position = Position(Column.A, Row.THREE)
val A4: Position = Position(Column.A, Row.FOUR)
val A5: Position = Position(Column.A, Row.FIVE)
val A6: Position = Position(Column.A, Row.SIX)
val A7: Position = Position(Column.A, Row.SEVEN)
val A8: Position = Position(Column.A, Row.EIGHT)

val B1: Position = Position(Column.B, Row.ONE)
val B2: Position = Position(Column.B, Row.TWO)
val B3: Position = Position(Column.B, Row.THREE)
val B4: Position = Position(Column.B, Row.FOUR)
val B5: Position = Position(Column.B, Row.FIVE)
val B6: Position = Position(Column.B, Row.SIX)
val B7: Position = Position(Column.B, Row.SEVEN)
val B8: Position = Position(Column.B, Row.EIGHT)

val C1: Position = Position(Column.C, Row.ONE)
val C2: Position = Position(Column.C, Row.TWO)
val C3: Position = Position(Column.C, Row.THREE)
val C4: Position = Position(Column.C, Row.FOUR)
val C5: Position = Position(Column.C, Row.FIVE)
val C6: Position = Position(Column.C, Row.SIX)
val C7: Position = Position(Column.C, Row.SEVEN)
val C8: Position = Position(Column.C, Row.EIGHT)

val D1: Position = Position(Column.D, Row.ONE)
val D2: Position = Position(Column.D, Row.TWO)
val D3: Position = Position(Column.D, Row.THREE)
val D4: Position = Position(Column.D, Row.FOUR)
val D5: Position = Position(Column.D, Row.FIVE)
val D6: Position = Position(Column.D, Row.SIX)
val D7: Position = Position(Column.D, Row.SEVEN)
val D8: Position = Position(Column.D, Row.EIGHT)

val E1: Position = Position(Column.E, Row.ONE)
val E2: Position = Position(Column.E, Row.TWO)
val E3: Position = Position(Column.E, Row.THREE)
val E4: Position = Position(Column.E, Row.FOUR)
val E5: Position = Position(Column.E, Row.FIVE)
val E6: Position = Position(Column.E, Row.SIX)
val E7: Position = Position(Column.E, Row.SEVEN)
val E8: Position = Position(Column.E, Row.EIGHT)

val F1: Position = Position(Column.F, Row.ONE)
val F2: Position = Position(Column.F, Row.TWO)
val F3: Position = Position(Column.F, Row.THREE)
val F4: Position = Position(Column.F, Row.FOUR)
val F5: Position = Position(Column.F, Row.FIVE)
val F6: Position = Position(Column.F, Row.SIX)
val F7: Position = Position(Column.F, Row.SEVEN)
val F8: Position = Position(Column.F, Row.EIGHT)

val G1: Position = Position(Column.G, Row.ONE)
val G2: Position = Position(Column.G, Row.TWO)
val G3: Position = Position(Column.G, Row.THREE)
val G4: Position = Position(Column.G, Row.FOUR)
val G5: Position = Position(Column.G, Row.FIVE)
val G6: Position = Position(Column.G, Row.SIX)
val G7: Position = Position(Column.G, Row.SEVEN)
val G8: Position = Position(Column.G, Row.EIGHT)

val H1: Position = Position(Column.H, Row.ONE)
val H2: Position = Position(Column.H, Row.TWO)
val H3: Position = Position(Column.H, Row.THREE)
val H4: Position = Position(Column.H, Row.FOUR)
val H5: Position = Position(Column.H, Row.FIVE)
val H6: Position = Position(Column.H, Row.SIX)
val H7: Position = Position(Column.H, Row.SEVEN)
val H8: Position = Position(Column.H, Row.EIGHT)


val allPositions = listOf(
    A1, B1, C1, D1, E1, F1, G1, H1,
    A2, B2, C2, D2, E2, F2, G2, H2,
    A3, B3, C3, D3, E3, F3, G3, H3,
    A4, B4, C4, D4, E4, F4, G4, H4,
    A5, B5, C5, D5, E5, F5, G5, H5,
    A6, B6, C6, D6, E6, F6, G6, H6,
    A7, B7, C7, D7, E7, F7, G7, H7,
    A8, B8, C8, D8, E8, F8, G8, H8,
)

val startPositionsWithChessPiece = mapOf(
    A1 to WHITE_ROOK,
    B1 to WHITE_KNIGHT,
    C1 to WHITE_BISHOP,
    D1 to WHITE_QUEEN,
    E1 to WHITE_KING,
    F1 to WHITE_BISHOP,
    G1 to WHITE_KNIGHT,
    H1 to WHITE_ROOK,

    A2 to WHITE_PAWN,
    B2 to WHITE_PAWN,
    C2 to WHITE_PAWN,
    D2 to WHITE_PAWN,
    E2 to WHITE_PAWN,
    F2 to WHITE_PAWN,
    G2 to WHITE_PAWN,
    H2 to WHITE_PAWN,

    A7 to BLACK_PAWN,
    B7 to BLACK_PAWN,
    C7 to BLACK_PAWN,
    D7 to BLACK_PAWN,
    E7 to BLACK_PAWN,
    F7 to BLACK_PAWN,
    G7 to BLACK_PAWN,
    H7 to BLACK_PAWN,

    A8 to BLACK_ROOK,
    B8 to BLACK_KNIGHT,
    C8 to BLACK_BISHOP,
    D8 to BLACK_QUEEN,
    E8 to BLACK_KING,
    F8 to BLACK_BISHOP,
    G8 to BLACK_KNIGHT,
    H8 to BLACK_ROOK,
)

