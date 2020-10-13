package com.example.testgame.registerEntrance

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testgame.network.MarsApi
import com.example.testgame.registerEntrance.validator.InputResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegisterEntranceViewModel : ViewModel() {

    companion object {
    }

    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    private val _loginCompleted = MutableLiveData<Boolean>(false)
    val loginCompleted: LiveData<Boolean>
        get() = _loginCompleted

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    fun logIn(username: String, password: String): InputResponse {
        if (username == "" && password == "") {
            return InputResponse.EMPTY_USERNAME_AND_PASSWORD
        }
        if (username == "") {
            return  InputResponse.EMPTY_USERNAME
        }
        if (password == "") {
            return InputResponse.EMPTY_PASSWORD
        }
        coroutineScope.launch {
            val loginDeferred = MarsApi.retrofitService.login()
            try {
                val answer = loginDeferred.await()
                _response.value = "Success: ${answer} Mars properties retrieved"
            } catch (e: Exception) {
                _response.value = "Failure: ${e.message}"
            }
        }
        return InputResponse.SUCCESS
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("GameViewModel", "View model destroyed")
    }
}