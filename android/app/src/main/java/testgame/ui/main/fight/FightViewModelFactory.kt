package testgame.ui.main.fight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import testgame.data.GameApp
import testgame.data.Match

class FightViewModelFactory(private val app: GameApp, private val token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FightViewModel::class.java)) {
            return FightViewModel(app, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}