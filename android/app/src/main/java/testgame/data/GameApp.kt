package testgame.data

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.example.testgame.R
import item.ItemData
import item.ItemType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import testgame.network.MainApi
import testgame.network.NetworkService
import timber.log.Timber
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*

class GameApp : MultiDexApplication() {

    companion object {
        const val ATTACK_ANIMATION_PLAY_DELAY: Long = 1200 // see animation resource
        val itemsList = listOf(
                InventoryItem(0, ItemType.MainWeapon, "Hands", 5, 0),
                InventoryItem(1, ItemType.MainWeapon, "Club", 30, 20, R.drawable.club),
                InventoryItem(2, ItemType.MainWeapon, "Sword", 100, 40, R.drawable.sword),
                InventoryItem(3, ItemType.Additional, "Banana", 10, -40, R.drawable.banana),
                InventoryItem(4, ItemType.Additional, "Dice", 150, 1000, R.drawable.dice)
        )
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    fun isInternetAvailable(context: Context?): Boolean {
        if (context == null) {
            throw IllegalArgumentException("Null context was passed")
        }
        var result = false
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }
        return result
    }

    fun showToast(activity: FragmentActivity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    suspend fun executeSafeNetworkCall(functionBody: suspend () -> Unit, scope: CoroutineScope, userExceptionHandler: MutableLiveData<String>? = null) {
        try {
            withContext(scope.coroutineContext) {
                functionBody()
            }
        } catch (e: NetworkService.UserNetworkException) {
            userExceptionHandler?.postValue(e.message) ?: Timber.e(java.lang.IllegalArgumentException("No message handler was passed"))
        } catch (e: NetworkService.NetworkException) {
            Timber.e(e)
        } catch (exception: java.lang.NullPointerException) {
            Timber.e("Null Pointer exception")
        } catch (exception: MainApi.NullWebSocketSessionException) {
            Timber.e(exception)
        } catch (exception: SocketTimeoutException) {
            Timber.e(exception)
        } catch (exception: SocketException) {
            Timber.e(exception)
        }
    }

    fun changeLanguage(context: Context?, language: Language) {
        setLanguage(context, language)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        with(sharedPreferences.edit()) {
            putString(context?.getString(R.string.saved_language_key), language.name)
            apply()
        }
    }

    fun setLanguage(context: Context?, language: Language? = null) {
        var chosenLanguage = language
        if (chosenLanguage == null) {
            try {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val languageName = sharedPreferences.getString(getString(R.string.saved_language_key), null)!!
                chosenLanguage = Language.valueOf(languageName)
            } catch (e: java.lang.IllegalArgumentException) {
                chosenLanguage = Language.ENGLISH
            } catch (e: NullPointerException) {
                chosenLanguage = Language.ENGLISH
            }
        }
        when (chosenLanguage) {
            Language.RUSSIAN -> {
                val configuration = Configuration(context?.resources?.configuration)
                configuration.locale = Locale("ru")
                context?.resources?.updateConfiguration(configuration, context.resources.displayMetrics)
            }
            Language.ENGLISH -> {
                val configuration = Configuration(context?.resources?.configuration)
                configuration.locale = Locale.ENGLISH
                context?.resources?.updateConfiguration(configuration, context.resources.displayMetrics)
            }
        }
        Timber.i("Set language: ${chosenLanguage?.name}")
    }

    fun getItemImageIdByItemId(id: Int) : Int {
        return when (id) {

            else -> 0
        }
    }

    fun getItemById(id: Int): InventoryItem {
        try {
            return itemsList[id]
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw ItemsArrayIndexOutOfBoundsException("There is no such thing in inventory")
        }
    }

    class NullAppDataException(message: String) : IllegalStateException(message)
    class ItemsArrayIndexOutOfBoundsException(message: String) : ArrayIndexOutOfBoundsException(message)
}