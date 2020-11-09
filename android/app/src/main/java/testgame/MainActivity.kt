package testgame

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.testgame.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {

    private val gameApp = GameApplication()

    override fun onCreate(savedInstanceState: Bundle?) {
        var isTokenAlive: Boolean = true
        runBlocking {
            isTokenAlive = true
        }
        if (!isTokenAlive) {
            val intent = Intent(this, EntranceActivity::class.java)
            startActivity(intent)
        } else if (!gameApp.isInternetAvailable(this)) {
            setTheme(R.style.AppTheme)
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_no_internet)
        } else {
            setTheme(R.style.AppTheme)
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
            val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController: NavController = navHostFragment.navController
            val appBarConfiguration = AppBarConfiguration(
                    setOf(
                            R.id.homeFragment, R.id.locationsFragment, R.id.shopFragment, R.id.settingsFragment
                    )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            bottomNavigationView.setupWithNavController(navController)
        }
    }
}
