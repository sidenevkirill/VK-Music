package com.flaco_music.ui.screens.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.flaco_music.R
import com.flaco_music.databinding.ActivityLoginBinding
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.extentions.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModel()

    private val snackbarManager: SnackbarManager by inject()

    private val view: View
        get() = window.decorView.findViewById(android.R.id.content)

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        snackbarManager.rootView = binding.root

        observeViewModel()

        binding.loginButton.onClick {
            loginViewModel.obtainVkToken(
                login = binding.loginInput.string,
                password = binding.passwordInput.string,
                code = binding.authCodeInput.string,
                onSuccess = {
                    MainActivity.startMe(this)
                    finish()
                }
            )
        }

        binding.container.setOnFocusChangeListener { _, _ ->
            view.hideKeyboard()
        }

        binding.loginInput.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(this@LoginActivity, R.color.background)
        }
    }

    override fun onDestroy() {
        (loginViewModel.isCodeNeededLiveData as MutableLiveData).postValue(false)
        super.onDestroy()
    }

    private fun observeViewModel() {
        loginViewModel.isLoggingProcessingLiveData.observe(this) {
            if (it) showLoading() else hideLoading()
        }
        loginViewModel.errorMessageLiveData.observe(this) {
            snackbarManager.showSnackbar(it)
        }
        loginViewModel.isCodeNeededLiveData.observe(this) {
            if (it) {
                binding.authCodeInputContainer.visible()
                binding.authCodeInput.requestFocus()
            }
        }
    }

    private fun showLoading() {
        view.hideKeyboard()
        binding.loginButton.invisible()
        binding.progressBar.visible()
    }

    private fun hideLoading() {
        binding.loginButton.visible()
        binding.progressBar.gone()
    }

    companion object {
        private const val TAG = "LoginActivity"

        fun startMe(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}