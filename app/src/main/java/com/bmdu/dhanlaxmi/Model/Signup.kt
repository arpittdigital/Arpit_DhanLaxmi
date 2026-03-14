package com.bmdu.dhanlaxmi.Model

data class SignupResponse(
    val success: Boolean,
    val errors: String,
    val data: SignupData
)

data class SignupData(
    val id: Int,
    val cin: String,
    val name: String,
    val phone: String
)

data class SignupRequest(
    val name: String,
    val mobile: String,
    val password: String
)