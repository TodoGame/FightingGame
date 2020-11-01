package com.somegame.match.matchmaking

import match.*
import match.websocketuser.WebSocketClient
import kotlin.math.max

class Player(private val client: WebSocketClient, private val match: Match) {

    val username = client.username

    var isActive = false
    private var health = 15

    val isAlive
        get() = health > 0

    fun changeIsActive() {
        isActive = !isActive
    }

    suspend fun onMatchStart() {
        client.sendMessage(MatchStarted(match.playersUsernames))
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
        client.disconnect()
    }

    suspend fun handleDisconnect() {
        match.handleDisconnect(this)
        PlayerService.clearPlayer(username)
        client.handleDisconnect()
    }

    private fun takeDamage(damage: Int) {
        health = max(health - damage, 0)
    }

    fun getSnapshot() = PlayerSnapshot(username, isActive, health)

    override fun toString() = "Player(username=$username)"
}