package testgame.ui.main.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import testgame.ui.main.inventory.InventoryItem
import testgame.ui.main.shop.features.ShopItem

class ShopViewModel : ViewModel() {
    private var _itemsToBuy = MutableLiveData<List<ShopItem>>()
    val itemsToBuy: LiveData<List<ShopItem>>
        get() = _itemsToBuy

    private var _inventoryItems = MutableLiveData(listOf(
            InventoryItem("id1", "Bubble"),
            InventoryItem("id2", "Aid kit"),
            InventoryItem("id3", "Gun"),
            InventoryItem("id4", "Banana"),
            InventoryItem("id5", "Card"),
    ))
    val inventoryItems: LiveData<List<InventoryItem>>
        get() = _inventoryItems


    fun onErrorDisplayed() {
    }
}
