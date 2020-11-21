package testgame.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.testgame.R
import com.example.testgame.databinding.ActivityEntranceBinding
import com.example.testgame.databinding.ActivityFightBinding

class FightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val binding = DataBindingUtil.setContentView<ActivityEntranceBinding>(this, R.layout.activity_fight)
//        val mediaPlayer: MediaPlayer? = MediaPlayer.create(this, R.raw.app_music)
//        mediaPlayer?.isLooping = true
//        mediaPlayer?.start()
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