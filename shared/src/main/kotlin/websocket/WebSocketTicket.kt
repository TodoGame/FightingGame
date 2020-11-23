package websocket

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketTicket(val webSocketName: String, val username: String, val expiresAt: Long, val code: String)
