package com.somegame.websocket

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJwtHeader
import com.somegame.match.MatchRouting
import com.somegame.match.MatchTestUtils.generateActivePlayerLog
import com.somegame.match.MatchTestUtils.generatePassivePlayerLog
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import match.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import user.Username
import websocket.WebSocketTicket

class MatchRoutingTest : SimpleKtorTest() {
    private fun withApp(block: TestApplicationEngine.() -> Unit) = withBaseApp({
        routing {
            MatchRouting().setUpMatchRoutes(this)
        }
    }) { block() }

    private fun TestApplicationEngine.getTicket(username: Username): String? = handleRequest {
        uri = matchWebSocketTicketEndpoint
        method = HttpMethod.Get
        addJwtHeader(username)
    }.response.content

    private fun TestApplicationEngine.connect(
        username: Username,
        callback: suspend TestApplicationCall.(incoming: ReceiveChannel<Frame>, outgoing: SendChannel<Frame>) -> Unit
    ) {
        val ticket = getTicket(username) ?: IllegalArgumentException("Ticket not received")
        handleWebSocketConversation("$matchWebSocketEndpoint?ticket=$ticket", {}, callback)
    }

    private fun TestApplicationEngine.connect2SimplePlayers(
        username1: Username,
        username2: Username,
        log1: MutableList<Message>,
        log2: MutableList<Message>
    ) {
        connect(username1) { incoming1, outgoing1 ->
            val job = launch {
                connect(username2) { incoming2, outgoing2 ->
                    val player2 = SimplePlayer(username2, username1, log2, incoming2, outgoing2)
                    player2.start()
                }
            }
            val player1 = SimplePlayer(username1, username2, log1, incoming1, outgoing1)
            player1.start()
            job.join()
        }
    }

    @Test
    fun `getTicket should return ticket`() = withApp {
        val ticket = getTicket("user1")
        assertNotNull(ticket)
    }

    @Test
    fun `getTicket should return second ticket per 1 user`() = withApp {
        getTicket("user1")
        val anotherTicket = getTicket("user1")?.let {
            Json.decodeFromString<WebSocketTicket>(it)
        }
        assertNotNull(anotherTicket)
    }

    @Test
    fun `active player should receive a predictable log of messages`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

        val activePlayerUsername =
            log1.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
        val passivePlayerUsername = if (activePlayerUsername == "user1") "user2" else "user1"

        val activePlayerLog = generateActivePlayerLog(activePlayerUsername, passivePlayerUsername)

        if (activePlayerUsername == "user1") {
            assertEquals(activePlayerLog, log1)
        } else {
            assertEquals(activePlayerLog, log2)
        }
    }

    @Test
    fun `passive player should receive a predictable log of messages`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

        val activePlayerUsername =
            log1.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
        val passivePlayerUsername = if (activePlayerUsername == "user1") "user2" else "user1"

        val passivePlayerLog = generatePassivePlayerLog(activePlayerUsername, passivePlayerUsername)

        if (activePlayerUsername == "user1") {
            assertEquals(passivePlayerLog, log2)
        } else {
            assertEquals(passivePlayerLog, log1)
        }
    }

    @Disabled
    @Test
    fun `players that always hit should have the same logs as the normal players`() = withApp {
        val username1 = "user1"
        val username2 = "user2"

        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        connect(username1) { incoming1, outgoing1 ->
            val job = launch {
                connect(username2) { incoming2, outgoing2 ->
                    val player2 = SimplePlayerThatAlwaysHits(username2, username1, log2, incoming2, outgoing2)
                    player2.start()
                }
            }
            val player1 = SimplePlayerThatAlwaysHits(username1, username2, log1, incoming1, outgoing1)
            player1.start()
            job.join()
        }

        val activePlayerUsername =
            log1.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
        val passivePlayerUsername = if (activePlayerUsername == "user1") "user2" else "user1"

        val activePlayerLog = generateActivePlayerLog(activePlayerUsername, passivePlayerUsername)
        val passivePlayerLog = generatePassivePlayerLog(activePlayerUsername, passivePlayerUsername)

        if (activePlayerUsername == "user1") {
            assertEquals(activePlayerLog, log1)
            assertEquals(passivePlayerLog, log2)
        } else {
            assertEquals(activePlayerLog, log2)
            assertEquals(passivePlayerLog, log1)
        }
    }
    // TODO: test multiple matches at the same time
}
