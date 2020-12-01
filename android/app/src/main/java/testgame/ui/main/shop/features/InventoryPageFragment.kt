package testgame.ui.main.shop.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testgame.R
import com.example.testgame.databinding.PageMainShopInventoryBinding
import testgame.ui.main.InventoryItemsAdapter
import testgame.ui.main.fight.FightViewModel
import testgame.ui.main.home.HomeViewModel
import testgame.ui.main.shop.ShopViewModel

class InventoryPageFragment: Fragment() {
    private lateinit var viewModel: ShopViewModel

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
        viewModel = ViewModelProvider(this).get(ShopViewModel::class.java)

        val manager = GridLayoutManager(activity, 3)
        binding.inventoryRecyclerView.layoutManager = manager
        val adapter = InventoryRecyclerAdapter()
        binding.inventoryRecyclerView.adapter = adapter

        viewModel.inventoryItems.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}