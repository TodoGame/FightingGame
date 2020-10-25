package com.example.testgame.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.testgame.R
import com.example.testgame.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding: FragmentRegisterBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_register,
            container,
            false)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        viewModel.usernameInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.usernameInputLayout.error = hint
        })

        viewModel.passwordInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.passwordInputLayout.error = hint
        })

        viewModel.userInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.userInputLayout.error = hint
        })

        viewModel.errorIsCalled.observe(viewLifecycleOwner, Observer { isCalled ->
            if (isCalled) {
                Toast.makeText(this.activity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.signUpCompleted.observe(viewLifecycleOwner, Observer { isCalled ->
            if (isCalled) {
                viewModel.onSignUpConfirm()
                val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                NavHostFragment.findNavController(this).navigate(action)
            }
        })

        return binding.root
    }

}