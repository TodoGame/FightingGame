package com.somegame.match.player

import com.somegame.match.MatchRouting
import com.somegame.match.START_HEALTH
import com.somegame.match.matchmaking.Match
import com.somegame.user.UserMoneyManager
import match.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class Player(private val client: MatchRouting.MatchClient, val match: Match) : KoinComponent {

    val userMoneyManager: UserMoneyManager by inject()

    val username = client.username

    val user = client.user

    var isActive = false
    private var health = START_HEALTH

    private val disconnected = AtomicBoolean(false)

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

    suspend fun makeDecision(playerDecision: PlayerDecision) {
        if (!isActive) {
            throw Match.IllegalActionException(playerDecision)
        }
        match.handlePlayerDecision(playerDecision)
    }

    suspend fun handleCalculatedDecision(decision: CalculatedPlayerDecision) {
        client.sendMessage(decision)
        if (decision is CalculatedPlayerAction && decision.target == username) {
            takeDamage(decision.damage)
        }
    }

    suspend fun handleGameEnd(winner: Player) {
        if (!disconnected.get()) {
            if (winner == this) {
                userMoneyManager.onUserWin(user)
            } else {
                userMoneyManager.onUserLose(user)
            }
            client.sendMessage(MatchEnded(winner.username))
            client.kick("Match Ended")
        }
    }

    suspend fun handleDisconnect() {
        disconnected.set(true)
        match.handleDisconnect(this)
    }

    private fun takeDamage(damage: Int) {
        health = max(health - damage, 0)
    }

    fun getSnapshot() = PlayerSnapshot(username, isActive, health)

    override fun toString() = "Player(username=$username)"
}
