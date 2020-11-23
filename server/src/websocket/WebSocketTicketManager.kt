package com.somegame.websocket

import com.somegame.security.UnauthorizedException
import com.somegame.security.UserPrincipal
import com.somegame.security.UserSource
import com.somegame.user.UserEntity
import org.slf4j.LoggerFactory
import user.Username
import websocket.WebSocketTicket
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

class WebSocketTicketManager(
    private val webSocketName: String,
    private val maxTicketsPerUser: Int,
    private val ticketLifeExpectancyMillis: Long = DEFAULT_TICKET_LIFE_EXPECTANCY
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val TICKET_CODE_LENGTH = 30
        const val DEFAULT_TICKET_LIFE_EXPECTANCY: Long = 60 * 1000
    }

    private val tickets = ConcurrentHashMap<Username, MutableList<WebSocketTicket>>()

    private fun clearTickets(username: Username) {
        tickets[username]?.removeIf { isTicketExpired(it) }
        logger.info("Cleared $username's tickets")
    }

    fun makeTicket(userPrincipal: UserPrincipal): WebSocketTicket {
        if (maxTicketsPerUser != -1 && countTickets(userPrincipal) >= maxTicketsPerUser) {
            throw MaxNumberOfTicketsReachedException()
        }
        val username = userPrincipal.username
        clearTickets(username)
        val code = getRandomString(TICKET_CODE_LENGTH)
        val expiresAt = getTokenExpiration()
        val ticket = WebSocketTicket(webSocketName, username, expiresAt, code)
        registerTicket(ticket)
        return ticket
    }

    // TODO: may cause multithreading issues
    private fun registerTicket(ticket: WebSocketTicket) {
        val username = ticket.username
        val thisUserTickets = tickets[username]
        if (thisUserTickets == null) {
            tickets[username] = mutableListOf(ticket)
        } else {
            thisUserTickets.add(ticket)
        }
    }

    private fun unregisterTicket(ticket: WebSocketTicket) {
        tickets[ticket.username]?.remove(ticket)
    }

    fun authorize(ticket: WebSocketTicket): UserEntity {
        validateTicket(ticket)
        unregisterTicket(ticket)
        return UserSource.findUserByUsername(ticket.username) ?: throw InvalidTicketException("User not found")
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

    private fun validateTicket(ticket: WebSocketTicket) {
        if (isTicketExpired(ticket)) {
            throw InvalidTicketException("Ticket is expired")
        }
        if (!isTicketRegistered(ticket)) {
            throw InvalidTicketException("Ticket is not registered")
        }
    }

    private fun isTicketExpired(ticket: WebSocketTicket) = ticket.expiresAt <= System.currentTimeMillis()

    private fun isTicketRegistered(ticket: WebSocketTicket) = tickets[ticket.username]?.contains(ticket) ?: false

    private fun countTickets(userPrincipal: UserPrincipal) = tickets[userPrincipal.username]?.size ?: 0

    class InvalidTicketException(msg: String) : UnauthorizedException(msg)

    class MaxNumberOfTicketsReachedException : IllegalStateException()
}
