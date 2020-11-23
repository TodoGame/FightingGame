package tests

import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import match.Message
import testgame.data.GameApp
import testgame.network.MatchApi
import testgame.network.NetworkService
import testgame.network.SecurityApi

const val AUTHORIZATION_HEADER_NAME = "Authorization"
val data = TestDataClass

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
        GlobalScope.launch {
            MatchApi.connectMatchWebSocket(
                    data,
                    ticket,
                    ::onMatchStarted,
                    ::onMatchEnded
            )
        }
        Thread.sleep(5000)
        val turnSnapshot = data.match.currentSnapshot
        try {
            if (turnSnapshot != null) {
                println("Not null snapshot")
                val enemy = data.match.currentSnapshot!!.players.find { it.username != myUsername }?.username
                        ?: ""
                val action = NetworkService.jsonFormat.encodeToString<Message>(
                        match.PlayerAction(enemy, myUsername)
                )
                GlobalScope.launch {
                    data.match.webSocketSession?.send(action) ?: throw GameApp.NullAppDataException("Null match webSocketSession")
                }
            }
            else {
                println("Null snapshot")
            }
        } catch (exception: java.lang.NullPointerException) {
            println("Null pointer exception")
        }
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