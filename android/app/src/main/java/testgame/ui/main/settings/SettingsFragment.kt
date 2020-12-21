package testgame.ui.main.settings

import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Build
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
import kotlinx.coroutines.*
import testgame.activities.EntranceActivity
import testgame.data.GameApp
import testgame.data.Language
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

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

        val languageOptionDialogBuilder = activity?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        val languages = Language.values()
        val languagesOptionDialog = languageOptionDialogBuilder?.setTitle("Choose your faculty")
                ?.setItems(languages.map { it.languageName }.toTypedArray()) { dialog, which ->
                    when (languages[which]) {
                        Language.RUSSIAN -> {
                            val configuration = Configuration(context?.resources?.configuration)
                            configuration.locale = Locale("ru")
                            context?.resources?.updateConfiguration(configuration, requireContext().resources.displayMetrics)
                        }
                        Language.ENGLISH -> {
                            val configuration = Configuration(context?.resources?.configuration)
                            configuration.locale = Locale.ENGLISH
                            context?.resources?.updateConfiguration(configuration, requireContext().resources.displayMetrics)
                        }
                    }
                    dialog.cancel()
                }
        binding.changeLanguageButton.setOnClickListener {
            languagesOptionDialog?.show()
        }

        viewModel.isLogOutPressed.observe(viewLifecycleOwner, { isPressed ->
            if (isPressed) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                with(sharedPreferences.edit()) {
                    remove(getString(R.string.saved_token_key))
                    remove(getString(R.string.saved_username_key))
                    apply()
                }
                viewModel.onLogOutConfirmed()
                val intent = Intent(activity, EntranceActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
                activity?.finish()
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
