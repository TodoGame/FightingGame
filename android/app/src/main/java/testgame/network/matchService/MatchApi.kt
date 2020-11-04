package com.example.testgame.network.matchService

import com.example.testgame.network.securityService.LoginData
import com.example.testgame.network.securityService.RegisterData
import com.example.testgame.network.securityService.SecurityApi
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import java.io.EOFException
import java.util.concurrent.CancellationException

object MatchApi {
    private const val BASE_URL = "https://fighting-game-server.herokuapp.com/"
    const val AUTHORIZATION_HEADER_NAME = "Authorization"
    @KtorExperimentalAPI
    private val client: HttpClient = HttpClient(OkHttp.create()) {
        install(WebSockets)
    }

    @KtorExperimentalAPI
    suspend fun findMatch(token: String, connectionBody: () -> Unit) {
        client.ws(
            method = HttpMethod.Get,
            path = "/match/findGame?token=$token"
        ) {
            connectionBody()
        }
    }

    fun responseIsSuccessful(httpResponse: HttpResponse): Boolean {
        return httpResponse.status.value in 200..299
    }
}