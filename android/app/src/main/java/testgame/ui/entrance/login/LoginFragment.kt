package testgame.ui.entrance.login

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentEntranceLoginBinding
import io.ktor.util.*
//import com.example.testgame.ui.entrance.login.LoginFragmentArgs
//import com.example.testgame.ui.entrance.login.LoginFragmentDirections
import testgame.activities.EntranceActivity
import testgame.activities.MainActivity
import testgame.data.GameApp
import testgame.data.User

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    @KtorExperimentalAPI
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentEntranceLoginBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_entrance_login,
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

        binding.usernameInput.setOnEditorActionListener { _: TextView, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.passwordInput.requestFocus()
                binding.passwordInput.isCursorVisible = true
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.passwordInput.setOnEditorActionListener { _: TextView, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.logIn()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewModel.usernameInputErrorHint.observe(
            viewLifecycleOwner, { hint ->
                binding.usernameInputLayout.error = hint
                binding.usernameInput.error = hint
            }
        )

        viewModel.passwordInputErrorHint.observe(
            viewLifecycleOwner, { hint ->
                binding.passwordInputLayout.error = hint
                binding.passwordInput.error = hint
            }
        )

        viewModel.errorIsCalled.observe(
            viewLifecycleOwner, { isCalled ->
                if (isCalled) {
                    val errorString = viewModel.errorString
                    GameApp().showToast(requireActivity(), errorString)
//                    Toast.makeText(this.activity, errorString, Toast.LENGTH_SHORT).show()
                    viewModel.onErrorDisplayed()
                }
            }
        )

        viewModel.loginCompleted.observe(
            viewLifecycleOwner, { isCompleted ->
                if (isCompleted) {
                    viewModel.onLoginConfirm()
                    setUpAppData()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        )

        viewModel.signUpCalled.observe(viewLifecycleOwner, { isCalled ->
                if (isCalled) {
                    viewModel.onSignUpConfirm()
                    val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                    NavHostFragment.findNavController(this).navigate(action)
                }
            }
        )

        return binding.root
    }

    private fun setUpAppData() {
        val token = viewModel.token
        val username = viewModel.user.username
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        with(sharedPreferences.edit()) {
            putString(getString(R.string.saved_token_key), token)
            putString(getString(R.string.saved_username_key), username)
            apply()
        }
        User.username.postValue(username)
        User.authenticationToken = token
    }
}
