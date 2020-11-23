package match

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import user.Username

class SimplePlayer(
    private val username: Username,
    private val opponentUsername: Username,
    private val log: MutableList<Message>,
    private val incoming: ReceiveChannel<Frame>,
    private val outgoing: SendChannel<Frame>
) {
    var health = 15

    suspend fun start() {
        for (frame in incoming) {
            if (frame is Frame.Text) {
                val message = Json.decodeFromString<Message>(frame.readText())
                handleMessage(message)
            }
        }
    }

    private suspend fun handleMessage(message: Message) {
        log.add(message)
        when (message) {
            is TurnStarted -> {
                val activePlayer = message.matchSnapshot.players.find { it.isActive }
                if (activePlayer?.username == username) {
                    hitBack()
                }
            }
            is PlayerAction -> {
                if (message.target == username) {
                    health -= 10
                }
            }
        }
    }

    private suspend fun hitBack() {
        if (health > 0) {
            sendMessage(PlayerAction(opponentUsername, username))
        }
    }

    private suspend fun sendMessage(message: Message) {
        val string = Json.encodeToString(message)
        outgoing.send(Frame.Text(string))
    }
}
