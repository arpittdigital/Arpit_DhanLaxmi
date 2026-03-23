package com.bmdu.dhanlaxmi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.bmdu.dhanlaxmi.Api.RetrofitClient
import com.bmdu.dhanlaxmi.Model.HistoryItem
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
        data class Success(val data: List<HistoryItem>) : HistoryState()
        data class Error(val message: String) : HistoryState()
    }

    fun fetchHistory(token: String) {
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading
            try {
                val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = RetrofitClient.instance.bidHistory(bearerToken)

                when {
                    response.isSuccessful && response.body() != null -> {
                        val body = response.body()!!
                        if (body.success && body.data.isNotEmpty()) {
                            _historyState.value = HistoryState.Success(body.data)
                        } else {
                            _historyState.value = HistoryState.Success(emptyList()) // ← empty not error
                        }
                    }
                    else -> {
                        // ← 500 or any other error shows empty state not error screen
                        _historyState.value = HistoryState.Success(emptyList())
                    }
                }
            } catch (e: Exception) {
                _historyState.value = HistoryState.Success(emptyList()) // ← never show error
            }
        }
    }
}