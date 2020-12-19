package com.somegame.match.matchmaking

import com.somegame.RepositoriesMock
import com.somegame.match.MatchRouting
import com.somegame.match.player.Player
import io.mockk.*
import match.Message
import match.PlayerDecision
import user.Username

class MockSimpleClient(repositoriesMock: RepositoriesMock, val username: Username) {
    var player = slot<Player>()
    var client = mockk<MatchRouting.MatchClient>()
    val log = mutableListOf<Message>()

    init {
        val user = repositoriesMock.makeNewTestUser(username)
        every { client.username } returns username
        every { client.onJoinMatch(capture(player)) } just Runs
        every { client.user } returns user
        coEvery { client.kick(any()) } just Runs
        coEvery { client.sendMessage(capture(log)) } just Runs
    }

    suspend fun makeDecision(playerDecision: PlayerDecision) {
        player.captured.makeDecision(playerDecision)
    }
}
