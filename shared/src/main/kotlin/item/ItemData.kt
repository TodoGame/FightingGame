package item

import kotlinx.serialization.Serializable

@Serializable
data class ItemData(val id: Int, val name: String, val price: Int)
