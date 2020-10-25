package com.somegame.match

import com.somegame.match.matchmaking.Matchmaker
import com.somegame.match.matchmaking.Player
import com.somegame.match.matchmaking.PlayerService
import com.somegame.match.messages.Message
import com.somegame.match.messages.PlayerAction
import com.somegame.match.websocketuser.WebSocketClientService
import com.somegame.security.UnauthorizedException
import com.somegame.user.User
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
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
        authenticate {
            webSocket("match/findGame") {
                val client = tryConnectClient(this)
                MatchLogger.logClientRegister(client)
                startFindingMatch(client)
                try {
                    var player = PlayerService.getPlayer(client.username)
                    for (frame in incoming) {
                        if (player == null) {
                            player = PlayerService.getPlayer(client.username)
                        } else {
                            handleFrame(player, frame)
                        }
                    }
                } finally {
                    handleDisconnect(client)
                }
            }
        }
    }

    private suspend fun startFindingMatch(client: WebSocketClient) {
        val match = Matchmaker.findMatch(client)
        match?.start()
    }


    private fun tryConnectClient(session: WebSocketServerSession): WebSocketClient {
        val user = session.call.principal<User>()
        if (user == null) {
            session.call.response.status(HttpStatusCode.Unauthorized)
            throw UnauthorizedException()
        }
        MatchLogger.logUserConnect(user)
        return try {
            WebSocketClientService.connect(user, session)
        } catch (e: WebSocketClientService.UserAlreadyConnected) {
            throw PlayerAlreadyConnected()
        }

    }

    private suspend fun handleFrame(player: Player, frame: Frame) {
        if (frame is Frame.Text) {
            val string = frame.readText()
            val action = Json.decodeFromString<PlayerAction>(string)
            MatchLogger.logActionReceive(player, action)
            player.doAction(action)
        }
    }

    private suspend fun handleDisconnect(client: WebSocketClient) {
        val player = PlayerService.getPlayer(client.username)
        if (player != null) {
            player.handleDisconnect()
        } else {
            client.handleDisconnect()
        }
    }

    class PlayerAlreadyConnected : IllegalArgumentException()
}
