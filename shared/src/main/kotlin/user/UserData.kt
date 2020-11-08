package user

import kotlinx.serialization.Serializable

@Serializable
data class UserData(override val username: String, override val name: String) : User
