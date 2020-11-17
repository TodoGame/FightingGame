package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import websocket.WebSocketTicket

class WebSocketInstantExpireServiceTest : BaseWebSocketServiceKtorTest(1, 0) {
    @Test
    fun `should not authorize expired tickets`() = withApp {
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
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }

    @Test
    fun `should allow to register new ticket when old has expired`() = withApp {
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            handleRequest {
                uri = ticketEndpoint
                addJwtHeader("user1")
                method = HttpMethod.Get
            }.apply {
                assert(requestHandled)
                val ticket = response.content?.let { Json.decodeFromString<WebSocketTicket>(it) }
                assertNotNull(ticket)
                assertEquals("user1", ticket?.username)
            }
        }
    }
}
