package com.somegame

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import io.ktor.auth.*
import com.fasterxml.jackson.databind.*
import com.somegame.security.JwtConfig
import com.somegame.security.UserSource
import com.somegame.user.User
import io.ktor.auth.jwt.*
import io.ktor.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.lang.Exception

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val userSource = UserSource()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
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
                it.payload.subject?.let { username -> userSource.findUserByUsername(username) }
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

        post("/login") {
            val credentials = call.receive<UserPasswordCredential>()
            val user = userSource.findUserByCredentials(credentials)
            if (user != null && user.password == credentials.password) {
                val token = JwtConfig.makeToken(user)
                call.response.header("Authorization", "Bearer $token")
                call.respond(user)
            } else {
                call.response.status(HttpStatusCode.Unauthorized)
            }
        }

        post("/register") {
            val user = call.receive<User>()
            try {
                userSource.registerUser(user)
                val token = JwtConfig.makeToken(user)
                call.response.headers.append("Authorization", "Bearer $token")
                call.respond(user)
            } catch (e: UserSource.UserAlreadyExists) {
                call.response.status(HttpStatusCode.BadRequest)
            }
        }

        authenticate {
            get("/me") {
                val user = call.principal<User>()
                if (user != null) {
                    call.respond(user)
                } else {
                    call.response.status(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}

