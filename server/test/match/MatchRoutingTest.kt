package com.somegame.websocket

import com.somegame.TestUsers.addJwtHeader
import com.somegame.match.MatchRouting
import com.somegame.security.JwtConfig
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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import match.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import user.Username
import websocket.WebSocketTicket
import java.time.Duration

class MatchRoutingTest {
    private fun withApp(block: TestApplicationEngine.() -> Unit) {
        withTestApplication(
            {
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
            incoming1.receive()
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
    fun `getTicket should return only 1 ticket per 1 user`() = withApp {
        getTicket("user1")
        val anotherTicket = getTicket("user1") ?: ""
        assertThrows(SerializationException::class.java) {
            Json.decodeFromString<WebSocketTicket>(anotherTicket)
        }
    }

//    @Test
//    fun `should send 1 MatchStarted message to each user if 2 players join`() = withApp {
//        val log1 = mutableListOf<Frame.Text>()
//        val log2 = mutableListOf<Frame.Text>()
//
//        connect("user1") { incoming1, _ ->
//            // TODO: should add test without this line. This line makes sure that 2 requests are made 1 after another. This line is present in next tests too
//            incoming1.receive()
//            connect("user2") { incoming2, _ ->
//                for (frame in incoming2) {
//                    if (frame is Frame.Text) {
//                        log2.add(frame)
//                    }
//                }
//            }
//
//            for (frame in incoming1) {
//                if (frame is Frame.Text) {
//                    log1.add(frame)
//                }
//            }
//        }
//        val messages1 = log1.map { Json.decodeFromString<Message>(it.readText()) }
//        val messages2 = log2.map { Json.decodeFromString<Message>(it.readText()) }
//
//        assertEquals(1, messages1.count { it is MatchStarted })
//        assertEquals(1, messages2.count { it is MatchStarted })
//    }
//
//    @Test
//    fun `should send 1 TurnStarted message to each user if 2 users join`() = withApp {
//        val log1 = mutableListOf<Frame.Text>()
//        val log2 = mutableListOf<Frame.Text>()
//
//        connect("user1") { incoming1, outgoing1 ->
//            incoming1.receive()
//            connect("user2") { incoming2, outgoing2 ->
//                for (frame in incoming2) {
//                    if (frame is Frame.Text) {
//                        log2.add(frame)
//                    }
//                }
//            }
//
//            for (frame in incoming1) {
//                if (frame is Frame.Text) {
//                    log1.add(frame)
//                }
//            }
//        }
//
//        val messages1 = log1.map { Json.decodeFromString<Message>(it.readText()) }
//        val messages2 = log2.map { Json.decodeFromString<Message>(it.readText()) }
//
//        assertEquals(1, messages1.count { it is TurnStarted })
//        assertEquals(1, messages2.count { it is TurnStarted })
//    }
//
//    @Test
//    fun `first TurnStarted message should include snapshots that are equal for both users`() = withApp {
//        val log1 = mutableListOf<Frame.Text>()
//        val log2 = mutableListOf<Frame.Text>()
//
//        connect("user1") { incoming1, _ ->
//            incoming1.receive()
//            connect("user2") { incoming2, _ ->
//                for (frame in incoming2) {
//                    if (frame is Frame.Text) {
//                        log2.add(frame)
//                    }
//                }
//            }
//
//            for (frame in incoming1) {
//                if (frame is Frame.Text) {
//                    log1.add(frame)
//                }
//            }
//        }
//
//        val messages1 = log1.map { Json.decodeFromString<Message>(it.readText()) }
//        val messages2 = log2.map { Json.decodeFromString<Message>(it.readText()) }
//
//        val turnStarted1 = messages1.find { it is TurnStarted } as TurnStarted?
//        val turnStarted2 = messages2.find { it is TurnStarted } as TurnStarted?
//
//        assertNotNull(turnStarted1)
//        assertNotNull(turnStarted2)
//
//        assertEquals(turnStarted1?.matchSnapshot, turnStarted2?.matchSnapshot)
//    }
//
//    @Test
//    fun `first TurnStarted message should include snapshots that include both users`() = withApp {
//        val log1 = mutableListOf<Frame.Text>()
//        val log2 = mutableListOf<Frame.Text>()
//
//        connect("user1") { incoming1, outgoing1 ->
//            incoming1.receive()
//            connect("user2") { incoming2, outgoing2 ->
//                for (frame in incoming2) {
//                    if (frame is Frame.Text) {
//                        log2.add(frame)
//                    }
//                }
//            }
//
//            for (frame in incoming1) {
//                if (frame is Frame.Text) {
//                    log1.add(frame)
//                }
//            }
//        }
//
//        val messages1 = log1.map { Json.decodeFromString<Message>(it.readText()) }
//        val turnStarted1 = messages1.find { it is TurnStarted } as TurnStarted?
//
//        val receivedUsernames = turnStarted1?.matchSnapshot?.players?.map { it.username }?.toSet()
//
//        assertEquals(setOf("user1", "user2"), receivedUsernames)
//    }
//
//    @Test
//    fun `first TurnStarted message should include snapshots where 1 user is active and another is not`() = withApp {
//        val log1 = mutableListOf<Frame.Text>()
//        val log2 = mutableListOf<Frame.Text>()
//
//        connect("user1") { incoming1, outgoing1 ->
//            incoming1.receive()
//            connect("user2") { incoming2, outgoing2 ->
//                for (frame in incoming2) {
//                    if (frame is Frame.Text) {
//                        log2.add(frame)
//                    }
//                }
//            }
//
//            for (frame in incoming1) {
//                if (frame is Frame.Text) {
//                    log1.add(frame)
//                }
//            }
//        }
//
//        val messages1 = log1.map { Json.decodeFromString<Message>(it.readText()) }
//        val turnStarted1 = messages1.find { it is TurnStarted } as TurnStarted?
//
//        val activeUsersCount = turnStarted1?.matchSnapshot?.players?.count { it.isActive }
//
//        assertEquals(1, activeUsersCount)
//    }
//
//    @Test
//    fun `the player who started first should be the winner`() = withApp {
//        val log1 = mutableListOf<Message>()
//        val log2 = mutableListOf<Message>()
//        connect2SimplePlayers("user1", "user2", log1, log2)
//
//        val firstActivePlayerUsername =
//            log1.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username
//
//        assertEquals(firstActivePlayerUsername, log1.filterIsInstance<MatchEnded>().first().winner)
//    }
//
//    @Test
//    fun `2 players should receive the same winner`() = withApp {
//        val log1 = mutableListOf<Message>()
//        val log2 = mutableListOf<Message>()
//        connect2SimplePlayers("user1", "user2", log1, log2)
//
//        val winner1 = log1.filterIsInstance<MatchEnded>().first().winner
//        val winner2 = log2.filterIsInstance<MatchEnded>().first().winner
//
//        assertEquals(winner1, winner2)
//    }

    @Test
    fun `two players should play the game to the end and receive a predicted log of messages`() = withApp {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()
        connect2SimplePlayers("user1", "user2", log1, log2)

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
//        connect2SimplePlayers(username1, username2, log1, log2)

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
