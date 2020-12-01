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
        try {
            val response = client.get<HttpResponse> {
                url("${BASE_HTTP_URL}$matchWebSocketTicketEndpoint")
                header(AUTHORIZATION_HEADER_NAME, token)
            }
            val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
            return Json.decodeFromString(stringResponse)
        } catch (exception: IOException) {
            throw GetWebSocketTicketException("Unexpected end of server stream")
        } catch (exception: Exception) {
            throw GetWebSocketTicketException(exception.message ?: "Receiving ticket error")
        }
    }

    @KtorExperimentalAPI
    suspend fun connectMatchWebSocket(
            match: Match,
            ticket: WebSocketTicket,
            onMatchStart: (players: Set<String>) -> Unit,
            onTurnStart: (matchSnapshot: MatchSnapshot) -> Unit,
            onPlayerAction: (attacker: String, target: String) -> Unit,
            onMatchEnd: suspend (winner: String) -> Unit
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

    private suspend fun readMessage(
            message: Message,
            onMatchStart: (players: Set<String>) -> Unit,
            onTurnStart: (matchSnapshot: MatchSnapshot) -> Unit,
            onPlayerAction: (attacker: String, target: String) -> Unit,
            onMatchEnd: suspend (winner: String) -> Unit
    ) {
        when (message) {
            is MatchStarted -> {
                println("MATCH STARTED")
                onMatchStart(message.players)
            }
            is TurnStarted -> {
                println("TURN STARTED")
                onTurnStart(message.matchSnapshot)
            }
            is PlayerAction -> {
                println("PLAYER ACTION")
                onPlayerAction(message.attacker, message.target)
            }
            is MatchEnded -> {
                println("MATCH ENDED")
                onMatchEnd(message.winner)
            }
        }
    }

    class GetWebSocketTicketException(message: String) : IllegalArgumentException(message)
}
