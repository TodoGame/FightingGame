package testgame.ui.main.home

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import testgame.data.GameApp
import testgame.data.Match
import testgame.network.MainApi
import testgame.network.MatchApi
import testgame.network.NetworkService
import testgame.ui.main.featuresNews.NewsItem
import user.Username
import java.lang.NullPointerException
import java.net.SocketTimeoutException

class HomeViewModel : ViewModel() {

    val app = GameApp()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _facultyScore = MutableLiveData(500)
    val facultyScore: LiveData<Int>
        get() = _facultyScore

    private val _text = MutableLiveData("This is home Fragment")
    val text: LiveData<String> = _text

    private var _username = MutableLiveData("GeneralBum")
    val username: LiveData<String>
        get() = _username

    private var _level = MutableLiveData<String>()
    val level: LiveData<String>
        get() = _level

    private var _faculty = MutableLiveData("MathMech")
    val faculty: LiveData<String>
        get() = _faculty

    private var _errorString = MutableLiveData<String>()
    val errorString: LiveData<String>
        get() = _errorString

    private var _newsItems = MutableLiveData(listOf(
            NewsItem(1, "Somebody killed somebody1"),
            NewsItem(2, "Somebody killed somebody2"),
            NewsItem(3, "Somebody killed somebody3"),
            NewsItem(4, "Somebody killed somebody4"),
            NewsItem(5, "Somebody killed somebody5"),
    ))
    val newsItems: LiveData<List<NewsItem>>
        get() = _newsItems

    private val _testIsCalled = MutableLiveData(false)
    val testIsCalled: LiveData<Boolean>
        get() = _testIsCalled

    @KtorExperimentalAPI
    fun getUserData() {
        try {
            coroutineScope.launch {
                val userData = MainApi.getPlayerData(app.user.authenticationToken)
                app.user.userData = userData
            }
        } catch (exception: NetworkService.ConnectionException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }

    @KtorExperimentalAPI
    fun getLeadingFacultyData() {
        try {
            coroutineScope.launch {
                val facultyData = MainApi.getLeadingFacultyData(app.user.authenticationToken)
                TODO()
            }
        } catch (exception: NetworkService.ConnectionException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }

    @KtorExperimentalAPI
    fun makeSubscriptions() {
        coroutineScope.launch {
            try {
                val webSocketTicket = MainApi.getWebSocketTicket(app.user.authenticationToken)
                _errorString.postValue("Got ticket")
                MainApi.connectToMainWebSocket(
                        webSocketTicket,
                        ::onUserMoneyUpdate,
                        ::onLeadingFacultyUpdate,
                        ::onFacultiesPointsUpdate,
                )
//                MainApi.subscribeUser()
//                MainApi.subscribeLeadingFaculty()
//                MainApi.subscribeFacultyPoints()
            } catch (exception: NetworkService.NoResponseException) {
                exception.message?.let { _errorString.postValue(it) }
            } catch (exception: NullPointerException) {
                _errorString.postValue("Null Pointer exception")
            } catch (exception: MainApi.NullWebSocketSessionException) {
                _errorString.postValue(exception.message)
            } catch (exception: SocketTimeoutException) {
                exception.message?.let { _errorString.postValue(it) }
            }
        }
    }

    private fun onUserMoneyUpdate(username: Username, money: Int) {

    }

    private fun onLeadingFacultyUpdate(facultyId: Int, points: Int) {

    }

    private fun onFacultiesPointsUpdate(facultyId: Int, points: Int, winnerUsername: String) {

    }

    fun test() {
        _testIsCalled.postValue(true)
    }
}
