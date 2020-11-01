package com.example.testgame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking
import match.PlayerAction


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var isTokenAlive = true
        runBlocking {
//            val isTokenALive = isTokenValid()
            isTokenAlive = true
        }
        if (!isTokenAlive) {
            val intent = Intent(this, EntranceActivity::class.java)
            startActivity(intent)
        } else {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment ) as NavHostFragment
            val navController: NavController = navHostFragment.navController
//            val navController = findNavController(R.id.nav_host_fragment)
//            val appBarConfiguration = AppBarConfiguration(
//                setOf(
//                    R.id.navigation_home, R.id.navigation_settings, R.id.navigation_shop, R.id.navigation_fight
//                )
//            )
//            setupActionBarWithNavController(navController, appBarConfiguration)
            bottomNavigationView.setupWithNavController(navController)
        }
    }

//    fun isTokenValid(): Boolean {
//        val registerDeferred = SecurityApi.RETROFIT_SERVICE.checkToken()
//        val answer = registerDeferred.await()
//        if (answer.isSuccessful) {
//            val headerToken = answer.headers().get("Authorization")
//            if (headerToken != null) {
//                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//                with (sharedPreferences.edit()) {
//                    putString(getString(R.string.saved_token_key), headerToken)
//                    apply()
//                }
//            } else {
//                return false
//            }
//        } else {
//            return false
//        }
//    }
}