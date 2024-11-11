package com.pakelcomedy.authenotp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.pakelcomedy.authenotp.model.User
import com.pakelcomedy.authenotp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> get() = _authResult

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String) {
        _authResult.value = AuthResult(AuthStatus.LOADING, null)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Fetch user data after successful login
                    fetchUserData(email)
                } else {
                    _authResult.value = AuthResult(AuthStatus.FAILURE, task.exception?.message ?: "Login failed")
                }
            }
    }

    private fun fetchUserData(email: String) {
        ApiClient.apiService.getUserByEmail(email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    _userData.postValue(response.body())  // Set the user data if the response is successful
                    _authResult.postValue(AuthResult(AuthStatus.SUCCESS, "Login successful"))
                } else {
                    _authResult.postValue(AuthResult(AuthStatus.FAILURE, "Failed to fetch user data"))
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                _authResult.postValue(AuthResult(AuthStatus.FAILURE, t.message ?: "Error fetching user data"))
            }
        })
    }

    data class AuthResult(val status: AuthStatus, val message: String?)
    enum class AuthStatus { SUCCESS, FAILURE, LOADING }
}