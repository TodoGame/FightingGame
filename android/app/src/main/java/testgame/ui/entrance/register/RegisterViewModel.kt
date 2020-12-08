package testgame.ui.entrance.register

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import testgame.network.SecurityApi
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import testgame.data.FacultyOption
import testgame.network.NetworkService
import java.lang.NullPointerException

class RegisterViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )

    val username = ObservableField("")
    val password = ObservableField("")
    val user = ObservableField("")

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

    private val _facultyOption = MutableLiveData<FacultyOption>()
    val facultyOption: LiveData<FacultyOption>
        get() = _facultyOption

    private val _signUpCompleted = MutableLiveData(false)
    val signUpCompleted: LiveData<Boolean>
        get() = _signUpCompleted

    fun chooseFacultyOption(option: FacultyOption) {
        _facultyOption.value = option
    }

    @KtorExperimentalAPI
    fun signUp() {
        if (username.get() == "" || password.get() == "" || user.get() == "" || _facultyOption.value == null) {
            if (username.get() == "") {
                _usernameInputErrorHint.value = "Please enter the username"
            }
            if (password.get() == "") {
                _passwordInputErrorHint.value = "Please enter the password"
            }
            if (user.get() == "") {
                _userInputErrorHint.value = "Please enter the user"
            }
            if (_facultyOption.value == null) {
                callError("Choose your faculty")
            }
            return
        }
        coroutineScope.launch {
            try {
                val response = SecurityApi.register(
                        security.UserRegisterInput(
                                username.get()!!,
                                password.get()!!,
                                user.get()!!,
                                facultyOption.value?.faultyId!!
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
