package com.somegame.match

import com.somegame.match.matchmaking.*
import com.somegame.match.player.Player
import com.somegame.match.player.PlayerService
import com.somegame.security.UnauthorizedException
import com.somegame.websocket.WebSocketService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import match.Message
import match.PlayerAction
import match.matchWebSocketEndpoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MatchRouting {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val webSocketService = WebSocketService("match", 1)
    val matchService = MatchService(webSocketService)

    fun setUpMatchRoutes(routing: Routing) {
        routing.webSocket(matchWebSocketEndpoint) {
            logger.info("New connection")
            val webSocketClient = try {
                webSocketService.tryConnect(this)
            } catch (e: UnauthorizedException) {
                logger.info("Connection was not authorized")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unauthorized"))
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

        private val username
            get() = client.username
        private var player: Player? = null

        private fun tryGetPlayer(): Player? {
            if (player == null) {
                player = PlayerService.getPlayer(username)
            }
            return player
        }

        suspend fun startSearchingForMatch() {
            matchService.startSearchingForMatch(client)
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
                tryGetPlayer()?.doAction(message)
            } catch (e: Match.IllegalAction) {
                logger.info("Player $player sent an illegal action $e")
            }
        }

        suspend fun handleDisconnect() {
            matchService.stopSearchingForMatch(client)
            tryGetPlayer()?.handleDisconnect()
            client.handleDisconnect()
        }

        override fun toString() = "MatchClient(username=$username)"
    }
}
