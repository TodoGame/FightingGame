package subscription

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJwtHeader
import com.somegame.faculty.FacultyPointsManager
import com.somegame.match.LOSING_USER_PRIZE
import com.somegame.match.WINNING_FACULTY_PRIZE
import com.somegame.match.WINNING_USER_PRIZE
import com.somegame.subscription.subscription
import com.somegame.user.UserMoneyManager
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.core.inject
import user.Username

internal class SubscriptionRoutingKtTest : SimpleKtorTest() {
    private fun withApp(block: TestApplicationEngine.() -> Unit): Unit = withBaseApp(
        {
            routing {
                subscription()
            }
        },
        block
    )

    private fun TestApplicationEngine.getTicket(username: Username): String? = handleRequest {
        uri = SUBSCRIPTION_WEBSOCKET_TICKET_ENDPOINT
        method = HttpMethod.Get
        addJwtHeader(username)
    }.response.content

    private fun TestApplicationEngine.connect(
        username: Username,
        callback: suspend TestApplicationCall.(incoming: ReceiveChannel<Frame>, outgoing: SendChannel<Frame>) -> Unit
    ) {
        val ticket = getTicket(username) ?: IllegalArgumentException("Ticket not received")
        handleWebSocketConversation("$SUBSCRIPTION_WEBSOCKET_ENDPOINT?ticket=$ticket", {}, callback)
    }

    @Test
    fun `user money subscription should notify when user wins`() = withApp {
        val userMoneyManager: UserMoneyManager by inject()
        val user = makeNewTestUser("testUser")
        connect("testUser") { incoming, outgoing ->
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(UserMoneyUpdateSubscription("testUser"))))
            launch {
                delay(100L)
                userMoneyManager.onUserWin(user)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(UserMoneyUpdate("testUser", WINNING_USER_PRIZE), update)
        }
    }

    @Test
    fun `user money subscription should notify when user loses`() = withApp {
        val userMoneyManager: UserMoneyManager by inject()
        val user = makeNewTestUser("testUser")
        connect("testUser") { incoming, outgoing ->
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(UserMoneyUpdateSubscription("testUser"))))
            launch {
                delay(100L)
                userMoneyManager.onUserLose(user)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(UserMoneyUpdate("testUser", LOSING_USER_PRIZE), update)
        }
    }

    @Test
    fun `user money subscription should send correct new user money the second time`() = withApp {
        val userMoneyManager: UserMoneyManager by inject()
        val user = makeNewTestUser("testUser")
        connect("testUser") { incoming, outgoing ->
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(UserMoneyUpdateSubscription("testUser"))))
            launch {
                delay(100L)
                userMoneyManager.onUserWin(user)
            }
            incoming.receive()
            launch {
                delay(100L)
                userMoneyManager.onUserLose(user)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(
                UserMoneyUpdate(
                    "testUser",
                    LOSING_USER_PRIZE + WINNING_USER_PRIZE
                ),
                update
            )
        }
    }

    @Test
    fun `all faculties subscription should notify when first faculty points are changed`() = withApp {
        val facultyPointsManager: FacultyPointsManager by inject()
        val user = user1
        connect("user1") { incoming, outgoing ->
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(AllFacultiesPointsSubscription())))
            launch {
                delay(100L)
                facultyPointsManager.onFacultyMemberWin(user)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(
                FacultyPointsUpdate(
                    1,
                    WINNING_FACULTY_PRIZE,
                    "user1"
                ),
                update
            )
        }
    }

    @Test
    fun `all faculties subscription should notify when second faculty points are changed`() = withApp {
        val facultyPointsManager: FacultyPointsManager by inject()
        val user = user2
        connect("user2") { incoming, outgoing ->
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(AllFacultiesPointsSubscription())))
            launch {
                delay(100L)
                facultyPointsManager.onFacultyMemberWin(user)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(
                FacultyPointsUpdate(
                    2,
                    WINNING_FACULTY_PRIZE,
                    "user2"
                ),
                update
            )
        }
    }

    @Test
    fun `all faculties subscription should send correct data even with new user`() = withApp {
        val facultyPointsManager: FacultyPointsManager by inject()
        makeNewTestUser("testUser")
        connect("testUser") { incoming, outgoing ->
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(AllFacultiesPointsSubscription())))
            launch {
                delay(100L)
                facultyPointsManager.onFacultyMemberWin(user2)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(
                FacultyPointsUpdate(
                    2,
                    WINNING_FACULTY_PRIZE,
                    "user2"
                ),
                update
            )
        }
    }

    @Test
    fun `leading faculty subscription should notify when faculty becomes leading`() = withApp {
        val facultyPointsManager: FacultyPointsManager by inject()
        makeNewTestUser("testUser")
        connect("testUser") { incoming, outgoing ->
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(LeadingFacultySubscription())))
            launch {
                delay(100L)
                facultyPointsManager.onFacultyMemberWin(user1)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(
                LeadingFacultyUpdate(
                    1,
                    WINNING_FACULTY_PRIZE
                ),
                update
            )
        }
    }

    @Test
    fun `leading faculty subscription should notify when faculty overtakes another faculty`() = withApp {
        val facultyPointsManager: FacultyPointsManager by inject()
        makeNewTestUser("testUser")
        connect("testUser") { incoming, outgoing ->
            facultyPointsManager.onFacultyMemberWin(user1)
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(LeadingFacultySubscription())))
            launch {
                delay(100L)
                facultyPointsManager.onFacultyMemberWin(user2)
                facultyPointsManager.onFacultyMemberWin(user2)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(
                LeadingFacultyUpdate(
                    2,
                    WINNING_FACULTY_PRIZE * 2
                ),
                update
            )
        }
    }

    @Test
    fun `leading faculty subscription should notify when leading faculty has its points updates`() = withApp {
        val facultyPointsManager: FacultyPointsManager by inject()
        makeNewTestUser("testUser")
        connect("testUser") { incoming, outgoing ->
            facultyPointsManager.onFacultyMemberWin(user1)
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(LeadingFacultySubscription())))
            launch {
                delay(100L)
                facultyPointsManager.onFacultyMemberWin(user1)
            }
            val frame = incoming.receive()
            assert(frame is Frame.Text) { "Received not Frame.Text" }
            val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
            assertEquals(
                LeadingFacultyUpdate(
                    1,
                    WINNING_FACULTY_PRIZE * 2
                ),
                update
            )
        }
    }

    @Test
    fun `leading faculty subscription should send correct sequence of take-overs`() = withApp {
        val facultyPointsManager: FacultyPointsManager by inject()
        makeNewTestUser("testUser")
        val log = mutableListOf<SubscriptionUpdate>()
        connect("testUser") { incoming, outgoing ->
            facultyPointsManager.onFacultyMemberWin(user1)
            outgoing.send(Frame.Text(Json.encodeToString<SubscriptionMessage>(LeadingFacultySubscription())))
            launch {
                delay(100L)
                facultyPointsManager.onFacultyMemberWin(user2)
                facultyPointsManager.onFacultyMemberWin(user2)
                facultyPointsManager.onFacultyMemberWin(user1)
                facultyPointsManager.onFacultyMemberWin(user1)
            }
            for (frame in incoming) {
                assert(frame is Frame.Text) { "Received not Frame.Text" }
                val update = Json.decodeFromString<SubscriptionUpdate>((frame as Frame.Text).readText())
                log.add(update)
                if (log.size == 2) {
                    break
                }
            }
            val expectedLog = listOf(
                LeadingFacultyUpdate(2, WINNING_FACULTY_PRIZE * 2),
                LeadingFacultyUpdate(1, WINNING_FACULTY_PRIZE * 3)
            )
            assertEquals(expectedLog, log)
        }
    }
}
