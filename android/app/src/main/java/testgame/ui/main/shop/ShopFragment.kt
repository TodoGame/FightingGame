package com.example.testgame.ui.main.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainShopBinding
import testgame.ui.main.shop.features.ShopPagerAdapter
import testgame.ui.main.shop.ShopViewModel

class ShopFragment : Fragment() {
    private lateinit var viewModel: ShopViewModel
    private lateinit var shopPagerAdapter: ShopPagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainShopBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_main_shop,
                container,
                false
        )

        viewModel = ViewModelProvider(this).get(ShopViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        shopPagerAdapter =
                ShopPagerAdapter(
                        childFragmentManager
                )
        viewPager = binding.viewPager
        viewPager.adapter = shopPagerAdapter

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        tabLayout.setupWithViewPager(viewPager)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

}
