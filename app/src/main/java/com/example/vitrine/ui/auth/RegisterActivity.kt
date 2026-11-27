
package com.example.vitrine.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.vitrine.databinding.ActivityRegisterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {

        binding.btnRegister.setOnClickListener {
            validateRegistration()
        }

        binding.btnGoToLogin.setOnClickListener {
            finish()
        }

        binding.etName.doAfterTextChanged {
            binding.nameLayout.error = null
            hideGeneralError()
        }

        binding.etEmail.doAfterTextChanged {
            binding.emailLayout.error = null
            hideGeneralError()
        }

        binding.etPassword.doAfterTextChanged {
            binding.passwordLayout.error = null
            binding.confirmPasswordLayout.error = null
            hideGeneralError()
        }

        binding.etConfirmPassword.doAfterTextChanged {
            binding.confirmPasswordLayout.error = null
            hideGeneralError()
        }

        binding.etConfirmPassword.setOnEditorActionListener {
                _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateRegistration()
                true
            } else {
                false
            }
        }
    }

    private fun validateRegistration() {

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword =
            binding.etConfirmPassword.text.toString()

        binding.nameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null

        hideGeneralError()

        var isValid = true

        // Validate full name

        if (name.isEmpty()) {
            binding.nameLayout.error = "Full name is required"
            isValid = false
        }

        // Validate email

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error =
                "Enter a valid email address"

            isValid = false
        }

        // Validate password

        if (password.isEmpty()) {
            binding.passwordLayout.error =
                "Password is required"

            isValid = false

        } else if (password.length < 8) {
            binding.passwordLayout.error =
                "Password must contain at least 8 characters"

            isValid = false
        }

        // Validate password confirmation

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordLayout.error =
                "Please confirm your password"

            isValid = false

        } else if (password != confirmPassword) {
            binding.confirmPasswordLayout.error =
                "Passwords do not match"

            isValid = false
        }

        if (!isValid) {
            return
        }

        submitRegistration(name, email, password)
    }

    private fun submitRegistration(
        name: String,
        email: String,
        password: String
    ) {

        // TODO: Replace temporary testing logic with REST API.

        // Simulate an already registered test account.

        if (email.equals("test@example.com", ignoreCase = true)) {

            showGeneralError(
                "Unable to register with this email address."
            )

            return
        }

        // UI validation test only.
        // No real account is created or stored.

        MaterialAlertDialogBuilder(this)
            .setTitle("Validation successful")
            .setMessage(
                "Your registration details are valid. " +
                        "Account creation will be available after " +
                        "connecting to the server."
            )
            .setPositiveButton("Back to sign in") { _, _ ->
                finish()
            }
            .show()
    }

    private fun showGeneralError(message: String) {

        binding.tvRegisterError.text = message
        binding.tvRegisterError.visibility = View.VISIBLE
    }

    private fun hideGeneralError() {

        binding.tvRegisterError.visibility = View.GONE
    }

    private fun setLoading(isLoading: Boolean) {

        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.btnRegister.isEnabled = !isLoading
        binding.btnGoToLogin.isEnabled = !isLoading
        binding.etName.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }
}
