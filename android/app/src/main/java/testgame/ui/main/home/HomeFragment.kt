package testgame.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainHomeBinding
import io.ktor.util.*
import testgame.ui.main.featuresNews.NewsItemListener
import testgame.ui.main.featuresNews.NewsRecyclerAdapter
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    @KtorExperimentalAPI
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

//        viewModel.getUserData()
//        viewModel.getLeadingFacultyData()
//        viewModel.makeSubscriptions()

        viewModel.testIsCalled.observe(viewLifecycleOwner, { isCalled ->
            if (isCalled) {
                val progressBar = binding.experienceProgressBar
                progressBar.update(100, 40)
                progressBar.invalidate()
            }
        })

        val adapter = NewsRecyclerAdapter(NewsItemListener {
            itemId -> Timber.i("Inventory item with $itemId was clicked")
        })
        binding.newsRecyclerView.adapter = adapter

        viewModel.newsItems.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}
