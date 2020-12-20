package com.somegame.match

import com.somegame.faculty.FacultyRepository
import com.somegame.faculty.doesFacultyExist
import com.somegame.match.matchmaking.*
import com.somegame.match.player.Player
import com.somegame.websocket.WebSocketService
import com.somegame.websocket.WebSocketTicketManager
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MatchRouting : KoinComponent {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val webSocketService = WebSocketService("match", 1)
    val matchmaker = Matchmaker()

    val facultyRepository: FacultyRepository by inject()

    fun setUpMatchRoutes(routing: Routing) {
        routing.webSocket(matchWebSocketEndpoint) {
            logger.info("New connection")
            val webSocketClient = try {
                webSocketService.tryConnect(this)
            } catch (e: WebSocketTicketManager.InvalidTicketException) {
                logger.info("Connection was not authorized")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unauthorized"))
                return@webSocket
            } catch (e: WebSocketService.MaximumNumberOfConnectionsReached) {
                logger.info("Client tried to connect but max number of connections is reached")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Max number of connections reached"))
                return@webSocket
            }

            val opponentFacultyId = call.request.queryParameters[MATCH_PREFERRED_OPPONENT_FACULTY_ID]?.toIntOrNull()

            if (opponentFacultyId != null && !facultyRepository.doesFacultyExist(opponentFacultyId)) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Faculty with id=$opponentFacultyId not found"))
                return@webSocket
            }

            val client = MatchClient(webSocketClient)
            client.startSearchingForMatch(opponentFacultyId)
            try {
                for (frame in incoming) {
                    logger.info("Received frame from $client")
                    client.handleFrame(frame)
                }
            } finally {
                client.handleDisconnect()
            }
        }
        webSocketService.getTicketRoute(routing)
    }

    inner class MatchClient(private val client: WebSocketService.Client) {
        private val logger = LoggerFactory.getLogger(javaClass)

        val user = client.user

        val username
            get() = client.username
        private var player: Player? = null

        fun onJoinMatch(player: Player) {
            this.player = player
        }

        suspend fun kick(message: String) {
            client.kick(message)
        }

        suspend fun sendMessage(message: Message) {
            val string = Json.encodeToString(message)
            client.sendText(string)
        }

        suspend fun startSearchingForMatch(opponentFacultyId: Int?) {
            matchmaker.startSearchingForMatch(this, opponentFacultyId)
        }

        suspend fun handleFrame(frame: Frame) {
            if (frame !is Frame.Text) {
                return
            }
            val string = frame.readText()
            try {
                val message = Json.decodeFromString<Message>(string)
                handleMessage(message)
            } catch (e: SerializationException) {
                logger.info("Could not deserialize frame $e")
            }
        }

        private suspend fun handleMessage(message: Message) {
            if (message !is PlayerDecision) {
                return
            }
            try {
                player?.makeDecision(message)
            } catch (e: Match.IllegalActionException) {
                logger.info("Player $player sent an illegal action $e")
            }
        }

        suspend fun handleDisconnect() {
            client.handleDisconnect()
            if (player == null) {
                matchmaker.stopSearchingForMatch(this)
            } else {
                player?.handleDisconnect()
            }
        }

        override fun toString() = "MatchClient(username=$username)"
    }
}
