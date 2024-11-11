package com.pakelcomedy.authenotp.model

data class UpdatePasswordRequest(
    val uid: String,
    val password: String
)
