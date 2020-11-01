package security

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginCredentials(
    val username: String,
    val password: String
)