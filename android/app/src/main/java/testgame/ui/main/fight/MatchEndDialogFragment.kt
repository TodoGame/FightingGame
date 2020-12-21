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

class MatchEndDialogFragment(val winner: String, val onConfirmFunction: () -> Unit) : DialogFragment() {
    companion object {
        const val TAG = "MatchEndDialogFragment"

        fun newInstance(winner: String, onConfirmFunction: () -> Unit): MatchEndDialogFragment {
            val args = Bundle()
            val fragment = MatchEndDialogFragment(winner, onConfirmFunction)
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
