package com.somegame.websocket

import com.somegame.TestUsers
import com.somegame.TestUsers.addJwtHeader
import com.somegame.security.JwtConfig
import com.somegame.security.UnauthorizedException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.MatchStarted
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import websocket.WebSocketTicket
import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint
import java.time.Duration

class WebSocketSingleConnectionServiceTest {
    private val userPrinciple = TestUsers.user1.principal

    private val webSocketName = "ws"
    private val endpoint = getWebSocketEndpoint(webSocketName)
    private val ticketEndpoint = getWebSocketTicketEndpoint(webSocketName)

    private val webSocketService = WebSocketService(webSocketName, 1)

    private val unauthorizedResponse = "Unauthorized"

    private fun withApp(block: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            {
                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(1)
                    timeout = Duration.ofSeconds(15)
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }

                install(Authentication) {
                    jwt {
                        verifier(JwtConfig.verifier)
                        validate {
                            JwtConfig.verifyCredentialsAndGetPrincipal(it)
                        }
                    }
                }

                routing {
                    webSocket(endpoint) {
                        val webSocketClient = try {
                            webSocketService.tryConnect(this)
                        } catch (e: UnauthorizedException) {
                            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, unauthorizedResponse))
                            return@webSocket
                        }
                        send(Frame.Text(webSocketClient.username))
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                when (frame.readText()) {
                                    "kick" -> webSocketClient.kick("Kicked")
                                    "message" -> webSocketClient.sendMessage(MatchStarted(setOf("name")))
                                }
                            }
                        }
                    }
                    webSocketService.getTicketRoute(this)
                }
            },
            block
        )
    }

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
            assertEquals(userPrinciple.username, ticket?.username)
        }
    }

    @Test
    fun `webSocket connection should close if connected without ticket param`() = withApp {
        handleWebSocketConversation(endpoint, {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
                assertEquals(unauthorizedResponse, frame.readReason()?.message)
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
                assertEquals(unauthorizedResponse, frame.readReason()?.message)
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
                assertEquals(unauthorizedResponse, frame.readReason()?.message)
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
                assertEquals(unauthorizedResponse, frame.readReason()?.message)
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
