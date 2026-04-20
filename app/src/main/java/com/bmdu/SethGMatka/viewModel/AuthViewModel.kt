package com.bmdu.SethGMatka.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bmdu.SethGMatka.Api.RetrofitClient
import com.bmdu.SethGMatka.Model.ForgotRequest
import com.bmdu.SethGMatka.Model.LoginRequest
import com.bmdu.SethGMatka.Model.SignupRequest
import com.bmdu.SethGMatka.Model.resetPasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.bmdu.SethGMatka.otp.SendOtpRequest
import com.bmdu.SethGMatka.otp.VerifyOtpRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState?>(null)
    val authState: StateFlow<AuthState?> = _authState

    private val _savedMobile = MutableStateFlow("")
    val savedMobile: StateFlow<String> = _savedMobile
    private val appContext = application.applicationContext

    sealed class AuthState {
        object Loading : AuthState()
        data class OtpSent(val message: String) : AuthState()
        data class OtpVerified(val message: String) : AuthState()
        data class Error(val message: String) : AuthState()
        data class Success(val message: String) : AuthState()
    }
    private suspend fun getFcmToken(): String {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "FCM token error: ${e.message}")
            ""
        }
    }
    // Signup
    fun signup(name: String, mobile: String, password: String) {
        Log.d(TAG, "─────────────────────────────────")
        Log.d(TAG, "signup() → API CALL START")
        Log.d(TAG, "signup() → Request Params: name=$name | mobile=$mobile | password=****")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d(TAG, "signup() → Hitting endpoint: POST api/signup")
                val fcmToken = getFcmToken()
                val response = RetrofitClient.instance.signup(
                    SignupRequest(
                        name = name,
                        mobile = mobile,
                        password = password,
                        password_confirmation = password,
                        fcm_token = fcmToken
                    )
                )
                Log.d(TAG, "signup() → Response Code: ${response.code()}")
                Log.d(TAG, "signup() → Response Body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "signup() → FULL RESPONSE: ${response.body()}")
                    Log.d(TAG, "signup() → SignupData: ${response.body()!!.data}")

                    val userData = response.body()!!.data
                    val prefs = appContext.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)

                    // Clear old token and save new user's data as fallback for HomeScreen
                    prefs.edit()
                        .remove("auth_token")
                        .putString("signup_name", userData?.name ?: "")
                        .putString("signup_phone", userData?.phone ?: "")
                        .apply()

                    // Save token if signup response includes one
                    val token = userData?.token
                    if (!token.isNullOrBlank()) {
                        prefs.edit().putString("auth_token", token).apply()
                        Log.d(TAG, "signup() → Token Saved: $token")
                    }

                    val successMsg = response.body()!!.errors ?: "Signup successful"
                    Log.d(TAG, "signup() → SUCCESS: $successMsg")
                    _authState.value = AuthState.Success(successMsg)

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "signup() → FAILED: HTTP ${response.code()} | ErrorBody: $errorBody")
                    _authState.value = AuthState.Error("Signup failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "signup() → EXCEPTION: ${e.localizedMessage}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    // Login — context removed, uses appContext internally
    fun login(mobile: String, password: String) {
        Log.d(TAG, "─────────────────────────────────")
        Log.d(TAG, "login() → API CALL START")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val fcmToken = getFcmToken()
                val response = RetrofitClient.instance.login(LoginRequest(mobile, password,fcm_token = fcmToken))
                Log.d(TAG, "login() → Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    val prefs = appContext.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)

                    prefs.edit()
                        .remove("auth_token")       //  clear old token
                        .remove("signup_name")      // clear signup fallback
                        .remove("signup_phone")     // clear signup fallback
                        .putString("auth_token", token)
                        .apply()

                    Log.d(TAG, "login() → Token Saved: $token")
                    _authState.value = AuthState.Success(response.body()!!.message)
                } else {
                    Log.e(TAG, "login() → FAILED: HTTP ${response.code()}")
                    _authState.value = AuthState.Error("Invalid mobile number or password")
                }
            } catch (e: Exception) {
                Log.e(TAG, "login() → EXCEPTION: ${e.localizedMessage}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    // Forgot Password
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

    //  Verify OTP
    fun sendOtp(mobile: String) {
        viewModelScope.launch {
            _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Loading
            try {
                val response = RetrofitClient.instance.sendOtp(
                    SendOtpRequest(phone = formatMobile(mobile))
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Success(
                        response.body()?.message ?: "OTP sent successfully"
                    )
                } else {
                    // Parse error body JSON safely, fallback to plain message
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        ?: response.body()?.message
                        ?: "Failed to send OTP"
                    _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    fun verifyOtp(mobile: String, otp: String) {
        viewModelScope.launch {
            _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Loading
            try {
                val fcmToken = getFcmToken()

                val response = RetrofitClient.instance.verifyOtp(
                    VerifyOtpRequest(
                        phone     = formatMobile(mobile),
                        otp       = otp.trim(),
                        fcm_token = fcmToken
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

                    // ✅ Save the REAL token from response, not "logged_in"
                    val realToken = response.body()?.token
                    if (!realToken.isNullOrBlank()) {
                        prefs.edit().putString("auth_token", realToken).apply()
                        Log.d(com.bmdu.SethGMatka.viewModel.AuthViewModel.Companion.TAG, "verifyOtp() → ✅ Real token saved: $realToken")
                    } else {
                        // Token not in verifyOtp response — it may already be saved from a
                        // previous step. Log a warning but don't overwrite with "logged_in".
                        Log.w(com.bmdu.SethGMatka.viewModel.AuthViewModel.Companion.TAG, "verifyOtp() → ⚠️ No token in response body. Check API.")
                    }

                    _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Success(
                        response.body()?.message ?: "OTP verified"
                    )
                } else {
                    val errorMsg = response.body()?.message
                        ?: parseErrorMessage(response.errorBody()?.string())
                        ?: "Invalid OTP"
                    _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = com.bmdu.SethGMatka.viewModel.AuthViewModel.AuthState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    // ── parseErrorMessage — paste this too if not already in your AuthViewModel ──
    private fun parseErrorMessage(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            org.json.JSONObject(errorJson).optString("message", "").ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }
    fun formatMobile(mobile: String): String {
        var cleaned = mobile.trim()

        if (cleaned.startsWith("+91")) {
            cleaned = cleaned.substring(3)
        } else if (cleaned.startsWith("91") && cleaned.length > 10) {
            cleaned = cleaned.substring(2)
        }

        return cleaned
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
                    Log.e(TAG, "resetPassword() → ❌ BLOCKED: savedMobile is empty! Session expired.")
                    _authState.value = AuthState.Error("Session expired. Please try again")
                    return@launch
                }
                Log.d(TAG, "resetPassword() → Hitting endpoint: POST api/reset-password")
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
                    Log.d(TAG, "resetPassword() → ✅ SUCCESS: Password reset | Clearing savedMobile")
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
                // ✅ Read actual token from SharedPreferences (was wrongly using _savedMobile)
                val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val token = "Bearer ${prefs.getString("auth_token", "")}"

                val response = RetrofitClient.instance.logout(token)
                if (response.isSuccessful) {
                    prefs.edit().remove("auth_token").apply()  // ✅ Clear token
                    Log.d(TAG, "logout() → ✅ SUCCESS: Logout successful")
                    _authState.value = AuthState.Success("Logged out successfully")  // ✅ Update state
                } else {
                    Log.e(TAG, "logout() → ❌ FAILED: HTTP ${response.code()}")
                    _authState.value = AuthState.Error("Logout failed. Try again")  // ✅ Handle failure
                }

            } catch (e: Exception) {
                Log.e(TAG, "logout() → ❌ EXCEPTION: ${e.localizedMessage}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Something went wrong")  // ✅ Handle exception
            }
        }
    }

    fun resetState() {
        Log.d(TAG, "resetState() → Auth state cleared")
        _authState.value = null
    }

}