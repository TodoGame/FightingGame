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

class WebSocketSingleConnectionServiceTest : NotInstantExpireWebSocketServiceKtorTest(1) {
    @Test
    fun `should not connect 1 user second time`() = withApp {
        val ticketString1 = handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.response.content
        handleWebSocketConversation("$endpoint?ticket=$ticketString1", {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Text)
            if (frame is Frame.Text) {
                assertEquals("user1", frame.readText())
            }
        }
        val ticketString2 = handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.response.content
        handleWebSocketConversation("$endpoint?ticket=$ticketString2", {}) { incoming, _ ->
            assert(incoming.receive() is Frame.Close)
        }
    }

    @Test
    fun `should return any number of tickets for 1 user`() = withApp {
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            assert(requestHandled)
        }
        for (i in 0 until 100) {
            handleRequest {
                uri = ticketEndpoint
                addJwtHeader("user1")
                method = HttpMethod.Get
            }.apply {
                assert(requestHandled)
                val ticket = response.content?.let { Json.decodeFromString<WebSocketTicket>(it) }
                assertNotNull(ticket)
            }
        }
    }
}
