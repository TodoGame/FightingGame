package com.example.testgame.registerEntrance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.testgame.R
import com.example.testgame.databinding.RegisterEntranceFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegisterEntranceFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterEntranceFragment()
    }

    private lateinit var viewModel: RegisterEntranceViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding: RegisterEntranceFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.register_entrance_fragment,
            container,
            false)

        binding.lifecycleOwner = this

        viewModel.loginCompleted.observe(viewLifecycleOwner, Observer { isCompleted ->
            if (isCompleted) {
//                val actions = RegisterEntranceFragmentDirections
//                val action = GameFragmentDirections.actionGameToScore()
                NavHostFragment.findNavController(this).navigate(action)
            }
        })

        return binding.root
    }
}