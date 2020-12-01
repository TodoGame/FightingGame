package testgame.ui.main.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainHomeBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import testgame.ui.main.shop.features.ShopItem
import testgame.ui.main.shop.features.ShopRecyclerAdapter

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainHomeBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main_home,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this


        viewModel.testIsCalled.observe(viewLifecycleOwner, { isCalled ->
            if (isCalled) {
                val progressBar = binding.experienceProgressBar
                progressBar.update(100, 40)
            }
        })

        val adapter = ShopRecyclerAdapter(10000, arrayOf(
                ShopItem("testId1", "Sword", 130),
                ShopItem("testId2", "Saint granade", 30),
                ShopItem("testId3", "Hill", 30),
                ShopItem("testId4", "Bomb", 30),
                ShopItem("testId5", "Knife", 30)
        ))
        binding.newsRecyclerView.adapter = adapter

        return binding.root
    }
}
