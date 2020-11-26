package com.somegame.websocket

import com.somegame.security.UnauthorizedException
import com.somegame.security.UserPrincipal
import com.somegame.user.User
import com.somegame.user.service.UserService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import websocket.WebSocketTicket
import java.lang.IllegalStateException

class WebSocketTicketManager(
    private val webSocketName: String,
    private val ticketLifeExpectancyMillis: Long = DEFAULT_TICKET_LIFE_EXPECTANCY
) : KoinComponent {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val userService: UserService by inject()

    companion object {
        private const val TICKET_CODE_LENGTH = 30
        const val DEFAULT_TICKET_LIFE_EXPECTANCY: Long = 60 * 1000
    }

    private val mutex = Mutex()
    // ticket code -> WebSocketTicket
    private val tickets = mutableMapOf<String, WebSocketTicket>()

    private suspend fun clearExpiredTickets() = mutex.withLock {
        val expiredTicketCodes = tickets.values.filter { isTicketExpired(it) }.map { it.code }

        for (code in expiredTicketCodes) {
            tickets.remove(code)
        }
        logger.info("Cleared ${expiredTicketCodes.size} expired tickets")
    }

    suspend fun makeTicket(userPrincipal: UserPrincipal): WebSocketTicket {
        val username = userPrincipal.username
        clearExpiredTickets()
        val code = getRandomString(TICKET_CODE_LENGTH)
        val expiresAt = getTokenExpiration()
        val ticket = WebSocketTicket(webSocketName, username, expiresAt, code)
        registerTicket(ticket)
        return ticket
    }

    private suspend fun registerTicket(ticket: WebSocketTicket) = mutex.withLock {
        tickets[ticket.code] = ticket
        logger.info("Ticket $ticket registered")
    }

    private suspend fun unregisterTicket(ticket: WebSocketTicket) = mutex.withLock {
        tickets.remove(ticket.code)
        logger.info("Ticket $ticket unregistered")
    }

    suspend fun authorize(ticket: WebSocketTicket): User {
        validateTicket(ticket)
        unregisterTicket(ticket)
        return userService.findUserByUsername(ticket.username) ?: throw InvalidTicketException("User not found")
    }

    private fun getTokenExpiration(): Long {
        return System.currentTimeMillis() + ticketLifeExpectancyMillis
    }

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private suspend fun validateTicket(ticket: WebSocketTicket) {
        if (isTicketExpired(ticket)) {
            throw InvalidTicketException("Ticket is expired")
        }
        if (!isTicketRegistered(ticket)) {
            throw InvalidTicketException("Ticket is not registered")
        }
    }

    private fun isTicketExpired(ticket: WebSocketTicket) = ticket.expiresAt <= System.currentTimeMillis()

    private suspend fun isTicketRegistered(ticket: WebSocketTicket) = mutex.withLock {
        tickets[ticket.code] == ticket
    }

    class InvalidTicketException(msg: String) : UnauthorizedException(msg)

    class MaxNumberOfTicketsReachedException : IllegalStateException()
}
