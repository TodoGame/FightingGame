package testgame.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.*
import match.MatchSnapshot
import match.PlayerAction
import match.PlayerSnapshot
import java.lang.NullPointerException
import java.util.concurrent.atomic.AtomicInteger

object Match : ViewModel() {
    var player = MutableLiveData<PlayerSnapshot>()
    var playerUsername = MutableLiveData<String>()
    var playerMaxHealth: Int? = null
    var playerHealthPrettyString = MutableLiveData<String>()
    var enemy = MutableLiveData<PlayerSnapshot>()
    var enemyUsername = MutableLiveData<String>()
    var enemyMaxHealth: Int? = null
    var enemyHealthPrettyString = MutableLiveData<String>()
    var winner = MutableLiveData<String>()
    var state = State.NO_MATCH

    enum class State {
        NO_MATCH,
        SEARCHING,
        STARTED,
        MY_TURN,
        ENEMY_TURN,
    }

    var webSocketSession: WebSocketSession? = null

    fun findPlayerByUsername(username: String) : PlayerSnapshot {
        try {
            if (username == player.value!!.username) {
                return player.value!!
            }
            return enemy.value!!
        } catch (exception: NullPointerException) {
            throw GameApp.NullAppDataException("Uninitialized players")
        }
    }

    fun updateDataFromSnapshots(playerSnapshot: PlayerSnapshot, enemySnapshot: PlayerSnapshot) {
        if (player.value == null) {
            playerMaxHealth = playerSnapshot.health
        }
        if (enemy.value == null) {
            enemyMaxHealth = enemySnapshot.health
        }
        player.value = (playerSnapshot)
        playerUsername.value = playerSnapshot.username
        enemy.value = (enemySnapshot)
        enemyUsername.value = enemySnapshot.username
        updatePlayerPrettyHealthPoints()
        updateEnemyPrettyHealthPoints()
    }
    private fun updatePlayerPrettyHealthPoints() {
        playerHealthPrettyString.value = "${player.value?.health}/$playerMaxHealth" ?: ""
    }
    private fun updateEnemyPrettyHealthPoints() {
        enemyHealthPrettyString.value = "${enemy.value?.health}/$enemyMaxHealth" ?: ""
    }
}