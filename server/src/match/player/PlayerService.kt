package com.somegame.match.player

import com.somegame.match.matchmaking.Match
import com.somegame.websocket.WebSocketService
import org.slf4j.LoggerFactory

object PlayerService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val players = mutableMapOf<String, Player>()

    fun getPlayer(username: String): Player? = players[username]

    fun makePlayer(client: WebSocketService.Client, match: Match): Player {
        val player = Player(client, match)
        logger.info("Player $player registered")
        players[player.username] = player
        return player
    }

    fun clearPlayer(player: Player) {
        players.remove(player.username)
        logger.info("Player $player unregistered")
    }
}
