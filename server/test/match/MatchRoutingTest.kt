package com.somegame.websocket

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJwtHeader
import com.somegame.match.LOSING_USER_PRIZE
import com.somegame.match.MatchRouting
import com.somegame.match.WINNING_FACULTY_PRIZE
import com.somegame.match.WINNING_USER_PRIZE
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import match.*
import org.junit.jupiter.api.Assertions.*
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
    fun `winning player should have WINNING_USER_PRIZE money`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

        val winnerUsername = log1.filterIsInstance<MatchEnded>().first().winner
        val winner = userRepository.findUserByUsername(winnerUsername)

        assertEquals(WINNING_USER_PRIZE, winner?.money)
    }

    @Test
    fun `losing player should have LOSING_USER_PRIZE money`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

        val winnerUsername = log1.filterIsInstance<MatchEnded>().first().winner

        val loserUsername = if (winnerUsername == "user1") "user2" else "user1"
        val loser = userRepository.findUserByUsername(loserUsername)

        assertEquals(LOSING_USER_PRIZE, loser?.money)
    }

    @Test
    fun `winning player's faculty should have WINNING_FACULTY_PRIZE points`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

        val winnerUsername = log1.filterIsInstance<MatchEnded>().first().winner
        val winner = userRepository.findUserByUsername(winnerUsername)

        assertEquals(WINNING_FACULTY_PRIZE, winner?.loadFaculty()?.points)
    }

    // TODO: test multiple matches at the same time
}
