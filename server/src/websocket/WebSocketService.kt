package com.somegame.websocket

import com.somegame.security.UnauthorizedException
import com.somegame.security.UserPrincipal
import com.somegame.user.repository.UserEntity
import com.somegame.websocket.WebSocketTicketManager.Companion.DEFAULT_TICKET_LIFE_EXPECTANCY
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.Message
import org.slf4j.LoggerFactory
import user.Username
import websocket.WebSocketTicket
import websocket.getWebSocketTicketEndpoint
import java.util.concurrent.ConcurrentHashMap

class WebSocketService(
    webSocketName: String,
    maxConnectionsPerUser: Int = -1,
    ticketLifeExpectancyMillis: Long = DEFAULT_TICKET_LIFE_EXPECTANCY
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val TICKET_QUERY_PARAM_KEY = "ticket"
        const val MAXIMUM_NUMBER_OF_TICKETS_REACHED_MESSAGE = "Maximum number of tickets reached message"
    }

    private val ticketManager = WebSocketTicketManager(webSocketName, maxConnectionsPerUser, ticketLifeExpectancyMillis)

    private val clients = ConcurrentHashMap<Username, MutableList<Client>>()

    private val ticketGetEndpoint = getWebSocketTicketEndpoint(webSocketName)

    fun getTicketRoute(routing: Routing, endpoint: String = ticketGetEndpoint) {
        routing.authenticate {
            get(endpoint) {
                val userPrincipal = call.principal<UserPrincipal>()
                if (userPrincipal == null) {
                    call.response.status(HttpStatusCode.Unauthorized)
                } else {
                    try {
                        val ticket = makeTicket(userPrincipal)
                        call.respond(Json.encodeToString(ticket))
                    } catch (e: WebSocketTicketManager.MaxNumberOfTicketsReachedException) {
                        logger.info("User $userPrincipal requested a ticket but the maximum number of tickets is reached")
                        call.respond(HttpStatusCode.Conflict, MAXIMUM_NUMBER_OF_TICKETS_REACHED_MESSAGE)
                    }
                }
            }
        }
    }

    fun makeTicket(userPrincipal: UserPrincipal) = ticketManager.makeTicket(userPrincipal)

    fun tryConnect(session: WebSocketServerSession): Client {
        val user = authorizeSessionByTicket(session)
        val client = Client(user, session)
        registerClient(client)
        return client
    }

    private fun authorizeSessionByTicket(session: WebSocketServerSession): UserEntity {
        val ticketString = session.call.request.queryParameters[TICKET_QUERY_PARAM_KEY]
        if (ticketString == null) {
            logger.info("Received request with no $TICKET_QUERY_PARAM_KEY query parameter")
            throw UnauthorizedException()
        }
        val ticket = try {
            Json.decodeFromString<WebSocketTicket>(ticketString)
        } catch (e: SerializationException) {
            throw UnauthorizedException("Error parsing ticket: $e")
        }
        return ticketManager.authorize(ticket)
    }

    private fun registerClient(client: Client) {
        val clientsForThisUser = clients[client.username]
        if (clientsForThisUser == null) {
            clients[client.username] = mutableListOf(client)
        } else {
            clientsForThisUser.add(client)
        }
        logger.info("Client $client registered")
    }

    private fun unregisterClient(client: Client) {
        clients[client.username]?.remove(client)
        logger.info("Unregistered client $client")
    }

    fun getClients(username: String): List<Client> {
        return clients[username] ?: listOf()
    }

    inner class Client(val user: UserEntity, private val session: WebSocketServerSession) {
        private val logger = LoggerFactory.getLogger(javaClass)

        val username = user.username

        suspend fun sendMessage(message: Message) {
            logger.info("Sending $message to $this")
            val string = Json.encodeToString(message)
            session.send(Frame.Text(string))
        }

        suspend fun kick(message: String = "Match ended") {
            logger.info("Kicked $this")
            session.close(CloseReason(CloseReason.Codes.NORMAL, message))
            unregisterClient(this)
        }

        fun handleDisconnect() {
            unregisterClient(this)
        }

        override fun toString() = "Client(username=$username)"
    }
}
