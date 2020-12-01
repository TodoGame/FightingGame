package com.somegame.websocket

import com.somegame.security.UserPrincipal
import com.somegame.user.User
import com.somegame.websocket.WebSocketTicketManager.Companion.DEFAULT_TICKET_LIFE_EXPECTANCY
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.Message
import org.slf4j.LoggerFactory
import user.Username
import websocket.WebSocketTicket
import websocket.getWebSocketTicketEndpoint

class WebSocketService(
    webSocketName: String,
    private val maxConnectionsPerUser: Int = -1,
    ticketLifeExpectancyMillis: Long = DEFAULT_TICKET_LIFE_EXPECTANCY
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val TICKET_QUERY_PARAM_KEY = "ticket"
    }

    private val ticketManager = WebSocketTicketManager(webSocketName, ticketLifeExpectancyMillis)

    private val connectionsPerUserMutex = Mutex()
    private val connectionsPerUser = mutableMapOf<Username, Int>()

    private val ticketGetEndpoint = getWebSocketTicketEndpoint(webSocketName)

    fun getTicketRoute(routing: Routing, endpoint: String = ticketGetEndpoint) {
        routing.authenticate {
            get(endpoint) {
                val userPrincipal = call.principal<UserPrincipal>()
                if (userPrincipal == null) {
                    call.response.status(HttpStatusCode.Unauthorized)
                } else {
                    val ticket = makeTicket(userPrincipal)
                    call.respond(Json.encodeToString(ticket))
                }
            }
        }
    }

    suspend fun makeTicket(userPrincipal: UserPrincipal) = ticketManager.makeTicket(userPrincipal)

    suspend fun tryConnect(session: WebSocketServerSession): Client {
        val user = authorizeSessionByTicket(session)
        verifyNumberOfConnections(user)
        val client = Client(user, session)
        registerClient(client)
        return client
    }

    private suspend fun authorizeSessionByTicket(session: WebSocketServerSession): User {
        val ticketString = session.call.request.queryParameters[TICKET_QUERY_PARAM_KEY]
        if (ticketString == null) {
            logger.info("Received request with no $TICKET_QUERY_PARAM_KEY query parameter")
            throw WebSocketTicketManager.InvalidTicketException("No ticket provided")
        }
        val ticket = try {
            Json.decodeFromString<WebSocketTicket>(ticketString)
        } catch (e: SerializationException) {
            throw WebSocketTicketManager.InvalidTicketException("Could not deserialize ticket: $e")
        }
        return ticketManager.authorize(ticket)
    }

    private suspend fun verifyNumberOfConnections(user: User) = connectionsPerUserMutex.withLock {
        val connectionsNumber = connectionsPerUser[user.username] ?: 0
        if (maxConnectionsPerUser != -1 && connectionsNumber >= maxConnectionsPerUser) {
            throw MaximumNumberOfConnectionsReached(user)
        }
    }

    private suspend fun registerClient(client: Client) = connectionsPerUserMutex.withLock {
        val connectionsCount = connectionsPerUser[client.username] ?: 0
        connectionsPerUser[client.username] = connectionsCount + 1
        logger.info("Client $client registered")
    }

    private suspend fun unregisterClient(client: Client) = connectionsPerUserMutex.withLock {
        connectionsPerUser[client.username]?.let {
            if (it - 1 <= 0) {
                connectionsPerUser.remove(client.username)
            } else {
                connectionsPerUser[client.username] = it - 1
            }
        }
        logger.info("Unregistered client $client")
    }

    inner class Client(val user: User, private val session: WebSocketServerSession) {
        private val logger = LoggerFactory.getLogger(javaClass)

        val username = user.username

        suspend fun sendText(text: String) {
            logger.info("Sending $text to $this")
            session.send(Frame.Text(text))
        }

        suspend fun sendMessage(message: Message) {
            logger.info("Sending $message to $this")
            val string = Json.encodeToString(message)
            session.send(Frame.Text(string))
        }

        suspend fun kick(message: String = "Kicked") {
            logger.info("Kicked $this")
            session.close(CloseReason(CloseReason.Codes.NORMAL, message))
            unregisterClient(this)
        }

        suspend fun handleDisconnect() {
            unregisterClient(this)
        }

        override fun toString() = "Client(username=$username)"
    }

    class MaximumNumberOfConnectionsReached(user: User) :
        IllegalStateException("Maximum number of connections reached by $user")
}
