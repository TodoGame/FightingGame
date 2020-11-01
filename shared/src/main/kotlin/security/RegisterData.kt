package security

import kotlinx.serialization.Serializable

@Serializable
data class RegisterData(
    val username: String,
    val password: String,
    val name: String
)