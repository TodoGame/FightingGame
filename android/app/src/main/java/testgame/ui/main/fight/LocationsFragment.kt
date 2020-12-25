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
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightLocationsBinding
import testgame.activities.EntranceActivity
import testgame.activities.MainActivity
import testgame.data.GameApp
import testgame.data.Match
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
            viewModelFactory = FightViewModelFactory(token)
            viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(FightViewModel::class.java)

            binding.viewModel = viewModel

            binding.lifecycleOwner = this

            viewModel.chosenLocation.observe(viewLifecycleOwner, { location ->
                if (location != null) {
                    val locationModule = binding.MatMechLocationButton.progressBar
                    locationModule.visibility = View.VISIBLE
                }
            })

            viewModel.matchState.observe(viewLifecycleOwner, { state ->
                if (state == Match.State.STARTED) {
                    Timber.i("Accepted match found")
                    viewModel.confirmMatchEntrance()
                    val locationModule = binding.MatMechLocationButton.progressBar
                    locationModule.visibility = View.INVISIBLE
                    val activityMediaPlayer = (activity as MainActivity).mediaPlayer
                    if(activityMediaPlayer?.isPlaying == true) {
                        activityMediaPlayer.pause()
                    }
                    val action = LocationsFragmentDirections.actionLocationsFragmentToFightFragment()
                    NavHostFragment.findNavController(this).navigate(action)
                } else if (state == Match.State.SEARCHING) {
                    Timber.i("Started searching match")
                }
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activityMediaPlayer = (activity as MainActivity).mediaPlayer
        if(activityMediaPlayer?.isPlaying == false) {
            activityMediaPlayer.start()
        }
    }
}
