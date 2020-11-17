package com.somegame.match.player

import com.somegame.match.MatchRouting
import com.somegame.match.matchmaking.Match
import match.*
import kotlin.math.max

class Player(private val client: MatchRouting.MatchClient, val match: Match) {
    val username = client.username

    var isActive = false
    private var health = 15

    val isAlive
        get() = health > 0

    init {
        client.onJoinMatch(this)
    }

    fun changeIsActive() {
        isActive = !isActive
    }

    suspend fun onMatchStart() {
        client.sendMessage(MatchStarted(match.playersUsernames.toSet()))
    }

    suspend fun onTurnStart(matchSnapshot: MatchSnapshot) {
        client.sendMessage(TurnStarted(matchSnapshot))
    }

    suspend fun doAction(action: PlayerAction) {
        match.handlePlayerAction(action)
    }

    suspend fun handleAction(action: PlayerAction) {
        client.sendMessage(action)
        if (action.target == username) {
            takeDamage(10)
        }
    }

    suspend fun handleGameEnd(winner: Player) {
        client.sendMessage(MatchEnded(winner.username))
        client.kick()
    }

    suspend fun handleDisconnect() {
        match.handleDisconnect(this)
    }

    private fun takeDamage(damage: Int) {
        health = max(health - damage, 0)
    }

    fun getSnapshot() = PlayerSnapshot(username, isActive, health)

    override fun toString() = "Player(username=$username)"
}
