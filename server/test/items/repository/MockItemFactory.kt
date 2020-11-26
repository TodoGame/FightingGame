package items.repository

import com.somegame.items.Item
import io.mockk.every
import io.mockk.mockk
import item.ItemData
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.jupiter.api.Assertions.*

object MockItemFactory {
    fun create(id: Int, name: String, price: Int): Item {
        val item = mockk<Item>()

        every { item.id.value } returns id
        every { item.name } returns name
        every {item.price} returns price
        every {item.getPublicData()} returns ItemData(id, name, price)

        return item
    }
}
