package testgame.ui.main.fight

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import item.ItemData
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import match.*
import testgame.data.*
import testgame.network.NetworkService
import testgame.network.MatchApi
import timber.log.Timber
import java.lang.NullPointerException
import java.net.SocketTimeoutException
import java.util.concurrent.CountDownLatch

class FightViewModel(val token: String) : ViewModel() {

    private val match = Match()

    val matchWinner: String
        get() = match.winner.value ?: ""

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _matchState = MutableLiveData(Match.State.SEARCHING)
    val matchState: LiveData<Match.State>
        get() = _matchState

    /** Block for
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
                val webSocketTicket = MatchApi.getWebSocketTicket(User.authenticationToken)
                Timber.i("Got ticket")
                _matchState.postValue(Match.State.SEARCHING)
                MatchApi.connectToMatchWebSocket(
                        webSocketTicket,
                        ::onMatchStart,
                        ::onTurnStart,
                        ::onPlayerAction,
                        ::onMatchEnd
                )
            } catch (exception: NetworkService.NetworkException) {
                exception.message?.let { Timber.i(it) }
            } catch (exception: NullPointerException) {
                Timber.i("Null Pointer exception")
            } catch (exception: SocketTimeoutException) {
                exception.message?.let { Timber.i(it) }
            }
        }
    }

    private fun onMatchStart(players: Set<String>) {
        _matchState.value = Match.State.STARTED
        Timber.i("Match started. Players: $players")
    }

    private fun onTurnStart(matchSnapshot: MatchSnapshot) {
        Timber.i("TurnStarted")
        val players = matchSnapshot.players
        val playerSnapshot = players.find { it.username == User.username.value }
                ?: throw GameApp.NullAppDataException("Null playerSnapshot. Players: $players. Username: ${User.username.value}")
        val enemySnapshot = players.find { it.username != User.username.value }
                ?: throw GameApp.NullAppDataException("Null enemySnapshot")
        match.updateDataFromSnapshots(playerSnapshot, enemySnapshot)
        if (playerSnapshot.isActive) {
            _matchState.value = Match.State.MY_TURN
        } else {
            _matchState.value = Match.State.ENEMY_TURN
        }
    }

    private fun onPlayerAction(message: CalculatedPlayerDecision) {
        when (message) {
            is CalculatedPlayerAction -> {
                val weapon = message.itemId?.let { GameApp().getItemNameById(it) }
                val attacker = match.findPlayerByUsername(message.attacker)
                val target = match.findPlayerByUsername(message.target)
                if (message.attacker == match.player.value?.username) {
                    _fightAction.value = FightAction.PLAYER_ATTACK
                } else {
                    _fightAction.value = FightAction.ENEMY_ATTACK
                }
                target.health -= message.damage
                if (target == attacker) {
                    _action.postValue("${attacker.username} healed himself \n " +
                            "${-message.damage} health with banana")
                } else {
                    _action.postValue("${attacker.username} hit ${target.username} \n " +
                            "${message.damage} health with $weapon")
                }
            }
            is CalculatedSkipTurn -> {
                _action.postValue("${message.username} skipped the turn" + if (message.isDefenced) "Defending" else "")
            }
        }
    }

    private fun onMatchEnd(winner: String) {
        _matchState.value = Match.State.NO_MATCH
        match.winner.value = winner
        Timber.i("Match ended. Winner : $winner")
    }

    fun confirmMatchEntrance() {
        _chosenLocation.value = null
    }

    /**Block for
     * FightFragment
     * data and functions */

    companion object {
        // These represent different important times
        // This is when the game is over
        const val DONE = 0L
        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 10L
        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        // This is the total time of the game
        const val COUNTDOWN_TIME = 10000L

        val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
        val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
        val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
        val NO_BUZZ_PATTERN = longArrayOf(0)

    }

    private val timer: CountDownTimer

    init {
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _turnTime.value = (millisUntilFinished / ONE_SECOND)
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }
            override fun onFinish() {
                _turnTime.value = DONE
                _eventBuzz.value = BuzzType.GAME_OVER
//                skipTurn()
            }
        }
        timer.start()
    }

    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    private val _turnTime = MutableLiveData<Long>()
    val turnTime: LiveData<Long>
        get() = _turnTime

    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz

    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }

    fun onCorrect() {
        _eventBuzz.value = BuzzType.CORRECT
    }

    private val _action = MutableLiveData("There will be game actions")
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

    enum class FightMenuOption() {
        ATTACK,
        INVENTORY,
        SKILLS
    }

    fun attack(itemId: Int? = null) {
        try {
            val enemyUsername = if (itemId == 3) User.username.value!! else match.enemy.value!!.username
            User.username.value!!.let {
                val action = NetworkService.jsonFormat.encodeToString<Message>(
                        PlayerAction(enemyUsername, it, itemId)
                )
                coroutineScope.launch {
                    User.matchSession?.send(action)
                            ?: throw GameApp.NullAppDataException("Null match webSocketSession")
                }
            }
            _fightAction.postValue(FightAction.PLAYER_ATTACK)
        } catch (exception: NullPointerException) {
            Timber.i("Null pointer exception when attacking")
        }
    }

    fun attackWithPrimaryButton() {
        val primaryWeaponId = if (User.primaryWeapon.value == null) {
            null
        } else {
            User.primaryWeapon.value!!.id
        }
        attack(primaryWeaponId)
    }

    fun skipTurn(isDefend: Boolean) {
        try {
            User.username.value!!.let {
                val action = NetworkService.jsonFormat.encodeToString<Message>(
                        SkipTurn(isDefend)
                )
                coroutineScope.launch {
                    User.matchSession?.send(action)
                            ?: throw GameApp.NullAppDataException("Null match webSocketSession")
                }
            }
            _fightAction.postValue(FightAction.PLAYER_ATTACK)
        } catch (exception: NullPointerException) {
            Timber.i("Null pointer exception when attacking")
        }
    }

    fun onActionHandled() {
        _fightAction.postValue(null)
    }

    fun selectOption(optionId: Int) {
        _currentOption.postValue(when (optionId) {
            1 -> FightMenuOption.ATTACK
            2 -> FightMenuOption.INVENTORY
            3 -> FightMenuOption.SKILLS
            else -> throw IllegalArgumentException("Wrong fight option")
        })
    }

    fun escape() {
        _playerWantToEscape.postValue(true)
    }

    fun confirmMatchEscape() {
        _playerWantToEscape.postValue(false)
        refreshMatch()
    }

    private fun refreshMatch() {
        coroutineScope.launch {
            User.matchSession?.close()
            User.matchSession = null
        }
        match.winner.value = null
        if (_matchState.value != Match.State.NO_MATCH) {
            _matchState.value = Match.State.NO_MATCH
        }
        match.enemy.value = null
        match.player.value = null
    }
}
