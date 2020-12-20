package websocket

fun getWebSocketEndpoint(webSocketName: String) = "/ws/$webSocketName"

fun getWebSocketTicketEndpoint(webSocketName: String) = "/getWebSocketTicket/$webSocketName"

const val TICKET_QUERY_PARAM_KEY = "ticket"
