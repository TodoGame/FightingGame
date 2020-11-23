package com.example.testgame.ui.entrance.register

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import testgame.network.SecurityApi
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import testgame.network.NetworkService
import java.lang.NullPointerException

class RegisterViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    val username = ObservableField<String>("")
    val password = ObservableField<String>("")
    val user = ObservableField<String>("")

    private var _errorString = String()
    val errorString: String
        get() = _errorString

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

    private val _usernameInputErrorHint = MutableLiveData<String>()
    val usernameInputErrorHint: LiveData<String>
        get() = _usernameInputErrorHint

    private val _passwordInputErrorHint = MutableLiveData<String>()
    val passwordInputErrorHint: LiveData<String>
        get() = _passwordInputErrorHint

    private val _userInputErrorHint = MutableLiveData<String>()
    val userInputErrorHint: LiveData<String>
        get() = _userInputErrorHint

    private val _signUpCompleted = MutableLiveData<Boolean>(false)
    val signUpCompleted: LiveData<Boolean>
        get() = _signUpCompleted

    @KtorExperimentalAPI
    fun signUp() {
        if (username.get() == "" || password.get() == "" || user.get() == "") {
            if (username.get() == "") {
                _usernameInputErrorHint.value = "Please enter the username"
            }
            if (password.get() == "") {
                _passwordInputErrorHint.value = "Please enter the password"
            }
            if (user.get() == "") {
                _userInputErrorHint.value = "Please enter the user"
            }
            return
        }
        coroutineScope.launch {
            try {
                val response = SecurityApi.register(
                        security.UserRegisterInput(
                                username.get()!!,
                                password.get()!!,
                                user.get()!!
                        )
                )
                val token = response.headers[NetworkService.AUTHORIZATION_HEADER_NAME]
                if (token != null) {
                    _signUpCompleted.postValue(true)
                } else {
                    callError("Wrong token response")
                }
            } catch (exception: NetworkService.ConnectionException) {
                exception.message?.let { callError(it) }
            } catch (exception: NullPointerException) {
                callError("Some data missed")
            }
        }
    }

    fun onSignUpConfirm() {
        _signUpCompleted.value = false
    }

    private fun callError(message: String) {
        _errorString = message
        _errorIsCalled.postValue(true)
    }
}
