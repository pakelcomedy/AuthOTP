package com.pakelcomedy.authenotp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pakelcomedy.authenotp.network.ApiClient
import com.pakelcomedy.authenotp.network.UserRequest
import com.pakelcomedy.authenotp.utils.EmailSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.awaitResponse
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.*


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> get() = _authResult

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Temporary variables to hold user data during registration process
    private var currentOtp: String? = null
    private var currentEmail: String? = null
    private var uid: String? = null
    private var namaPengguna: String? = null
    private var namaLengkap: String? = null
    private var profilePic: String? = null
    private var kredensial: String? = null
    private var otpExpiry: Long? = null

    // Updated register method in AuthViewModel:
    fun register(namaLengkap: String, namaPengguna: String, email: String) {
        if (email.isEmpty()) {
            _authResult.value = AuthResult(AuthStatus.FAILURE, "Email cannot be empty.")
            return
        }

        _authResult.value = AuthResult(AuthStatus.LOADING, null)

        // Register user with Firebase Auth without password initially
        firebaseAuth.createUserWithEmailAndPassword(email, "tempPasswordForRegistration")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uid = task.result?.user?.uid
                    currentEmail = email
                    currentOtp = generateOtp()
                    this.namaPengguna = namaPengguna
                    this.namaLengkap = namaLengkap

                    // Send OTP email asynchronously
                    viewModelScope.launch {
                        sendOtpAsync(email)
                    }
                } else {
                    _authResult.value = AuthResult(
                        AuthStatus.FAILURE,
                        task.exception?.message ?: "Registration failed."
                    )
                }
            }
    }

    // Function to send OTP asynchronously in the background
    private suspend fun sendOtpAsync(email: String) {
        withContext(Dispatchers.IO) {
            Log.d("AuthViewModel", "Sending OTP to $email")
            Log.d("AuthViewModel", "OTP: $currentOtp")
            Log.d("AuthViewModel", "UID: $uid")
            Log.d("AuthViewModel", "Nama Pengguna: $namaPengguna")
            Log.d("AuthViewModel", "Nama Lengkap: $namaLengkap")

            EmailSender.sendOtpEmail(email, currentOtp!!) { success, message ->
                if (success) {
                    val userRequest = UserRequest(
                        uid = uid ?: "",
                        email = email,
                        otp = currentOtp ?: "",
                        otp_expiry = getOneHourFromNow().toString(),
                        role = "pembaca",
                        password = "", // atau password yang sesuai
                        nama_pengguna = namaPengguna ?: "",
                        nama_lengkap = namaLengkap ?: "",
                        profile_pic = profilePic ?: "", // Pastikan ini tidak null atau kosong
                        kredensial = kredensial ?: ""  // Pastikan ini juga tidak null atau kosong
                    )


                    viewModelScope.launch {
                        Log.d("AuthViewModel", "Making server request with: $userRequest")
                        val response = ApiClient.apiService.createUser(userRequest).awaitResponse()
                        handleServerResponse(response)
                    }
                } else {
                    _authResult.postValue(AuthResult(AuthStatus.FAILURE, message))
                }
            }
        }
    }

    private suspend fun handleServerResponse(response: Response<ResponseBody>) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                _authResult.value = AuthResult(AuthStatus.SUCCESS, "OTP sent successfully. Please verify.")
            } else {
                // Log lebih rinci untuk debug
                val errorBody = response.errorBody()?.string()
                Log.e("AuthViewModel", "Server error: $errorBody")

                _authResult.value = AuthResult(AuthStatus.FAILURE, "Server error: $errorBody")

                // Handle tambahan jika perlu
                firebaseAuth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        _authResult.value = AuthResult(AuthStatus.FAILURE, "Failed to register user on server. Firebase account deleted.")
                    } else {
                        _authResult.value = AuthResult(
                            AuthStatus.FAILURE,
                            "Failed to register user on server and could not delete Firebase account: ${deleteTask.exception?.message}"
                        )
                    }
                }
            }
        }
    }


    private fun getOneHourFromNow(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1) // Adding 1 hour to the current time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Setting the date format
        return dateFormat.format(calendar.time) // Returning the formatted time
    }

    // Method to verify OTP
    fun verifyOtp(otp: String) {
        _authResult.value = AuthResult(AuthStatus.LOADING, null)

        val currentTime = System.currentTimeMillis()
        if (otp == currentOtp && otpExpiry != null && currentTime <= otpExpiry!!) {
            _authResult.value = AuthResult(AuthStatus.SUCCESS, "OTP verified successfully.")
            currentOtp = null
            otpExpiry = null
        } else {
            _authResult.value = AuthResult(AuthStatus.FAILURE, "Invalid or expired OTP.")
        }
    }

    // Resend OTP method that uses the stored email and generates a new OTP
    fun resendOtp() {
        if (currentEmail.isNullOrEmpty()) {
            _authResult.value = AuthResult(AuthStatus.FAILURE, "Email address is missing.")
            return
        }

        _authResult.value = AuthResult(AuthStatus.LOADING, null)
        currentOtp = generateOtp()

        viewModelScope.launch {
            sendOtpAsync(currentEmail!!)
        }
    }

    // Method for user login
    fun login(email: String, password: String) {
        _authResult.value = AuthResult(AuthStatus.LOADING, null)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authResult.value = AuthResult(AuthStatus.SUCCESS, "Login successful")
                } else {
                    _authResult.value =
                        AuthResult(AuthStatus.FAILURE, task.exception?.message ?: "Login failed")
                }
            }
    }

    // Function to set password after OTP verification
    fun setPassword(newPassword: String): LiveData<AuthResult> {
        val resultLiveData = MutableLiveData<AuthResult>()
        resultLiveData.value = AuthResult(AuthStatus.LOADING, null)

        firebaseAuth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                resultLiveData.value = AuthResult(AuthStatus.SUCCESS, "Password set successfully.")
            } else {
                resultLiveData.value = AuthResult(AuthStatus.FAILURE, task.exception?.message ?: "Failed to set password.")
            }
        }
        return resultLiveData
    }

    // Function to generate a 6-digit OTP
    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    // Result data class to represent success or failure of authentication operations
    data class AuthResult(val status: AuthStatus, val message: String?)

    // Enum class to represent the status of the authentication process
    enum class AuthStatus {
        SUCCESS,
        FAILURE,
        LOADING
    }
}