package com.example.testgame

import com.example.testgame.network.SecurityApi
import com.example.testgame.network.securityService.RegisterData
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        try {
//            val answerDeferred = SecurityApi.retrofitService.login(LoginData("user1", "111"))
            val answerDeferred = SecurityApi.RETROFIT_SERVICE.register(
                RegisterData(
                    "user5",
                    "111",
                    "me"
                )
            )
            val answer = answerDeferred.await()
            println("Answer: ${answer}")
            println("Headers: ${answer.headers()}")
            println("Try to get Authorization: ${answer.headers().get("Authorization")}")
            println("Body: ${answer.body()}")
        } catch (e: Exception) {
            println("Failure: ${e.message}")
        }
    }
}