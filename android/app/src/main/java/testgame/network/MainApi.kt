package testgame.network

import faculty.FacultyData
import faculty.GET_ALL_FACULTIES_ENDPOINT
import faculty.GET_LEADING_FACULTY
import faculty.GET_SINGLE_FACULTY_ENDPOINT
import io.ktor.client.features.websocket.ws
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.charsets.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import subscription.*
import timber.log.Timber
import user.GET_ME_ENDPOINT
import user.GET_USER_ENDPOINT
import user.UserData
import user.Username
import websocket.WebSocketTicket
import java.lang.IllegalStateException

object MainApi : NetworkService() {

    private var session: WebSocketSession? = null

    @KtorExperimentalAPI
    suspend fun getPlayerData(token: String): UserData {
        val response =  getSuccessfulResponseOrException {
            client.get() {
                url("${BASE_HTTP_URL}$GET_ME_ENDPOINT")
                header(AUTHORIZATION_HEADER_NAME, token)
                contentType(ContentType.Application.Json)
            }
        }
        try {
            val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
            return jsonFormat.decodeFromString(stringResponse)
        } catch (e: TooLongLineException) {
            Timber.i(response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: "")
            throw e
        }
    }

    @KtorExperimentalAPI
    suspend fun getUserData(token: String, username: String): HttpResponse {
        return getSuccessfulResponseOrException {
            client.post() {
                url("${BASE_HTTP_URL}$GET_USER_ENDPOINT")
                header(AUTHORIZATION_HEADER_NAME, token)
                parameter(USERNAME_QUERY_PARAM_KEY, username)
                contentType(ContentType.Application.Json)
            }
        }
    }

    @KtorExperimentalAPI
    suspend fun getLeadingFacultyData(token: String): FacultyData {
        val response =  getSuccessfulResponseOrException {
            client.get() {
                url("${BASE_HTTP_URL}$GET_LEADING_FACULTY")
                header(AUTHORIZATION_HEADER_NAME, token)
                contentType(ContentType.Application.Json)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }

    @KtorExperimentalAPI
    suspend fun getAllFacultiesData(token: String): List<FacultyData> {
        val response =  getSuccessfulResponseOrException {
            client.get() {
                url("${BASE_HTTP_URL}$GET_ALL_FACULTIES_ENDPOINT")
                header(AUTHORIZATION_HEADER_NAME, token)
                contentType(ContentType.Application.Json)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }

    @KtorExperimentalAPI
    suspend fun getConcreteFacultyData(token: String, facultyId: Int): FacultyData {
        val response =  getSuccessfulResponseOrException {
            client.get() {
                url("${BASE_HTTP_URL}$GET_SINGLE_FACULTY_ENDPOINT")
                header(AUTHORIZATION_HEADER_NAME, token)
                parameter(ID_QUERY_PARAM_KEY, facultyId)
                contentType(ContentType.Application.Json)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }

    @KtorExperimentalAPI
    suspend fun getWebSocketTicket(token: String): WebSocketTicket {
        val response = getSuccessfulResponseOrException {
            client.get {
                url("${BASE_HTTP_URL}$SUBSCRIPTION_WEBSOCKET_TICKET_ENDPOINT")
                header(AUTHORIZATION_HEADER_NAME, token)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }

    @KtorExperimentalAPI
    suspend fun connectToMainWebSocket(
            ticket: WebSocketTicket,
            onUserMoneyUpdate: (username: Username, money: Int) -> Unit,
            onLeadingFacultyUpdate: (facultyId: Int, points: Int) -> Unit,
            onFacultiesPointsUpdate: (facultyId: Int, points: Int, winnerUsername: String) -> Unit,
    ) {
        client.ws(
                method = HttpMethod.Get,
                request = {
                    url("$BASE_WS_URL$SUBSCRIPTION_WEBSOCKET_ENDPOINT")
                    parameter(TICKET_QUERY_PARAM_KEY, Json.encodeToString(ticket))
                }
        ) {
            session = this
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    readSubscriptionUpdateMessage(
                            jsonFormat.decodeFromString(frame.readText()),
                            onUserMoneyUpdate,
                            onLeadingFacultyUpdate,
                            onFacultiesPointsUpdate,
                    )
                }
            }
        }
    }

    private fun readSubscriptionUpdateMessage(
            update: SubscriptionUpdate,
            onUserMoneyUpdate: (username: Username, money: Int) -> Unit,
            onLeadingFacultyUpdate: (facultyId: Int, points: Int) -> Unit,
            onFacultiesPointsUpdate: (facultyId: Int, points: Int, winnerUsername: String) -> Unit,
    ) {
        when (update) {
            is UserMoneyUpdate -> {
                println("MoneyUpdate")
                onUserMoneyUpdate(update.username, update.money)
            }
            is LeadingFacultyUpdate -> {
                println("LeadingFacultyUpdate")
                onLeadingFacultyUpdate(update.facultyId, update.points)
            }
            is FacultyPointsUpdate -> {
                println("PointsUpdate")
                onFacultiesPointsUpdate(update.facultyId, update.points, update.winnerUsername)
            }
        }
    }

    @KtorExperimentalAPI
    suspend fun subscribeUser(username: Username, state: Boolean) {
        subscribeEvent(UserMoneyUpdateSubscription(username, state))
    }

    @KtorExperimentalAPI
    suspend fun subscribeLeadingFaculty(state: Boolean) {
        subscribeEvent(LeadingFacultySubscription(state))
    }

    @KtorExperimentalAPI
    suspend fun subscribeFacultyPoints(state: Boolean) {
        subscribeEvent(AllFacultiesPointsSubscription(state))
    }

    @KtorExperimentalAPI
    suspend fun subscribeEvent(subscriptionMessage: SubscriptionMessage) {
        if (session == null) {
            throw NullWebSocketSessionException("You are not connected to the webSocket")
        }
        val message = jsonFormat.encodeToString(subscriptionMessage)
        session?.send(message)
    }

    class NullWebSocketSessionException(message: String) : IllegalStateException(message)
}