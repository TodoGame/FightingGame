package testgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.testgame.R
import com.example.testgame.databinding.ActivityEntranceBinding

class EntranceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityEntranceBinding>(this, R.layout.activity_entrance)
        val navController = this.findNavController(R.id.entrance_nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }
}
