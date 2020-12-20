package com.somegame.websocket

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJwtHeader
import com.somegame.websocket.WebSocketTicketManager.Companion.DEFAULT_TICKET_LIFE_EXPECTANCY
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.MatchStarted
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import websocket.WebSocketTicket
import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint

@Disabled
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
                } catch (e: WebSocketTicketManager.InvalidTicketException) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, ""))
                    return@webSocket
                } catch (e: WebSocketService.MaximumNumberOfConnectionsReached) {
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

    @Test
    fun `getTicket endpoint should return ticket`() = withApp {
        handleRequest {
            uri = ticketEndpoint
            addJwtHeader("user1")
            method = HttpMethod.Get
        }.apply {
            assert(requestHandled)
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
            val ticket = response.content?.let { Json.decodeFromString<WebSocketTicket>(it) }
            Assertions.assertNotNull(ticket)
            Assertions.assertEquals(user1.username, ticket?.username)
        }
    }

    @Test
    fun `webSocket connection should close if connected without ticket param`() = withApp {
        handleWebSocketConversation(endpoint, {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                Assertions.assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }

    @Test
    fun `webSocket connection should close if connected with random string or characters as ticket`() = withApp {
        handleWebSocketConversation("$endpoint?ticket=randomSomething", {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                Assertions.assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }

    @Test
    fun `webSocket connection should close if connected with not complete string of json`() = withApp {
        val ticket = WebSocketTicket(webSocketName, "user1", System.currentTimeMillis() + 100000, "code")
        val ticketString = Json.encodeToString(ticket)
        handleWebSocketConversation("$endpoint?ticket=$ticketString$1", {}) { incoming, _ ->
            val frame = incoming.receive()
            assert(frame is Frame.Close)
            if (frame is Frame.Close) {
                Assertions.assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
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
                Assertions.assertEquals(CloseReason.Codes.CANNOT_ACCEPT, frame.readReason()?.knownReason)
            }
        }
    }
}
