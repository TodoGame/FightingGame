package match.matchmaking

import com.somegame.BaseKoinTest
import com.somegame.match.MatchRouting
import com.somegame.match.matchmaking.Matchmaker
import com.somegame.match.player.Player
import com.somegame.user.makeNewTestUser
import io.mockk.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import user.Username

internal class MatchmakerTest : BaseKoinTest() {
    var matchmaker = Matchmaker()

    private fun mockClient(username: Username): MatchRouting.MatchClient {
        val user = userRepository.makeNewTestUser(username)
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
}
