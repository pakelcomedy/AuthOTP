package com.pakelcomedy.authenotp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.pakelcomedy.authenotp.R
import com.pakelcomedy.authenotp.databinding.FragmentPasswordBinding
import com.pakelcomedy.authenotp.viewmodel.HomeViewModel
import com.pakelcomedy.authenotp.viewmodel.PasswordViewModel
import com.pakelcomedy.authenotp.viewmodel.OtpVerificationViewModel

class PasswordFragment : Fragment() {

    private val passwordViewModel: PasswordViewModel by viewModels()
    private val otpVerificationViewModel: OtpVerificationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels() // HomeViewModel instance

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)

        // Ambil userId dari Firebase
        userId = FirebaseAuth.getInstance().currentUser?.uid

        // Pastikan untuk mengecek apakah userId null atau tidak
        if (userId == null) {
            Toast.makeText(context, "User ID is missing", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passwordViewModel.passwordResult.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                PasswordViewModel.AuthStatus.SUCCESS -> {
                    // Pindah ke HomeFragment setelah password berhasil diatur, membawa userId
                    userId?.let {
                        val bundle = Bundle().apply {
                            putString("userId", userId)  // Menambahkan userId ke Bundle
                        }
                        findNavController().navigate(R.id.action_passwordFragment_to_homeFragment, bundle)

                        // Load user data in HomeViewModel
                        homeViewModel.loadUserData(it)

                        Toast.makeText(requireContext(), "Password set successfully!", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(requireContext(), "User ID is missing.", Toast.LENGTH_SHORT).show()
                    }
                }
                PasswordViewModel.AuthStatus.FAILURE -> {
                    Toast.makeText(requireContext(), result.message ?: "Failed to set password.", Toast.LENGTH_SHORT).show()
                }
                PasswordViewModel.AuthStatus.LOADING -> {
                    showLoading(true)
                }
            }
            showLoading(result.status == PasswordViewModel.AuthStatus.LOADING)
        }

        binding.submitButton.setOnClickListener {
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (validatePasswords(password, confirmPassword)) {
                userId?.let {
                    passwordViewModel.setPassword(it, password)
                } ?: Toast.makeText(requireContext(), "User ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }

        // Memastikan OTP sudah diverifikasi dan userId didapatkan
        otpVerificationViewModel.otpResult.observe(viewLifecycleOwner) { otpResult ->
            if (otpResult.status == OtpVerificationViewModel.AuthStatus.SUCCESS) {
                // Jika OTP berhasil, dapatkan userId
                userId = otpResult.userId
            }
        }
    }

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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.submitButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}