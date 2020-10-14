package com.somegame.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.somegame.user.User

object JwtConfig {
    private const val secret = "SecretHehe"
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier = JWT.require(algorithm).build()

    fun makeToken(user: User): String = JWT.create().withSubject(user.username).sign(algorithm)
}
