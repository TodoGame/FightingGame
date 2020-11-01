package com.example.testgame.ui.entrance.register

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
import com.example.testgame.databinding.FragmentEntranceRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding: FragmentEntranceRegisterBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_entrance_register,
            container,
            false)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        viewModel.usernameInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.usernameInputLayout.error = hint
            binding.usernameInput.error = hint
        })

        viewModel.passwordInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.passwordInputLayout.error = hint
            binding.passwordInput.error = hint
        })

        viewModel.userInputErrorHint.observe(viewLifecycleOwner, Observer { hint ->
            binding.userInputLayout.error = hint
            binding.userInput.error = hint
        })

        viewModel.errorIsCalled.observe(viewLifecycleOwner, Observer { isCalled ->
            if (isCalled) {
                val errorString = viewModel.errorString
                Toast.makeText(this.activity, errorString, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.signUpCompleted.observe(viewLifecycleOwner, Observer { isCalled ->
            if (isCalled) {
                viewModel.onSignUpConfirm()
                val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment(
                    viewModel.username.get(),
                    viewModel.password.get()
                )
                NavHostFragment.findNavController(this).navigate(action)
            }
        })

        return binding.root
    }

}