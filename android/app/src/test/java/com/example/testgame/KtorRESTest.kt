package com.example.testgame

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
//import io.ktor.client.features.websocket.WebSockets
//import io.ktor.client.features.websocket.ws
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class KtorRESTest                       {

    companion object {
        const val BASE_URL = "http://0.0.0.0:8080/"
        const val HELLO_WORLD = "${BASE_URL}"
    }

    @Test
    fun simpleCase() {
        val client = HttpClient()

        runBlocking {
            val data = client.get<String>(HELLO_WORLD)
            println("Test: $data")
        }
    }
}