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
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightLocationsBinding
import testgame.FightActivity

class LocationsFragment : Fragment() {

    private lateinit var viewModel: FightViewModel

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

        viewModel = ViewModelProvider(this).get(FightViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        viewModel.isMatchStarted.observe(viewLifecycleOwner, Observer { isMatchStarted ->
                if (isMatchStarted) {
                    viewModel.confirmRoomEntrance()
                    val intent = Intent(activity, FightActivity::class.java)
                    startActivity(intent)
                }
            }
        )

        return binding.root
    }
}
