package com.pakelcomedy.authenotp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.pakelcomedy.authenotp.model.User
import com.pakelcomedy.authenotp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> get() = _logoutStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Use the public apiService from ApiClient
    private val apiService = ApiClient.apiService

    // Load user data based on UID passed from fragment
    fun loadUserData(uid: String) {
        Log.d("HomeViewModel", "Loading user data for UID: $uid")
        _isLoading.value = true

        // Make the API request to fetch user data by UID
        apiService.getUserData(uid).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    // Successfully fetched user data
                    _userData.value = response.body()
                    Log.d("HomeViewModel", "User data loaded: ${response.body()?.email}")
                } else {
                    // Error: empty or invalid response
                    _error.value = "No such user found or invalid response."
                    Log.e("HomeViewModel", "No such user found or invalid response.")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                _isLoading.value = false
                // Handle failure
                _error.value = "Error fetching user data: ${t.message}"
                Log.e("HomeViewModel", "Error fetching user data: ${t.message}")
            }
        })
    }

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun logout() {
        // Handle logout logic, e.g., FirebaseAuth sign out
        FirebaseAuth.getInstance().signOut()
        _logoutStatus.value = true
        Log.d("HomeViewModel", "User logged out.")
    }
}