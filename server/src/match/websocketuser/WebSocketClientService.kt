package com.somegame.match.websocketuser

import com.somegame.match.MatchLogger
import com.somegame.user.User
import io.ktor.websocket.*
import match.websocketuser.WebSocketClient

object WebSocketClientService {
    private val clients = mutableMapOf<String, WebSocketClient>()

    fun connect(user: User, session: WebSocketServerSession): WebSocketClient {
        if (clients[user.username] != null) {
            throw UserAlreadyConnected(user)
        }
        val webSocketUser = WebSocketClient(user, session)
        clients[user.username] = webSocketUser
        MatchLogger.logClientRegister(webSocketUser)
        return webSocketUser
    }

    fun disconnect(user: User) {
        clients.remove(user.username)
        MatchLogger.logClientClear(user.username)
    }

    fun getClient(username: String): WebSocketClient {
        return clients[username] ?: throw PlayerNotFound(username)
    }

    class UserAlreadyConnected(user: User) :
        IllegalArgumentException("User ${user.username} has already connected to this websocket")

    class PlayerNotFound(username: String) : IllegalArgumentException("Player with username $username not found")
}
