package testgame.activities

import android.content.Intent
import com.example.testgame.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.example.testgame.databinding.ActivityEntranceBinding
import java.lang.IllegalStateException

class EntranceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityEntranceBinding>(this, R.layout.activity_entrance)
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.entrance_nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)
        if (isTokenAlive()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isTokenAlive(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.contains(getString(R.string.saved_token_key)) &&
                sharedPreferences.contains(getString(R.string.saved_username_key))
    }
}
