package com.example.testgame.network.securityService

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import security.RegisterData
import security.UserLoginCredentials

object SecurityApi {
    private const val BASE_URL = "https://fighting-game-server.herokuapp.com/"
    const val AUTHORIZATION_HEADER_NAME = "Authorization"
    private val client = HttpClient()

    suspend fun login(loginData: UserLoginCredentials): HttpResponse {
        return client.post<HttpResponse>() {
            url("$BASE_URL/login")
            body = loginData
        }
    }

    suspend fun register(registerData: RegisterData): HttpResponse {
        return client.post<HttpResponse>() {
            url("$BASE_URL/register")
            body = registerData
        }
    }

    fun responseIsSuccessful(httpResponse: HttpResponse): Boolean {
        return httpResponse.status.value in 200..299
    }
}
