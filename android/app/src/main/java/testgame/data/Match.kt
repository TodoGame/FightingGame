package testgame.data

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.*
import match.MatchSnapshot
import match.PlayerAction
import java.lang.NullPointerException
import java.util.concurrent.atomic.AtomicInteger

class Match {
    var player: MatchPlayer? = null
    var enemy: MatchPlayer? = null
    var winner: String? = null
    var state = State.NO_MATCH

    enum class State {
        NO_MATCH,
        SEARCHING,
        STARTED,
        MY_TURN,
        ENEMY_TURN,
    }

    var webSocketSession: WebSocketSession? = null

    fun findPlayerByUsername(username: String) : MatchPlayer {
        try {
            if (username == player!!.username) {
                return player!!
            }
            return enemy!!
        } catch (exception: NullPointerException) {
            throw GameApp.NullAppDataException("Uninitialized players")
        }
    }
}