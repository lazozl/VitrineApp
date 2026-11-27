
package com.example.vitrine.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.vitrine.databinding.ActivityLoginBinding

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
            Toast.makeText(
                this,
                "Registration screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
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

        // Temporary mock authentication for testing only.
        val testEmail = "test@example.com"
        val testPassword = "Test1234"

        binding.tvLoginError.visibility = View.GONE

        if (email != testEmail || password != testPassword) {

            binding.tvLoginError.text =
                "Invalid email or password"

            binding.tvLoginError.visibility = View.VISIBLE

            return
        }

        Toast.makeText(
            this,
            "Test login successful. API connection pending.",
            Toast.LENGTH_LONG
        ).show()
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
