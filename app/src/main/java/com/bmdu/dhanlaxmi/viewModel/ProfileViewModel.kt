package com.bmdu.dhanlaxmi.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmdu.dhanlaxmi.Api.RetrofitClient
import com.bmdu.dhanlaxmi.Model.NotificationItem
import com.bmdu.dhanlaxmi.Model.ProfileResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    // ── Profile state ──────────────────────────────────────
    private val _profileState = MutableStateFlow<ProfileState?>(null)
    val profileState: StateFlow<ProfileState?> = _profileState

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Error(val message: String) : ProfileState()
        data class Success(val data: ProfileResponse) : ProfileState()
    }

    // ── Notification state ─────────────────────────────────
    private val _notificationState = MutableStateFlow<NotificationState>(NotificationState.Idle)
    val notificationState: StateFlow<NotificationState> = _notificationState

    sealed class NotificationState {
        object Idle    : NotificationState()
        object Loading : NotificationState()
        data class Error(val message: String)           : NotificationState()
        data class Success(val items: List<NotificationItem>) : NotificationState()
    }

    // ══════════════════════════════════════════════════════════
    //  FETCH PROFILE
    // ══════════════════════════════════════════════════════════
    fun fetchProfile(token: String) {
        if (token.isBlank()) {
            Log.e(TAG, "fetchProfile: Token is blank — aborting")
            _profileState.value = ProfileState.Error("Authentication token missing. Please login again.")
            return
        }
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        Log.d(TAG, "fetchProfile: Calling API → token=${authHeader.take(20)}...")

        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = RetrofitClient.instance.getProfile(authHeader)
                Log.d(TAG, "fetchProfile: HTTP ${response.code()} | Success=${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "fetchProfile: ✅ Success → data=${response.body()!!.data}")
                    _profileState.value = ProfileState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "fetchProfile:  Error → code=${response.code()}, body=$errorBody")
                    _profileState.value = ProfileState.Error(
                        when (response.code()) {
                            401  -> "Session expired. Please login again."
                            404  -> "Profile not found."
                            500  -> "Server error. Please try again later."
                            else -> "Something went wrong (code: ${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchProfile:  Exception → ${e.javaClass.simpleName}: ${e.message}", e)
                _profileState.value = ProfileState.Error(
                    e.localizedMessage ?: "Network error. Check your internet connection."
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  FETCH NOTIFICATIONS
    // ══════════════════════════════════════════════════════════
    fun fetchNotifications(token: String?) {
        if (token.isNullOrBlank()) {
            Log.e(TAG, "fetchNotifications: Token is blank — aborting")
            _notificationState.value = NotificationState.Error("Authentication token missing. Please login again.")
            return
        }
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        Log.d(TAG, "fetchNotifications: Calling API...")

        viewModelScope.launch {
            _notificationState.value = NotificationState.Loading
            try {
                val response = RetrofitClient.instance.getNotifications(authHeader)
                Log.d(TAG, "fetchNotifications: HTTP ${response.code()} | Success=${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val items = response.body()!!.data ?: emptyList()
                    Log.d(TAG, "fetchNotifications: Got ${items.size} notifications")
                    _notificationState.value = NotificationState.Success(items)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "fetchNotifications:  code=${response.code()}, body=$errorBody")
                    _notificationState.value = NotificationState.Error(
                        when (response.code()) {
                            401  -> "Session expired. Please login again."
                            500  -> "Server error. Please try again later."
                            else -> "Something went wrong (code: ${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchNotifications:  Exception → ${e.localizedMessage}", e)
                _notificationState.value = NotificationState.Error(
                    e.localizedMessage ?: "Network error. Check your internet connection."
                )
            }
        }
    }
}