package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WebSocketInfiniteConnectionsTest : NotInstantExpireWebSocketServiceKtorTest(-1) {
    @Test
    fun `should return 100 tickets and establish 100 websocket connections`() = withApp {
        for (i in 0 until 100) {
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
                    Assertions.assertEquals("user1", frame.readText())
                }
            }
        }
    }
}
