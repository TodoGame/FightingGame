package com.somegame.match

import com.somegame.match.matchmaking.Match
import com.somegame.match.matchmaking.Matchmaker
import com.somegame.match.matchmaking.Player
import com.somegame.match.matchmaking.PlayerService
import com.somegame.match.messages.Message
import com.somegame.match.messages.PlayerAction
import com.somegame.match.websocketuser.WebSocketClientService
import com.somegame.security.JwtConfig
import com.somegame.security.UnauthorizedException
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancel
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.websocketuser.WebSocketClient

object MatchRouting {
    fun Routing.match() {
        webSocket("hello") {
            for (frame in incoming) {
                val string = Json.encodeToString(PlayerAction("you", "me"))
                send(Frame.Text(string))
            }
        }
        webSocket("match/findGame") {
            val client = try {
                tryConnectClient(this)
            } catch (e: UnauthorizedException) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unauthorized"))
                return@webSocket
            } catch (e: WebSocketClientService.UserAlreadyConnected) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "User already connected"))
                return@webSocket
            }
            startFindingMatch(client)
            var player = PlayerService.getPlayer(client.username)
            try {
                for (frame in incoming) {
                    if (player == null) {
                        player = PlayerService.getPlayer(client.username)
                    }
                    if (player != null) {
                        handleFrame(player, frame)
                    }
                }
            } finally {
                handleDisconnect(client)
            }
        }
    }

    private suspend fun startFindingMatch(client: WebSocketClient) {
        val match = Matchmaker.findMatch(client)
        match?.start()
    }


    private fun tryConnectClient(session: WebSocketServerSession): WebSocketClient {
        val user = JwtConfig.authorizeWebSocketUser(session)
        MatchLogger.logUserConnect(user)
        return WebSocketClientService.connect(user, session)
    }

    private suspend fun handleFrame(player: Player, frame: Frame) {
        if (frame is Frame.Text) {
            val string = frame.readText()
            try {
                val action = Json.decodeFromString<PlayerAction>(string)
                MatchLogger.logActionReceive(player, action)
                player.doAction(action)
            } catch (e: SerializationException) {
                MatchLogger.logCouldNotParseFrame(e)
            } catch (e: Match.IllegalAction) {
                MatchLogger.logIllegalAction(e)
            }
        }
    }

    private suspend fun handleDisconnect(client: WebSocketClient) {
        println("handleDisconnect fired for ${client.username}")
        val player = PlayerService.getPlayer(client.username)
        if (player != null) {
            player.handleDisconnect()
        } else {
            client.handleDisconnect()
        }
    }
}
