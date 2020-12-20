package user

import faculty.FacultyData
import item.ItemData
import kotlinx.serialization.Serializable

const val USER_MAX_USERNAME_LENGTH = 16
const val USER_MAX_NAME_LENGTH = 30

@Serializable
data class UserData(
    override val username: String,
    override val name: String,
    val inventory: List<ItemData>,
    val money: Int,
    val faculty: FacultyData,
) : User
