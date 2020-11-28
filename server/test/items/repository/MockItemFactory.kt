package items.repository

import com.somegame.items.Item
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*

object MockItemFactory {
    fun create(id: Int, name: String, price: Int): Item {
        val item = mockk<Item>()

        every { item.id.value } returns id
        every { item.name } returns name
        every { item.price } returns price

        return item
    }
}
