package com.somegame.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.somegame.user.User
import com.somegame.user.principal
import io.ktor.auth.jwt.*

object JwtConfig {
    private const val secret = "SecretHehe"
    val algorithm: Algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT.require(algorithm).build()

    private fun makeToken(userPrincipal: UserPrincipal): JWTCreator.Builder =
        JWT.create().withSubject(userPrincipal.username)

    fun makeLoginToken(user: User): String = makeToken(user.principal()).sign(algorithm)

    fun verifyCredentialsAndGetPrincipal(credential: JWTCredential) = UserPrincipal(credential.payload.subject)
}
