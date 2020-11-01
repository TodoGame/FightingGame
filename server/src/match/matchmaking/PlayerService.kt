package com.somegame.match.matchmaking

import com.somegame.match.MatchLogger
import match.websocketuser.WebSocketClient

object PlayerService {
    private val players = mutableMapOf<String, Player>()

    fun getPlayer(username: String): Player? = players[username]

    fun makePlayer(webSocketUser: WebSocketClient, match: Match): Player {
        val player = Player(webSocketUser, match)
        MatchLogger.logPlayerRegistered(player)
        match.joinPlayer(player)
        players[player.username] = player
        return player
    }

    fun clearPlayer(username: String) {
        players.remove(username)
        MatchLogger.logPlayerCleared(username)
    }
}
