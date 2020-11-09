package testgame.ui.main.fight

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightRoomBinding


class FightFragment : Fragment() {

    private lateinit var viewModel: FightViewModel

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

        viewModel = ViewModelProvider(this).get(FightViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        val actionTextView = binding.actionTextView
        val inventoryGrid = binding.inventoryGridView
        try {
            val inventoryAdapter = InventoryItemsAdapter(requireContext(), R.layout.inventory_item_view)
            inventoryGrid.adapter = inventoryAdapter
            inventoryGrid.onItemClickListener = AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
                actionTextView.text = "InventoryItemSelected"
            }
        } catch (exception: IllegalStateException) {

        }

        viewModel.activePlayerId.observe(viewLifecycleOwner, Observer { id ->
            if (id == 1) {
                val myImageView = binding.myImageView
                myImageView.bringToFront()
            } else {
                val enemyImageView = binding.enemyImageView
                enemyImageView.bringToFront()
            }
        })

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

//    fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int,
//                       id: Long) {
//        mSelectText.setText("Выбранный элемент: " + mAdapter.getItem(position))
//    }
//
//    fun onNothingSelected(parent: AdapterView<*>?) {
//        mSelectText.setText("Выбранный элемент: ничего")
//    }
}