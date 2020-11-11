package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import com.somegame.user.repository.MockUserRepository
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import websocket.WebSocketTicket

@Disabled
open class WorkingWebSocketServiceKtorTest(
    maxConnectionsPerUser: Int,
    ticketLifeExpectancyMillis: Long = WebSocketTicketManager.DEFAULT_TICKET_LIFE_EXPECTANCY
) : BaseWebSocketServiceKtorTest(maxConnectionsPerUser, ticketLifeExpectancyMillis) {
    @Test
    fun `single connection getTicket endpoint should return ticket`() = withApp {
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            assert(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())
            val ticket = response.content?.let { Json.decodeFromString<WebSocketTicket>(it) }
            assertNotNull(ticket)
            assertEquals(MockUserRepository.user1.username, ticket?.username)
        }
    }

    @Test
    fun `webSocket connection should close if connected without ticket param`() = withApp {
        handleWebSocketConversation(endpoint, {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }

    @Test
    fun `webSocket connection should close if connected with random string or characters as ticket`() = withApp {
        handleWebSocketConversation("$endpoint?ticket=randomSomething", {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }

    @Test
    fun `webSocket connection should close if connected with invalid ticket`() = withApp {
        val ticket = WebSocketTicket(webSocketName, "user1", System.currentTimeMillis() + 100000, "code")
        val ticketString = Json.encodeToString(ticket)
        handleWebSocketConversation("$endpoint?ticket=$ticketString", {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }

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
}
