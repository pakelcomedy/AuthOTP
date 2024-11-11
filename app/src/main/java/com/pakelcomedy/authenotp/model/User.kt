package com.pakelcomedy.authenotp.model

data class User(
    val email: String,
    val nama_lengkap: String,
    val nama_pengguna: String,
    val uid: String,
    val role: String,
    val password: String,
    val otp: String?,           // OTP can be nullable if it's not provided at the signup stage
    val otp_expiry: String?,    // OTP expiry can be nullable as well, should be a formatted timestamp string
    val profile_pic: String?,   // Profile picture URL (nullable if not provided)
    val kredensial: String?     // Any credentials (nullable if not provided)
)