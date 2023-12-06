package siem.chess.application.projections

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import siem.chess.application.repositories.ChessGameStateRepository
import siem.chess.domain.*
import siem.chess.domain.commandside.board.boardTextualOpeningSettling
import siem.chess.domain.commandside.board.constants.PieceColor
import siem.chess.domain.queryside.ChessGameState

@Component
class ChessGameStateProjection(val queryUpdateEmitter: QueryUpdateEmitter, val repository: ChessGameStateRepository) {

    @EventHandler
    fun handle(event: GameStartedEvent) {
        val startGameState = startGameState(event.gameId)
        queryUpdateEmitter.emit(
            ChessGameState::class.java,
            { query: ChessGameState -> event.gameId == query.gameId },
            startGameState
        )
        repository.upsertGameState(startGameState)
    }

    @EventHandler
    fun handle(event: ChessPieceMovedEvent) {





        //Flip turn
    }

    @EventHandler
    fun handle(event: MoveNotPossibleByWrongTargetEvent) {
        //Show feedback message
    }

    @EventHandler
    fun handle(event: MoveAttemptByWrongPlayerEvent ) {
        //Show feedback message
    }


    @EventHandler
    fun handle(event: CastlingAppliedEvent) {
        //Flip turn
    }

    @EventHandler
    fun handle(event: CastlingNotPossibleEvent) {
        //show feedback message
    }

    @QueryHandler
    fun handle(query: ChessMoveQuery): ChessGameState {
        return startGameState(query.gameId)
    }
    private fun startGameState(gameId: String): ChessGameState {
        return ChessGameState(
            gameId = gameId,
            currentTurn = PieceColor.WHITE,
            turnNumber = 1,
            castlingShortStillPossibleByWhite = true,
            castlingLongStillPossibleByWhite = true,
            castlingShortStillPossibleByBlack = true,
            castlingLongStillPossibleByBlack = true,
            settling = boardTextualOpeningSettling()
        )
    }
}