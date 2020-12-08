package testgame.ui.main.shop.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.testgame.R
import com.example.testgame.databinding.PageMainShopBuyBinding

class ShopPageFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: PageMainShopBuyBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.page_main_shop_buy,
            container,
            false
        )
        binding.lifecycleOwner = this
        val adapter = ShopRecyclerAdapter(10000, arrayOf(
                ShopItem("1", "Sword", 130),
                ShopItem("2", "Saint grenade", 30),
                ShopItem("3", "Hill", 30),
                ShopItem("4", "Bomb", 30),
                ShopItem("5", "Knife", 30)
        ))
        binding.shopListRecyclerView.adapter = adapter

        return binding.root
    }
}