package com.bmdu.SethGMatka.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmdu.SethGMatka.Api.RetrofitClient
import com.bmdu.SethGMatka.Model.HistoryItem
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
                            val formatter =
                                java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.US)
                            val sorted = body.data.sortedByDescending { item ->
                                runCatching { formatter.parse(item.date) }.getOrNull()
                                    ?: java.util.Date(0)
                            }
                            _historyState.value =
                                HistoryState.Success(sorted) // ← was body.data, now sorted
                        } else {
                            _historyState.value = HistoryState.Success(emptyList())
                        }
                    }
                }
            } catch (e: Exception) {
                _historyState.value = HistoryState.Success(emptyList()) // ← never show error
            }
        }
    }
}