package com.example.testgame

import com.example.testgame.network.SecurityApi
import security.ChangePasswordData
import security.LoginData
import security.RegisterData
import kotlinx.coroutines.runBlocking
import match.PlayerSnapshot
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import java.net.ConnectException


class KtorServerConnectionTest {
    companion object {
        const val testUsername = "user"
        const val testUserPassword = "111"
        const val testNewUserPassword = "222"
        const val testName = "me"
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
    }

    @Test
    fun shouldThrowConnectException() {
        val userProperty = PlayerSnapshot
        assertThrows(ConnectException::class.java) {
            runBlocking {
                val answerDeferred = SecurityApi.RETROFIT_SERVICE.login(
                    LoginData(
                        testUsername,
                        testUserPassword
                    )
                )
                val answer = answerDeferred.await()
            }
        }
    }

    @Test
    fun shouldRegisterNewUser() {
        runBlocking {
            val answerDeferred = SecurityApi.RETROFIT_SERVICE.register(
                RegisterData(
                    testUsername,
                    testUserPassword,
                    testName
                )
            )
            val answer = answerDeferred.await()
            if (answer.isSuccessful) {
                println("User registered")
                assertTrue(answer.headers().names().contains("Authorization"))
            } else {
                println("Bad request")
                assertTrue(answer.code() == BAD_REQUEST)
            }
        }
    }

    // user login twice -> user receives token twice
    @Test
    fun shouldLoginNewUser() {
        runBlocking {
            val answerDeferred = SecurityApi.RETROFIT_SERVICE.login(
                LoginData(
                    testUsername,
                    testUserPassword
                )
            )
            val answer = answerDeferred.await()
            if (answer.isSuccessful) {
                println("User logined")
                assertTrue(answer.headers().names().contains("Authorization"))
            } else {
                println("Bad request")
                assertTrue(answer.code() == UNAUTHORIZED || answer.code() == BAD_REQUEST)
            }
        }
    }

    // if successful password will change and defaults will cause Unauthorized exception
    @Test
    fun shouldLoginAndChangePassword() {
        var token = ""

        runBlocking {
            val loginAnswerDeferred = SecurityApi.RETROFIT_SERVICE.login(
                LoginData(
                    testUsername,
                    testUserPassword
                )
            )
            val loginAnswer = loginAnswerDeferred.await()
            if (loginAnswer.isSuccessful) {
                println("User logined")
                assertTrue(loginAnswer.headers().names().contains("Authorization"))
                token = loginAnswer.headers().get("Authorization") ?: ""
                val changeAnswerDeferred = SecurityApi.RETROFIT_SERVICE.changePassword(
                    ChangePasswordData(
                        testUserPassword,
                        testNewUserPassword
                    ),
                    mapOf("Authorization" to token)
                )
                val changeAnswer = changeAnswerDeferred.await()
                if (changeAnswer.isSuccessful) {
                    println("ChangePassword successfully")
                } else {
                    println("ChangePassword is not successfully")
                }
            } else {
                println("Bad request")
                assertTrue(loginAnswer.code() == UNAUTHORIZED || loginAnswer.code() == BAD_REQUEST)
            }
        }
    }
}