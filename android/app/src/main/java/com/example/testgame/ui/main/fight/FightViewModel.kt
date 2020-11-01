package com.example.testgame.ui.main.fight

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testgame.network.matchService.MatchApi
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

    private var _errorString = String()
    val errorString: String
        get() = _errorString

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

    enum class Location(locationName: String) {
        MM("MatMech"),
        PM_PU("PM_PU"),
        Chem("Chemistry"),
        Phys("Physics")
    }

    @KtorExperimentalAPI
    fun findMatch(location: String) {
        GlobalScope.launch {
            try {
//                MatchApi.findMatch()
            } catch (exception: Exception) {

            }
        }
    }

    fun onRoomEntranceConfirm() {
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