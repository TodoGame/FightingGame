package testgame.activities

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.testgame.R
import com.example.testgame.databinding.ActivityFightBinding
import timber.log.Timber

class FightActivity : AppCompatActivity() {

    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityFightBinding>(this, R.layout.activity_fight)
        setMusic()
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

    private fun setMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.fight_activity_music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    override fun onBackPressed() {
        val dialog = buildOnEscapeDialog().setPositiveButton(R.string.yes) { _, _ ->
            super.onBackPressed()
        }.create()
        dialog.show()
    }

    fun buildOnEscapeDialog():  AlertDialog.Builder {
        val builder = AlertDialog.Builder(this)
        return builder.setTitle(R.string.sure_to_leave_fight)
                .setMessage(R.string.you_will_lose_progress)
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.cancel()
                }
    }
}