package match.matchmaking

import com.somegame.TestUsers
import com.somegame.match.matchmaking.Matchmaker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import user.User

internal class MatchmakerTest {
    var matchmaker = Matchmaker()

    @BeforeEach
    fun init() {
        matchmaker = Matchmaker()
    }

    @Test()
    fun `should throw if user already joined`() {
        matchmaker.join(TestUsers.user1)
        assertThrows(Matchmaker.UserAlreadyWaiting::class.java) {
            matchmaker.join(TestUsers.user1)
        }
    }

    @Test
    fun `should return null if first user joins matchmaking`() {
        val otherUsers = matchmaker.join(TestUsers.user1)
        assertNull(otherUsers)
    }

    @Test
    fun `should return match with all 2 users if these 2 users join matchmaking`() {
        matchmaker.join(TestUsers.user1)
        val otherUsers = matchmaker.join(TestUsers.user2)
        assertEquals(setOf(TestUsers.user1, TestUsers.user2), otherUsers?.toSet())
    }

    @Test
    fun `should make 50 correct matches for 100 users`() {
        val users = List(100) { TestUsers.makeUser(it) }
        val expectedMatches = users.chunked(2).map { it.toSet() }
        val actualMatches = mutableListOf<Set<User>>()

        for (user in users) {
            val match = matchmaker.join(user)
            if (match != null) {
                actualMatches.add(match.toSet())
            }
        }

        assertEquals(expectedMatches, actualMatches)
    }

    @Test
    fun `should make 50 matches for 100 concurrent users`() {
        val users = List(100) { TestUsers.makeUser(it) }
        val actualMatches = mutableListOf<Set<User>>()

        runBlocking {
            for (user in users) {
                launch {
                    val match = matchmaker.join(user)
                    if (match != null) {
                        actualMatches.add(match.toSet())
                    }
                }
            }
        }

        assertEquals(50, actualMatches.size)
    }

    @Test
    fun `should make 50 matches with all players and with each player only once for 100 concurrent users`() {
        val users = List(100) { TestUsers.makeUser(it) }
        val actualMatches = mutableListOf<Set<User>>()

        runBlocking {
            for (user in users) {
                launch {
                    val match = matchmaker.join(user)
                    if (match != null) {
                        actualMatches.add(match.toSet())
                    }
                }
            }
        }

        val allUsersInActualMatches = actualMatches.flatMap { it.toList() }.toSet()

        assertEquals(users.toSet(), allUsersInActualMatches)
    }
}
