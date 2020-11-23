package testgame.ui.main.clicker

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClickerViewModel : ViewModel() {

    val score = ObservableField("0")

    private var _isImageClicked = MutableLiveData<Boolean>(false)
    val isImageClicked: LiveData<Boolean>
        get() = _isImageClicked

    private var _isStartActivityClicked = MutableLiveData<Boolean>(false)
    val isStartActivityClicked: LiveData<Boolean>
        get() = _isStartActivityClicked

    fun increaseScore() {
        _isImageClicked.value = true
        score.set(score.get()?.toInt()?.plus(1).toString())
    }

    fun onImageClicked() {
        _isImageClicked.value = false
    }

    fun onStartMainActivityClicked() {
        _isStartActivityClicked.value = true
    }

    fun onMainActivityMoved() {
        _isStartActivityClicked.value = false
    }
}