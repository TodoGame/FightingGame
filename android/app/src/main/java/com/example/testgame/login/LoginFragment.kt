package com.example.testgame.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.example.testgame.EntranceActivity
import com.example.testgame.R
import com.example.testgame.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentLoginBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        val args = arguments
        if (args != null && args.containsKey("username") && args.containsKey("password")) {
            val argsFromRegisterFragment = LoginFragmentArgs.fromBundle(args)
            if (argsFromRegisterFragment.username != null) {
                viewModel.username.set(argsFromRegisterFragment.username)
            }
            if (argsFromRegisterFragment.password != null) {
                viewModel.username.set(argsFromRegisterFragment.password)
            }
        }

        viewModel.usernameInputErrorHint.observe(
            viewLifecycleOwner,
            Observer { hint ->
                binding.usernameInputLayout.error = hint
                binding.usernameInput.error = hint
            }
        )

        viewModel.passwordInputErrorHint.observe(
            viewLifecycleOwner,
            Observer { hint ->
                binding.passwordInputLayout.error = hint
                binding.passwordInput.error = hint
            }
        )

        viewModel.errorIsCalled.observe(
            viewLifecycleOwner,
            Observer { isCalled ->
                if (isCalled) {
                    val errorString = viewModel.errorString
                    Toast.makeText(this.activity, errorString, Toast.LENGTH_SHORT).show()
                    viewModel.onErrorDisplayed()
                }
            }
        )

        viewModel.loginCompleted.observe(
            viewLifecycleOwner,
            Observer { isCompleted ->
                if (isCompleted) {
                    viewModel.onLoginConfirm()
                    val token = viewModel.token
                    val username = viewModel.user.name
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                    with(sharedPreferences.edit()) {
                        putString(getString(R.string.saved_token_key), token)
                        putString(getString(R.string.saved_username_key), username)
                        apply()
                    }
                    val intent = Intent(activity, EntranceActivity::class.java)
                    startActivity(intent)
                }
            }
        )

        viewModel.signUpCalled.observe(
            viewLifecycleOwner,
            Observer { isCalled ->
                if (isCalled) {
                    viewModel.onSignUpConfirm()
                    val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                    NavHostFragment.findNavController(this).navigate(action)
                }
            }
        )

        val testButton = view?.findViewById<Button>(R.id.testButton)
        testButton?.setOnClickListener {
            val intent = Intent(activity, EntranceActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}
