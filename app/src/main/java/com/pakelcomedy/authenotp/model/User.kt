package com.pakelcomedy.authenotp.data.model

data class User(
    val uid: String? = null,
    val nama_pengguna: String? = null,
    val password: String,
    val email: String,
    val profile_pic: String? = null,
    val role: String? = null,
    val kredensial: String? = null,
    val nama_lengkap: String? = null,
    val otp: String? = null,
    val otp_expiry: String? = null
)
