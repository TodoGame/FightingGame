package testgame.network.mainService

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import java.lang.Exception

object MainApi {


//    @KtorExperimentalAPI
//    private val client: HttpClient = HttpClient(OkHttp.create()) {
//        install(WebSockets)
//    }
//
//    @KtorExperimentalAPI
//    suspend fun findMatch(token: String, connectionBody: () -> Unit) {
//        client.ws(
//                method = HttpMethod.Get,
//                path = "/match/findGame?token=$token"
//        ) {
//            webSocketSession = this
//            connectionBody()
//        }
//    }
//
//    fun sendMessage(message: String) {
//        if (webSocketSession != null) {
//
//        } else {
//            throw NullWebSocketException("Session does not exist")
//        }
//    }
}