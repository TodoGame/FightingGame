package match.matchmaking

import com.somegame.BaseKoinTest
import com.somegame.match.HANDS_DAMAGE
import com.somegame.match.MatchRouting
import com.somegame.match.MatchTestUtils
import com.somegame.match.START_HEALTH
import com.somegame.match.matchmaking.Match
import com.somegame.match.matchmaking.MockSimpleClient
import io.mockk.*
import io.mockk.every
import kotlinx.coroutines.runBlocking
import match.*
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
    fun `attacking with the hand should result in first client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = null

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client1.log.filterIsInstance<CalculatedPlayerAction>().first()
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, HANDS_DAMAGE), answer)
    }

    @Test
    fun `attacking with the hand should result in second client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = null

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client2.log.filterIsInstance<CalculatedPlayerAction>().first()
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, HANDS_DAMAGE), answer)
    }

    @Test
    fun `attacking with item1 should result in first client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 1

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client1.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `attacking with item1 should result in second client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 1

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client2.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `attacking with item2 should result in first client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 2

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client1.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `attacking with item2 should result in second client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 2

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client2.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `attacking with banana should result in first client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 3

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client1.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `attacking with banana should result in second client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 3

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client2.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser2", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `attacking with dice should result in first client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 4

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client1.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser1", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `attacking with dice should result in second client receiving CalculatedPlayerAction`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 4

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client2.log.filterIsInstance<CalculatedPlayerAction>().first()
        val damage = itemRepository.getItemById(itemId)!!.damage
        assertEquals(CalculatedPlayerAction("testUser1", "testUser1", itemId, damage), answer)
    }

    @Test
    fun `skipping a turn should result in first client receiving CalculatedSkipTurn`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(SkipTurn())
        }

        val answer = client1.log.filterIsInstance<CalculatedSkipTurn>().first()
        assertEquals(CalculatedSkipTurn("testUser1", true), answer)
    }

    @Test
    fun `skipping a turn should result in second client receiving CalculatedSkipTurn`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(SkipTurn())
        }

        val answer = client2.log.filterIsInstance<CalculatedSkipTurn>().first()
        assertEquals(CalculatedSkipTurn("testUser1", true), answer)
    }

    @Test
    fun `passive player cannot attack`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = null

        assertThrows(Match.IllegalActionException::class.java) {
            runBlocking {
                Match(listOf(client1.client, client2.client)).start()
                client2.makeDecision(PlayerAction("testUser1", "testUser2", itemId))
            }
        }
    }

    @Test
    fun `passive player send a fake attack as an active player`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = null

        assertThrows(Match.IllegalActionException::class.java) {
            runBlocking {
                Match(listOf(client1.client, client2.client)).start()
                client2.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
            }
        }
    }

    @Test
    fun `player attached with hand should have the predicted health`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = null

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val answer = client2.log.filterIsInstance<TurnStarted>().last()
        val expected = TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot("testUser1", false, START_HEALTH),
                    PlayerSnapshot("testUser2", true, START_HEALTH - HANDS_DAMAGE)
                )
            )
        )
        assertEquals(expected, answer)
    }

    @Test
    fun `player attached with item1 should have the predicted health`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 1

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val damage = itemRepository.getItemById(itemId)!!.damage

        val answer = client2.log.filterIsInstance<TurnStarted>().last()
        val expected = TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot("testUser1", false, START_HEALTH),
                    PlayerSnapshot("testUser2", true, START_HEALTH - damage)
                )
            )
        )
        assertEquals(expected, answer)
    }

    @Test
    fun `player attached with item2 should have the predicted health`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 2

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val damage = itemRepository.getItemById(itemId)!!.damage

        val answer = client2.log.filterIsInstance<TurnStarted>().last()
        val expected = TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot("testUser1", false, START_HEALTH),
                    PlayerSnapshot("testUser2", true, START_HEALTH - damage)
                )
            )
        )
        assertEquals(expected, answer)
    }

    @Test
    fun `player attached with item3 should have the predicted health`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 3

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }

        val damage = itemRepository.getItemById(itemId)!!.damage

        val answer = client2.log.filterIsInstance<TurnStarted>().last()
        val expected = TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot("testUser1", false, START_HEALTH),
                    PlayerSnapshot("testUser2", true, START_HEALTH - damage)
                )
            )
        )
        assertEquals(expected, answer)
    }

    @Test
    fun `after attack with dice the second player should be the winner`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = 4

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }
        val answer = client2.log.filterIsInstance<MatchEnded>().last()
        val expected = MatchEnded("testUser2")
        assertEquals(expected, answer)
    }

    @Test
    fun `only hands match should end when the first player kills the second`() {
        val client1 = MockSimpleClient(repositoriesMock, "testUser1")
        val client2 = MockSimpleClient(repositoriesMock, "testUser2")

        val itemId = null

        runBlocking {
            Match(listOf(client1.client, client2.client)).start()
            repeat(3) {
                client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
                client2.makeDecision(PlayerAction("testUser1", "testUser2", itemId))
            }
            client1.makeDecision(PlayerAction("testUser2", "testUser1", itemId))
        }
        val answer = client2.log.filterIsInstance<MatchEnded>().last()
        val expected = MatchEnded("testUser1")
        assertEquals(expected, answer)
    }
}
