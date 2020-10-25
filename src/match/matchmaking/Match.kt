package com.somegame.match.matchmaking

import com.somegame.match.MatchLogger
import com.somegame.match.messages.PlayerAction

class Match {
    private val players = mutableSetOf<Player>()

    fun joinPlayer(player: Player) {
        players.add(player)
        MatchLogger.logPlayerAddedToMatch(player, this)
    }

    suspend fun start() {
        val activePlayer = players.first()
        activePlayer.changeIsActive()
        MatchLogger.logMatchStart(this, activePlayer)
        handleTurnStart()
    }

    private suspend fun handleTurnStart() {
        val snapshot = getSnapshot()
        MatchLogger.logTurnStart(snapshot)
        for (player in players) {
            player.onTurnStart(snapshot)
        }
    }

    suspend fun handlePlayerAction(action: PlayerAction) {
        val target = PlayerService.getPlayer(action.target)
        val attacker = PlayerService.getPlayer(action.attacker)
        if (target == null || attacker == null || !target.isAlive) {
            return
        }
        val playersExceptAttacker = players.filter { it.username != attacker.username }
        for (player in playersExceptAttacker) {
            player.handleAction(action)
        }
        handleTurnEnd()
    }

    private suspend fun handleTurnEnd() {
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

    suspend fun endGame(winner: Player) {
        for (player in players) {
            player.handleGameEnd(winner)
        }
    }

    suspend fun handleDisconnect(player: Player) {
        players.remove(player)
        val winner = players.first()
        endGame(winner)
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

    private fun getSnapshot() = MatchSnapshot(players.map { it.getSnapshot() })

    data class MatchSnapshot(val players: List<Player.PlayerSnapshot>)

    override fun toString() = "Match(users=${players.map { it.username }})"
}