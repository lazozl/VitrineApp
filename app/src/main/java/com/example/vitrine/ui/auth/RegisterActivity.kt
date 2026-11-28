
package com.example.vitrine.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.vitrine.data.api.ApiClient
import com.example.vitrine.data.model.RegisterRequest
import com.example.vitrine.data.model.RegisterMetadata
import com.example.vitrine.databinding.ActivityRegisterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val groups = listOf(
        "GRP01", "GRP02", "GRP03", "GRP04",
        "GRP05", "GRP06", "GRP07"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdown()
        setupListeners()
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            groups
        )

        binding.etGroupCode.setAdapter(adapter)

        binding.etGroupCode.setOnItemClickListener { _, _, _, _ ->
            binding.groupLayout.error = null
            hideError()
        }
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
            hideError()
        }

        binding.etEmail.doAfterTextChanged {
            binding.emailLayout.error = null
            hideError()
        }

        binding.etPhone.doAfterTextChanged {
            binding.phoneLayout.error = null
            hideError()
        }

        binding.etPassword.doAfterTextChanged {
            binding.passwordLayout.error = null
            binding.confirmPasswordLayout.error = null
            hideError()
        }

        binding.etConfirmPassword.doAfterTextChanged {
            binding.confirmPasswordLayout.error = null
            hideError()
        }

        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateRegistration()
                true
            } else {
                false
            }
        }
    }

    private fun validateRegistration() {
        if (!binding.btnRegister.isEnabled) return

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val group = binding.etGroupCode.text.toString()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        hideError()

        when {
            name.isBlank() -> {
                binding.nameLayout.error = "Full name is required"
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailLayout.error = "Enter a valid email"
            }

            phone.isNotEmpty() &&
                    !phone.matches(Regex("^\\+?[0-9]{8,15}$")) -> {
                binding.phoneLayout.error = "Invalid phone number"
            }

            group !in groups -> {
                binding.groupLayout.error = "Select your group"
            }

            password.length < 8 -> {
                binding.passwordLayout.error = "Minimum 8 characters"
            }

            password != confirm -> {
                binding.confirmPasswordLayout.error = "Passwords do not match"
            }

            else -> {
                val data = RegisterMetadata(
                    name,
                    group,
                    phone.ifBlank { null }
                )

                register(RegisterRequest(email, password, data))
            }
        }
    }

    private fun register(request: RegisterRequest) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.service.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val message =
                        if (!response.body()?.accessToken.isNullOrBlank()) {
                            "You can now sign in."
                        } else {
                            "Check your email for confirmation if required."
                        }

                    MaterialAlertDialogBuilder(this@RegisterActivity)
                        .setTitle("Registration request received")
                        .setMessage(message)
                        .setPositiveButton("Sign in") { _, _ ->
                            finish()
                        }
                        .show()
                } else {
                    val message = when (response.code()) {
                        400, 409, 422 -> "Check your details or use another email"
                        401, 403 -> "Registration is not authorized"
                        429 -> "Too many attempts. Try again later"
                        else -> "Server error. Please try again"
                    }

                    showError(message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                showError("Check your internet connection")
            } catch (e: Exception) {
                showError("Something went wrong")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        binding.tvRegisterError.text = message
        binding.tvRegisterError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvRegisterError.visibility = View.GONE
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility =
            if (loading) View.VISIBLE else View.GONE

        binding.btnRegister.isEnabled = !loading
        binding.btnGoToLogin.isEnabled = !loading
        binding.etName.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPhone.isEnabled = !loading
        binding.etGroupCode.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.etConfirmPassword.isEnabled = !loading
    }
}
