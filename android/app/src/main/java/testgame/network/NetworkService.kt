package testgame.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import java.lang.IllegalStateException
import java.net.UnknownHostException

abstract class NetworkService {

    companion object {
        val jsonFormat = Json {
            ignoreUnknownKeys = true
        }
        const val RESPONSE_CONTENT_READ_LIMIT = 300
        const val BASE_HTTP_URL = "https://fighting-game-server.herokuapp.com"
        const val BASE_WS_URL = "ws://fighting-game-server.herokuapp.com"
        const val AUTHORIZATION_HEADER_NAME = "Authorization"
        const val TICKET_QUERY_PARAM_KEY = "ticket"
    }

    @KtorExperimentalAPI
    val client = HttpClient(OkHttp.create()) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        install(WebSockets)

        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    suspend fun getSuccessfulResponseOrException(funcBody: suspend () -> HttpResponse): HttpResponse {
        val response: HttpResponse
        try {
            response = funcBody()
            when {
                responseIsSuccessful(response) -> return response
                response.status == HttpStatusCode.BadRequest -> throw BadRequestException("Bad request")
                response.status == HttpStatusCode.Unauthorized -> throw UnauthorisedException("Unauthorized")
                else -> throw UnknownNetworkException("Unknown exception")
            }
        } catch (exception: UnknownHostException) {
            throw NoResponseException("Enable to connect ot server")
        }
    }

    private fun responseIsSuccessful(httpResponse: HttpResponse): Boolean {
        return httpResponse.status.value in 200..299
    }

    open class ConnectionException(message: String) : IllegalStateException(message)
    class UnauthorisedException(message: String) : ConnectionException(message)
    class UnknownNetworkException(message: String) : ConnectionException(message)
    class NoResponseException(message: String) : ConnectionException(message)
    class BadRequestException(message: String) : ConnectionException(message)
}