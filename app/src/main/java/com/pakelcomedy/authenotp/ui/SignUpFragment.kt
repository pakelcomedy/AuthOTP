package com.pakelcomedy.authenotp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pakelcomedy.authenotp.R
import com.pakelcomedy.authenotp.databinding.FragmentSignUpBinding
import com.pakelcomedy.authenotp.viewmodel.SignUpViewModel

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels() // Menggunakan SignUpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listener for the register button
        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val username = binding.usernameEditText.text.toString().trim()
            val fullName = binding.fullNameEditText.text.toString().trim()

            if (validateInput(email, username, fullName)) {
                showLoading(true)

                // Register the user using the ViewModel with email, username, and full name
                viewModel.register(
                    namaLengkap = fullName,
                    namaPengguna = username,
                    email = email // Only pass email, username, and full name
                )
            }
        }

        // Observe ViewModel for registration result
        viewModel.signUpResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it.status) {
                    SignUpViewModel.AuthStatus.SUCCESS -> {
                        // Navigate to OTP Verification after successful registration
                        val email = binding.emailEditText.text.toString().trim()
                        val bundle = Bundle().apply {
                            putString("email", email) // Pass email to OTP verification fragment
                        }
                        findNavController().navigate(R.id.action_signUpFragment_to_otpVerificationFragment, bundle)
                    }
                    SignUpViewModel.AuthStatus.FAILURE -> {
                        // Show an error message when registration fails
                        Toast.makeText(requireContext(), it.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                    SignUpViewModel.AuthStatus.LOADING -> {
                        // Optionally show a loading spinner if needed
                    }
                }
            }
            showLoading(false)
        }

        // Navigate to Sign In fragment if login link is clicked
        binding.loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }

    private fun validateInput(email: String, username: String, fullName: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.emailTextInputLayout.error = "Email required"
            isValid = false
        } else {
            binding.emailTextInputLayout.error = null
        }

        if (username.isEmpty()) {
            binding.usernameTextInputLayout.error = "Username required"
            isValid = false
        } else {
            binding.usernameTextInputLayout.error = null
        }

        if (fullName.isEmpty()) {
            binding.fullNameTextInputLayout.error = "Full name required"
            isValid = false
        } else {
            binding.fullNameTextInputLayout.error = null
        }

        return isValid
    }

    // Helper function to show or hide the loading indicator
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}