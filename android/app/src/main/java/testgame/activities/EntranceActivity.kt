package testgame.activities

import com.example.testgame.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.testgame.databinding.ActivityEntranceBinding
import testgame.data.GameApp

class EntranceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        GameApp().setLanguage(this)
        DataBindingUtil.setContentView<ActivityEntranceBinding>(this, R.layout.activity_entrance)
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.entrance_nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.entrance_nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        return navController.navigateUp()
    }
}
