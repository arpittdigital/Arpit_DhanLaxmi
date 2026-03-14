package com.bmdu.dhanlaxmi.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmdu.dhanlaxmi.Api.RetrofitClient
import com.bmdu.dhanlaxmi.Model.ForgotRequest
import com.bmdu.dhanlaxmi.Model.LoginRequest
import com.bmdu.dhanlaxmi.Model.SignupRequest
import com.bmdu.dhanlaxmi.Model.VerifyOtpRequest
import com.bmdu.dhanlaxmi.Model.resetPasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState?>(null)
    val authState: StateFlow<AuthState?> = _authState

    private val _savedMobile = MutableStateFlow("")
    val savedMobile: StateFlow<String> = _savedMobile

    sealed class AuthState {
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
        data class Success(val message: String) : AuthState()
    }

    // ✅ Signup
    fun signup(name: String, mobile: String, password: String) {
        Log.d(TAG, "─────────────────────────────────")
        Log.d(TAG, "signup() → API CALL START")
        Log.d(TAG, "signup() → Request Params: name=$name | mobile=$mobile | password=****")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d(TAG, "signup() → Hitting endpoint: POST api/signup")
                val response = RetrofitClient.instance.signup(SignupRequest(name, mobile, password))
                Log.d(TAG, "signup() → Response Code: ${response.code()}")
                Log.d(TAG, "signup() → Response Body: ${response.body()}")
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "signup() → ✅ SUCCESS: ${response.body()!!.errors}")
                    _authState.value = AuthState.Success(response.body()!!.errors)
                } else {
                    Log.e(
                        TAG,
                        "signup() → ❌ FAILED: HTTP ${response.code()} | ErrorBody: ${
                            response.errorBody()?.string()
                        }"
                    )
                    _authState.value = AuthState.Error("Signup failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "signup() → ❌ EXCEPTION: ${e.localizedMessage}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    // ✅ Login
    fun login(mobile: String, password: String, context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(mobile, password)
                )

                if (response.isSuccessful && response.body() != null) {

                    val token = response.body()!!.token   // ✅ get token

                    // ✅ SAVE TOKEN
                    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("auth_token", token).apply()

                    Log.d("LOGIN_DEBUG", "Token Saved: $token")

                    _authState.value = AuthState.Success(response.body()!!.message)

                } else {
                    _authState.value = AuthState.Error("Invalid mobile number or password")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    // ✅ Forgot Password
    fun forgotPassword(mobile: String) {
        Log.d(TAG, "─────────────────────────────────")
        Log.d(TAG, "forgotPassword() → API CALL START")
        Log.d(TAG, "forgotPassword() → Request Params: mobile=$mobile")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d(TAG, "forgotPassword() → Hitting endpoint: POST api/forgot-password")
                val response = RetrofitClient.instance.forgotPassword(ForgotRequest(mobile))
                Log.d(TAG, "forgotPassword() → Response Code: ${response.code()}")
                Log.d(TAG, "forgotPassword() → Response Body: ${response.body()}")
                if (response.isSuccessful) {
                    _savedMobile.value = mobile
                    Log.d(TAG, "forgotPassword() → ✅ SUCCESS: OTP sent | Mobile saved: $mobile")
                    _authState.value = AuthState.Success("OTP sent successfully")
                } else {
                    Log.e(
                        TAG,
                        "forgotPassword() → ❌ FAILED: HTTP ${response.code()} | ErrorBody: ${
                            response.errorBody()?.string()
                        }"
                    )
                    _authState.value = AuthState.Error("Mobile number not registered")
                }
            } catch (e: Exception) {
                Log.e(TAG, "forgotPassword() → ❌ EXCEPTION: ${e.localizedMessage}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    // ✅ Verify OTP
    fun verifyOtp(otp: String) {
        Log.d(TAG, "─────────────────────────────────")
        Log.d(TAG, "verifyOtp() → API CALL START")
        Log.d(TAG, "verifyOtp() → Request Params: otp=$otp")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d(TAG, "verifyOtp() → Hitting endpoint: POST api/verify-otp")
                val response = RetrofitClient.instance.verifyOtp(VerifyOtpRequest(otp))
                Log.d(TAG, "verifyOtp() → Response Code: ${response.code()}")
                Log.d(TAG, "verifyOtp() → Response Body: ${response.body()}")
                if (response.isSuccessful) {
                    Log.d(TAG, "verifyOtp() → ✅ SUCCESS: OTP verified")
                    _authState.value = AuthState.Success("OTP verified successfully")
                } else {
                    Log.e(
                        TAG,
                        "verifyOtp() → ❌ FAILED: HTTP ${response.code()} | ErrorBody: ${
                            response.errorBody()?.string()
                        }"
                    )
                    _authState.value = AuthState.Error("Invalid OTP. Please try again")
                }
            } catch (e: Exception) {
                Log.e(TAG, "verifyOtp() → ❌ EXCEPTION: ${e.localizedMessage}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    // ✅ Reset Password
    fun resetPassword(password: String, passwordConfirmation: String) {
        Log.d(TAG, "─────────────────────────────────")
        Log.d(TAG, "resetPassword() → API CALL START")
        Log.d(TAG, "resetPassword() → Saved Mobile in ViewModel: ${_savedMobile.value}")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val mobile = _savedMobile.value
                if (mobile.isEmpty()) {
                    Log.e(
                        TAG,
                        "resetPassword() → ❌ BLOCKED: savedMobile is empty! Session expired."
                    )
                    _authState.value = AuthState.Error("Session expired. Please try again")
                    return@launch
                }
                Log.d(TAG, "resetPassword() → Hitting endpoint: POST api/reset-password")
                Log.d(
                    TAG,
                    "resetPassword() → Request Body: mobile=$mobile | password=**** | password_confirmation=****"
                )
                val response = RetrofitClient.instance.resetPassword(
                    resetPasswordRequest(
                        mobile = mobile,
                        password = password,
                        password_confirmation = passwordConfirmation
                    )
                )
                Log.d(TAG, "resetPassword() → Response Code: ${response.code()}")
                Log.d(TAG, "resetPassword() → Response Body: ${response.body()}")
                if (response.isSuccessful) {
                    Log.d(
                        TAG,
                        "resetPassword() → ✅ SUCCESS: Password reset done | Clearing savedMobile"
                    )
                    _savedMobile.value = ""
                    _authState.value = AuthState.Success("Password reset successfully")
                } else {
                    Log.e(
                        TAG,
                        "resetPassword() → ❌ FAILED: HTTP ${response.code()} | ErrorBody: ${
                            response.errorBody()?.string()
                        }"
                    )
                    _authState.value = AuthState.Error("Failed to reset password. Try again")
                }
            } catch (e: Exception) {
                Log.e(TAG, "resetPassword() → ❌ EXCEPTION: ${e.localizedMessage}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val token = "Bearer ${_savedMobile.value}"
                val response = RetrofitClient.instance.logout(token)
                if (response.isSuccessful) {
                    Log.d(TAG, "logout() → ✅ SUCCESS: Logout successful")

                }

            } catch (e: Exception) {
                Log.e(TAG, "logout() → ❌ EXCEPTION: ${e.localizedMessage}", e)

            }
        }
    }

        fun resetState() {
            Log.d(TAG, "resetState() → Auth state cleared")
            _authState.value = null
        }

}