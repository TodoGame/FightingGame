package testgame.ui.main.fight.customEvent

import androidx.lifecycle.Observer
import kotlinx.serialization.Serializable

class FightEventObserver(private val onEventUnhandledContent: (String) -> Unit) : Observer<String> {
    override fun onChanged(event: String) {
        onEventUnhandledContent(event)
    }
}
