package match

import kotlinx.serialization.Serializable
import user.Username

@Serializable
sealed class Message

@Serializable
sealed class PlayerDecision : Message()

@Serializable
sealed class CalculatedPlayerDecision : Message()

@Serializable
data class MatchStarted(val players: Set<String>) : Message()

@Serializable
data class MatchEnded(val winner: String) : Message()

@Serializable
data class PlayerAction(val target: String, val attacker: String, val itemId: Int? = null) : PlayerDecision()

@Serializable
data class SkipTurn(val isDefenced: Boolean = true) : PlayerDecision()

@Serializable
data class CalculatedPlayerAction(
    val target: String,
    val attacker: String,
    val itemId: Int?,
    val damage: Int,
) : CalculatedPlayerDecision()

@Serializable
data class CalculatedSkipTurn(val username: Username, val isDefenced: Boolean) : CalculatedPlayerDecision()

@Serializable
data class TurnStarted(val matchSnapshot: MatchSnapshot) : Message()
