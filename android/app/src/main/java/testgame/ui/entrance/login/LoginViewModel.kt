package com.example.testgame.ui.entrance.login

import android.util.Log
import androidx.databinding.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testgame.network.securityService.LoginData
import com.example.testgame.network.securityService.SecurityApi
import com.example.testgame.network.securityService.UserProperty
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.NullPointerException

class LoginViewModel : ViewModel() {

    val username = ObservableField<String>("")
    val password = ObservableField<String>("")

    private val _usernameInputErrorHint = MutableLiveData<String>()
    val usernameInputErrorHint: LiveData<String>
        get() = _usernameInputErrorHint

    private val _passwordInputErrorHint = MutableLiveData<String>()
    val passwordInputErrorHint: LiveData<String>
        get() = _passwordInputErrorHint

    private var _token = String()
    val token: String
        get() = _token

    private lateinit var _user: UserProperty
    val user: UserProperty
        get() = _user

    private val _loginCompleted = MutableLiveData<Boolean>(false)
    val loginCompleted: LiveData<Boolean>
        get() = _loginCompleted

    private val _signUpCalled = MutableLiveData<Boolean>(false)
    val signUpCalled: LiveData<Boolean>
        get() = _signUpCalled

    private var _errorString = String()
    val errorString: String
        get() = _errorString

    private var _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

    fun logIn() {
        if (username.get() == "" || password.get() == "") {
            if (username.get() == "") {
                println("Empty username called")
                _usernameInputErrorHint.value = "Please enter the username"
            }
            if (password.get() == "") {
                println("Empty password called")
                _passwordInputErrorHint.value = "Please enter the password"
            }
            return
        }
        GlobalScope.launch {
            try {
                val response = SecurityApi.login(
                    LoginData(
                        username.get()!!,
                        password.get()!!
                    )
                )
                if (SecurityApi.responseIsSuccessful(response)) {
                    val token = response.headers[SecurityApi.AUTHORIZATION_HEADER_NAME]
                    if (token != null) {
                        _user = Json.decodeFromString<UserProperty>(response.readText())
                        _token = token
                        _loginCompleted.value = true
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

    fun signUp() {
        _signUpCalled.value = true
    }

    private fun callError(message: String) {
        _errorString = message
        _errorIsCalled.value = true
    }

    fun onSignUpConfirm() {
        _signUpCalled.value = false
    }

    fun onLoginConfirm() {
        _loginCompleted.value = false
    }

    fun onErrorDisplayed() {
        _errorIsCalled.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("LoginVModel", "View model destroyed")
    }
}
