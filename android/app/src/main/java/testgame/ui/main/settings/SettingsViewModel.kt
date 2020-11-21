package testgame.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _isLogOutPressed = MutableLiveData<Boolean>(false)
    val isLogOutPressed: LiveData<Boolean>
        get() = _isLogOutPressed

    fun logOut() {
        _isLogOutPressed.value = true
    }

    fun onLogOutConfirmed() {
        _isLogOutPressed.value = false
    }
}
