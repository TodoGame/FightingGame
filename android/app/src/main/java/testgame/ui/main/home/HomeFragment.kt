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
import kotlinx.coroutines.*
import testgame.ui.main.featuresNews.NewsItemListener
import testgame.ui.main.featuresNews.NewsRecyclerAdapter
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

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

        coroutineScope.launch {
            viewModel.getUserData()
            viewModel.getLeadingFacultyData()
            viewModel.makeSubscriptions()
        }

        viewModel.testProgress.observe(viewLifecycleOwner, { progress ->
            val progressBar = binding.experienceProgressBar
            progressBar.update(100, progress)
            progressBar.invalidate()
        })

        val adapter = NewsRecyclerAdapter(NewsItemListener {
            itemId -> Timber.i("Inventory item with $itemId was clicked")
        })
        binding.newsRecyclerView.adapter = adapter

        viewModel.newsItems.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                binding.emptyRecyclerView.visibility = View.GONE
                binding.newsRecyclerView.visibility = View.VISIBLE
            }
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}
