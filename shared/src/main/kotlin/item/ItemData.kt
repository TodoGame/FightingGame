package item

import kotlinx.serialization.Serializable

@Serializable
enum class ItemType {
    MainWeapon,
    Additional
}

@Serializable
data class ItemData(val id: Int, val type: ItemType, val name: String, val price: Int, val damage: Int)
