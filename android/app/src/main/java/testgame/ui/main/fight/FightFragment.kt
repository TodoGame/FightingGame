package testgame.ui.main.fight

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightRoomBinding
import testgame.activities.EntranceActivity
import testgame.data.GameApp
import testgame.ui.main.fight.features.InventoryItemsAdapter
import timber.log.Timber


class FightFragment : Fragment() {

    private lateinit var viewModel: FightViewModel
    private lateinit var viewModelFactory: FightViewModelFactory
    val app: GameApp = this.activity?.application as GameApp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainFightRoomBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main_fight_room,
            container,
            false
        )

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)
        if (token == null) {
            val intent = Intent(activity, EntranceActivity::class.java)
            startActivity(intent)
        } else {
            viewModelFactory = FightViewModelFactory(app, token)
            viewModel = ViewModelProvider(this, viewModelFactory).get(FightViewModel::class.java)
        }

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        viewModel.errorIsCalled.observe(
                viewLifecycleOwner,
                Observer { isCalled ->
                    if (isCalled) {
                        val errorString = viewModel.errorString
                        Timber.i(errorString.value)
                        viewModel.onErrorDisplayed()
                    }
                }
        )

        val actionTextView = binding.actionTextView
        val inventoryGrid = binding.inventoryGridView
        try {
            val inventoryAdapter =
                InventoryItemsAdapter(
                    requireContext(),
                    R.layout.inventory_item_view
                )
            inventoryGrid.adapter = inventoryAdapter
            inventoryGrid.onItemClickListener = AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
                actionTextView.text = "InventoryItemSelected"
            }
        } catch (exception: IllegalStateException) {

        }

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

        viewModel.currentOption.observe(viewLifecycleOwner, Observer { option ->
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