package com.pakelcomedy.authenotp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pakelcomedy.authenotp.model.UpdatePasswordRequest
import com.pakelcomedy.authenotp.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordViewModel : ViewModel() {

    // Status untuk menampilkan hasil autentikasi
    enum class AuthStatus { SUCCESS, FAILURE, LOADING }

    // LiveData untuk memantau hasil setting password
    private val _passwordResult = MutableLiveData<AuthResult>()
    val passwordResult: LiveData<AuthResult> get() = _passwordResult

    data class AuthResult(val status: AuthStatus, val message: String? = null)

    // Method untuk mengatur password
    fun setPassword(userId: String, password: String): LiveData<AuthResult> {
        _passwordResult.value = AuthResult(AuthStatus.LOADING)

        // Check if the password meets the required length
        if (password.length < 6) {
            _passwordResult.value = AuthResult(AuthStatus.FAILURE, "Password is too short")
            return _passwordResult
        }

        // Call method to update the password on the server
        updatePasswordOnServer(userId, password)

        return _passwordResult
    }

    private fun updatePasswordOnServer(userId: String, password: String) {
        // Create the UpdatePasswordRequest object for the password update request
        val updatePasswordRequest = UpdatePasswordRequest(
            uid = userId,
            password = password
        )

        // Make the API call to update the password
        CoroutineScope(Dispatchers.IO).launch {
            ApiClient.apiService.updatePassword(updatePasswordRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        // Success case
                        _passwordResult.postValue(AuthResult(AuthStatus.SUCCESS, "Password updated successfully"))
                    } else {
                        // Failure case when response is not successful
                        _passwordResult.postValue(AuthResult(AuthStatus.FAILURE, "Failed to update password"))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Handle failure in the API call (network issues, etc.)
                    _passwordResult.postValue(AuthResult(AuthStatus.FAILURE, "API call failed: ${t.message}"))
                }
            })
        }
    }
}