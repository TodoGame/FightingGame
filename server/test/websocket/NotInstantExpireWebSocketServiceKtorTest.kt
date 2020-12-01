package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

@Disabled
open class NotInstantExpireWebSocketServiceKtorTest(
    maxConnectionsPerUser: Int,
    ticketLifeExpectancyMillis: Long = WebSocketTicketManager.DEFAULT_TICKET_LIFE_EXPECTANCY
) : BaseWebSocketServiceKtorTest(maxConnectionsPerUser, ticketLifeExpectancyMillis) {

    @Test
    fun `webSocket connection should be established if valid ticket is provided and websocket should respond with correct username`() =
        withApp {
            var ticketString: String?
            handleRequest {
                uri = ticketEndpoint
                addJwtHeader("user1")
                method = HttpMethod.Get
            }.apply {
                ticketString = response.content
            }
            handleWebSocketConversation("$endpoint?ticket=$ticketString", {}) { incoming, _ ->
                val frame = incoming.receive()
                assert(frame is Frame.Text)
                if (frame is Frame.Text) {
                    assertEquals("user1", frame.readText())
                }
            }
        }

    @Test
    fun `webSocket should respond with correct username for second user`() =
        withApp {
            var ticketString: String?
            handleRequest {
                uri = ticketEndpoint
                addJwtHeader("user2")
                method = HttpMethod.Get
            }.apply {
                ticketString = response.content
            }
            handleWebSocketConversation("$endpoint?ticket=$ticketString", {}) { incoming, _ ->
                val frame = incoming.receive()
                assert(frame is Frame.Text)
                if (frame is Frame.Text) {
                    assertEquals("user2", frame.readText())
                }
            }
        }

    @Test
    fun `webSocket should not authorize the same ticket twice`() = withApp {
        var ticketString: String?
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            ticketString = response.content
        }
        handleWebSocketConversation("$endpoint?ticket=$ticketString", {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Text)
        }
        handleWebSocketConversation("$endpoint?ticket=$ticketString", {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }

    @Test
    fun `webSocket should kick client on message 'kick'`() = withApp {
        var ticketString: String?
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            ticketString = response.content
        }
        handleWebSocketConversation("$endpoint?ticket=$ticketString", {}) { incoming, outgoing ->
            outgoing.send(Frame.Text("kick"))
            val frame = incoming.receive()
            if (frame is Frame.Close) {
                assertEquals(CloseReason.Codes.NORMAL, frame.readReason()?.knownReason)
                assertEquals("Kicked", frame.readReason()?.message)
            }
        }
    }

    @Test
    fun `only 1 user from 100 concurrent users with the same ticket should be connected`() = withApp {
        var ticketString: String?
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            ticketString = response.content
        }

        val connectedUsers = AtomicInteger(0)

        runBlocking {
            for (i in 0 until 100) {
                launch {
                    handleWebSocketConversation("$endpoint?ticket=$ticketString", {}) { incoming, _ ->
                        if (incoming.receive() is Frame.Text) {
                            connectedUsers.incrementAndGet()
                        }
                    }
                }
            }
        }
        assertEquals(1, connectedUsers.get())
    }
}
