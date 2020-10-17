package com.example.testgame.registerEntrance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

    private lateinit var viewModel: RegisterEntranceViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding: RegisterEntranceFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.register_entrance_fragment,
            container,
            false)

        viewModel = ViewModelProvider(this).get(RegisterEntranceViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        viewModel.usernameInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.usernameInputLayout.error = hint
        })

        viewModel.passwordInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.passwordInputLayout.error = hint
        })

        viewModel.errorIsCalled.observe(viewLifecycleOwner, Observer { isCalled ->
            if (isCalled) {
                Toast.makeText(this.activity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loginCompleted.observe(viewLifecycleOwner, Observer { isCompleted ->
            if (isCompleted) {
                viewModel.onLoginConfirm()
                val action = RegisterEntranceFragmentDirections.actionRegisterEntranceFragmentToMainScreenFragment()
                NavHostFragment.findNavController(this).navigate(action)
            }
        })

        viewModel.signUpCalled.observe(viewLifecycleOwner, Observer { isCalled ->
            if (isCalled) {
                viewModel.onSignUpConfirm()
                val action = RegisterEntranceFragmentDirections.actionRegisterEntranceFragmentToUserCreateFragment()
                NavHostFragment.findNavController(this).navigate(action)
            }
        })

        return binding.root
    }
}