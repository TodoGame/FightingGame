package testgame.ui.main.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import testgame.data.GameApp
import testgame.network.MainApi
import testgame.network.NetworkService
import testgame.network.ShopApi
import testgame.ui.main.featuresInventory.InventoryItem
import testgame.ui.main.featuresShop.ShopItem
import java.lang.NullPointerException

class ShopViewModel : ViewModel() {

    val app = GameApp()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private var _itemsToBuy = MutableLiveData<List<ShopItem>>()
    val itemsToBuy: LiveData<List<ShopItem>>
        get() = _itemsToBuy

    private var _errorString = MutableLiveData("5")
    val errorString: LiveData<String>
        get() = _errorString

    private var _inventoryItems = MutableLiveData(listOf(
            InventoryItem(1, "Club"),
            InventoryItem(2, "Sword"),
            InventoryItem(3, "Banana"),
            InventoryItem(4, "Dice"),
    ))
    val inventoryItems: LiveData<List<InventoryItem>>
        get() = _inventoryItems

    private var _shopItems = MutableLiveData(listOf(
            ShopItem(1, "Club", 40),
            ShopItem(2, "Sword", 30),
            ShopItem(3, "Banana", 20),
            ShopItem(4, "Dice", 5),
    ))
    val shopItems: LiveData<List<ShopItem>>
        get() = _shopItems

    @KtorExperimentalAPI
    fun getAllNotOwnedItems() {
        try {
            coroutineScope.launch {
                val items = MainApi.getLeadingFacultyData(app.user.authenticationToken)
                TODO()
            }
        } catch (exception: NetworkService.ConnectionException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }

    @KtorExperimentalAPI
    fun buyItem(itemId: Int) {
        try {
            coroutineScope.launch {
                val newUserData = ShopApi.buyItem(app.user.authenticationToken, itemId)
                getAllNotOwnedItems()
                TODO()
            }
        } catch (exception: NetworkService.ConnectionException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }
}
