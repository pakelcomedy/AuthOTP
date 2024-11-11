package com.pakelcomedy.authenotp.network

import com.pakelcomedy.authenotp.model.OtpRequest
import com.pakelcomedy.authenotp.model.UpdatePasswordRequest
import com.pakelcomedy.authenotp.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    // GET request to retrieve user data by email
    @GET("authentication.php")
    fun getUserByEmail(@Query("email") email: String): Call<User>

    // POST request to create a new user (signup)
    @POST("signup.php")
    fun createUser(@Body request: User): Call<ResponseBody>

    // PUT request to update the password for an existing user
    @PUT("signup.php")  // Using the same endpoint for updating password
    fun updatePassword(@Body request: UpdatePasswordRequest): Call<ResponseBody>

    // POST request to verify OTP
    @POST("authentication.php")
    fun verifyOtp(@Body otpRequest: OtpRequest): Call<ResponseBody>

    // POST request to resend OTP
    @POST("authentication.php")
    fun resendOtp(@Body email: String): Call<ResponseBody>
}