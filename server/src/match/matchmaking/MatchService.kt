package com.somegame.match.matchmaking

import com.somegame.websocket.WebSocketService
import org.slf4j.LoggerFactory
import user.User

class MatchService(private val webSocketService: WebSocketService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val matchMaker = Matchmaker()

    suspend fun startSearchingForMatch(client: WebSocketService.Client) {
        val createdMatchMembers = matchMaker.join(client.user)
        logger.info("Started searching for match for $client")
        if (createdMatchMembers != null) {
            makeMatch(createdMatchMembers)
        }
    }

    fun stopSearchingForMatch(client: WebSocketService.Client) {
        matchMaker.leave(client.user)
        logger.info("Stopped searching for match for $client")
    }

    private suspend fun makeMatch(users: List<User>) {
        val clients = users.map { webSocketService.getClients(it.username).first() }
        val match = Match(clients)
        match.start()
    }
}
