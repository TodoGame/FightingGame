package testgame.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import testgame.data.GameApp
import testgame.data.User
import testgame.ui.main.fight.FightFragment
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val gameApp = GameApp()
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NoActionBarAppTheme)
        super.onCreate(savedInstanceState)
        if (!isTokenAlive()) {
            val intent = Intent(this, EntranceActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
            finish()
        }
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
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.fightFragment -> {
                        bottomNavigationView.visibility = View.GONE
                    }
                    else -> {
                        if (bottomNavigationView.visibility == View.GONE) {
                            bottomNavigationView.visibility = View.VISIBLE
                        }
                    }
                }
            }
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

    override fun onBackPressed() {
        val fightFragment = supportFragmentManager.findFragmentById(R.id.fightFragment)
        if (fightFragment?.isVisible!!) {
            buildOnEscapeDialog().setPositiveButton(R.string.confirm) { _, _ ->
                super.onBackPressed()
            }.create()
        } else {
            super.onBackPressed()
        }
    }

    private fun setAppProperties() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)
        val username = sharedPreferences.getString(getString(R.string.saved_username_key), null)
        User.username.value = username ?: throw GameApp.NullAppDataException("Null username")
        User.authenticationToken = token ?: throw GameApp.NullAppDataException("Null token")
    }

    private fun setMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.main_activity_music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun buildOnEscapeDialog():  AlertDialog.Builder {
        val builder = AlertDialog.Builder(this)
        return builder.setTitle(R.string.sure_to_leave_fight)
                .setMessage(R.string.you_will_lose_progress)
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.cancel()
                }
    }

    private fun isTokenAlive(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.contains(getString(R.string.saved_token_key)) &&
                sharedPreferences.contains(getString(R.string.saved_username_key))
    }
}
