package testgame

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testgame.R
import com.example.testgame.databinding.ActivityEntranceBinding

class FightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setTheme(R.style.AppTheme)
        Log.i("FightActivity", "Fight created")
//        val binding = DataBindingUtil.setContentView<ActivityEntranceBinding>(this, R.layout.activity_fight)
//        setContentView(R.layout.activity_fight)
        setContentView(R.layout.activity_main_no_internet)
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        val dialog = builder.setTitle(R.string.sure_to_leave_fight)
            .setMessage(R.string.you_will_lose_progress)
            .setPositiveButton(R.string.yes) { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.cancel()
            }.create()
        dialog.show()
    }
}