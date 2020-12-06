package subscription

import kotlinx.serialization.Serializable
import user.Username

@Serializable
sealed class SubscriptionMessage {
    abstract val subscribe: Boolean
}

@Serializable
data class UserMoneyUpdateSubscription(val username: Username, override val subscribe: Boolean = true) :
    SubscriptionMessage()

@Serializable
sealed class SubscriptionEvent

@Serializable
data class UserMoneyUpdate(val username: Username, val money: Int) : SubscriptionEvent()
