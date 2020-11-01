package security

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordInput(
    val oldPassword: String,
    val newPassword: String
)
