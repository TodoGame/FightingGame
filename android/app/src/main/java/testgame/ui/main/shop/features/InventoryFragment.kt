package testgame.ui.main.shop.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.testgame.R
import com.example.testgame.databinding.PageMainShopInventoryBinding
import testgame.ui.main.fight.features.InventoryItemsAdapter

class InventoryFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: PageMainShopInventoryBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.page_main_shop_inventory,
            container,
            false
        )
        binding.lifecycleOwner = this

        val inventoryGrid = binding.inventoryGridView
        try {
            val inventoryAdapter =
                InventoryItemsAdapter(
                    requireContext(),
                    R.layout.inventory_item_view
                )
            inventoryGrid.adapter = inventoryAdapter
        } catch (exception: IllegalStateException) {

        }
        return binding.root
    }
}