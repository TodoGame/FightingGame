package match

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import user.Username

class SimplePlayerThatAlwaysHits(
    private val username: Username,
    private val opponentUsername: Username,
    private val log: MutableList<Message>,
    private val incoming: ReceiveChannel<Frame>,
    private val outgoing: SendChannel<Frame>
) {

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
                hitBack()
            }
        }
    }

    private suspend fun hitBack() {
        sendMessage(PlayerAction(opponentUsername, username))
    }

    private suspend fun sendMessage(message: Message) {
        val string = Json.encodeToString(message)
        outgoing.send(Frame.Text(string))
    }
}
