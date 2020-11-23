package testgame.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.testgame.R
import com.example.testgame.databinding.ActivityEntranceBinding
import com.example.testgame.databinding.ActivityFightBinding
import testgame.ui.main.fight.FightViewModel
import testgame.ui.main.fight.FightViewModelFactory

class FightActivity : AppCompatActivity() {

    private lateinit var viewModel: FightViewModel
    private lateinit var viewModelFactory: FightViewModelFactory
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityFightBinding>(this, R.layout.activity_fight)
        setMusic()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    private fun setMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.main_activity_music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
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