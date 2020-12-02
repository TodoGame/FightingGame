package match.matchmaking

import com.somegame.BaseKoinTest
import com.somegame.match.MatchRouting
import com.somegame.match.MatchTestUtils
import com.somegame.match.matchmaking.Match
import com.somegame.match.matchmaking.MockMatchClientThatPlays
import io.mockk.*
import io.mockk.every
import kotlinx.coroutines.runBlocking
import match.MatchStarted
import match.Message
import match.TurnStarted
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import user.Username

internal class MatchTest : BaseKoinTest() {
    private fun mockClient(username: Username): MatchRouting.MatchClient {
        val user = makeNewTestUser(username)
        val client = mockk<MatchRouting.MatchClient>()
        every { client.username } returns username
        every { client.onJoinMatch(any()) } just Runs
        every { client.user } returns user
        coEvery { client.sendMessage(any()) } just Runs
        return client
    }

    @Test
    fun `should call onJoinMatch of each client`() = runBlocking {
        val client1 = mockClient("user1")
        val client2 = mockClient("user2")

        Match(listOf(client1, client2)).start()

        verify { client1.onJoinMatch(any()) }
        verify { client2.onJoinMatch(any()) }
    }

    @Test
    fun `should send match started to all the clients`() {
        val client1 = mockClient("user1")
        val client2 = mockClient("user2")

        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        coEvery { client1.sendMessage(capture(log1)) } just Runs
        coEvery { client2.sendMessage(capture(log2)) } just Runs

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        assert(log1.first() is MatchStarted)
        assert(log2.first() is MatchStarted)
    }

    @Test
    fun `should send the same match started message to all the clients`() {
        val client1 = mockClient("user1")
        val client2 = mockClient("user2")

        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        coEvery { client1.sendMessage(capture(log1)) } just Runs
        coEvery { client2.sendMessage(capture(log2)) } just Runs

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        assertEquals(log1.first(), log2.first())
    }

    @Test
    fun `should send the match started message where users are user1 and user2`() {
        val client1 = mockClient("user1")
        val client2 = mockClient("user2")

        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        coEvery { client1.sendMessage(capture(log1)) } just Runs
        coEvery { client2.sendMessage(capture(log2)) } just Runs

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        val message = log1.first()

        if (message is MatchStarted) {
            assertEquals(setOf("user1", "user2"), message.players)
        } else {
            assert(false) { "Message was not MatchStarted" }
        }
    }

    @Test
    fun `should send turn started message to user1 where 1 player is active`() {
        val client1 = mockClient("user1")
        val client2 = mockClient("user2")

        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        coEvery { client1.sendMessage(capture(log1)) } just Runs
        coEvery { client2.sendMessage(capture(log2)) } just Runs

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        val turnStarted1 = log1.find { it is TurnStarted } as TurnStarted?
        assertNotNull(turnStarted1) { "TurnStarted was not sent" }
        assertNotNull(turnStarted1?.matchSnapshot?.players?.find { it.isActive }) { "There  no active player" }
    }

    @Test
    fun `both players should think that the same player is initially active`() {
        val client1 = mockClient("user1")
        val client2 = mockClient("user2")

        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        coEvery { client1.sendMessage(capture(log1)) } just Runs
        coEvery { client2.sendMessage(capture(log2)) } just Runs

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        val activePlayerUsername1 = MatchTestUtils.getActivePlayerUsernameFromLog(log1)
        val activePlayerUsername2 = MatchTestUtils.getActivePlayerUsernameFromLog(log2)

        assertEquals(activePlayerUsername1, activePlayerUsername2)
    }

    @Test
    fun `passive client should receive a predicted log of messages`() {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        val client1 = MockMatchClientThatPlays("user1", log1).build()
        val client2 = MockMatchClientThatPlays("user2", log2).build()

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        val activePlayerUsername = MatchTestUtils.getActivePlayerUsernameFromLog(log1)

        if (activePlayerUsername == "user1") {
            assertEquals(MatchTestUtils.generatePassivePlayerLog("user1", "user2"), log2)
        } else if (activePlayerUsername == "user2") {
            assertEquals(MatchTestUtils.generatePassivePlayerLog("user2", "user1"), log1)
        } else {
            throw Exception("Did not find active player")
        }
    }

    @Test
    fun `active client should receive a predicted log of messages`() {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        val client1 = MockMatchClientThatPlays("user1", log1).build()
        val client2 = MockMatchClientThatPlays("user2", log2).build()

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        val activePlayerUsername = MatchTestUtils.getActivePlayerUsernameFromLog(log1)

        if (activePlayerUsername == "user1") {
            assertEquals(MatchTestUtils.generateActivePlayerLog("user1", "user2"), log1)
        } else if (activePlayerUsername == "user2") {
            assertEquals(MatchTestUtils.generateActivePlayerLog("user2", "user1"), log2)
        } else {
            throw Exception("Did not find active player")
        }
    }

    @Test
    fun `passive client that always hits should receive a predicted log of messages`() {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        val client1 = MockMatchClientThatPlays("user1", log1, true).build()
        val client2 = MockMatchClientThatPlays("user2", log2, true).build()

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        val activePlayerUsername = MatchTestUtils.getActivePlayerUsernameFromLog(log1)

        if (activePlayerUsername == "user1") {
            assertEquals(MatchTestUtils.generatePassivePlayerLog("user1", "user2"), log2)
        } else if (activePlayerUsername == "user2") {
            assertEquals(MatchTestUtils.generatePassivePlayerLog("user2", "user1"), log1)
        } else {
            throw Exception("Did not find active player")
        }
    }

    @Test
    fun `active client that always hits should receive a predicted log of messages`() {
        val log1 = mutableListOf<Message>()
        val log2 = mutableListOf<Message>()

        val client1 = MockMatchClientThatPlays("user1", log1, true).build()
        val client2 = MockMatchClientThatPlays("user2", log2, true).build()

        runBlocking {
            Match(listOf(client1, client2)).start()
        }

        val activePlayerUsername = MatchTestUtils.getActivePlayerUsernameFromLog(log1)

        if (activePlayerUsername == "user1") {
            assertEquals(MatchTestUtils.generateActivePlayerLog("user1", "user2"), log1)
        } else if (activePlayerUsername == "user2") {
            assertEquals(MatchTestUtils.generateActivePlayerLog("user2", "user1"), log2)
        } else {
            throw Exception("Did not find active player")
        }
    }
}
