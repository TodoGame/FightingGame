package com.somegame.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.somegame.user.User
import io.ktor.auth.jwt.*
import io.ktor.websocket.*

object JwtConfig {
    private const val secret = "SecretHehe"
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT.require(algorithm).build()

    fun makeToken(user: User): String = JWT.create().withSubject(user.username).sign(algorithm)

    fun authorizeWebSocketUser(session: WebSocketServerSession): User {
        val token = session.call.request.queryParameters["token"] ?: throw UnauthorizedException()
        return authorizeUserFromJwt(token)
    }

    private fun authorizeUserFromJwt(token: String): User {
        val decodedJwt = try {
            verifier.verify(token)
        } catch (e: JWTVerificationException) {
            throw UnauthorizedException()
        }
        val username = decodedJwt.subject ?: throw UnauthorizedException()
        return UserSource.findUserByUsername(username) ?: throw UnauthorizedException()
    }
}
