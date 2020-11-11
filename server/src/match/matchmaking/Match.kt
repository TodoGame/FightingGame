package com.somegame.match.matchmaking

import com.somegame.match.player.Player
import com.somegame.match.player.PlayerService
import com.somegame.websocket.WebSocketService
import match.MatchSnapshot
import match.PlayerAction
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class Match(clients: List<WebSocketService.Client>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val players = clients.map { PlayerService.makePlayer(it, this) }.toMutableList()

    val playersUsernames
        get() = players.map { it.username }

    private val state = AtomicInteger(State.IDLE.code)

    enum class State(val code: Int) {
        IDLE(0),
        IN_PROGRESS(1),
        ENDED(2),
    }

    suspend fun start() {
        state.set(State.IN_PROGRESS.code)
        for (player in players) {
            player.onMatchStart()
        }
        val activePlayer = players.first()
        activePlayer.changeIsActive()
        logger.info("Match $this started with first active player $activePlayer")
        handleTurnStart()
    }

    private suspend fun handleTurnStart() {
        val snapshot = getSnapshot()
        logger.info("Turn started $snapshot")
        for (player in players) {
            player.onTurnStart(snapshot)
        }
    }

    suspend fun handlePlayerAction(action: PlayerAction) {
        val target = PlayerService.getPlayer(action.target)
        val attacker = PlayerService.getPlayer(action.attacker)
        if (target == null || attacker == null || !target.isAlive || !attacker.isActive) {
            throw IllegalAction(action)
        }
        val playersExceptAttacker = players.filter { it.username != attacker.username }
        for (player in playersExceptAttacker) {
            player.handleAction(action)
        }
        handleTurnEnd()
    }

    private suspend fun handleTurnEnd() {
        logger.info("Turn ended $this")
        val winner = getWinner()
        if (winner != null) {
            endGame(winner)
        } else {
            for (player in players) {
                player.changeIsActive()
            }
            handleTurnStart()
        }
    }

    private suspend fun endGame(winner: Player) {
        state.set(State.ENDED.code)
        logger.info("Match ended $this")
        for (player in players) {
            player.handleGameEnd(winner)
        }
    }

    suspend fun handleDisconnect(player: Player) {
        players.remove(player)
        logger.info("Player $player disconnected while match was in progress")
        if (state.get() == State.IN_PROGRESS.code) {
            val winner = players.first()
            endGame(winner)
        }
    }

    /**
     * Game ends if at least 1 player is dead
     * The winner is the first alive player
     * (this method only logically works when there are 2 players)
     */
    private fun getWinner(): Player? {
        val gameEnded = players.any { !it.isAlive }
        return if (gameEnded) {
            players.find { it.isAlive }
        } else {
            null
        }
    }

    private fun getSnapshot() = MatchSnapshot(players.map { it.getSnapshot() }.toSet())

    override fun toString() = "Match(users=${players.map { it.username }})"

    class IllegalAction(action: PlayerAction) : IllegalStateException(action.toString())
}
