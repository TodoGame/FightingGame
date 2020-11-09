package testgame.ui.main.fight

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class FightViewModel : ViewModel() {
    val username = ObservableField<String>("")
    val password = ObservableField<String>("")

    private var _isMatchStarted = MutableLiveData<Boolean>()
    val isMatchStarted: LiveData<Boolean>
        get() = _isMatchStarted

    private var _activePlayerId = MutableLiveData<Int>()
    val activePlayerId: LiveData<Int>
        get() = _activePlayerId

    private var _currentOption = MutableLiveData<Option>()
    val currentOption: LiveData<Option>
        get() = _currentOption

    private var _errorString = String()
    val errorString: String
        get() = _errorString

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

    enum class Option() {
        ATTACK,
        INVENTORY,
        DEFEND,
        SKILLS
    }

//    @KtorExperimentalAPI
    fun findMatch() {
        _isMatchStarted.value = true
        GlobalScope.launch {
            try {
//                MatchApi.findMatch()
            } catch (exception: Exception) {
            }
        }
    }

    fun selectFightOption() {
        if (_currentOption.value == Option.ATTACK) {
            _currentOption.value = Option.INVENTORY
        } else {
            _currentOption.value = Option.ATTACK
        }
    }

    fun confirmRoomEntrance() {
        _isMatchStarted.value = false
    }

    private fun callError(message: String) {
        _errorString = message
        _errorIsCalled.value = true
    }

    fun onErrorDisplayed() {
        _errorIsCalled.value = false
    }
}
