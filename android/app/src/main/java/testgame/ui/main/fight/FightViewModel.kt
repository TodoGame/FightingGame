package testgame.ui.main.fight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import match.MatchSnapshot
import match.Message
import match.PlayerAction
import testgame.data.GameApp
import testgame.data.Match
import testgame.data.MatchPlayer
import testgame.network.NetworkService
import testgame.network.MatchApi
import java.lang.NullPointerException
import java.net.SocketTimeoutException

class FightViewModel(val app: GameApp, val token: String) : ViewModel() {

    private val match = Match

    val matchWinner: String
        get() = match.winner ?: ""

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /* Block for
     * Shared
     * data and functions */
    private var _toastInfo = MutableLiveData<String>()
    val toastInfo: LiveData<String>
        get() = _toastInfo

    private val _infoDisplayIsCalled = MutableLiveData(false)
    val infoDisplayIsCalled: LiveData<Boolean>
        get() = _infoDisplayIsCalled

    private fun callInfo(message: String) {
        _toastInfo.postValue(message)
        _infoDisplayIsCalled.value = true
    }

    private val _matchState = MutableLiveData(Match.State.SEARCHING)
    val matchState: LiveData<Match.State>
        get() = _matchState

    fun onErrorDisplayed() {
        _infoDisplayIsCalled.value = false
    }

    /* Block for
     * LocationsFragment
     * data and functions */
    private var _chosenLocation = MutableLiveData<String>()
    val chosenLocation: LiveData<String>
        get() = _chosenLocation

    @KtorExperimentalAPI
    fun findMatch(location: String) {
        _chosenLocation.value = location
        coroutineScope.launch {
            try {
                val webSocketTicket = MatchApi.getWebSocketTicket(app.user.authenticationToken)
                callInfo("Got ticket")
                match.state = Match.State.SEARCHING
                _matchState.postValue(Match.State.SEARCHING)
                MatchApi.connectMatchWebSocket(
                        match,
                        webSocketTicket,
                        ::onMatchStart,
                        ::onTurnStart,
                        ::onPlayerAction,
                        ::onMatchEnd
                )
            } catch (exception: NullPointerException) {
                callInfo("Null Pointer exception")
            } catch (exception: SocketTimeoutException) {
                exception.message?.let { callInfo(it) }
            }
        }
    }

    private fun onMatchStart(players: Set<String>) {
        match.state = Match.State.STARTED
        _matchState.value = Match.State.STARTED
        callInfo("Match started")
    }

    private fun onTurnStart(matchSnapshot: MatchSnapshot) {
        println("Turn started ${matchSnapshot.players}")
        val players = matchSnapshot.players
        val playerSnapshot = players.find { it.username == app.user.username }
                ?: throw GameApp.NullAppDataException("Null playerSnapshot")
        val enemySnapshot = players.find { it.username != app.user.username }
                ?: throw GameApp.NullAppDataException("Null enemySnapshot")
        if (match.player == null || match.enemy == null) {
            match.player = MatchPlayer(playerSnapshot.username, playerSnapshot.health, playerSnapshot.health)
            match.enemy = MatchPlayer(enemySnapshot.username, enemySnapshot.health, enemySnapshot.health)
        } else {
            match.player?.let { it.currentHealth = playerSnapshot.health }
            match.enemy?.let { it.currentHealth = enemySnapshot.health }
        }
        if (playerSnapshot.isActive) {
            match.state = Match.State.MY_TURN
            _matchState.value = Match.State.MY_TURN
        } else {
            match.state = Match.State.ENEMY_TURN
            _matchState.value = Match.State.ENEMY_TURN
        }
    }

    private fun onPlayerAction(attackerUsername: String, targetUsername: String) {
        println("OnPlayerAction Turn started $attackerUsername $targetUsername")
        val attacker = match.findPlayerByUsername(attackerUsername)
        val target = match.findPlayerByUsername(targetUsername)
        if (attackerUsername == match.player?.username) {
            _attackingPlayer.value = AttackingPlayer.PLAYER
        } else {
            _attackingPlayer.value = AttackingPlayer.ENEMY
        }
        target.currentHealth -= GameApp.PLAYER_ACTION_DAMAGE
        _action.value = "${attacker.username} hit ${target.username} \n " +
                "Hitted ${GameApp.PLAYER_ACTION_DAMAGE} health"
        println("Action value: ${action.value}")
        println("Attacking player: ${attackingPlayer.value}")
    }

    private fun onMatchEnd(winner: String) {
        println("Match ended")
        match.state = Match.State.NO_MATCH
        _matchState.value = Match.State.NO_MATCH
        match.winner = winner
    }

    fun confirmMatchEntrance() {
        _chosenLocation.value = null
    }

    /**Block for
     * FightFragment
     * data and functions */
    val time = "0:35"
    private val _action = MutableLiveData("")
    val action: LiveData<String>
        get() = _action

    private var _currentOption = MutableLiveData(FightMenuOption.ATTACK)
    val currentFightMenuOption: LiveData<FightMenuOption>
        get() = _currentOption

    private var _playerWantToEscape = MutableLiveData(false)
    val playerWantToEscape: LiveData<Boolean>
        get() = _playerWantToEscape

    private var _attackingPlayer = MutableLiveData(AttackingPlayer.IDLE)
    val attackingPlayer: LiveData<AttackingPlayer>
        get() = _attackingPlayer

    enum class FightMenuOption() {
        ATTACK,
        INVENTORY,
        SKILLS
    }

    enum class AttackingPlayer() {
        IDLE,
        PLAYER,
        ENEMY
    }

    fun attackWithPrimaryWeapon() {
        try {
            val enemyUsername = match.enemy!!.username
            val action = NetworkService.jsonFormat.encodeToString<Message>(
                    PlayerAction(enemyUsername, app.user.username)
            )
            coroutineScope.launch {
                match.webSocketSession?.send(action)
                        ?: throw GameApp.NullAppDataException("Null match webSocketSession")
            }
        } catch (exception: NullPointerException) {
            callInfo("Null pointer exception")
        }
    }

    fun useThingFromInventory() {

    }

    fun defend() {

    }

    fun selectAttackOption() {
        _currentOption.postValue(FightMenuOption.ATTACK)
    }

    fun selectInventoryOption() {
        _currentOption.postValue(FightMenuOption.INVENTORY)
    }

    fun selectSkillOption() {
        _currentOption.postValue(FightMenuOption.SKILLS)
    }

    fun escape() {
        _playerWantToEscape.postValue(true)
    }

    fun confirmMatchEscape() {
        _playerWantToEscape.postValue(false)
        refreshMatch()
    }

    fun confirmMatchRoomExit() {
        refreshMatch()
    }

    private fun refreshMatch() {
        coroutineScope.launch {
            match.webSocketSession?.close()
        }
        match.winner = null
        match.state = Match.State.NO_MATCH
        match.enemy = null
        match.player = null
        match.webSocketSession = null
    }
}
