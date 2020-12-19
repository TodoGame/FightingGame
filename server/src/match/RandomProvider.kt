package com.somegame.match

import com.somegame.match.player.Player
import kotlin.random.Random

class RandomProvider {
    fun nextDouble(from: Double, to: Double) = Random.Default.nextDouble(from, to)

    fun getRandomPlayer(players: List<Player>) = players.random()
}
