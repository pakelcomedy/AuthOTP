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
import com.pakelcomedy.authenotp.databinding.FragmentSignInBinding
import com.pakelcomedy.authenotp.viewmodel.AuthViewModel

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe authentication result
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                AuthViewModel.AuthStatus.SUCCESS -> {
                    // Navigate to HomeFragment
                    findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                    Toast.makeText(requireContext(), "Sign-in successful", Toast.LENGTH_SHORT).show()
                }
                AuthViewModel.AuthStatus.FAILURE -> {
                    // Show the failure message in Toast
                    Toast.makeText(requireContext(), "Sign-in failed: ${result.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
                AuthViewModel.AuthStatus.LOADING -> {
                    // Show a loading spinner
                    showLoading(true)
                }
            }

            // Hide loading spinner after receiving result
            if (result.status != AuthViewModel.AuthStatus.LOADING) {
                showLoading(false)
            }
        }

        // Set up the login button click listener
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                // Perform login using the ViewModel
                viewModel.login(email, password)
            }
        }

        // Navigate to SignUpFragment when the register link is clicked
        binding.registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
    }

    // Validate email and password input
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailTextInputLayout.error = "Email required"
            isValid = false
        } else {
            binding.emailTextInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = "Password required"
            isValid = false
        } else {
            binding.passwordTextInputLayout.error = null
        }

        return isValid
    }

    // Helper function to show or hide the loading spinner
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}