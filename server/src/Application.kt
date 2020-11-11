package com.somegame

import com.fasterxml.jackson.databind.*
import com.somegame.db.DatabaseConfig
import com.somegame.match.MatchRouting
import com.somegame.security.JwtConfig
import com.somegame.security.security
import com.somegame.user.user
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import org.koin.core.Koin
import org.slf4j.event.*
import java.time.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val dbUrl = environment.config.property("database.url").getString()
    val dbUser = environment.config.property("database.user").getString()
    val dbPassword = environment.config.property("database.password").getString()

    DatabaseConfig(dbUrl, dbUser, dbPassword).connect()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(org.koin.ktor.ext.Koin) {
        modules(com.somegame.databaseRepositoryModule, com.somegame.applicationModule)
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(Authentication) {
        jwt {
            verifier(JwtConfig.verifier)
            validate {
                JwtConfig.verifyCredentialsAndGetPrincipal(it)
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        security()
        user()
        MatchRouting().setUpMatchRoutes(this)
    }
}
