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
import testgame.data.User
import timber.log.Timber
import java.lang.NullPointerException

class MatchEndDialogFragment(val onConfirmFunction: () -> Unit) : DialogFragment() {
    companion object {
        const val TAG = "MatchEndDialogFragment"
        private const val KEY_WINNER = "KEY_TITLE"

        fun newInstance(winner: String, onConfirmFunction: () -> Unit): MatchEndDialogFragment {
            val args = Bundle()
            args.putString(KEY_WINNER, winner)
            val fragment = MatchEndDialogFragment(onConfirmFunction)
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
        Timber.i("Winner: $winner")
        Timber.i("Player username: ${User.username.value}")
        if (winner == User.username.value) {
            binding.winnerTextView.text = getString(R.string.match_win_congratulation)
        } else {
            binding.winnerTextView.text = getString(R.string.match_defeat_congratulation)
        }
        binding.confirmButton.setOnClickListener {
            onConfirmFunction()
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
