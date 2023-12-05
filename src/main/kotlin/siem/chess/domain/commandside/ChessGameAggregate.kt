package siem.chess.domain.commandside

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import siem.chess.adapter.`in`.rest.CastlingMove
import siem.chess.domain.*
import siem.chess.domain.commandside.board.Board
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.constants.*
import siem.chess.domain.commandside.board.generateChessBoardWithOpeningSettling
import siem.chess.domain.commandside.board.textualRepresentation
import siem.chess.domain.commandside.exceptions.MoveNotPossibleException
import siem.chess.domain.commandside.exceptions.PieceNotFoundAtPositionException
import siem.chess.domain.commandside.player.Player

@Aggregate
class ChessGameAggregate() {

    @AggregateIdentifier
    private lateinit var gameId: String
    private lateinit var  whitePlayer: Player
    private lateinit var  blackPlayer: Player

    private lateinit var onMove: Player
    private lateinit var board: Board

    private var shortCastlingStillPossibleByWhite: Boolean = true
    private var shortCastlingStillPossibleByBlack: Boolean = true
    private var longCastlingStillPossibleByWhite: Boolean = true
    private var longCastlingStillPossibleByBlack: Boolean = true

    private var castlingDoneByWhite: Boolean =  false
    private var castlingDoneByBlack: Boolean =  false

    private var winner: Player? = null


    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(command: StartGameCommand) {
        apply(GameStartedEvent(command.gameId, command.dateTime, command.whitePlayer, command.blackPlayer, boardTextualOpeningSettling()))
    }

    @CommandHandler
    fun handle(command: CastlingCommand) {

       if (winnerAlreadyDetermined()) { return }

        val castlingType = toCastlingType(command)

       if (isCastlingPossible(castlingType)) {
           val toBeBoard = board.castling(castlingType)
           apply (
               CastlingAppliedEvent(gameId = command.gameId,
                   dateTime = command.dateTime,
                   pieceColor = this.onMove.pieceColor,
                   castlingMove = command.castlingMove,
                   boardTextual =  textualRepresentation(toBeBoard.squares))
           )
       } else {
           apply ( CastlingNotPossibleEvent(
                       gameId = command.gameId,
                       dateTime = command.dateTime,
                       pieceColor = this.onMove.pieceColor,
                       castlingMove = command.castlingMove))
       }
    }

    private fun toCastlingType(command: CastlingCommand): CastlingType {
        return when (onMove.pieceColor) {
                PieceColor.WHITE -> {
                    when(command.castlingMove) {
                        CastlingMove.CASTLING_LONG  -> CastlingType.LONG_WHITE
                        CastlingMove.CASTLING_SHORT -> CastlingType.SHORT_WHITE
                    }
                }
                PieceColor.BLACK -> {
                    when(command.castlingMove) {
                        CastlingMove.CASTLING_LONG  -> CastlingType.LONG_BLACK
                        CastlingMove.CASTLING_SHORT -> CastlingType.SHORT_BLACK
                    }
                }
        }
    }

    private fun isCastlingPossible(castlingType: CastlingType): Boolean {
        return when(castlingType) {
                    CastlingType.SHORT_BLACK -> shortCastlingStillPossibleByBlack
                    CastlingType.SHORT_WHITE -> shortCastlingStillPossibleByWhite
                    CastlingType.LONG_BLACK  -> longCastlingStillPossibleByBlack
                    CastlingType.LONG_WHITE  -> longCastlingStillPossibleByWhite
        }
    }

    @CommandHandler
    fun handle(command: MoveChessPieceCommand) {

        if (winnerAlreadyDetermined()) { return }
        if (moveDoneByWrongPlayer(command)) {
            apply(MoveAttemptByWrongPlayerEvent(command.gameId, command.dateTime, onMove.pieceColor.opposite(), command.from, command.to))
            return
        }
        try {
            val toBeBoard = board.resultOfMove(command.from, command.to)
            val lastMove = toBeBoard.lastMove ?: return
            apply(
                ChessPieceMovedEvent(
                    gameId = command.gameId,
                    command.dateTime,
                    chessPiece = lastMove.piece,
                    from = lastMove.from,
                    to = lastMove.to,
                    boardTextual = textualRepresentation(toBeBoard.squares)
                )
            )
                .andThenApplyIf(
                    { toBeBoard.gameStatus?.check },
                    { CheckEvent(gameId = command.gameId, dateTime = command.dateTime, check = onMove.pieceColor.opposite(), boardTextual = textualRepresentation(this.board.squares)) }
                )
                .andThenApplyIf(
                    {  toBeBoard.gameStatus?.checkMate },
                    {  GameEndedByCheckMateEvent(gameId = gameId, dateTime = command.dateTime, winner = onMove.pieceColor, boardTextual = textualRepresentation(this.board.squares)) }
                )
        } catch (nsp: PieceNotFoundAtPositionException) {
            apply(
                MoveNotPossibleByMissingChessPieceEvent(
                gameId = command.gameId,
                dateTime = command.dateTime,
                from = command.from,
                to = command.to)
            )

        } catch (mnp: MoveNotPossibleException) {
            apply(
                MoveNotPossibleByWrongTargetEvent(
                gameId = command.gameId,
                dateTime = command.dateTime,
                chessPiece = mnp.from.piece!!,
                from = command.from,
                to = command.to)
            )
        }
    }

    private fun moveDoneByWrongPlayer(command: MoveChessPieceCommand): Boolean {
        return !this.board.pieceOfColorOnPosition(onMove.pieceColor, command.from)
    }

    private fun winnerAlreadyDetermined(): Boolean {
        return winner != null
    }

    @EventSourcingHandler
    fun on(event: GameStartedEvent) {
        this.whitePlayer = Player(event.whitePlayer, PieceColor.WHITE)
        this.blackPlayer = Player(event.blackPlayer, PieceColor.BLACK)
        this.gameId = event.gameId
        this.board = generateChessBoardWithOpeningSettling()
        this.onMove = whitePlayer
    }

    @EventSourcingHandler
    fun on(event: ChessPieceMovedEvent) {
        this.board = board.moveChessPiece(chessPiece = event.chessPiece, from = event.from, to = event.to)
        this.onMove = when(event.chessPiece.color) {
            PieceColor.WHITE -> blackPlayer
            PieceColor.BLACK -> whitePlayer
        }
        handleCastlingLogic(event)
    }

    private fun handleCastlingLogic(event: ChessPieceMovedEvent) {
        when (event.chessPiece) {
            WHITE_ROOK -> {
                    when (event.from) {
                        A1 -> this.longCastlingStillPossibleByWhite = false
                        H1 -> this.shortCastlingStillPossibleByWhite = false
                    }
                }
            BLACK_ROOK -> {
                    when (event.from) {
                        A8 -> this.longCastlingStillPossibleByBlack = false
                        H8 -> this.shortCastlingStillPossibleByBlack = false
                    }
            }
            BLACK_KING -> {
                if (event.from == E8) {
                    this.longCastlingStillPossibleByBlack = false
                    this.shortCastlingStillPossibleByBlack = false
                }
            }
            WHITE_KING -> {
                if (event.from == E1) {
                    this.longCastlingStillPossibleByWhite = false
                    this.shortCastlingStillPossibleByWhite = false
                }
            }
            else -> return
        }
    }

    @EventSourcingHandler
    fun on(event: CastlingAppliedEvent) {
        when(event.pieceColor) {
            PieceColor.WHITE -> this.castlingDoneByWhite = true
            PieceColor.BLACK -> this.castlingDoneByBlack = true
        }
    }

    @EventSourcingHandler
    fun on(event: CheckEvent) {
        this.board = board.setCheck()
    }

    @EventSourcingHandler
    fun on(event: GameEndedByCheckMateEvent) {
        this.board = board.setCheckMate()

        winner = when (event.winner) {
            PieceColor.WHITE -> whitePlayer
            PieceColor.BLACK -> blackPlayer
        }
    }
}