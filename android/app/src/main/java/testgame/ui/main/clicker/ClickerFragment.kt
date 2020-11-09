package testgame.ui.main.clicker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainClickerBinding
import testgame.EntranceActivity
import testgame.GameApplication
import testgame.MainActivity
import java.lang.IllegalArgumentException

class ClickerFragment : Fragment() {

    private lateinit var viewModel: ClickerViewModel
    private val gameApp = GameApplication()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainClickerBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main_clicker,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(ClickerViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        viewModel.isImageClicked.observe(viewLifecycleOwner, Observer { isClicked ->
            if (isClicked) {
                val image = binding.image
                val bounceAnimation = AnimationUtils.loadAnimation(activity, R.anim.bounce_animation)
                image.startAnimation(bounceAnimation)
                viewModel.onImageClicked()
            }
        })

        viewModel.isStartActivityClicked.observe(viewLifecycleOwner, Observer { isClicked ->
            try {
                if (isClicked && gameApp.isInternetAvailable(context)) {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                }
            } catch (exception: IllegalArgumentException) {
                Toast.makeText(this.activity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }
}