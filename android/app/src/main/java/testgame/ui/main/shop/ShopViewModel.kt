package testgame.ui.main.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.util.*
import item.ItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import testgame.data.GameApp
import testgame.data.User
import testgame.network.NetworkService
import testgame.network.ShopApi
import java.lang.NullPointerException

class ShopViewModel : ViewModel() {

    val app = GameApp()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private var _itemsToBuy = MutableLiveData<List<ItemData>>()
    val itemsToBuy: LiveData<List<ItemData>>
        get() = _itemsToBuy

    private var _errorString = MutableLiveData<String>()
    val errorString: LiveData<String>
        get() = _errorString

    private var _shopItems = MutableLiveData<List<ItemData>>()
    val shopItems: LiveData<List<ItemData>>
        get() = _shopItems

    @KtorExperimentalAPI
    fun getAllNotOwnedItems() {
        try {
            coroutineScope.launch {
                val items = ShopApi.getAllNotOwnedItems(User.authenticationToken)
                _shopItems.postValue(items)
            }
        } catch (exception: NetworkService.NetworkException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }

    @KtorExperimentalAPI
    fun buyItem(itemId: Int) {
        try {
            coroutineScope.launch {
                User.updateFromUserData(ShopApi.buyItem(User.authenticationToken, itemId))
                getAllNotOwnedItems()
            }
        } catch (exception: NetworkService.NetworkException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }
}
