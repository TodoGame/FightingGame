package testgame.ui.main.settings

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainSettingsBinding
import testgame.activities.EntranceActivity

class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainSettingsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main_settings,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        val warriorImage = binding.warriorImage
        val idleWarrior = warriorImage.drawable as AnimationDrawable
        idleWarrior.start()

        viewModel.isLogOutPressed.observe(viewLifecycleOwner, Observer { isPressed ->
            if (isPressed) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                with(sharedPreferences.edit()) {
                    remove(getString(R.string.saved_token_key))
                    remove(getString(R.string.saved_username_key))
                    apply()
                }
                val intent = Intent(activity, EntranceActivity::class.java)
                startActivity(intent)
            }
        })

        return binding.root
    }
}
