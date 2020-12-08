package testgame.ui.main.fight.customEvent

import androidx.lifecycle.Observer

class CustomStringObserver(private val onEventUnhandledContent: (String) -> Unit) : Observer<String> {
    override fun onChanged(testString: String) {
        onEventUnhandledContent(testString)
    }
}