package testgame.network

import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.*
import testgame.data.Match
import websocket.WebSocketTicket
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException

object MatchApi : NetworkService() {

    @KtorExperimentalAPI
    suspend fun getWebSocketTicket(token: String): WebSocketTicket {
        val response = getSuccessfulResponseOrException {
            client.get {
                url("${BASE_HTTP_URL}$matchWebSocketTicketEndpoint")
                header(AUTHORIZATION_HEADER_NAME, token)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }

    @KtorExperimentalAPI
    suspend fun connectMatchWebSocket(
            match: Match,
            ticket: WebSocketTicket,
            onMatchStart: (players: Set<String>) -> Unit,
            onTurnStart: (matchSnapshot: MatchSnapshot) -> Unit,
            onPlayerAction: (attacker: String, target: String) -> Unit,
            onMatchEnd: (winner: String) -> Unit
    ) {
        client.ws(
                method = HttpMethod.Get,
                request = {
                    url("$BASE_WS_URL$matchWebSocketEndpoint")
                    parameter(TICKET_QUERY_PARAM_KEY, Json.encodeToString(ticket))
                }
        ) {
            match.webSocketSession = this
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    readMessage(
                            jsonFormat.decodeFromString(frame.readText()),
                            onMatchStart,
                            onTurnStart,
                            onPlayerAction,
                            onMatchEnd
                    )
                }
            }
        }
    }

    private fun readMessage(
            message: Message,
            onMatchStart: (players: Set<String>) -> Unit,
            onTurnStart: (matchSnapshot: MatchSnapshot) -> Unit,
            onPlayerAction: (attacker: String, target: String) -> Unit,
            onMatchEnd: (winner: String) -> Unit
    ) {
        when (message) {
            is MatchStarted -> {
                onMatchStart(message.players)
            }
            is TurnStarted -> {
                onTurnStart(message.matchSnapshot)
            }
            is PlayerAction -> {
                onPlayerAction(message.attacker, message.target)
            }
            is MatchEnded -> {
                onMatchEnd(message.winner)
            }
        }
    }

    class GetWebSocketTicketException(message: String) : IllegalArgumentException(message)
}
