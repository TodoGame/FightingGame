package testgame.ui.main.shop.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.testgame.R
import com.example.testgame.databinding.PageMainShopBuyBinding

class ShopBuyFragment: Fragment() {
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
                ShopItem("testId1", "Sword", 130),
                ShopItem("testId2", "Saint granade", 30),
                ShopItem("testId3", "Hill", 30),
                ShopItem("testId4", "Bomb", 30),
                ShopItem("testId5", "Knife", 30)
        ))
        binding.shopList.adapter = adapter

        return binding.root
    }
}