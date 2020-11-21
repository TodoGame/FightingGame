package testgame.ui.main.fight

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import match.Message
import testgame.data.GameApp
import testgame.network.NetworkService
import testgame.network.matchService.MatchApi
import java.lang.Exception
import java.lang.NullPointerException
import java.net.SocketTimeoutException

class FightViewModel(val app: GameApp, val token: String) : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    //shared
    private var _errorString = MutableLiveData<String>()
    val errorString: LiveData<String>
        get() = _errorString

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

    private fun callError(message: String) {
        _errorString.postValue(message)
        _errorIsCalled.postValue(true)
    }

    fun onErrorDisplayed() {
        _errorIsCalled.value = false
    }

    //locations
    private var _chosenLocation = MutableLiveData<String>()
    val chosenLocation: LiveData<String>
        get() = _chosenLocation

    private var _isMatchStarted = MutableLiveData<Boolean>(false)
    val isMatchStarted: LiveData<Boolean>
        get() = _isMatchStarted

    private var _isMatchEnded = MutableLiveData<Boolean>(false)
    val isMatchEnded: LiveData<Boolean>
        get() = _isMatchEnded

    @KtorExperimentalAPI
    fun findMatch(location: String) {
        _chosenLocation.value = location
        coroutineScope.launch {
            try {
                val webSocketTicket = MatchApi.getWebSocketTicket(app.user.authenticationToken)
                callError("Got ticket")
                MatchApi.connectMatchWebSocket(
                        app,
                        webSocketTicket,
                        ::onMatchStarted,
                        ::onMatchEnded
                )
                callError("Connected to match")
            } catch (exception: NullPointerException) {
                callError("Null Pointer exception")
            } catch (exception: SocketTimeoutException) {
                exception.message?.let { callError(it) }
            }
        }
    }

    fun onMatchStarted() {
        callError("Match started")
        _isMatchStarted.postValue(true)
    }

    suspend fun onMatchEnded(winner: String) {
        app.match.webSocketSession?.close() ?: throw GameApp.NullAppDataException("Null match webSocketSession")
        if (winner == app.user.username) {

        } else {

        }
        _isMatchEnded.postValue(true)
    }

    fun confirmRoomEntrance() {
        _chosenLocation.value = null
        _isMatchStarted.value = false
    }

    fun confirmRoomExit() {
        _isMatchEnded.value = true
    }

    //fight
    val time = "0:35"
    val action = ObservableField<String>("Action: ")

    private var _activePlayerId = MutableLiveData<Int>()
    val activePlayerId: LiveData<Int>
        get() = _activePlayerId

    private var _currentOption = MutableLiveData<Option>()
    val currentOption: LiveData<Option>
        get() = _currentOption

    enum class Option() {
        ATTACK,
        INVENTORY,
        DEFEND,
        SKILLS
    }

    suspend fun attackWithPrimaryWeapon() {
        val turnSnapshot = app.match.currentSnapshot
        try {
            if (turnSnapshot != null) {
                val enemy = app.match.currentSnapshot!!.players.find { it.username != app.user.username }?.username
                        ?: ""
                val action = NetworkService.jsonFormat.encodeToString<Message>(
                        match.PlayerAction(enemy, app.user.username)
                )
                app.match.webSocketSession?.send(action) ?: throw GameApp.NullAppDataException("Null match webSocketSession")
            }
        } catch (exception: NullPointerException) {
            callError("Null pointer exception")
        }
    }

    fun useThingFromInventory() {

    }

    fun selectAttackOption() {
        _currentOption.value = Option.ATTACK
    }

    fun selectInventoryOption() {
        _currentOption.value = Option.INVENTORY
    }
}
