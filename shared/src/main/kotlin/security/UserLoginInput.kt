package security

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginInput(val username: String, val password: String)
