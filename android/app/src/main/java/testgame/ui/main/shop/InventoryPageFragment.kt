package testgame.ui.main.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testgame.R
import com.example.testgame.databinding.PageMainShopInventoryBinding
import item.ItemType
import testgame.data.User
import testgame.ui.main.featuresInventory.InventoryItemListener
import testgame.ui.main.featuresInventory.InventoryRecyclerAdapter
import timber.log.Timber

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

        val adapter = InventoryRecyclerAdapter(InventoryItemListener { item ->
            Timber.i("Inventory item with ${item.id} was clicked")
            if (item.type == ItemType.MainWeapon) {
                User.primaryWeapon.value = item
                Toast.makeText(this.activity, "Now ${item.name} is your primary weapon", Toast.LENGTH_SHORT).show()
            }
        })
        binding.inventoryRecyclerView.adapter = adapter

        User.inventory.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}