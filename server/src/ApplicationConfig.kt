package com.somegame

import com.somegame.db.DatabaseConfig
import com.somegame.faculty.faculties
import com.somegame.match.MatchRouting
import com.somegame.responseExceptions.TransformExceptionsIntoResponses
import com.somegame.security.JwtConfig
import com.somegame.security.security
import com.somegame.shop.shop
import com.somegame.user.user
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.*
import io.ktor.websocket.*
import java.time.Duration

object ApplicationConfig {
    @KtorExperimentalAPI
    fun Application.mainModule() {
        installDatabase()
        installCallLogging()
        installKoin()
        installWebSockets()
        installAuth()
        installSerialization()

        install(TransformExceptionsIntoResponses)

        routing {
            get("/") {
                call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
            }

            security()
            user()
            MatchRouting().setUpMatchRoutes(this)
            shop()
            faculties()
        }
    }

    @KtorExperimentalAPI
    fun Application.installDatabase() {
        val dbFullUrl = environment.config.property("database.fullUrl").getString()

        DatabaseConfig(dbFullUrl).configure()
    }

    fun Application.installExceptionsTransformation() {
        install(TransformExceptionsIntoResponses)
    }

    fun Application.installCallLogging() {
        install(CallLogging)
    }

    fun Application.installKoin() {
        install(org.koin.ktor.ext.Koin) {
            modules(databaseRepositoryModule)
        }
    }

    fun Application.installWebSockets() {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
    }

    fun Application.installAuth() {
        install(Authentication) {
            jwt {
                verifier(JwtConfig.verifier)
                validate {
                    JwtConfig.verifyCredentialsAndGetPrincipal(it)
                }
            }
        }
    }

    fun Application.installSerialization() {
        install(ContentNegotiation) {
            json()
        }
    }
}
