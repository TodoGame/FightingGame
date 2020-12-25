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
import testgame.data.GameApp
import testgame.activities.MainActivity
import timber.log.Timber
import java.lang.IllegalArgumentException

class ClickerFragment : Fragment() {

    private lateinit var viewModel: ClickerViewModel
    private val gameApp = GameApp()

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

        viewModel.isImageClicked.observe(viewLifecycleOwner, { isClicked ->
            if (isClicked) {
                val image = binding.image
                val bounceAnimation = AnimationUtils.loadAnimation(activity, R.anim.bounce_animation)
                bounceAnimation.interpolator = BounceInterpolator(0.2, 20.0)
                image.startAnimation(bounceAnimation)
                viewModel.onImageClicked()
            }
        })

        viewModel.isStartActivityClicked.observe(viewLifecycleOwner, { isClicked ->
            try {
                if (isClicked && gameApp.isInternetAvailable(context)) {
                    val intent = Intent(activity, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(this.activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            } catch (exception: IllegalArgumentException) {
                Toast.makeText(this.activity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }
}