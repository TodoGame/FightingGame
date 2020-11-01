package match

import kotlinx.serialization.Serializable

@Serializable
sealed class Message

@Serializable
data class MatchStarted(val players: List<String>) : Message()

@Serializable
data class MatchEnded(val winner: String) : Message()

@Serializable
data class PlayerAction(val target: String, val attacker: String) : Message()

@Serializable
data class TurnStarted(val matchSnapshot: MatchSnapshot) : Message()