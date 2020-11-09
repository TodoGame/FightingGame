package com.somegame.match.matchmaking

import org.slf4j.LoggerFactory
import user.User
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

class Matchmaker {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MATCH_SIZE = 2
    }

    private val waitingUsers = ConcurrentLinkedDeque<User>()
    private val waitingUsersCount = AtomicInteger(0)

    fun join(user: User): List<User>? {
        if (user in waitingUsers) {
            throw UserAlreadyWaiting(user)
        }
        return if (waitingUsersCount.get() >= MATCH_SIZE - 1) {
            makeMatch(user)
        } else {
            waitingUsers.add(user)
            waitingUsersCount.incrementAndGet()
            logger.info("User $user added to matchmaking queue")
            null
        }
    }

    fun leave(user: User) {
        waitingUsers.removeIf { it.username == user.username }
    }

    private fun makeMatch(with: User): List<User>? {
        if (waitingUsers.size < MATCH_SIZE - 1) {
            return null
        }
        val users = mutableListOf(with)
        for (i in 0 until MATCH_SIZE - 1) {
            users.add(waitingUsers.removeFirst())
            waitingUsersCount.decrementAndGet()
        }
        logger.info("Found match with users $users")
        return users
    }

    class UserAlreadyWaiting(user: User) :
        IllegalArgumentException("User ${user.username} is has already joined matchmaking")
}
