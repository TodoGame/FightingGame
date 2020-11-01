package security

import kotlinx.serialization.Serializable

@Serializable
data class UserProperty(
        val username: String,
        val name: String
)
