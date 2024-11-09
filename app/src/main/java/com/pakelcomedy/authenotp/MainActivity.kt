package com.pakelcomedy.authenotp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.pakelcomedy.authenotp.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {

    // ViewModel for authentication
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Navigation component
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Observe authentication result
        authViewModel.authResult.observe(this) { result ->
            when (result.status) {
                AuthViewModel.AuthStatus.LOADING -> {
                    // Show loading indicator if needed
                }
                AuthViewModel.AuthStatus.SUCCESS -> {
                    // Navigate to HomeFragment after successful login
                    navController.navigate(R.id.action_signInFragment_to_homeFragment)
                }
                AuthViewModel.AuthStatus.FAILURE -> {
                    // Show error message
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}