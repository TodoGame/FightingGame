package testgame.ui.main.home

import android.database.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val facultyScore = ObservableField("0")
    private val _text = MutableLiveData("This is home Fragment")
    val text: LiveData<String> = _text

    private var _username = MutableLiveData("GeneralBum")
    val username: LiveData<String>
        get() = _username

    private var _level = MutableLiveData("5")
    val level: LiveData<String>
        get() = _level

    private var _faculty = MutableLiveData("MathMech")
    val faculty: LiveData<String>
        get() = _faculty

    private var _errorString = MutableLiveData("5")
    val errorString: LiveData<String>
        get() = _errorString

    private val _errorIsCalled = MutableLiveData(false)
    val errorIsCalled: LiveData<Boolean>
        get() = _errorIsCalled

    private fun callError(message: String) {
        _errorString.postValue(message)
        _errorIsCalled.postValue(true)
    }

    fun onErrorDisplayed() {
        _errorIsCalled.value = false
    }

    private val _testIsCalled = MutableLiveData(false)
    val testIsCalled: LiveData<Boolean>
        get() = _testIsCalled

    fun test() {
        _testIsCalled.postValue(true)
    }
}
