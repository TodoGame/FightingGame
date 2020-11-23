package testgame.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>("This is home Fragment")
    val text: LiveData<String> = _text

    private var _username = MutableLiveData<String>("GeneralBum")
    val username: LiveData<String>
        get() = _username

    private var _level = MutableLiveData<String>("5")
    val level: LiveData<String>
        get() = _level

    private var _faculty = MutableLiveData<String>("MathMech")
    val faculty: LiveData<String>
        get() = _faculty

    private var _errorString = MutableLiveData<String>("5")
    val errorString: LiveData<String>
        get() = _errorString

    private val _errorIsCalled = MutableLiveData<Boolean>(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

    private fun callError(message: String) {
        _errorString.postValue(message)
        _errorIsCalled.postValue(true)
    }

    fun onErrorDisplayed() {
        _errorIsCalled.value = false
    }
}
