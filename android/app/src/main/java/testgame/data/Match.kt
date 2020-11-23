package testgame.data

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import match.MatchSnapshot
import match.PlayerAction

object Match {
    var players: Set<String>? = null
    var webSocketSession: DefaultClientWebSocketSession? = null
    var currentSnapshot: MatchSnapshot? = null
    var lastPlayerAction: PlayerAction? = null
}