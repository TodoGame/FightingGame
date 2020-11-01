package match.websocketuser

import com.somegame.match.MatchLogger
import com.somegame.match.matchmaking.Matchmaker
import com.somegame.match.websocketuser.WebSocketClientService
import user.User
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import match.Message

class WebSocketClient(val user: User, private val session: WebSocketServerSession) {
    val username = user.username

    suspend fun sendMessage(message: Message) {
        MatchLogger.logMessageSent(this, message)
        val string = Json.encodeToString(message)
        session.send(Frame.Text(string))
    }

    suspend fun disconnect() {
        session.close(CloseReason(CloseReason.Codes.NORMAL, "Match ended"))
    }

    fun handleDisconnect() {
        WebSocketClientService.disconnect(user)
        Matchmaker.leave(this)
    }

    override fun toString() = "Client(username=$username)"
}