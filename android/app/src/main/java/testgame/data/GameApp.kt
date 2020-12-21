package testgame.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.multidex.MultiDexApplication
import com.example.testgame.R
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import kotlinx.serialization.json.Json
import timber.log.Timber
import kotlin.IllegalArgumentException

class GameApp : MultiDexApplication() {

    companion object {
        const val ATTACK_ANIMATION_PLAY_DELAY: Long = 1200 // see animation resource
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

    fun getItemImageIdFromItemId(id: Int) : Int {
        return when (id) {
            1 -> R.drawable.club
            2 -> R.drawable.sword
            3 -> R.drawable.banana
            4 -> R.drawable.dice
            else -> 0
        }
    }

    class NullAppDataException(message: String) : IllegalStateException(message)
}