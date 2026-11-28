package com.example.vitrine.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.vitrine.MainActivity
import com.example.vitrine.data.api.ApiClient
import com.example.vitrine.data.model.LoginRequest
import com.example.vitrine.databinding.ActivityLoginBinding
import com.example.vitrine.utils.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            validateLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.etEmail.doAfterTextChanged {
            binding.emailLayout.error = null
            binding.tvLoginError.visibility = View.GONE
        }

        binding.etPassword.doAfterTextChanged {
            binding.passwordLayout.error = null
            binding.tvLoginError.visibility = View.GONE
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateLogin()
                true
            } else {
                false
            }
        }
    }

    private fun validateLogin() {
        if (!binding.btnLogin.isEnabled) return

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        binding.tvLoginError.visibility = View.GONE

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Enter a valid email address"
            return
        }

        if (password.isBlank()) {
            binding.passwordLayout.error = "Password is required"
            return
        }

        submitLogin(email, password)
    }

    private fun submitLogin(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.service.login(
                    LoginRequest(email, password)
                )

                val auth = response.body()

                if (response.isSuccessful && auth != null) {
                    val saved = withContext(Dispatchers.IO) {
                        sessionManager.saveSession(auth)
                    }

                    if (saved) {
                        openMainScreen()
                    } else {
                        showLoginError("Unable to save session")
                    }
                } else {
                    val message = when (response.code()) {
                        400, 401 -> "Invalid email or password"
                        403 -> "Access denied"
                        429 -> "Too many attempts. Try again later"
                        else -> "Server error. Please try again"
                    }

                    showLoginError(message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                showLoginError("Check your internet connection")
            } catch (e: Exception) {
                showLoginError("Something went wrong")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showLoginError(message: String) {
        binding.tvLoginError.text = message
        binding.tvLoginError.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility =
            if (loading) View.VISIBLE else View.GONE

        binding.btnLogin.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.tvRegister.isEnabled = !loading
    }

    private fun openMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
