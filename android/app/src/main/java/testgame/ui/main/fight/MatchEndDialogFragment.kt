package testgame.ui.main.fight

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightEndDialogBinding
import testgame.activities.MainActivity
import testgame.data.GameApp
import java.lang.NullPointerException

class MatchEndDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "MatchEndDialogFragment"
        private const val KEY_WINNER = "KEY_TITLE"

        fun newInstance(winner: String): MatchEndDialogFragment {
            val args = Bundle()
            args.putString(KEY_WINNER, winner)
            val fragment = MatchEndDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentMainFightEndDialogBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_main_fight_end_dialog,
                container,
                false
        )

        val winner = arguments?.getString(KEY_WINNER) ?: throw NullPointerException("No argument passed to dDialogFragment")
        if (winner == GameApp().user.username) {
            binding.winnerTextView.text = getString(R.string.match_win_congratulation)
        } else {
            binding.winnerTextView.text = getString(R.string.match_defeat_congratulation)
        }
        binding.confirmButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(getString(R.string.match_finish_winner_extra_key), winner)
            startActivity(intent)
            dismiss()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
