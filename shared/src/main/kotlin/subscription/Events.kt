package subscription

import kotlinx.serialization.Serializable
import user.Username

/**
 * The message that clients sends over the subscriptions websocket when they wish to update their subscription status
 * Client can subscribe to events by sending this message with the `subscribe` property set to true
 * Client can unsubscribe by sending this message with the `subscribe` property set to false
 *
 * Client must serialize every message as *SubscriptionMessage*, for instance
 *   val string = Json.encodeToString<SubscriptionMessage>(LeadingFacultySubscription())
 *
 * @property subscribe if true the server will subscribe the client to the corresponding event, will unsubscribe otherwise
 */
@Serializable
sealed class SubscriptionMessage {
    abstract val subscribe: Boolean
}

/**
 * Get notified every time users money updates
 * Receive UserMoneyUpdate
 */
@Serializable
data class UserMoneyUpdateSubscription(val username: Username, override val subscribe: Boolean = true) :
    SubscriptionMessage()

/**
 * Get notified every time leading faculty changes (some faculty takes over) or leading faculty's points update
 * Receive LeadingFacultyUpdate
 */
@Serializable
data class LeadingFacultySubscription(override val subscribe: Boolean = true) : SubscriptionMessage()

/**
 * Get notified every time any faculty's points are updated
 * Receive FacultyPointsUpdate
 */
@Serializable
data class AllFacultiesPointsSubscription(override val subscribe: Boolean = true) : SubscriptionMessage()

/**
 * Messages that the server sends every time something is updated
 */
@Serializable
sealed class SubscriptionUpdate

@Serializable
data class UserMoneyUpdate(val username: Username, val money: Int) : SubscriptionUpdate()

@Serializable
data class LeadingFacultyUpdate(val facultyId: Int, val points: Int) : SubscriptionUpdate()

@Serializable
data class FacultyPointsUpdate(val facultyId: Int, val points: Int, val winnerUsername: Username) : SubscriptionUpdate()
