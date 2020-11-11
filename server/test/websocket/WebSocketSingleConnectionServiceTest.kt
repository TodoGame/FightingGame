package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebSocketSingleConnectionServiceTest : WorkingWebSocketServiceKtorTest(1) {
    @Test
    fun `should not return second ticket for 1 user`() = withApp {
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            assert(requestHandled)
        }
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            assert(requestHandled)
            assertEquals(HttpStatusCode.Conflict, response.status())
        }
    }

    @Test
    fun `should not return any more tickets for 1 user`() = withApp {
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
                assertEquals(HttpStatusCode.Conflict, response.status())
            }
        }
    }
}
