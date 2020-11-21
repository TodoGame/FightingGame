package tests

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.close
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import match.matchWebSocketEndpoint
import testgame.data.GameApp
import testgame.network.NetworkService
import testgame.network.matchService.MatchApi
import testgame.network.securityService.SecurityApi
import websocket.WebSocketTicket

const val AUTHORIZATION_HEADER_NAME = "Authorization"
val app = GameApp()

@KtorExperimentalAPI
fun main() {
    runBlocking {
        var tokenTest: String = ""
        val myUsername = kotlin.random.Random.nextInt(0, 1000).toString()
//        val myUsername = "842"
        try {
            val userRegisterInput = security.UserRegisterInput(
                    myUsername,
                    "tdf",
                    "testUser"
            )
            val response = SecurityApi.register(userRegisterInput)

//            val userLoginInput = security.UserLoginInput(
//                    myUsername, "tdf"
//            )
//            val response = SecurityApi.login(userLoginInput)
            val token = response.headers[AUTHORIZATION_HEADER_NAME]
            if (token != null) {
                tokenTest = token
                println("Token is: ${token}")
            } else {
                println("Wrong token response")
            }
        } catch (exception: NullPointerException) {
            println("Some data missed")
        }

        val ticket = MatchApi.getWebSocketTicket(tokenTest)
        MatchApi.connectMatchWebSocket(
                app,
                ticket,
                ::onMatchStarted,
                ::onMatchEnded
        )
//        while (GameData.webSocketSession == null) {
//            println(GameData.webSocketSession)
//        }
//        val action = Json.encodeToString(match.PlayerAction("adsf", "asdf")) ?: ""
//        matchSession?.send(action)
//        println(matchSession)
    }
}

fun onMatchStarted() {
    println("Match started")
}

suspend fun onMatchEnded(winner: String) {
    println("Match ended. Winner: $winner")
}

fun responseIsSuccessful(httpResponse: HttpResponse): Boolean {
    return httpResponse.status.value in 200..299
}