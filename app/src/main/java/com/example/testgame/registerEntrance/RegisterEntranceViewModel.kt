package com.example.testgame.registerEntrance

import android.util.Log
import androidx.databinding.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testgame.network.SecurityApi
import com.example.testgame.network.securityService.LoginData
import com.example.testgame.registerEntrance.validator.InputResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.NullPointerException

class RegisterEntranceViewModel : ViewModel() {

    val username = ObservableField<String>("")
    val password = ObservableField<String>("")

    private val _usernameInputErrorHint = MutableLiveData<String>()
    val usernameInputErrorHint: LiveData<String>
        get() = _usernameInputErrorHint

    private val _passwordInputErrorHint = MutableLiveData<String>()
    val passwordInputErrorHint: LiveData<String>
        get() = _passwordInputErrorHint

    private val _loginCompleted = MutableLiveData<Boolean>(false)
    val loginCompleted: LiveData<Boolean>
        get() = _loginCompleted

    private val _signUpCalled = MutableLiveData<Boolean>(false)
    val signUpCalled: LiveData<Boolean>
        get() = _signUpCalled

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

//    private var viewModelJob = Job()
//    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    fun logIn() {
        if (username.get() == "" || password.get() == "") {
            if (username.get() == "") {
                _usernameInputErrorHint.value = "Please enter the username"
            }
            if (password.get() == "") {
                _passwordInputErrorHint.value = "Please enter the password"
            }
            return
        }
        GlobalScope.launch {
            val loginDeferred = SecurityApi.RETROFIT_SERVICE.login(
                LoginData(
                    username.get()!!,
                    password.get()!!
                )
            )
            try {
                val answer = loginDeferred.await()
                if (answer.isSuccessful) {
                    val token = answer.headers().get("Authorization")
                    TODO("Save token to the cache")
                    _loginCompleted.value = true
                }
            } catch (exception: NullPointerException) {
                _errorIsCalled.value = true
            } catch (exception: Exception) {
                _errorIsCalled.value = true
            }
        }
    }

    fun signUp() {
        _passwordInputErrorHint.value = ""
        _usernameInputErrorHint.value = ""
        _usernameInputErrorHint.value = ""
        _passwordInputErrorHint.value = ""
        _signUpCalled.value = true
    }

    fun onSignUpConfirm() {
        _signUpCalled.value = false
    }

    fun onLoginConfirm() {
        _loginCompleted.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("RegisterEntranceVModel", "View model destroyed")
    }
}