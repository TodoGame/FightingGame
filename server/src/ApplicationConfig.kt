package com.somegame

import com.somegame.db.DatabaseConfig
import com.somegame.match.MatchRouting
import com.somegame.security.JwtConfig
import com.somegame.security.SecurityRouting.security
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
import org.slf4j.event.Level
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

        routing {
            get("/") {
                call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
            }

            security()
            user()
            MatchRouting().setUpMatchRoutes(this)
        }
    }

    @KtorExperimentalAPI
    fun Application.installDatabase() {
        val dbUrl = environment.config.property("database.url").getString()
        val dbUser = environment.config.property("database.user").getString()
        val dbPassword = environment.config.property("database.password").getString()

        DatabaseConfig(dbUrl, dbUser, dbPassword).configure()
    }

    fun Application.installCallLogging() {
        install(CallLogging) {
            level = Level.INFO
        }
    }

    fun Application.installKoin() {
        install(org.koin.ktor.ext.Koin) {
            modules(com.somegame.databaseRepositoryModule, com.somegame.applicationModule)
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
