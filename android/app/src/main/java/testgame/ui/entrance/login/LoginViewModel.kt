package testgame.ui.entrance.login

import androidx.databinding.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import testgame.network.SecurityApi
import io.ktor.client.statement.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import testgame.network.NetworkService
import user.UserData
import java.lang.NullPointerException

class LoginViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val username = ObservableField("")
    val password = ObservableField("")

    private val _usernameInputErrorHint = MutableLiveData<String>()
    val usernameInputErrorHint: LiveData<String>
        get() = _usernameInputErrorHint

    private val _passwordInputErrorHint = MutableLiveData<String>()
    val passwordInputErrorHint: LiveData<String>
        get() = _passwordInputErrorHint

    private var _token = String()
    val token: String
        get() = _token

    private lateinit var _user: UserData
    val user: UserData
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

    @KtorExperimentalAPI
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
        coroutineScope.launch {
            try {
                val response = SecurityApi.login(
                        security.UserLoginInput(
                                username.get()!!,
                                password.get()!!
                        )
                )
                val token = response.headers[NetworkService.AUTHORIZATION_HEADER_NAME]
                if (token != null) {
                    _user = NetworkService.jsonFormat.decodeFromString(response.readText())
                    _token = token
                    _loginCompleted.postValue(true)
                } else {
                    callError("Wrong token response")
                }
            } catch (exception: NetworkService.NetworkException) {
                exception.message?.let { callError(it) }
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
        _errorIsCalled.postValue(true)
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
}
