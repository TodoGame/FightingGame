package testgame.ui.main.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testgame.R
import com.example.testgame.databinding.PageMainShopBuyBinding
import io.ktor.util.*
import testgame.data.GameApp
import testgame.data.User
import testgame.network.NetworkService
import testgame.ui.main.featuresShop.ShopItemListener
import testgame.ui.main.featuresShop.ShopRecyclerAdapter
import timber.log.Timber

class ShopPageFragment: Fragment() {
    private lateinit var viewModel: ShopViewModel

    @KtorExperimentalAPI
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: PageMainShopBuyBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.page_main_shop_buy,
            container,
            false
        )
        viewModel = ViewModelProvider(this).get(ShopViewModel::class.java)
        binding.lifecycleOwner = this

        val adapter = ShopRecyclerAdapter(ShopItemListener { itemId ->
            Timber.i("Shop item with $itemId was clicked")
            try {
                viewModel.buyItem(itemId)
            } catch (e: NetworkService.NetworkException) {
                Timber.i(e)
            }
        })
        binding.shopListRecyclerView.adapter = adapter

        try {
            viewModel.getAllNotOwnedItems()
        } catch (e: NetworkService.NetworkException) {
            Timber.i(e)
        }

        viewModel.userMessage.observe(viewLifecycleOwner, { message ->
            GameApp().showToast(requireActivity(), message)
        })

        viewModel.shopItems.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })
        return binding.root
    }
}
