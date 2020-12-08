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
import testgame.data.FightAction
import testgame.data.GameApp
import testgame.data.Match
import testgame.data.MatchPlayer
import testgame.network.NetworkService
import testgame.network.MatchApi
import testgame.ui.main.inventory.InventoryItem
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
    private var _logInfo = MutableLiveData<String>()
    val logInfo: LiveData<String>
        get() = _logInfo

    private fun callInfo(message: String) {
        _logInfo.postValue(message)
    }

    private val _matchState = MutableLiveData(Match.State.SEARCHING)
    val matchState: LiveData<Match.State>
        get() = _matchState

    /* Block for
     * LocationsFragment
     * data and functions */
    private var _chosenLocation = MutableLiveData<String>()
    val chosenLocation: LiveData<String>
        get() = _chosenLocation

    @KtorExperimentalAPI
    fun findMatch(location: String) {
        _chosenLocation.postValue(location)
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
            } catch (exception: NetworkService.NoResponseException) {
                exception.message?.let { callInfo(it) }
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
        _logInfo.postValue("TurnStarted")
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
            _matchState.value = Match.State.MY_TURN
        } else {
            _matchState.value = Match.State.ENEMY_TURN
        }
    }

    private fun onPlayerAction(attackerUsername: String, targetUsername: String) {
        val attacker = match.findPlayerByUsername(attackerUsername)
        val target = match.findPlayerByUsername(targetUsername)
        if (attackerUsername == match.player?.username) {
            _fightAction.value = FightAction.PLAYER_ATTACK
        } else {
            _fightAction.value = FightAction.ENEMY_ATTACK
        }
        target.currentHealth -= GameApp.PLAYER_ACTION_DAMAGE
        _action.postValue("${attacker.username} hit ${target.username} \n " +
                "${GameApp.PLAYER_ACTION_DAMAGE} health")
    }

    private fun onMatchEnd(winner: String) {
        match.state = Match.State.NO_MATCH
        _matchState.value = Match.State.NO_MATCH
        match.winner = winner
        callInfo("Match ended. Winner : $winner")
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

    private var _fightAction = MutableLiveData<FightAction>()
    val fightAction: LiveData<FightAction>
        get() = _fightAction

    private var _inventoryItems = MutableLiveData(listOf(
            InventoryItem("1", "Bubble"),
            InventoryItem("2", "Aid kit"),
            InventoryItem("3", "Gun"),
            InventoryItem("4", "Banana"),
            InventoryItem("5", "Card"),
    ))
    val inventoryItems: LiveData<List<InventoryItem>>
        get() = _inventoryItems

    enum class FightMenuOption() {
        ATTACK,
        INVENTORY,
        SKILLS
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
            _fightAction.postValue(FightAction.PLAYER_ATTACK)
        } catch (exception: NullPointerException) {
            callInfo("Null pointer exception")
        }
    }

    fun useThingFromInventory() {

    }

    fun defend() {

    }

    fun onActionHandled() {
        _fightAction.postValue(null)
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
