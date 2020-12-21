package match

import websocket.getWebSocketEndpoint
import websocket.getWebSocketTicketEndpoint

const val matchWebSocketName = "match"

val matchWebSocketEndpoint = getWebSocketEndpoint(matchWebSocketName)

const val MATCH_OPPONENT_FACULTY_ID_QUERY_PARAM_KEY = "opponentFacultyId"

val matchWebSocketTicketEndpoint = getWebSocketTicketEndpoint(matchWebSocketName)
