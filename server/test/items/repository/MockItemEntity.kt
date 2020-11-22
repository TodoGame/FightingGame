package items.repository

import com.somegame.items.repository.ItemEntity
import item.ItemData
import org.junit.jupiter.api.Assertions.*

class MockItemEntity(private val id: Int, override val name: String, override val price: Int) : ItemEntity {
    override fun getId() = id

    fun getPublicData() = ItemData(id, name, price)
}
