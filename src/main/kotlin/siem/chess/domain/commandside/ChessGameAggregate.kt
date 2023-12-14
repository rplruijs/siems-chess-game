package siem.chess.domain.commandside

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import siem.chess.domain.*
import siem.chess.domain.commandside.board.Board
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.constants.*
import siem.chess.domain.commandside.board.generateChessBoardWithOpeningSettling
import siem.chess.domain.commandside.board.textualRepresentation
import siem.chess.domain.commandside.exceptions.MoveNotPossibleException
import siem.chess.domain.commandside.exceptions.PieceNotFoundAtPositionException
import siem.chess.domain.commandside.exceptions.WrongSettlingForLongCastlingException
import siem.chess.domain.commandside.exceptions.WrongSettlingForShortCastlingException
import siem.chess.domain.commandside.gamestatus.Move
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

    private var shortCastlingDoneByWhite: Boolean =  false
    private var shortCastlingDoneByBlack: Boolean =  false
    private var longCastlingDoneByWhite: Boolean =  false
    private var longCastlingDoneByBlack: Boolean =  false


    private var winner: Player? = null


    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(command: StartGameCommand) {
        apply(GameStartedEvent(command.gameId, command.dateTime, command.whitePlayer, command.blackPlayer, boardTextualOpeningSettling()))
    }

    @CommandHandler
    fun handle(command: ShortCastlingCommand) {

       if (winnerAlreadyDetermined()) { return }

        if (shortCastlingStillPossible()) {
            try {
                val toBeBoard = board.castlingShort(onMove.pieceColor)

                apply (
                    ShortCastlingAppliedEvent(gameId = command.gameId,
                        dateTime = command.dateTime,
                        pieceColor = this.onMove.pieceColor,
                        boardTextual =  textualRepresentation(toBeBoard.squares))
                )
            } catch (ex: WrongSettlingForShortCastlingException) {
                apply(ShortCastlingNotPossibleBecauseWrongSettlingEvent(
                    gameId = command.gameId,
                    dateTime = command.dateTime,
                    pieceColor = this.onMove.pieceColor)
                )
            }
        } else {
           apply ( ShortCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent(
                       gameId = command.gameId,
                       dateTime = command.dateTime,
                       pieceColor = this.onMove.pieceColor
                       )
           )
       }
    }

    @CommandHandler
    fun handle(command: LongCastlingCommand) {

        if (winnerAlreadyDetermined()) { return }

        if (longCastlingStillPossible()) {
            try {
                val toBeBoard = board.castlingLong(onMove.pieceColor)
                apply (
                    LongCastlingAppliedEvent(gameId = command.gameId,
                        dateTime = command.dateTime,
                        pieceColor = this.onMove.pieceColor,
                        boardTextual =  textualRepresentation(toBeBoard.squares))
                )
            } catch (ex: WrongSettlingForLongCastlingException){
                apply (
                    LongCastlingNotPossibleBecauseWrongSettlingEvent(gameId = command.gameId,
                        dateTime = command.dateTime,
                        pieceColor = this.onMove.pieceColor)
                )
            }
        } else {
            apply ( LongCastlingNotPossibleBecauseRookAndOrKingAreMovedAlreadyEvent(
                gameId = command.gameId,
                dateTime = command.dateTime,
                pieceColor = this.onMove.pieceColor
            ))
        }
    }

    private fun shortCastlingStillPossible(): Boolean {
        return when(onMove.pieceColor) {
                    PieceColor.BLACK -> shortCastlingStillPossibleByBlack
                    PieceColor.WHITE -> shortCastlingStillPossibleByWhite
        }
    }

    private fun longCastlingStillPossible(): Boolean {
        return when(onMove.pieceColor) {
            PieceColor.BLACK -> longCastlingStillPossibleByBlack
            PieceColor.WHITE -> longCastlingStillPossibleByWhite
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
                ))
                .andThenApplyIf({castlingShortNotPossibleAnyMoreCausedByMove(lastMove)},
                    {ShortCastlingNotPossibleAnyMoreEvent(gameId = command.gameId, dateTime = command.dateTime, color = onMove.pieceColor)}
                )
                .andThenApplyIf({castlingLongNotPossibleAnyMoreCausedByMove(lastMove)},
                    {LongCastlingNotPossibleAnyMoreEvent(gameId = command.gameId, dateTime = command.dateTime, color = onMove.pieceColor)}
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

    private fun castlingShortNotPossibleAnyMoreCausedByMove(lastMove: Move): Boolean {

        val shortCastlingStillPossible = when (lastMove.piece.color) {
            PieceColor.WHITE -> shortCastlingStillPossibleByWhite
            PieceColor.BLACK -> shortCastlingStillPossibleByBlack
        }

        if(!shortCastlingStillPossible) {
            return false
        } else {
            when (lastMove.piece) {
                WHITE_ROOK -> { if (lastMove.from == H1) { return true } }
                BLACK_ROOK -> { if (lastMove.from == H8) { return true } }
                BLACK_KING -> { if (lastMove.from == E8) { return true } }
                WHITE_KING -> { if (lastMove.from == E1) { return true } }
            }
        }
        return false
    }

    private fun castlingLongNotPossibleAnyMoreCausedByMove(lastMove: Move): Boolean {

        val longCastlingStillPossible = when (lastMove.piece.color) {
            PieceColor.WHITE -> longCastlingStillPossibleByWhite
            PieceColor.BLACK -> longCastlingStillPossibleByBlack
        }

        if(!longCastlingStillPossible) {
            return false
        }

        when (lastMove.piece) {
            WHITE_ROOK -> { if (lastMove.from == A1) { return true} }
            BLACK_ROOK -> { if (lastMove.from == A8) { return true} }
            BLACK_KING -> { if (lastMove.from == E8) { return true} }
            WHITE_KING -> { if (lastMove.from == E1) { return true} }
        }

        return false
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
    }

    @EventSourcingHandler
    fun on(event: ShortCastlingAppliedEvent) {
        when(event.pieceColor) {
            PieceColor.WHITE -> {
                this.shortCastlingDoneByWhite = true
                this.longCastlingStillPossibleByWhite = false
                this.shortCastlingStillPossibleByWhite = false
                this.onMove = blackPlayer
            }
            PieceColor.BLACK -> {
                this.shortCastlingDoneByBlack = true
                this.longCastlingStillPossibleByBlack = false
                this.shortCastlingStillPossibleByBlack = false
                this.onMove = whitePlayer
            }
        }
    }

    @EventSourcingHandler
    fun on(event: LongCastlingAppliedEvent) {
        when(event.pieceColor) {
            PieceColor.WHITE -> {
                this.longCastlingDoneByWhite = true
                this.longCastlingStillPossibleByWhite = false
                this.shortCastlingStillPossibleByWhite = false
                this.onMove = blackPlayer
            }
            PieceColor.BLACK -> {
                this.longCastlingDoneByBlack = true
                this.longCastlingStillPossibleByBlack = false
                this.shortCastlingStillPossibleByBlack = false
                this.onMove = whitePlayer
            }
        }
    }

    @EventSourcingHandler
    fun on(event: ShortCastlingNotPossibleAnyMoreEvent) {
        when(event.color) {
            PieceColor.WHITE -> this.shortCastlingStillPossibleByWhite = false
            PieceColor.BLACK -> this.shortCastlingStillPossibleByBlack = false
        }
    }

    @EventSourcingHandler
    fun on(event: LongCastlingNotPossibleAnyMoreEvent) {
        when(event.color) {
            PieceColor.WHITE -> this.longCastlingStillPossibleByWhite = false
            PieceColor.BLACK -> this.longCastlingStillPossibleByBlack = false
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