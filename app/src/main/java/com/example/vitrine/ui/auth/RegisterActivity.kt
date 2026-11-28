
package com.example.vitrine.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.vitrine.databinding.ActivityRegisterBinding
import com.example.vitrine.data.api.ApiClient
import com.example.vitrine.data.model.RegisterRequest
import com.example.vitrine.data.model.RegisterMetadata
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val groupCodes = listOf(
        "GRP01",
        "GRP02",
        "GRP03",
        "GRP04",
        "GRP05",
        "GRP06",
        "GRP07"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGroupDropdown()
        setupListeners()
    }

    private fun setupGroupDropdown() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            groupCodes
        )

        binding.etGroupCode.setAdapter(adapter)

        binding.etGroupCode.setOnItemClickListener { _, _, _, _ ->
            binding.groupLayout.error = null
            hideGeneralError()
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
            hideGeneralError()
        }

        binding.etEmail.doAfterTextChanged {
            binding.emailLayout.error = null
            hideGeneralError()
        }

        binding.etPhone.doAfterTextChanged {
            binding.phoneLayout.error = null
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

        if (!binding.btnRegister.isEnabled) return

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val groupCode = binding.etGroupCode.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword =
            binding.etConfirmPassword.text.toString()

        binding.nameLayout.error = null
        binding.emailLayout.error = null
        binding.phoneLayout.error = null
        binding.groupLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null

        hideGeneralError()

        var isValid = true

        if (name.isEmpty()) {
            binding.nameLayout.error = "Full name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.emailLayout.error = "Enter a valid email address"
            isValid = false
        }

        if (phone.isNotEmpty() &&
            !phone.matches(Regex("^\\+?[0-9]{8,15}$"))
        ) {

            binding.phoneLayout.error = "Enter a valid phone number"
            isValid = false
        }

        if (groupCode !in groupCodes) {
            binding.groupLayout.error = "Select your group code"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false

        } else if (password.length < 8) {

            binding.passwordLayout.error =
                "Password must contain at least 8 characters"

            isValid = false
        }

        if (confirmPassword.isEmpty()) {

            binding.confirmPasswordLayout.error =
                "Please confirm your password"

            isValid = false

        } else if (password != confirmPassword) {

            binding.confirmPasswordLayout.error =
                "Passwords do not match"

            isValid = false
        }

        if (!isValid) return

        submitRegistration(
            name,
            email,
            phone,
            groupCode,
            password
        )
    }

    private fun submitRegistration(
        name: String,
        email: String,
        phone: String,
        groupCode: String,
        password: String
    ) {

        hideGeneralError()
        setLoading(true)

        lifecycleScope.launch {

            try {

                val request = RegisterRequest(
                    email = email,
                    password = password,
                    data = RegisterMetadata(
                        fullName = name,
                        groupCode = groupCode,
                        phone = phone.ifBlank { null }
                    )
                )

                val response = ApiClient.service.register(request)

                if (response.isSuccessful) {

                    val result = response.body()

                    if (!result?.accessToken.isNullOrBlank()) {

                        MaterialAlertDialogBuilder(
                            this@RegisterActivity
                        )
                            .setTitle("Registration successful")
                            .setMessage(
                                "Please sign in to your account."
                            )
                            .setPositiveButton("Sign in") { _, _ ->
                                finish()
                            }
                            .setCancelable(false)
                            .show()

                    } else {

                        MaterialAlertDialogBuilder(
                            this@RegisterActivity
                        )
                            .setTitle("Registration request received")
                            .setMessage(
                                "Check your email for confirmation if required, " +
                                        "then try signing in."
                            )
                            .setPositiveButton("Back to sign in") { _, _ ->
                                finish()
                            }
                            .show()
                    }

                } else {

                    when (response.code()) {

                        400, 409, 422 -> showGeneralError(
                            "Unable to register. Check your details " +
                                    "or try another email address."
                        )

                        429 -> showGeneralError(
                            "Too many attempts. Try again later."
                        )

                        401, 403 -> showGeneralError(
                            "Registration is not authorized."
                        )

                        else -> showGeneralError(
                            "Server error. Please try again."
                        )
                    }
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: IOException) {

                showGeneralError(
                    "Connection failed. Check your internet connection."
                )

            } catch (e: Exception) {

                showGeneralError(
                    "Something went wrong. Please try again."
                )

            } finally {

                setLoading(false)
            }
        }
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
        binding.etPhone.isEnabled = !isLoading
        binding.etGroupCode.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }
}
