package com.example.testgame.ui.entrance.register

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testgame.network.securityService.SecurityApi
import security.RegisterData
import security.UserProperty
import io.ktor.http.*
import kotlinx.coroutines.*
import java.lang.NullPointerException

class RegisterViewModel : ViewModel() {

    val username = ObservableField<String>("")
    val password = ObservableField<String>("")
    val user = ObservableField<String>("")

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

    private lateinit var _userResponseString: UserProperty
    val userResponseString: UserProperty
        get() = _userResponseString

    private var _errorString = String()
    val errorString: String
        get() = _errorString

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

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
        GlobalScope.launch {
            try {
                val response = SecurityApi.register(
                    RegisterData(
                        username.get()!!,
                        password.get()!!,
                        user.get()!!
                    )
                )
                if (SecurityApi.responseIsSuccessful(response)) {
                    val token = response.headers[SecurityApi.AUTHORIZATION_HEADER_NAME]
                    if (token != null) {
                        _signUpCompleted.value = true
                    } else {
                        callError("Wrong token response")
                    }
                } else if (response.status == HttpStatusCode.BadRequest) {
                    callError("Bad request")
                } else if (response.status == HttpStatusCode.Unauthorized) {
                    callError("Unauthorized")
                }
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
        _errorIsCalled.value = true
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("UserCreateVModel", "View model destroyed")
    }
}