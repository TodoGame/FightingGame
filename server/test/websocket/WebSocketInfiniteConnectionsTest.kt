package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import com.somegame.applicationModule
import com.somegame.mockRepositoryModule
import com.somegame.security.JwtConfig
import com.somegame.security.UnauthorizedException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.WebSockets
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import match.MatchStarted
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.koin.core.Koin
import org.koin.ktor.ext.modules
import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint
import java.time.Duration

class WebSocketInfiniteConnectionsTest {
    private val webSocketName = "ws"
    private val endpoint = getWebSocketEndpoint(webSocketName)
    private val ticketEndpoint = getWebSocketTicketEndpoint(webSocketName)

    private val webSocketService = WebSocketService(webSocketName, -1)

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

                install(org.koin.ktor.ext.Koin) {
                    modules(mockRepositoryModule, applicationModule)
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
