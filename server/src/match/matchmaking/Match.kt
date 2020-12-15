package com.somegame.match.matchmaking

import com.somegame.items.Item
import com.somegame.items.ItemRepository
import com.somegame.match.MatchRouting
import com.somegame.match.player.Player
import match.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import user.Username
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.floor
import kotlin.random.Random

class Match(clients: List<MatchRouting.MatchClient>) : KoinComponent {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val players = clients.map { Player(it, this) }

    val itemRepository: ItemRepository by inject()

    val playersUsernames
        get() = players.map { it.username }

    private val state = AtomicInteger(State.IDLE.code)

    companion object {
        const val HANDS_DAMAGE = 15
        const val DICE_ID = 4
    }

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

    suspend fun handlePlayerDecision(decision: PlayerDecision) {
        val calculatedPlayerDecision = when (decision) {
            is PlayerAction -> calculatePlayerAction(decision)
            is SkipTurn -> calculateSkipTurn(decision)
        }
        for (player in players) {
            player.handleCalculatedDecision(calculatedPlayerDecision)
        }
        handleTurnEnd()
    }

    private fun calculateSkipTurn(skipTurn: SkipTurn): CalculatedSkipTurn {
        val activePlayer = getActivePlayer() ?: throw IllegalActionException(skipTurn)
        return CalculatedSkipTurn(activePlayer.username, skipTurn.isDefenced)
    }

    private fun calculatePlayerAction(action: PlayerAction): CalculatedPlayerAction {
        val item = action.itemId?.let { itemRepository.getItemById(it) }
        val target = getPlayer(action.target)
        val attacker = getPlayer(action.attacker)
        if (target == null || attacker == null || !target.isAlive || !attacker.isActive) {
            throw IllegalActionException(action)
        }
        return if (item?.getId() == DICE_ID) {
            val randomTarget = players.random()
            CalculatedPlayerAction(
                target = randomTarget.username,
                attacker = action.attacker,
                itemId = item.getId(),
                damage = calculateItemDamage(item)
            )
        } else {
            CalculatedPlayerAction(
                target = action.target,
                attacker = action.attacker,
                itemId = item?.getId(),
                damage = calculateItemDamage(item)
            )
        }
    }

    private fun calculateItemDamage(item: Item?): Int {
        val damage = item?.damage ?: HANDS_DAMAGE
        return floor(Random.Default.nextDouble(0.5, 1.2) * damage).toInt()
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

    private fun getActivePlayer() = players.find { it.isActive }

    private fun getCurrentlyInActivePlayer() = players.find { !it.isActive }

    private fun getCurrentlyActivePlayer() = players.find { it.isActive }

    private fun getSnapshot() = MatchSnapshot(players.map { it.getSnapshot() }.toSet())

    override fun toString() = "Match(users=${players.map { it.username }})"

    class IllegalActionException(decision: PlayerDecision) : IllegalStateException(decision.toString())
}
