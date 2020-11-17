package com.somegame.match.matchmaking

import com.somegame.match.MatchRouting
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class Matchmaker {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val waitingClient = AtomicReference<MatchRouting.MatchClient?>(null)

    suspend fun startSearchingForMatch(client: MatchRouting.MatchClient) {
        val opponent = waitingClient.getAndUpdate { if (it != null && it != client) null else client }

        when (opponent) {
            null -> {
                logger.info("Client $client is now waiting for an opponent")
            }
            client -> {
                logger.info("Client $client tried to become waiting for a match twice")
                throw ClientAlreadyWaitingException(client)
            }
            else -> {
                logger.info("Found match for $client, $opponent")
                Match(listOf(client, opponent)).start()
            }
        }
    }

    fun stopSearchingForMatch(client: MatchRouting.MatchClient) {
        waitingClient.getAndUpdate { if (it == client) null else it }
    }

    class ClientAlreadyWaitingException(client: MatchRouting.MatchClient) :
        IllegalArgumentException("Client $client is already waiting for an opponent")
}
