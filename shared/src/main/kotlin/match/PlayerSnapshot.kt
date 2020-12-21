package match

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSnapshot(val username: String, val isActive: Boolean, var health: Int)
