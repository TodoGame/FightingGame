package testgame.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import faculty.FacultyData
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import testgame.data.GameApp
import testgame.data.User
import testgame.network.MainApi
import testgame.network.NetworkService
import testgame.ui.main.featuresNews.NewsItem
import user.Username
import java.lang.NullPointerException
import java.net.SocketTimeoutException

class HomeViewModel : ViewModel() {

    val app = GameApp()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

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

    private var _errorString = MutableLiveData<String>()
    val errorString: LiveData<String>
        get() = _errorString

    private var _newsItems = MutableLiveData<List<NewsItem>>()
    val newsItems: LiveData<List<NewsItem>>
        get() = _newsItems

    private val _testProgress = MutableLiveData(100)
    val testProgress: LiveData<Int>
        get() = _testProgress

    @KtorExperimentalAPI
    fun getUserData() {
        try {
            coroutineScope.launch {
                val userData = MainApi.getPlayerData(User.authenticationToken)
                User.updateFromUserData(userData)
            }
        } catch (exception: NetworkService.NetworkException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }

    @KtorExperimentalAPI
    fun getLeadingFacultyData() {
        try {
            coroutineScope.launch {
                val facultyData = MainApi.getLeadingFacultyData(User.authenticationToken)
                _leadingFaculty.postValue(facultyData)
            }
        } catch (exception: NetworkService.NetworkException) {
            exception.message?.let { _errorString.postValue(it) }
        } catch (exception: NullPointerException) {
            _errorString.postValue("Some data missed")
        }
    }

    @KtorExperimentalAPI
    fun makeSubscriptions() {
        coroutineScope.launch {
            try {
                val webSocketTicket = MainApi.getWebSocketTicket(User.authenticationToken)
                _errorString.postValue("Got ticket")
                MainApi.connectToMainWebSocket(
                        webSocketTicket,
                        ::onUserMoneyUpdate,
                        ::onLeadingFacultyUpdate,
                        ::onFacultiesPointsUpdate,
                )
                User.username.value?.let { MainApi.subscribeUser(it, true) }
                MainApi.subscribeLeadingFaculty(true)
                MainApi.subscribeFacultyPoints(true)
            } catch (exception: NetworkService.NetworkException) {
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
        _testProgress.postValue(_testProgress.value?.minus(20))
    }
}
