package user

import item.ItemData
import kotlinx.serialization.Serializable

@Serializable
data class UserData(override val username: String, override val name: String, val inventory: List<ItemData>) : User
