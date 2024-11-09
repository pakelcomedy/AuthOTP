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
import com.pakelcomedy.authenotp.databinding.FragmentOtpVerificationBinding
import com.pakelcomedy.authenotp.viewmodel.AuthViewModel

class OtpVerificationFragment : Fragment() {

    // ViewModel for authentication
    private val authViewModel: AuthViewModel by activityViewModels()

    // ViewBinding instance
    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe authentication result for OTP verification
        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result?.status) {
                AuthViewModel.AuthStatus.SUCCESS -> {
                    // Navigate to PasswordFragment after successful OTP verification
                    findNavController().navigate(R.id.action_otpVerificationFragment_to_passwordFragment)
                    Toast.makeText(requireContext(), "OTP verified successfully!", Toast.LENGTH_SHORT).show()
                }
                AuthViewModel.AuthStatus.FAILURE -> {
                    Toast.makeText(requireContext(), result.message ?: "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                }
                AuthViewModel.AuthStatus.LOADING -> {
                    // Show loading spinner during OTP verification
                    showLoading(true)
                }
                null -> {
                    Toast.makeText(requireContext(), "Unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

            // Hide loading spinner after result is received
            if (result?.status != AuthViewModel.AuthStatus.LOADING) {
                showLoading(false)
            }
        }

        // Handle verify button click
        binding.verifyButton.setOnClickListener {
            val otpCode = binding.otpEditText.text.toString().trim()

            if (otpCode.isNotEmpty()) {
                // Call verifyOtp method
                authViewModel.verifyOtp(otpCode)
            } else {
                Toast.makeText(requireContext(), "Please enter the OTP code", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle resend OTP link click
        binding.resendOtpTextView.setOnClickListener {
            // Call resendOtp
            authViewModel.resendOtp()
            Toast.makeText(requireContext(), "OTP resent. Please check your messages.", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to show or hide loading spinner
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.verifyButton.isEnabled = !isLoading
        binding.resendOtpTextView.isEnabled = !isLoading // Disable resend OTP button when loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up ViewBinding
    }
}
