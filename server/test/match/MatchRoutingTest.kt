package com.somegame.websocket

import com.somegame.TestUtils.addJwtHeader
import com.somegame.applicationModule
import com.somegame.match.MatchRouting
import com.somegame.security.JwtConfig
import com.somegame.user.repository.MockUserRepository
import com.somegame.user.repository.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import match.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import user.Username
import websocket.WebSocketTicket
import java.time.Duration

class MatchRoutingTest {
    private val mockUserRepository = MockUserRepository()

    private val repositoryModule = module {
        single<UserRepository> { mockUserRepository }
    }

    private fun withApp(block: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            {
                install(org.koin.ktor.ext.Koin) {
                    modules(repositoryModule, applicationModule)
                }

                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(1)
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

                routing {
                    MatchRouting().setUpMatchRoutes(this)
                }
            },
            block
        )
    }

    @BeforeEach
    fun clearRepository() {
        mockUserRepository.clear()
    }

    private fun TestApplicationEngine.getTicket(username: Username): String? = handleRequest {
        uri = matchWebSocketTicketEndpoint
        method = HttpMethod.Get
        addJwtHeader(username)
    }.response.content

    private fun TestApplicationEngine.connect(
        username: Username,
        callback: suspend TestApplicationCall.(incoming: ReceiveChannel<Frame>, outgoing: SendChannel<Frame>) -> Unit
    ) {
        val ticket = getTicket(username) ?: IllegalArgumentException("Ticket not received")
        handleWebSocketConversation("$matchWebSocketEndpoint?ticket=$ticket", {}, callback)
    }

    private fun TestApplicationEngine.connect2SimplePlayers(
        username1: Username,
        username2: Username,
        log1: MutableList<Message>,
        log2: MutableList<Message>
    ) {
        connect(username1) { incoming1, outgoing1 ->
            launch {
                connect(username2) { incoming2, outgoing2 ->
                    val player2 = SimplePlayer(username2, username1, log2, incoming2, outgoing2)
                    player2.start()
                }
            }
            val player1 = SimplePlayer(username1, username2, log1, incoming1, outgoing1)
            player1.start()
        }
    }

    fun generateActivePlayerLog(activeUsername: Username, passiveUsername: Username) = listOf(
        MatchStarted(setOf(activeUsername, passiveUsername)),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 15),
                    PlayerSnapshot(passiveUsername, false, 15)
                )
            )
        ),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, false, 15),
                    PlayerSnapshot(passiveUsername, true, 5)
                )
            )
        ),
        PlayerAction(activeUsername, passiveUsername),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 5),
                    PlayerSnapshot(passiveUsername, false, 5)
                )
            )
        ),
        MatchEnded(activeUsername)
    )

    fun generatePassivePlayerLog(activeUsername: Username, passiveUsername: Username) = listOf(
        MatchStarted(setOf(activeUsername, passiveUsername)),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 15),
                    PlayerSnapshot(passiveUsername, false, 15)
                )
            )
        ),
        PlayerAction(passiveUsername, activeUsername),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, false, 15),
                    PlayerSnapshot(passiveUsername, true, 5)
                )
            )
        ),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 5),
                    PlayerSnapshot(passiveUsername, false, 5)
                )
            )
        ),
        PlayerAction(passiveUsername, activeUsername),
        MatchEnded(activeUsername)
    )

    @Test
    fun `getTicket should return ticket`() = withApp {
        val ticket = getTicket("user1")
        assertNotNull(ticket)
    }

    @Test
    fun `getTicket should return second ticket per 1 user`() = withApp {
        getTicket("user1")
        val anotherTicket = getTicket("user1")?.let {
            Json.decodeFromString<WebSocketTicket>(it)
        }
        assertNotNull(anotherTicket)
    }

    @Test
    fun `active player should receive a predictable log of messages`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

        val activePlayerUsername =
            log1.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
        val passivePlayerUsername = if (activePlayerUsername == "user1") "user2" else "user1"

        val activePlayerLog = generateActivePlayerLog(activePlayerUsername, passivePlayerUsername)

        if (activePlayerUsername == "user1") {
            assertEquals(activePlayerLog, log1)
        } else {
            assertEquals(activePlayerLog, log2)
        }
    }

    @Test
    fun `passive player should receive a predictable log of messages`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

        val activePlayerUsername =
            log1.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
        val passivePlayerUsername = if (activePlayerUsername == "user1") "user2" else "user1"

        val passivePlayerLog = generatePassivePlayerLog(activePlayerUsername, passivePlayerUsername)

        if (activePlayerUsername == "user1") {
            assertEquals(passivePlayerLog, log2)
        } else {
            assertEquals(passivePlayerLog, log1)
        }
    }

    @Test
    fun `players that always hit should have the same logs as the normal players`() = withApp {
        val username1 = "user1"
        val username2 = "user2"

        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        connect(username1) { incoming1, outgoing1 ->
            incoming1.receive()
            launch {
                connect(username2) { incoming2, outgoing2 ->
                    val player2 = SimplePlayerThatAlwaysHits(username2, username1, log2, incoming2, outgoing2)
                    player2.start()
                }
            }
            val player1 = SimplePlayerThatAlwaysHits(username1, username2, log1, incoming1, outgoing1)
            player1.start()
        }

        val activePlayerUsername =
            log1.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
        val passivePlayerUsername = if (activePlayerUsername == "user1") "user2" else "user1"

        val activePlayerLog = generateActivePlayerLog(activePlayerUsername, passivePlayerUsername)
        val passivePlayerLog = generatePassivePlayerLog(activePlayerUsername, passivePlayerUsername)

        if (activePlayerUsername == "user1") {
            assertEquals(activePlayerLog, log1)
            assertEquals(passivePlayerLog, log2)
        } else {
            assertEquals(activePlayerLog, log2)
            assertEquals(passivePlayerLog, log1)
        }
    }
    // TODO: test multiple matches at the same time
}
