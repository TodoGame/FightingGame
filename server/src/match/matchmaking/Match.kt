package com.somegame.match.matchmaking

import com.somegame.match.MatchRouting
import com.somegame.match.player.Player
import match.MatchSnapshot
import match.PlayerAction
import org.slf4j.LoggerFactory
import user.Username
import java.util.concurrent.atomic.AtomicInteger

class Match(clients: List<MatchRouting.MatchClient>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val players = clients.map { Player(it, this) }

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
        // notify the inactive player first
        getCurrentlyInActivePlayer()?.onTurnStart(snapshot)
        getCurrentlyActivePlayer()?.onTurnStart(snapshot)
    }

    private fun getPlayer(username: Username) = players.find { it.username == username }

    suspend fun handlePlayerAction(action: PlayerAction) {
        val target = getPlayer(action.target)
        val attacker = getPlayer(action.attacker)
        if (target == null || attacker == null || !target.isAlive || !attacker.isActive) {
            throw IllegalActionException(action)
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
        // the players list
        for (player in players.toList()) {
            player.handleGameEnd(winner)
        }
    }

    suspend fun handleDisconnect(player: Player) {
        if (state.get() == State.IN_PROGRESS.code) {
            logger.info("Player $player disconnected while match was in progress")
            val winner = players.filter { it != player }.first()
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

    private fun getCurrentlyInActivePlayer() = players.find { !it.isActive }

    private fun getCurrentlyActivePlayer() = players.find { it.isActive }

    private fun getSnapshot() = MatchSnapshot(players.map { it.getSnapshot() }.toSet())

    override fun toString() = "Match(users=${players.map { it.username }})"

    class IllegalActionException(action: PlayerAction) : IllegalStateException(action.toString())
}
