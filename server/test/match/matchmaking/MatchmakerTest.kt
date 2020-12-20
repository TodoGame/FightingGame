package match.matchmaking

import com.somegame.BaseKoinTest
import com.somegame.match.MatchRouting
import com.somegame.match.matchmaking.Matchmaker
import com.somegame.match.player.Player
import io.mockk.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import user.Username

internal class MatchmakerTest : BaseKoinTest() {
    var matchmaker = Matchmaker()

    private fun mockClient(username: Username, facultyId: Int = 1): MatchRouting.MatchClient {
        val user = makeNewTestUser(username, facultyId)
        val client = mockk<MatchRouting.MatchClient>()
        every { client.username } returns username
        every { client.user } returns user
        coEvery { client.kick(any()) } just Runs
        coEvery { client.sendMessage(any()) } just Runs
        coEvery { client.onJoinMatch(any()) } just Runs
        return client
    }

    @BeforeEach
    fun init() {
        matchmaker = Matchmaker()
    }

    @Test
    fun `should throw if user already joined`(): Unit = runBlocking {
        val client = mockClient("user1")
        matchmaker.startSearchingForMatch(client)
        assertThrows(Matchmaker.ClientAlreadyWaitingException::class.java) {
            runBlocking {
                matchmaker.startSearchingForMatch(client)
            }
        }
    }

    @Test
    fun `should not make match if 1 user joins`() = runBlocking {
        val client = mockClient("user1")
        matchmaker.startSearchingForMatch(client)
        coVerify(exactly = 0) { client.onJoinMatch(any()) }
    }

    @Test
    fun `should call every client's onJoinMatch if 2 clients join`() = runBlocking {
        val client1 = mockClient("user1")
        val client2 = mockClient("user2")

        matchmaker.startSearchingForMatch(client1)
        matchmaker.startSearchingForMatch(client2)

        coVerify {
            client1.onJoinMatch(any())
        }
        coVerify {
            client2.onJoinMatch(any())
        }
    }

    @Test
    fun `should call every client's onJoinMatch if 100 clients join`() = runBlocking {
        val clients = List(100) { mockClient("user$it") }

        for (client in clients) {
            matchmaker.startSearchingForMatch(client)
        }

        for (client in clients) {
            coVerify { client.onJoinMatch(any()) }
        }
    }

    @Test
    fun `should make 50 matches if 100 clients join`() = runBlocking {
        val clients = List(100) { mockClient("user$it") }

        val players = mutableListOf<Player>()

        for (client in clients) {
            every { client.onJoinMatch(capture(players)) } just Runs
            matchmaker.startSearchingForMatch(client)
        }

        val matches = players.map { it.match }.toSet()

        assertEquals(50, matches.size)
    }

    @Test
    fun `should call every client's onJoinMatch if 100 concurrent clients join`() = runBlocking {
        val clients = List(100) { mockClient("user$it") }

        runBlocking {
            for (client in clients) {
                launch {
                    matchmaker.startSearchingForMatch(client)
                }
            }
        }

        for (client in clients) {
            coVerify { client.onJoinMatch(any()) }
        }
    }

    @Test
    fun `should make 50 matches if 100 concurrent clients join`() = runBlocking {
        val clients = List(100) { mockClient("user$it") }

        val players = mutableListOf<Player>()

        runBlocking {
            for (client in clients) {
                every { client.onJoinMatch(capture(players)) } just Runs
                launch {
                    matchmaker.startSearchingForMatch(client)
                }
            }
        }

        val matches = players.map { it.match }.toSet()

        assertEquals(50, matches.size)
    }

    @Test
    fun `should match easygoing client with picky if easygoing's faculty is the one that picky selected`() {
        val easygoing = mockClient("easy", 1)
        val picky = mockClient("picky", 2)

        val easygoingPlayer = slot<Player>()
        val pickyPlayer = slot<Player>()

        coEvery { easygoing.onJoinMatch(capture(easygoingPlayer)) } just Runs
        coEvery { picky.onJoinMatch(capture(pickyPlayer)) } just Runs

        runBlocking {
            matchmaker.startSearchingForMatch(easygoing)
            matchmaker.startSearchingForMatch(picky, 1)
        }

        assertNotNull(easygoingPlayer.captured)
        assertEquals(easygoingPlayer.captured.match, pickyPlayer.captured.match)
    }

    @Test
    fun `should match picky client with easygoing (commutative) if easygoing's faculty is the one that picky selected`() {
        val easygoing = mockClient("easy", 1)
        val picky = mockClient("picky", 2)

        val easygoingPlayer = slot<Player>()
        val pickyPlayer = slot<Player>()

        coEvery { easygoing.onJoinMatch(capture(easygoingPlayer)) } just Runs
        coEvery { picky.onJoinMatch(capture(pickyPlayer)) } just Runs

        runBlocking {
            matchmaker.startSearchingForMatch(picky, 1)
            matchmaker.startSearchingForMatch(easygoing)
        }

        assertNotNull(easygoingPlayer.captured)
        assertEquals(easygoingPlayer.captured.match, pickyPlayer.captured.match)
    }

    @Test
    fun `should not match easygoing client with picky if easygoing's faculty is not the one that picky selected`() {
        val easygoing = mockClient("easy", 1)
        val picky = mockClient("picky", 2)

        val easygoingPlayer = slot<Player>()
        val pickyPlayer = slot<Player>()

        coEvery { easygoing.onJoinMatch(capture(easygoingPlayer)) } just Runs
        coEvery { picky.onJoinMatch(capture(pickyPlayer)) } just Runs

        runBlocking {
            matchmaker.startSearchingForMatch(easygoing)
            matchmaker.startSearchingForMatch(picky, 2)
        }

        assert(!easygoingPlayer.isCaptured)
        assert(!pickyPlayer.isCaptured)
    }

    @Test
    fun `should match picky with picky if they both want to play with each other`() {
        val picky1 = mockClient("picky1", 1)
        val picky2 = mockClient("picky2", 2)

        val pickyPlayer1 = slot<Player>()
        val pickyPlayer2 = slot<Player>()

        coEvery { picky1.onJoinMatch(capture(pickyPlayer1)) } just Runs
        coEvery { picky2.onJoinMatch(capture(pickyPlayer2)) } just Runs

        runBlocking {
            matchmaker.startSearchingForMatch(picky1, 2)
            matchmaker.startSearchingForMatch(picky2, 1)
        }

        assertNotNull(pickyPlayer1.captured)
        assertEquals(pickyPlayer1.captured.match, pickyPlayer2.captured.match)
    }

    @Test
    fun `should not match picky with picky if one of them does not want to play with other`() {
        val picky1 = mockClient("picky1", 1)
        val picky2 = mockClient("picky2", 2)

        val pickyPlayer1 = slot<Player>()
        val pickyPlayer2 = slot<Player>()

        coEvery { picky1.onJoinMatch(capture(pickyPlayer1)) } just Runs
        coEvery { picky2.onJoinMatch(capture(pickyPlayer2)) } just Runs

        runBlocking {
            matchmaker.startSearchingForMatch(picky1, 2)
            matchmaker.startSearchingForMatch(picky2, 3)
        }

        assert(!pickyPlayer1.isCaptured)
        assert(!pickyPlayer2.isCaptured)
    }

    @Test
    fun `should not match picky with picky if both dont want to play with each other`() {
        val picky1 = mockClient("picky1", 1)
        val picky2 = mockClient("picky2", 2)

        val pickyPlayer1 = slot<Player>()
        val pickyPlayer2 = slot<Player>()

        coEvery { picky1.onJoinMatch(capture(pickyPlayer1)) } just Runs
        coEvery { picky2.onJoinMatch(capture(pickyPlayer2)) } just Runs

        runBlocking {
            matchmaker.startSearchingForMatch(picky1, 4)
            matchmaker.startSearchingForMatch(picky2, 3)
        }

        assert(!pickyPlayer1.isCaptured)
        assert(!pickyPlayer2.isCaptured)
    }
}
