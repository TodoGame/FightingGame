package websocket

import com.somegame.BaseKoinTest
import com.somegame.user.principal
import com.somegame.user.repository.MockUserRepositoryFactory
import com.somegame.user.repository.MockUserRepositoryFactory.user1
import com.somegame.user.repository.MockUserRepositoryFactory.user2
import com.somegame.websocket.WebSocketTicketManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WebSocketTicketManagerTest : BaseKoinTest() {
    private lateinit var ticketManager: WebSocketTicketManager
    private lateinit var ticketManager2: WebSocketTicketManager
    private lateinit var instantExpireTicketManager: WebSocketTicketManager
    private lateinit var singleTicketManager: WebSocketTicketManager

    @BeforeEach
    fun init() {
        ticketManager = WebSocketTicketManager("wsname")
        ticketManager2 = WebSocketTicketManager("otherwsname")
        instantExpireTicketManager = WebSocketTicketManager("instant expire", 0)
        singleTicketManager = WebSocketTicketManager("single")
    }

    @Test
    fun `should authorize tickets that are generated by this ticket manager`() = runBlocking {
        val userPrincipal = userRepository.user1().principal()
        val ticket = ticketManager.makeTicket(userRepository.user1().principal())
        val user = ticketManager.authorize(ticket)
        assertEquals(userPrincipal.username, user.username)
    }

    @Test
    fun `should not authorize the same ticket twice`(): Unit = runBlocking {
        val ticket = ticketManager.makeTicket(userRepository.user1().principal())
        ticketManager.authorize(ticket)
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                ticketManager.authorize(ticket)
            }
        }
    }

    @Test
    fun `should not authorize random tickets with incorrect name`(): Unit = runBlocking {
        val expiresAt = System.currentTimeMillis() + 60 * 1000
        val ticket = WebSocketTicket("incorrent name", userRepository.user1().username, expiresAt, "some code")
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                ticketManager.authorize(ticket)
            }
        }
    }

    @Test
    fun `should not authorize tickets with correct name but with fake username`(): Unit = runBlocking {
        val expiresAt = System.currentTimeMillis() + 60 * 1000
        val ticket = WebSocketTicket("wsname", MockUserRepositoryFactory.fakeUser.username, expiresAt, "some code")
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                ticketManager.authorize(ticket)
            }
        }
    }

    @Test
    fun `should not authorize registered tickets but with changed code`(): Unit = runBlocking {
        val ticket = ticketManager.makeTicket(userRepository.user1().principal())
        val fakeTicket =
            WebSocketTicket(ticket.webSocketName, ticket.username, ticket.expiresAt, ticket.code + " but fake")
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                ticketManager.authorize(fakeTicket)
            }
        }
    }

    @Test
    fun `should not authorize tickets registered by another ticket manager`(): Unit = runBlocking {
        val ticket = ticketManager2.makeTicket(userRepository.user1().principal())
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                ticketManager.authorize(ticket)
            }
        }
    }

    @Test
    fun `should not authorize registered tickets but with changed username`(): Unit = runBlocking {
        val ticket = ticketManager.makeTicket(userRepository.user1().principal())
        val fakeTicket =
            WebSocketTicket(ticket.webSocketName, userRepository.user2().username, ticket.expiresAt, ticket.code)
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                ticketManager.authorize(fakeTicket)
            }
        }
    }

    @Test
    fun `should not authorize expired tickets`(): Unit = runBlocking {
        val expiredTicket = instantExpireTicketManager.makeTicket(userRepository.user1().principal())
        assertThrows(WebSocketTicketManager.InvalidTicketException::class.java) {
            runBlocking {
                instantExpireTicketManager.authorize(expiredTicket)
            }
        }
    }
}
