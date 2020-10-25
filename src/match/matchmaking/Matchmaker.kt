package com.somegame.match.matchmaking

import com.somegame.match.MatchLogger
import com.somegame.match.websocketuser.WebSocketClientService
import match.websocketuser.WebSocketClient

object Matchmaker {
    const val MATCH_SIZE = 2

    private val waitingUsers = ArrayDeque<String>()

    fun findMatch(client: WebSocketClient): Match? {
        val matchUsernames = join(client.username)
        return if (matchUsernames != null) {
            makeMatch(matchUsernames)
        } else {
            null
        }
    }

    private fun makeMatch(usernames: List<String>): Match {
        val clients = usernames.map { WebSocketClientService.getClient(it) }
        val match = Match()
        for (client in clients) {
            PlayerService.makePlayer(client, match)
        }
        return match
    }

    private fun join(username: String): List<String>? {
        if (username in waitingUsers) {
            throw UserAlreadyWaiting(username)
        }
        return if (waitingUsers.size >= MATCH_SIZE - 1) {
            makeMatch(username)
        } else {
            waitingUsers.add(username)
            MatchLogger.logUserAddedToQueue(username)
            null
        }
    }

    fun leave(client: WebSocketClient) {
        waitingUsers.remove(client.username)
    }

    private fun makeMatch(with: String): List<String>? {
        if (waitingUsers.size < MATCH_SIZE - 1) {
            return null
        }
        val users = mutableListOf(with)
        for (i in 0 until MATCH_SIZE - 1) {
            users.add(waitingUsers.removeFirst())
        }
        return users
    }

    class UserAlreadyWaiting(username: String) :
        IllegalArgumentException("User $username is has already joined matchmaking")
}
