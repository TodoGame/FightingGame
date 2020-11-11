package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
}
