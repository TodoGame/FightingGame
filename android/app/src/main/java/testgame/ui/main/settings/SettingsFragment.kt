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
import timber.log.Timber
import java.lang.NullPointerException
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
                    GameApp().changeLanguage(context, languages[which])
                    restartFragment()
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

    private fun restartFragment() {
        try {
            val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.settingsFragment)!!
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.detach(currentFragment)
            transaction.attach(currentFragment)
            transaction.commit()
        } catch (e: NullPointerException) {
            Timber.i("Can not restart SettingsFragment")
        }
    }
}
