package com.somegame.match.matchmaking

import com.somegame.match.MatchRouting
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.*

class Matchmaker {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val mutex = Mutex()
    private val queue = LinkedList<LonelyClient>()

    suspend fun startSearchingForMatch(client: MatchRouting.MatchClient, opponentFacultyId: Int? = null) = mutex.withLock {
        if (isClientAlreadyInQueue(client)) {
            throw ClientAlreadyWaitingException(client)
        }
        val lonelyClient = LonelyClient(client, client.user.loadFaculty().getId(), opponentFacultyId)
        val match = findMatchInQueue(lonelyClient)
        if (match != null) {
            logger.info("Found match: $lonelyClient and $match")
            queue.remove(match)
            Match(listOf(lonelyClient.client, match.client)).start()
        } else {
            logger.info("Client $lonelyClient added to matchmaking queue")
            queue.add(lonelyClient)
        }
    }

    private fun isClientAlreadyInQueue(client: MatchRouting.MatchClient): Boolean =
        queue.find { it.client == client } != null

    suspend fun stopSearchingForMatch(client: MatchRouting.MatchClient) = mutex.withLock {
        queue.removeIf { it.client == client }
    }

    private fun findMatchInQueue(lonelyClient: LonelyClient): LonelyClient? =
        queue.find { doClientsMatch(it, lonelyClient) }

    private fun doClientsMatch(client1: LonelyClient, client2: LonelyClient): Boolean =
        client1.wantToPlayWith(client2) && client2.wantToPlayWith(client1)

    private data class LonelyClient(
        val client: MatchRouting.MatchClient,
        val facultyId: Int,
        val opponentFacultyId: Int?
    )

    private fun LonelyClient.wantToPlayWith(otherClient: LonelyClient): Boolean =
        opponentFacultyId == null || opponentFacultyId == otherClient.facultyId

    class ClientAlreadyWaitingException(client: MatchRouting.MatchClient) :
        IllegalArgumentException("Client $client is already waiting for an opponent")
}
