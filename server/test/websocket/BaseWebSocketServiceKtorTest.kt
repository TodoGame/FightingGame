package com.somegame.websocket

import com.somegame.SimpleKtorTest
import com.somegame.security.UnauthorizedException
import com.somegame.websocket.WebSocketTicketManager.Companion.DEFAULT_TICKET_LIFE_EXPECTANCY
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import match.MatchStarted
import org.junit.jupiter.api.BeforeEach
import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint

open class BaseWebSocketServiceKtorTest(
    private val maxConnectionsPerUser: Int,
    private val ticketLifeExpectancyMillis: Long = DEFAULT_TICKET_LIFE_EXPECTANCY
) : SimpleKtorTest() {
    protected val webSocketName = "ws"
    protected val endpoint = getWebSocketEndpoint(webSocketName)
    protected val ticketEndpoint = getWebSocketTicketEndpoint(webSocketName)

    private var webSocketService = WebSocketService(webSocketName, maxConnectionsPerUser, ticketLifeExpectancyMillis)

    protected fun withApp(block: TestApplicationEngine.() -> Unit) = withBaseApp({
        routing {
            webSocket(endpoint) {
                val webSocketClient = try {
                    webSocketService.tryConnect(this)
                } catch (e: UnauthorizedException) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, ""))
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
    }) {
        block()
    }

    @BeforeEach
    fun initWebSocketService() {
        webSocketService = WebSocketService(webSocketName, maxConnectionsPerUser, ticketLifeExpectancyMillis)
    }
}
