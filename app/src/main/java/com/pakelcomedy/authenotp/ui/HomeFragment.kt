package com.pakelcomedy.authenotp.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.pakelcomedy.authenotp.R
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImageView = view.findViewById(R.id.profileImageView)
        fullNameTextView = view.findViewById(R.id.fullNameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        logoutButton = view.findViewById(R.id.logoutButton)

        // Load user data
        loadUserData()

        // Logout button click listener
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            // Set the email
            emailTextView.text = user.email

            // Optionally set a placeholder for full name and profile image
            fullNameTextView.text = "Full Name" // Replace this with the actual logic to get the user's full name.
            // For profile image, you could use a URL or a default drawable
            val profileImageUrl = user.photoUrl?.toString() // If you are storing the user's photo URL in Firebase

            if (profileImageUrl != null) {
                // Load image using Picasso (make sure you have Picasso dependency in your build.gradle)
                Picasso.get().load(profileImageUrl).into(profileImageView)
            } else {
                // Set a default image if the user has no profile picture
                profileImageView.setImageResource(R.drawable.ic_launcher_foreground) // Placeholder image
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        auth.signOut()
        findNavController().navigate(R.id.action_homeFragment_to_signInFragment)
    }
}