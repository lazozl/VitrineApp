
package com.example.vitrine.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.vitrine.databinding.ActivityLoginBinding
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.vitrine.data.api.ApiClient
import com.example.vitrine.data.model.LoginRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.io.IOException
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {

        binding.btnLogin.setOnClickListener {
            validateLogin()
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

        binding.tvRegister.setOnClickListener {

            val intent = Intent(
                this,
                RegisterActivity::class.java
            )

            startActivity(intent)
        }
    }

    private fun validateLogin() {
        binding.tvLoginError.visibility = View.GONE

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        binding.emailLayout.error = null
        binding.passwordLayout.error = null

        var isValid = true

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        }

        if (!isValid) {
            return
        }

        submitLogin(email, password)
    }


    private fun submitLogin(email: String, password: String) {

        binding.tvLoginError.visibility = View.GONE

        setLoading(true)

        lifecycleScope.launch {

            try {

                val request = LoginRequest(
                    email = email,
                    password = password
                )

                val response = ApiClient.service.login(request)

                if (response.isSuccessful) {

                    val authResponse = response.body()

                    if (!authResponse?.accessToken.isNullOrBlank()) {

                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful!",
                            Toast.LENGTH_LONG
                        ).show()

                        // Session management will be added next.

                    } else {
                        showLoginError("Invalid server response.")
                    }

                } else {

                    when (response.code()) {

                        400, 401 -> showLoginError(
                            "Invalid email or password"
                        )

                        403 -> showLoginError(
                            "Access denied."
                        )

                        429 -> showLoginError(
                            "Too many attempts. Please try again later."
                        )

                        else -> showLoginError(
                            "Server error. Please try again."
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {

                showLoginError(
                    "Connection failed. Check your internet connection."
                )

            } catch (e: Exception) {

                showLoginError(
                    "Something went wrong. Please try again."
                )

            } finally {

                setLoading(false)
            }
        }
    }
    private fun showLoginError(message: String) {

        binding.tvLoginError.text = message

        binding.tvLoginError.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {

        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.tvRegister.isEnabled = !isLoading
    }
}
