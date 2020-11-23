package match

import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint

const val matchWebSocketName = "match"

val matchWebSocketEndpoint = getWebSocketEndpoint(matchWebSocketName)

val matchWebSocketTicketEndpoint = getWebSocketTicketEndpoint(matchWebSocketName)
