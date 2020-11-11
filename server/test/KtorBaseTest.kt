package com.somegame

import com.somegame.security.JwtConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import org.koin.core.module.Module
import java.time.Duration

abstract class KtorBaseTest {
    protected abstract val applicationModules: List<Module>

    private fun <R> withBaseApp(block: TestApplicationEngine.() -> R) = withTestApplication({
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(1)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        install(org.koin.ktor.ext.Koin) {
            modules(applicationModules)
        }

        install(Authentication) {
            jwt {
                verifier(JwtConfig.verifier)
                validate {
                    JwtConfig.verifyCredentialsAndGetPrincipal(it)
                }
            }
        }
    }) {
        block()
    }
}
