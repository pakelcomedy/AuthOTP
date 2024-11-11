package com.pakelcomedy.authenotp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pakelcomedy.authenotp.model.OtpRequest
import com.pakelcomedy.authenotp.network.ApiClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpVerificationViewModel : ViewModel() {

    // Status untuk menampilkan hasil autentikasi
    enum class AuthStatus { SUCCESS, FAILURE, LOADING }

    // LiveData untuk memantau hasil OTP verification
    private val _otpResult = MutableLiveData<AuthResult>()
    val otpResult: LiveData<AuthResult> get() = _otpResult

    data class AuthResult(val status: AuthStatus, val message: String? = null, val userId: String? = null)

    // Method untuk verifikasi OTP
    fun verifyOtp(email: String, otpCode: String) {
        Log.d("OtpVerificationViewModel", "Verifying OTP for email: $email with code: $otpCode")
        _otpResult.value = AuthResult(AuthStatus.LOADING)

        val otpRequest = OtpRequest(email, otpCode)

        // Make the API call to verify OTP
        ApiClient.apiService.verifyOtp(otpRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!.string()
                    val jsonObject = JSONObject(responseBody)
                    val userId = jsonObject.optString("userId") // Extract userId from JSON response

                    // Post success result with userId on the main thread
                    _otpResult.postValue(AuthResult(AuthStatus.SUCCESS, "OTP verified successfully", userId))
                } else {
                    // Post failure result on the main thread
                    _otpResult.postValue(AuthResult(AuthStatus.FAILURE, "Invalid OTP"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Post failure result on the main thread
                _otpResult.postValue(AuthResult(AuthStatus.FAILURE, "API call failed: ${t.message}"))
            }
        })
    }

    // Method untuk mengirim ulang OTP
    fun resendOtp(email: String) {
        // Make the API call to resend OTP
        ApiClient.apiService.resendOtp(email).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Post success result on the main thread
                    _otpResult.postValue(AuthResult(AuthStatus.SUCCESS, "OTP resent successfully"))
                } else {
                    // Post failure result on the main thread
                    _otpResult.postValue(AuthResult(AuthStatus.FAILURE, "Failed to resend OTP"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Post failure result on the main thread
                _otpResult.postValue(AuthResult(AuthStatus.FAILURE, "API call failed: ${t.message}"))
            }
        })
    }
}