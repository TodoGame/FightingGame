package testgame.activities

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import testgame.data.GameApp
import timber.log.Timber

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

    override fun onStop() {
        super.onStop()
        Timber.i("On stop")
    }
    override fun onPause() {
        super.onPause()
        Timber.i("On pause")
        if(mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }
    override fun onResume() {
        super.onResume()
        Timber.i("On resume")
        if(mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    override fun onRestart() {
        super.onRestart()
        Timber.i("On restart")
        val winner = intent.getStringExtra(getString(R.string.match_finish_winner_extra_key))
        if (winner != null && winner == gameApp.user.username) {
            val builder = AlertDialog.Builder(this)
            val dialog = builder.setTitle(R.string.sure_to_leave_fight)
                    .setMessage(R.string.match_win_congradulation)
                    .setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.cancel()
                    }.create()
            dialog.show()
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

    private fun getUserInformation() {

    }

    private fun getFacultiesInformation() {

    }

    private fun openMainWebSocketConnection() {

    }

    private fun sendBoostRequest() {

    }

    fun buyThing(thingId: Int) {

    }
}
