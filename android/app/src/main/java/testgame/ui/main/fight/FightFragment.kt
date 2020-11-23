package testgame.ui.main.fight

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightRoomBinding
import testgame.activities.EntranceActivity
import testgame.activities.MainActivity
import testgame.data.GameApp
import testgame.ui.main.fight.features.InventoryItemsAdapter
import timber.log.Timber

class FightFragment : Fragment() {

    private lateinit var viewModel: FightViewModel
    private lateinit var viewModelFactory: FightViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val app: GameApp = this.activity?.application as GameApp
        val binding: FragmentMainFightRoomBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_main_fight_room,
                container,
                false
        )

        setUpViewModel(app)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setUpInventory(binding)

        viewModel.errorIsCalled.observe(viewLifecycleOwner, { isCalled ->
            if (isCalled) {
                val errorString = viewModel.errorString
                Timber.i(errorString.value)
                viewModel.onErrorDisplayed()
            }
        })

//        viewModel.activePlayerId.observe(viewLifecycleOwner, Observer { id ->
//            val actionsLayout = binding.actionLayout
//            if (id == 1) {
//                setViewAndChildrenEnabled(actionsLayout, true)
//                val myImageView = binding.myImageView
//                myImageView.bringToFront()
//            } else {
//                setViewAndChildrenEnabled(actionsLayout, false)
//                val enemyImageView = binding.enemyImageView
//                enemyImageView.bringToFront()
//            }
//        })

        viewModel.isMatchEnded.observe(viewLifecycleOwner, { isEnded ->
            if (isEnded) {
                viewModel.onRoomExit()
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
            }
        })

        viewModel.currentOption.observe(viewLifecycleOwner, { option ->
            when (option) {
                FightViewModel.Option.ATTACK -> {
                    binding.attackWindow.visibility = View.VISIBLE
                    binding.inventoryWindow.visibility = View.GONE
                }
                FightViewModel.Option.INVENTORY -> {
                    binding.attackWindow.visibility = View.GONE
                    binding.inventoryWindow.visibility = View.VISIBLE
                }
            }
        })
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onRoomExit()
    }

    private fun setUpViewModel(app: GameApp) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)
        if (token == null) {
            val intent = Intent(activity, EntranceActivity::class.java)
            startActivity(intent)
        } else {
            viewModelFactory = FightViewModelFactory(app, token)
            viewModel = ViewModelProvider(this, viewModelFactory).get(FightViewModel::class.java)
        }
    }

    private fun setUpInventory(binding: FragmentMainFightRoomBinding) {
        val actionTextView = binding.actionTextView
        val inventoryGrid = binding.inventoryGridView
        try {
            val inventoryAdapter =
                    InventoryItemsAdapter(
                            requireContext(),
                            R.layout.inventory_item_view
                    )
            inventoryGrid.adapter = inventoryAdapter
            inventoryGrid.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, _: Int, _: Long ->
                actionTextView.text = "InventoryItemSelected"
            }
        } catch (exception: IllegalStateException) {
            Timber.i(exception)
        }
    }

    private fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                setViewAndChildrenEnabled(child, enabled)
            }
        }
    }
}