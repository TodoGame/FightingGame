package testgame.activities

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import testgame.data.GameApp

class MainActivity : AppCompatActivity() {

    private val gameApp = GameApp()
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setAppProperties()
        if (!gameApp.isInternetAvailable(this)) {
            setContentView(R.layout.activity_main_no_internet)
        } else {
            setContentView(R.layout.activity_main)
            val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController: NavController = navHostFragment.navController
            bottomNavigationView.setupWithNavController(navController)
            setMusic()
        }
    }

    private fun setAppProperties() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)
        val username = sharedPreferences.getString(getString(R.string.saved_username_key), null)
        val app: GameApp = this.application as GameApp
        app.user.username = username ?: throw GameApp.NullAppDataException("Null username")
        app.user.authenticationToken = token ?: throw GameApp.NullAppDataException("Null token")
    }

    private fun setMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.main_activity_music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    private fun openMainWebSocketConnection() {

    }

    private fun sendBoostRequest() {

    }

    fun buyThing(thingId: Int) {

    }
}
