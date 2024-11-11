package com.pakelcomedy.authenotp.model

data class OtpRequest(
    val email: String,  // The user's email to verify the OTP
    val otp: String     // The OTP code entered by the user
)
