package testgame.network.matchService

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
import testgame.data.GameApp
import testgame.network.NetworkService
import websocket.WebSocketTicket
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.SocketTimeoutException

object MatchApi : NetworkService() {

    @KtorExperimentalAPI
    suspend fun getWebSocketTicket(token: String): WebSocketTicket {
        try {
            val response = client.get<HttpResponse>() {
                url("${BASE_HTTP_URL}$matchWebSocketTicketEndpoint")
                header(AUTHORIZATION_HEADER_NAME, token)
            }
            val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
            return Json.decodeFromString(stringResponse)
        } catch (exception: Exception) {
            throw GetWebSocketTicketException(exception.message ?: "Receiving ticket error")
        }
    }

    @KtorExperimentalAPI
    suspend fun connectMatchWebSocket(
            app: GameApp,
            ticket: WebSocketTicket,
            onMatchStart: () -> Unit,
            onMatchEnd: suspend (winner: String) -> Unit
    ) {
        client.ws(
                method = HttpMethod.Get,
                request = {
                    url("$BASE_WS_URL$matchWebSocketEndpoint")
                    parameter(TICKET_QUERY_PARAM_KEY, Json.encodeToString(ticket))
                }
        ) {
            app.match.webSocketSession = this
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    readMessage(
                            app,
                            jsonFormat.decodeFromString(frame.readText()),
                            onMatchStart,
                            onMatchEnd
                    )
                }
            }
        }
    }

    private suspend fun readMessage(
            app: GameApp,
            message: Message,
            onMatchStart: () -> Unit,
            onMatchEnd: suspend (winner: String) -> Unit
    ) {
        when (message) {
            is MatchStarted -> {
                app.match.players = message.players
                onMatchStart()
            }
            is TurnStarted -> {
                app.match.currentSnapshot = message.matchSnapshot
            }
            is PlayerAction -> {
                app.match.lastPlayerAction = message
            }
            is MatchEnded -> {
                onMatchEnd(message.winner)
            }
        }
    }

    class GetWebSocketTicketException(message: String) : IllegalArgumentException(message)
}
