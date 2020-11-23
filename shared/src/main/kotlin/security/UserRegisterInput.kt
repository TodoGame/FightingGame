package security

import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterInput(val username: String, val password: String, val name: String)
