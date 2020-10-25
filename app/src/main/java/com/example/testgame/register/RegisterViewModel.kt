package com.example.testgame.register

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testgame.network.SecurityApi
import com.example.testgame.network.securityService.RegisterData
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

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

//    private var viewModelJob = Job()
//    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

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
            val registerDeferred = SecurityApi.RETROFIT_SERVICE.register(
                RegisterData(
                    username.get()!!,
                    password.get()!!,
                    user.get()!!
                )
            )
            try {
                val answer = registerDeferred.await()
                if (answer.isSuccessful) {
                    _signUpCompleted.value = true
                }
            } catch (exception: NullPointerException) {
                _errorIsCalled.value = true
            } catch (exception: Exception) {
                _errorIsCalled.value = true
            }
        }
    }

    fun onSignUpConfirm() {
        _signUpCompleted.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("UserCreateVModel", "View model destroyed")
    }
}