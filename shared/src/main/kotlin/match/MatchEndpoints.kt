package match

import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint

const val matchWebSocketName = "match"

val matchWebSocketEndpoint = getWebSocketEndpoint(matchWebSocketName)

val MATCH_PREFERRED_OPPONENT_FACULTY_ID = "opponentFacultyId"

val matchWebSocketTicketEndpoint = getWebSocketTicketEndpoint(matchWebSocketName)
