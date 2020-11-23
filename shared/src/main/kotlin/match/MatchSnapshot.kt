package match

import kotlinx.serialization.Serializable

@Serializable
data class MatchSnapshot(val players: Set<PlayerSnapshot>)
