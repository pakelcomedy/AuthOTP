package com.pakelcomedy.authenotp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.pakelcomedy.authenotp.R
import com.pakelcomedy.authenotp.viewmodel.HomeViewModel
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private lateinit var loadingIndicator: ProgressBar
    private lateinit var profileImageView: ImageView
    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var credentialsTextView: TextView
    private lateinit var usernameTextView: TextView // Tambahkan TextView untuk username
    private lateinit var logoutButton: Button

    private val homeViewModel: HomeViewModel by viewModels()

    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        profileImageView = view.findViewById(R.id.profileImageView)
        fullNameTextView = view.findViewById(R.id.fullNameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        roleTextView = view.findViewById(R.id.roleTextView)
        credentialsTextView = view.findViewById(R.id.credentialsTextView)
        usernameTextView = view.findViewById(R.id.usernameTextView) // Inisialisasi usernameTextView
        logoutButton = view.findViewById(R.id.logoutButton)

        // Ambil userId dari Firebase Authentication
        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            homeViewModel.loadUserData(userId!!)
        } else {
            Toast.makeText(requireContext(), "User ID is missing.", Toast.LENGTH_SHORT).show()
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                loadingIndicator.visibility = View.VISIBLE
            } else {
                loadingIndicator.visibility = View.GONE
            }
        }

        homeViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                fullNameTextView.text = it.nama_lengkap
                emailTextView.text = it.email
                roleTextView.text = it.role
                credentialsTextView.text = it.kredensial ?: "Belum ada kredensial"
                usernameTextView.text = it.nama_pengguna // Menampilkan username

                val profileImageUrl = it.profile_pic
                if (!profileImageUrl.isNullOrEmpty()) {
                    Picasso.get().load(profileImageUrl).into(profileImageView)
                    Log.d("HomeFragment", "Profile image URL: $profileImageUrl")
                } else {
                    profileImageView.setImageResource(R.drawable.ic_launcher_foreground) // Default image
                }
            }
        }

        homeViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        homeViewModel.logoutStatus.observe(viewLifecycleOwner) { loggedOut ->
            if (loggedOut) {
                findNavController().navigate(R.id.action_homeFragment_to_signInFragment)
            }
        }

        logoutButton.setOnClickListener {
            homeViewModel.logout()
        }
    }
}