package testgame.ui.main.fight

import android.content.Intent
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
import com.example.testgame.databinding.FragmentMainFightLocationsBinding
import testgame.activities.EntranceActivity
import testgame.activities.FightActivity
import testgame.data.GameApp
import timber.log.Timber

class LocationsFragment : Fragment() {

    private lateinit var viewModel: FightViewModel
    private lateinit var viewModelFactory: FightViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainFightLocationsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main_fight_locations,
            container,
            false
        )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)

        if (token == null) {
            val intent = Intent(activity, EntranceActivity::class.java)
            startActivity(intent)
        } else {
            val app: GameApp = this.activity?.application as GameApp
            viewModelFactory = FightViewModelFactory(app, token)
            viewModel = ViewModelProvider(this, viewModelFactory).get(FightViewModel::class.java)

            binding.viewModel = viewModel

            binding.lifecycleOwner = this

            viewModel.errorIsCalled.observe(viewLifecycleOwner, Observer { isCalled ->
                    if (isCalled) {
                        val errorString = viewModel.errorString
                        Timber.i(errorString.value)
                        viewModel.onErrorDisplayed()
                    }
                }
            )
            viewModel.chosenLocation.observe(viewLifecycleOwner, Observer { location ->
                if (location != null) {
                    val locationModule = binding.button3.progressBar
                    locationModule.visibility = View.VISIBLE
                }
            })

            viewModel.isMatchStarted.observe(viewLifecycleOwner, Observer { isMatchStarted ->
                if (isMatchStarted) {
                    viewModel.confirmRoomEntrance()
                    val intent = Intent(activity, FightActivity::class.java)
                    startActivity(intent)
                }
            })
        }

        return binding.root
    }
}
