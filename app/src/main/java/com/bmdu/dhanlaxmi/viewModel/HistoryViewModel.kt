package com.bmdu.dhanlaxmi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.bmdu.dhanlaxmi.Api.RetrofitClient
import com.bmdu.dhanlaxmi.Model.HistoryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    sealed class HistoryState {
        object Loading : HistoryState()
        data class Success(val data: List<HistoryData>) : HistoryState()
        data class Error(val message: String) : HistoryState()
    }

    fun fetchHistory(token: String) {
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading
            try {
                Log.d(TAG, "fetchHistory → token: $token")
                val response = RetrofitClient.instance.bidHistory(token)
                Log.d(TAG, "fetchHistory → HTTP code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _historyState.value = HistoryState.Success(body.data)
                        Log.d(TAG, "fetchHistory: ✅ ${body.data.size} records loaded")
                    } else {
                        _historyState.value = HistoryState.Error("No history found")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "fetchHistory: ❌ code=${response.code()}, body=$err")
                    _historyState.value = HistoryState.Error("Failed to load history")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchHistory → Exception: ${e.localizedMessage}", e)
                _historyState.value = HistoryState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}