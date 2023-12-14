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
import siem.chess.domain.queryside.GameState

@Component
class ChessGameStateProjection(val queryUpdateEmitter: QueryUpdateEmitter, val repository: ChessGameStateRepository) {

    @EventHandler
    fun handle(event: GameStartedEvent) {
        val startGameState = startGameState(event.gameId)
        upsertAndEmit(startGameState)
    }

    @EventHandler
    fun handle(event: ChessPieceMovedEvent) {
        upsertAndEmit(getCurrentGameState(event.gameId)
            .defaultChessGameStateFlip(event.boardTextual)
        )
    }

    @EventHandler
    fun handle(event: ShortCastlingAppliedEvent) {
        upsertAndEmit(getCurrentGameState(event.gameId)
            .defaultChessGameStateFlip(boardTextual = event.boardTextual)
            .castlingLongNotPossible(event.pieceColor)
        )
    }

    @EventHandler
    fun handle(event: LongCastlingAppliedEvent) {
        upsertAndEmit(getCurrentGameState(gameId = event.gameId)
            .defaultChessGameStateFlip(event.boardTextual)
            .castlingNotPossibleFor(event.pieceColor)
        )
    }

    @EventHandler
    fun handle(event: GameEndedByCheckMateEvent) {
        repository.upsertGameState(
            getCurrentGameState(event.gameId)
                .endedGame()
        )
    }

    @EventHandler
    fun hanlde(event: ShortCastlingNotPossibleAnyMoreEvent){
        repository.upsertGameState(
            repository.retrieveGameState(event.gameId)
                .castlingShortNotPossible(event.color)
        )
    }

    @EventHandler
    fun hanlde(event: LongCastlingNotPossibleAnyMoreEvent){
        repository.upsertGameState(
            repository.retrieveGameState(event.gameId)
                .castlingLongNotPossible(event.color)
        )
    }

    @QueryHandler
    fun handle(query: ChessGameTurnQuery): ChessGameState {
        return startGameState(query.gameId)
    }

    private fun getCurrentGameState(gameId: String): ChessGameState {
        return repository.retrieveGameState(gameId)
    }

    private fun upsertAndEmit(gameState: ChessGameState) {
        queryUpdateEmitter.emit(
            ChessGameTurnQuery::class.java,
            { query: ChessGameTurnQuery -> gameState.gameId == query.gameId },
            gameState
        )
        repository.upsertGameState(gameState)
    }
    private fun startGameState(gameId: String): ChessGameState {
        return ChessGameState(
            gameId = gameId,
            gameState = GameState.STARTED,
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