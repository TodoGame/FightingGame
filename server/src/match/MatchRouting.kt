package com.somegame.match

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
import match.Message
import match.PlayerAction
import match.matchWebSocketEndpoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MatchRouting {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val webSocketService = WebSocketService("match", 1)
    val matchmaker = Matchmaker()

    fun setUpMatchRoutes(routing: Routing) {
        routing.webSocket(matchWebSocketEndpoint) {
            logger.info("New connection")
            val webSocketClient = try {
                webSocketService.tryConnect(this)
            } catch (e: WebSocketTicketManager.InvalidTicketException) {
                logger.info("Connection was not authorized")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unauthorized"))
                return@webSocket
            } catch (e: WebSocketTicketManager.MaxNumberOfTicketsReachedException) {
                logger.info("Client tried to connect but max number of connections is reached")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Max number of connections reached"))
                return@webSocket
            }

            val client = MatchClient(webSocketClient)
            client.startSearchingForMatch()
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

        suspend fun startSearchingForMatch() {
            matchmaker.startSearchingForMatch(this)
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
            if (message !is PlayerAction) {
                return
            }
            try {
                player?.doAction(message)
            } catch (e: Match.IllegalActionException) {
                logger.info("Player $player sent an illegal action $e")
            }
        }

        suspend fun handleDisconnect() {
            player?.handleDisconnect() ?: run {
                matchmaker.stopSearchingForMatch(this)
            }
            client.handleDisconnect()
        }

        override fun toString() = "MatchClient(username=$username)"
    }
}
