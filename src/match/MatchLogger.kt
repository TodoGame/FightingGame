package com.somegame.match

import com.somegame.match.matchmaking.Match
import com.somegame.match.matchmaking.Player
import com.somegame.match.messages.Message
import com.somegame.match.messages.PlayerAction
import com.somegame.user.User
import kotlinx.serialization.SerializationException
import match.websocketuser.WebSocketClient

object MatchLogger {
    fun logUserConnect(user: User) {
        println("$user connected")
    }

    fun logClientRegister(client: WebSocketClient) {
        println("$client registered")
    }

    fun logClientClear(username: String) {
        println("Client $username cleared")
    }

    fun logUserAddedToQueue(username: String) {
        println("$username has been added to matchmaking queue")
    }

    fun logPlayerRegistered(player: Player) {
        println("$player registered")
    }

    fun logPlayerCleared(username: String) {
        println("Player $username cleared")
    }

    fun logPlayerAddedToMatch(player: Player, match: Match) {
        println("$player added to $match")
    }

    fun logMatchStart(match: Match, activePlayer: Player) {
        println("Match $match started. Active player: $activePlayer")
    }

    fun logTurnStart(snapshot: Match.MatchSnapshot) {
        println("Turn started with current $snapshot")
    }

    fun logMessageSent(client: WebSocketClient, message: Message) {
        println("Sent to $client message $message")
    }

    fun logActionReceive(player: Player, action: PlayerAction) {
        println("Action $action received from $player")
    }

    fun logCouldNotParseFrame(e: SerializationException) {
        println("Could not parsed incoming frame: $e")
    }

    fun logIllegalAction(e: Match.IllegalAction) {
        println("Received illegal action at current state $e")
    }
}
