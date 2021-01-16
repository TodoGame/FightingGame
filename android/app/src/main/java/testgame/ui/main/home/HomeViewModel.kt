package testgame.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import faculty.FacultyData
import faculty.FixedFaculties
import io.ktor.util.*
import kotlinx.coroutines.*
import testgame.data.GameApp
import testgame.data.User
import testgame.network.MainApi
import testgame.network.NetworkService
import testgame.ui.main.featuresNews.NewsItem
import timber.log.Timber
import user.Username
import java.lang.NullPointerException
import java.net.SocketException
import java.net.SocketTimeoutException

class HomeViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val userMessage = MutableLiveData<String>()

    private val _facultyScore = MutableLiveData<Int>()
    val facultyScore: LiveData<Int>
        get() = _facultyScore

    private val _leadingFaculty = MutableLiveData<FacultyData>()
    val leadingFaculty: LiveData<FacultyData>
        get() = _leadingFaculty

    private var _username = MutableLiveData<String>()
    val username: LiveData<String>
        get() = _username

    private var _level = MutableLiveData<String>()
    val level: LiveData<String>
        get() = _level

    private var _faculty = MutableLiveData<String>()
    val faculty: LiveData<String>
        get() = _faculty

    private var _newsItems = MutableLiveData(mutableListOf<NewsItem>())
    val newsItems: LiveData<MutableList<NewsItem>>
        get() = _newsItems

    private val _testProgress = MutableLiveData(100)
    val testProgress: LiveData<Int>
        get() = _testProgress

    @KtorExperimentalAPI
    fun getUserData() {
        val networkFunction: suspend () -> Unit = {
            val userData = MainApi.getPlayerData(User.authenticationToken)
            User.updateFromUserData(userData)
        }
        coroutineScope.launch {
            GameApp().executeSafeNetworkCall(networkFunction, coroutineScope, userMessage)
        }
    }

    @KtorExperimentalAPI
    fun getLeadingFacultyData() {
        val networkFunction: suspend () -> Unit = {
            val facultyData = MainApi.getLeadingFacultyData(User.authenticationToken)
            _leadingFaculty.postValue(facultyData)
        }
        coroutineScope.launch {
            GameApp().executeSafeNetworkCall(networkFunction, coroutineScope, userMessage)
        }
//        try {
//            coroutineScope.launch {
//                val facultyData = MainApi.getLeadingFacultyData(User.authenticationToken)
//                _leadingFaculty.postValue(facultyData)
//            }
//        } catch (exception: NetworkService.NetworkException) {
//            Timber.i(exception)
//        } catch (exception: NullPointerException) {
//            Timber.i("Some data missed")
//        }
    }

    @KtorExperimentalAPI
    fun makeSubscriptions() {
        val networkFunction: suspend () -> Unit = {
            val webSocketTicket = MainApi.getWebSocketTicket(User.authenticationToken)
            Timber.i("Got ticket")
            MainApi.connectToMainWebSocket(
                    webSocketTicket,
                    ::onUserMoneyUpdate,
                    ::onLeadingFacultyUpdate,
                    ::onFacultiesPointsUpdate,
            )
            User.username.value?.let { MainApi.subscribeUser(it, true) }
            MainApi.subscribeLeadingFaculty(true)
            MainApi.subscribeFacultyPoints(true)
        }
        coroutineScope.launch {
            GameApp().executeSafeNetworkCall(networkFunction, coroutineScope, userMessage)
        }
    }

    private fun onUserMoneyUpdate(username: Username, money: Int) {
        if (username == User.username.value) {
            User.money.value = money
        }
    }

    private fun onLeadingFacultyUpdate(facultyId: Int, points: Int) {
        try {
            val leadingFaculty = FixedFaculties.values().find { it.id == facultyId }!!
            _leadingFaculty.value = FacultyData(facultyId, leadingFaculty.facultyName, points)
        } catch (e: NullPointerException) {
            Timber.i("Unknown faculty id")
        }
    }

    private fun onFacultiesPointsUpdate(facultyId: Int, points: Int, winnerUsername: String) {
        val faculty = FixedFaculties.values().find { it.id == facultyId }
        _newsItems.value?.add(NewsItem(facultyId, "$winnerUsername won $points for $faculty faculty"))
    }
}
