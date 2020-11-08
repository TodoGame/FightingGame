package com.somegame.websocket

import com.somegame.security.UnauthorizedException
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import match.MatchStarted
import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint

object TestWebSocketServiceRouting {
    const val unauthorizedResponse = "Unauthorized"
    const val tooManyConnectionsResponse = "Too many connections"

    fun getWebSocketName(maxConnectionsPerUser: Int) = "testWebSocketService:$maxConnectionsPerUser"

    fun getEndpoint(maxConnectionsPerUser: Int) = getWebSocketEndpoint(getWebSocketName(maxConnectionsPerUser))

    fun getTicketEndpoint(maxConnectionsPerUser: Int) = getWebSocketTicketEndpoint(getWebSocketName(maxConnectionsPerUser))

    fun Routing.testWebSocketServiceRouting(maxConnectionsPerUser: Int) {
        val webSocketName = getWebSocketName(maxConnectionsPerUser)
        val webSocketService = WebSocketService(webSocketName, maxConnectionsPerUser)

        webSocket(getEndpoint(maxConnectionsPerUser)) {
            val webSocketClient = try {
                webSocketService.tryConnect(this)
            } catch (e: UnauthorizedException) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, unauthorizedResponse))
                return@webSocket
            }
            send(Frame.Text(webSocketClient.username))
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    when (frame.readText()) {
                        "kick" -> webSocketClient.kick()
                        "message" -> webSocketClient.sendMessage(MatchStarted(setOf("name")))
                    }
                }
            }
        }
        webSocketService.getTicketRoute(this)
    }
}
