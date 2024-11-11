package com.pakelcomedy.authenotp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pakelcomedy.authenotp.R
import com.pakelcomedy.authenotp.databinding.FragmentOtpVerificationBinding
import com.pakelcomedy.authenotp.viewmodel.OtpVerificationViewModel

class OtpVerificationFragment : Fragment() {

    // ViewModel for OTP verification
    private val otpVerificationViewModel: OtpVerificationViewModel by viewModels()

    // ViewBinding instance
    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    // Email variable to be used in resendOtp
    private var userEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)

        // Get email from arguments or from previous fragment/activity
        userEmail = arguments?.getString("email") // or retrieve it some other way

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe OTP verification result
        otpVerificationViewModel.otpResult.observe(viewLifecycleOwner) { result ->
            when (result?.status) {
                OtpVerificationViewModel.AuthStatus.SUCCESS -> {
                    // Navigate to PasswordFragment after successful OTP verification
                    findNavController().navigate(R.id.action_otpVerificationFragment_to_passwordFragment)
                    Toast.makeText(requireContext(), "OTP verified successfully!", Toast.LENGTH_SHORT).show()
                }
                OtpVerificationViewModel.AuthStatus.FAILURE -> {
                    Toast.makeText(requireContext(), result.message ?: "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                }
                OtpVerificationViewModel.AuthStatus.LOADING -> {
                    // Show loading spinner during OTP verification
                    showLoading(true)
                }
                null -> {
                    Toast.makeText(requireContext(), "Unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

            // Hide loading spinner after result is received
            if (result?.status != OtpVerificationViewModel.AuthStatus.LOADING) {
                showLoading(false)
            }
        }

        // Handle verify button click
// Inside onViewCreated
        binding.verifyButton.setOnClickListener {
            val otpCode = binding.otpEditText.text.toString().trim()

            // Debug log to check the value of otpCode
            Log.d("OtpVerificationFragment", "Entered OTP: $otpCode")

            if (otpCode.isNotEmpty() && userEmail != null) {
                // Call verifyOtp method in ViewModel with both email and otpCode
                otpVerificationViewModel.verifyOtp(userEmail!!, otpCode)
            } else {
                Toast.makeText(requireContext(), "Please enter the OTP code", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle resend OTP link click
        binding.resendOtpTextView.setOnClickListener {
            userEmail?.let {
                otpVerificationViewModel.resendOtp(it)
                Toast.makeText(requireContext(), "OTP resent. Please check your messages.", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "Email not available. Cannot resend OTP.", Toast.LENGTH_SHORT).show()
            }
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