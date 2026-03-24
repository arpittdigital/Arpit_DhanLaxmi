package com.bmdu.dhanlaxmi.Model

data class LoginRequest(
    val mobile: String,
    val password: String,
    val fcm_token: String
)

data class loginresponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val data: logindata
)

data class logindata(
    val id: Int,
    val phone: String,
    val name: String,
    val role: String
)

data class ForgotRequest(
    val mobile: String
)

data class forgotResponse(
    val success: Boolean,
    val message: String,
    val otp: String
)

data class VerifyOtpRequest(
    val otp: String
)

data class verifyOtpResponse(
    val success: Boolean,
    val message: String
)

data class resetPasswordRequest(
    val mobile: String,
    val password: String,
    val password_confirmation: String
)

data class resetPasswordResponse(
    val success: Boolean,
    val message: String
)









