package websocket

fun getWebSocketEndpoint(webSocketName: String) = "/ws/$webSocketName"

fun getWebSocketTicketEndpoint(webSocketName: String) = "/getWebSocketTicket/$webSocketName"
