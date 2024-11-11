package com.pakelcomedy.authenotp.model

data class User(
    val email: String,                    // User's email (non-nullable)
    val nama_lengkap: String,             // Full name (non-nullable)
    val nama_pengguna: String,            // Username (non-nullable)
    val uid: String,                      // User ID (non-nullable)
    val role: String,                     // User role (non-nullable)
    val password: String,                 // Password (non-nullable)
    val otp: String? = null,              // OTP, nullable as it might not be set
    val otp_expiry: String? = null,       // OTP expiry, nullable as it might not be set
    val profile_pic: String? = null,      // Profile picture URL, nullable
    val kredensial: String? = null        // Credentials, nullable
)