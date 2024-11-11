package com.pakelcomedy.authenotp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.pakelcomedy.authenotp.model.User
import com.pakelcomedy.authenotp.network.ApiClient
import com.pakelcomedy.authenotp.utils.EmailSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import java.util.*

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val _signUpResult = MutableLiveData<AuthResult>()
    val signUpResult: LiveData<AuthResult> get() = _signUpResult

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentOtp: String? = null
    private var otpExpiry: String? = null

    fun register(email: String, namaPengguna: String, namaLengkap: String) {
        if (email.isEmpty() || namaPengguna.isEmpty() || namaLengkap.isEmpty()) {
            _signUpResult.value = AuthResult(AuthStatus.FAILURE, "All fields must be filled.")
            return
        }

        // Register with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, "temporaryPassword")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After Firebase user is created, create the user on the server
                    currentOtp = generateOtp()
                    otpExpiry = generateOtpExpiry()  // Use the updated method
                    createUserOnServer(email, namaPengguna, namaLengkap)
                } else {
                    _signUpResult.value = AuthResult(AuthStatus.FAILURE, task.exception?.message ?: "Registration failed.")
                }
            }
    }

    private fun createUserOnServer(email: String, namaPengguna: String, namaLengkap: String) {
        // Create User object to send to API
        val user = User(
            uid = firebaseAuth.currentUser?.uid ?: "",
            email = email,
            nama_pengguna = namaPengguna,
            nama_lengkap = namaLengkap,
            role = "penulis",  // Example role, adjust as needed
            password = "temporaryPassword",  // Set a temporary password, will be updated later
            otp = currentOtp,
            otp_expiry = otpExpiry,  // Send the properly formatted expiry date
            profile_pic = "",  // Set default or empty for now
            kredensial = ""  // Set default or empty for now
        )

        // Make API call to register user
        CoroutineScope(Dispatchers.IO).launch {
            ApiClient.apiService.createUser(user).enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        // Send OTP to the email
                        sendOtp(email, currentOtp!!)
                    } else {
                        _signUpResult.postValue(AuthResult(AuthStatus.FAILURE, "Failed to register user on server"))
                    }
                }

                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                    _signUpResult.postValue(AuthResult(AuthStatus.FAILURE, "API call failed: ${t.message}"))
                }
            })
        }
    }

    private fun sendOtp(email: String, otp: String) {
        CoroutineScope(Dispatchers.IO).launch {
            EmailSender.sendOtpEmail(email, otp) { success, message ->
                if (success) {
                    _signUpResult.postValue(AuthResult(AuthStatus.SUCCESS, "OTP sent successfully."))
                } else {
                    _signUpResult.postValue(AuthResult(AuthStatus.FAILURE, message))
                }
            }
        }
    }

    private fun generateOtp(): String = (100000..999999).random().toString()

    private fun generateOtpExpiry(): String {
        // Set OTP expiry time to 1 hour from now
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 60)  // Expiry time is 60 minutes from now

        // Format the date to "yyyy-MM-dd HH:mm:ss"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    data class AuthResult(val status: AuthStatus, val message: String?)
    enum class AuthStatus { SUCCESS, FAILURE, LOADING }
}
