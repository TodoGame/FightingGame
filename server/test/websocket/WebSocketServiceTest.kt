package websocket

import com.somegame.BaseKoinTest
import com.somegame.user.principal
import com.somegame.websocket.WebSocketService
import com.somegame.websocket.WebSocketTicketManager
import io.ktor.websocket.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WebSocketServiceTest : BaseKoinTest() {

    var webSocketService = WebSocketService("ws", -1)
    var singleConnectionWebSocketService = WebSocketService("singlews", 1)

    private fun mockSession(ticketString: String?): WebSocketServerSession {
        val session = mockk<WebSocketServerSession>()
        every { session.call.request.queryParameters["ticket"] } returns ticketString
        return session
    }

    @BeforeEach
    fun reinit() {
        webSocketService = WebSocketService("ws", -1)
        singleConnectionWebSocketService = WebSocketService("singlews", 1)
    }

    @Test
    fun `should make ticket for user`(): Unit = runBlocking {
        val principal = makeNewTestUser("user1").principal()
        webSocketService.makeTicket(principal)
    }

    @Test
    fun `should make at least 2 different tickets for user`() = runBlocking {
        val principal = makeNewTestUser("user1").principal()
        val ticket1 = webSocketService.makeTicket(principal)
        val ticket2 = webSocketService.makeTicket(principal)

        assertNotEquals(ticket1, ticket2)
    }

    @Test
    fun `tryConnect should throw if given session without ticket`() {
        val session = mockSession(null)
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                webSocketService.tryConnect(session)
            }
        }
    }

    @Test
    fun `tryConnect should throw if given session with gibberish instead of ticket`() {
        val session = mockSession("gibberish")
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                webSocketService.tryConnect(session)
            }
        }
    }

    @Test
    fun `tryConnect should throw if given invalid ticket`(): Unit = runBlocking {
        val principal = makeNewTestUser("user1").principal()
        val ticket = singleConnectionWebSocketService.makeTicket(principal)
        val session = mockSession(Json.encodeToString(ticket))
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                webSocketService.tryConnect(session)
            }
        }
    }

    @Test
    fun `tryConnect should return user entity if session has valid ticket`(): Unit = runBlocking {
        val principal = makeNewTestUser("user1").principal()
        val ticket = webSocketService.makeTicket(principal)
        val session = mockSession(Json.encodeToString(ticket))
        webSocketService.tryConnect(session)
    }

    @Test
    fun `single connection tryConnect should throw if second session of the same user tries to connect while first session is connected`(): Unit =
        runBlocking {
            val principal = makeNewTestUser("user1").principal()
            val ticket1 = singleConnectionWebSocketService.makeTicket(principal)
            val session1 = mockSession(Json.encodeToString(ticket1))
            val ticket2 = singleConnectionWebSocketService.makeTicket(principal)
            val session2 = mockSession(Json.encodeToString(ticket2))
            singleConnectionWebSocketService.tryConnect(session1)
            assertThrows(WebSocketService.MaximumNumberOfConnectionsReached::class.java) {
                runBlocking {
                    singleConnectionWebSocketService.tryConnect(session2)
                }
            }
        }

    @Test
    fun `tryConnect should connect second session of the same user if the first session is already disconnected`(): Unit = runBlocking {
        val principal = makeNewTestUser("user1").principal()
        val ticket1 = webSocketService.makeTicket(principal)
        val session1 = mockSession(Json.encodeToString(ticket1))
        val ticket2 = webSocketService.makeTicket(principal)
        val session2 = mockSession(Json.encodeToString(ticket2))
        val client1 = webSocketService.tryConnect(session1)
        client1.handleDisconnect()
        webSocketService.tryConnect(session2)
    }
}
