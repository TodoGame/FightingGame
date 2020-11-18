package com.somegame

import com.somegame.ApplicationConfig.installAuth
import com.somegame.ApplicationConfig.installSerialization
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.serialization.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import org.junit.jupiter.api.AfterEach
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.slf4j.event.Level
import java.time.Duration

abstract class BaseKtorTest : KoinTest {
    protected abstract val applicationModules: List<Module>

    protected fun <R> withBaseApp(setUpApp: Application.() -> Unit, block: TestApplicationEngine.() -> R) =
        withTestApplication({
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(1)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            install(CallLogging) {
                level = Level.INFO
            }

            install(org.koin.ktor.ext.Koin) {
                modules(applicationModules)
            }

            installAuth()
            installSerialization()

            setUpApp()
        }) {
            block()
        }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}
