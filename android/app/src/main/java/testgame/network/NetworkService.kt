package testgame.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.util.*
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException
import java.net.UnknownHostException

abstract class NetworkService {

    companion object {
        val jsonFormat = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        const val RESPONSE_CONTENT_READ_LIMIT = 500

        const val BASE_HTTP_URL = "https://fighting-game-server.herokuapp.com"
        const val BASE_WS_URL = "ws://fighting-game-server.herokuapp.com"

        const val AUTHORIZATION_HEADER_NAME = "Authorization"

        const val TICKET_QUERY_PARAM_KEY = "ticket"
        const val ID_QUERY_PARAM_KEY = "id"
        const val USERNAME_QUERY_PARAM_KEY = "username"
    }

    @KtorExperimentalAPI
    val client = HttpClient(OkHttp.create()) {
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
                response.status == HttpStatusCode.Unauthorized -> throw NetworkException("Unauthorized")
                else -> {
                    throw UserNetworkException(response.content.readUTF8Line(400) ?: "Unknown exception")
                }
            }
        } catch (exception: UnknownHostException) {
            throw NetworkException("Enable to connect ot server")
        } catch (exception: IOException) {
            Timber.e("Something wrong with server connection")
            Timber.e(exception)
            throw NetworkException("Check your Internet connection and try again")
        } catch (e: Exception) {
            throw e
        }
    }

    private fun responseIsSuccessful(httpResponse: HttpResponse): Boolean {
        return httpResponse.status.value in 200..299
    }

    open class NetworkException(message: String) : IllegalArgumentException(message)
    class UserNetworkException(message: String) : NetworkException(message)
}