package com.somegame.match.matchmaking

import com.somegame.match.MatchRouting
import com.somegame.match.player.Player
import com.somegame.user.UserRepository
import com.somegame.user.repository.MockUserRepositoryFactory.makeNewTestUser
import io.mockk.*
import match.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import user.Username

class MockMatchClientThatPlays(
    private val username: Username,
    private val log: MutableList<Message>,
    private val alwaysHits: Boolean = false
) : KoinComponent {
    private val userRepository: UserRepository by inject()

    private val player = slot<Player>()
    lateinit var opponentUsername: Username

    fun build(): MatchRouting.MatchClient {
        val user = userRepository.makeNewTestUser(username)
        val client = mockk<MatchRouting.MatchClient>()
        every { client.username } returns username
        every { client.user } returns user
        every { client.onJoinMatch(capture(player)) } just Runs
        coEvery { client.kick(any()) } just Runs
        coEvery { client.sendMessage(any()) } coAnswers {
            val message = firstArg<Message>()
            log.add(message)
            handleMessage(message)
        }
        return client
    }

    private suspend fun handleMessage(message: Message) {
        when (message) {
            is MatchStarted -> {
                opponentUsername = message.players.find { it != username }!!
            }
            is TurnStarted -> {
                val active = amIActive(message)
                if (alwaysHits || active) {
                    try {
                        player.captured.doAction(PlayerAction(opponentUsername, username))
                    } catch (e: Match.IllegalActionException) {
                        // do nothing
                    }
                }
            }
        }
    }

    fun amIActive(turnStarted: TurnStarted) =
        turnStarted.matchSnapshot.players.find { it.username == username }?.isActive ?: false
}
