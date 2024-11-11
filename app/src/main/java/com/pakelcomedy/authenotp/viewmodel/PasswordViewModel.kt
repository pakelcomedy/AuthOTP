package com.pakelcomedy.authenotp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pakelcomedy.authenotp.model.UpdatePasswordRequest
import com.pakelcomedy.authenotp.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordViewModel : ViewModel() {

    enum class AuthStatus { SUCCESS, FAILURE, LOADING }

    private val _passwordResult = MutableLiveData<AuthResult>()
    val passwordResult: LiveData<AuthResult> get() = _passwordResult

    data class AuthResult(val status: AuthStatus, val message: String? = null)

    fun setPassword(userId: String, password: String) {
        _passwordResult.value = AuthResult(AuthStatus.LOADING)

        if (password.length < 6) {
            _passwordResult.value = AuthResult(AuthStatus.FAILURE, "Password is too short")
            return
        }

        updatePasswordOnServer(userId, password)
    }

    private fun updatePasswordOnServer(userId: String, password: String) {
        val updatePasswordRequest = UpdatePasswordRequest(uid = userId, password = password)

        viewModelScope.launch {
            ApiClient.apiService.updatePassword(updatePasswordRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        _passwordResult.postValue(AuthResult(AuthStatus.SUCCESS, "Password updated successfully"))
                    } else {
                        _passwordResult.postValue(AuthResult(AuthStatus.FAILURE, "Failed to update password"))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _passwordResult.postValue(AuthResult(AuthStatus.FAILURE, "API call failed: ${t.message}"))
                }
            })
        }
    }
}