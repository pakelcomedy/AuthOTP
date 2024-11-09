package com.pakelcomedy.authenotp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.pakelcomedy.authenotp.R
import com.pakelcomedy.authenotp.databinding.FragmentPasswordBinding
import com.pakelcomedy.authenotp.viewmodel.AuthViewModel

class PasswordFragment : Fragment() {

    // ViewModel for authentication
    private val authViewModel: AuthViewModel by activityViewModels()

    // ViewBinding instance
    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listener for the submit button
        binding.submitButton.setOnClickListener {
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (validatePasswords(password, confirmPassword)) {
                // Show loading state
                showLoading(true)

                // Call ViewModel method to set up password
                authViewModel.setPassword(password).observe(viewLifecycleOwner) { result ->
                    when (result.status) {
                        AuthViewModel.AuthStatus.SUCCESS -> {
                            // Navigate to HomeFragment after password setup is complete
                            findNavController().navigate(R.id.action_passwordFragment_to_homeFragment)
                            Toast.makeText(requireContext(), "Password set successfully!", Toast.LENGTH_SHORT).show()
                        }
                        AuthViewModel.AuthStatus.FAILURE -> {
                            // Show error message if setting password fails
                            Toast.makeText(requireContext(), result.message ?: "Failed to set password.", Toast.LENGTH_SHORT).show()
                        }
                        AuthViewModel.AuthStatus.LOADING -> {
                            // Loading state, no additional action needed as loading is already shown
                        }
                    }
                    // Hide loading spinner after result is received
                    if (result.status != AuthViewModel.AuthStatus.LOADING) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    // Validate passwords and show error messages if needed
    private fun validatePasswords(password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = "Password required"
            isValid = false
        } else {
            binding.passwordTextInputLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordTextInputLayout.error = "Confirm password required"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordTextInputLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordTextInputLayout.error = null
        }

        return isValid
    }

    // Helper function to show or hide loading indicator
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.submitButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up ViewBinding
    }
}