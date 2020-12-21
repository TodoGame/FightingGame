package testgame.data

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import faculty.FacultyData
import io.ktor.http.cio.websocket.*
import item.ItemData
import user.Username

object User : ViewModel() {
    var username = MutableLiveData<Username>()
    var name = MutableLiveData<String>()
    var faculty =  MutableLiveData<FacultyData>()
    var inventory = MutableLiveData<List<ItemData>>()
    var primaryWeapon = MutableLiveData<ItemData>()
    var money = MutableLiveData<Int>()
    lateinit var authenticationToken: String
    var matchSession: WebSocketSession? = null

    fun updateFromUserData(data: user.UserData) {
        this.inventory.postValue(data.inventory)
        this.money.postValue(data.money)
    }
}
