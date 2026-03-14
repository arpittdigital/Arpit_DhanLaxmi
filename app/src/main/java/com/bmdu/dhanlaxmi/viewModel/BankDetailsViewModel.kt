package com.bmdu.dhanlaxmi.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmdu.dhanlaxmi.Api.RetrofitClient
import com.bmdu.dhanlaxmi.Model.BankDetailsRequest
import com.bmdu.dhanlaxmi.Model.GETBankDetails
import com.bmdu.dhanlaxmi.Model.PaymentHistoryItem
import com.bmdu.dhanlaxmi.Model.addfundsRequest
import com.bmdu.dhanlaxmi.Model.withdrawalRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BankDetailsViewModel : ViewModel() {

    companion object {
        private const val TAG = "BankDetailsViewModel"
    }

    // ── General action state ───────────────────────────────
    private val _bankDetailsState = MutableStateFlow<BankDetailsState?>(null)
    val bankDetailsState: StateFlow<BankDetailsState?> = _bankDetailsState

    sealed class BankDetailsState {
        object Loading : BankDetailsState()
        data class Error(val message: String) : BankDetailsState()
        data class Success(val message: String) : BankDetailsState()
    }

    // ── GET saved bank details state ───────────────────────
    private val _savedBankState = MutableStateFlow<SavedBankState>(SavedBankState.Idle)
    val savedBankState: StateFlow<SavedBankState> = _savedBankState

    sealed class SavedBankState {
        object Idle : SavedBankState()
        object Loading : SavedBankState()
        data class Error(val message: String) : SavedBankState()
        data class Success(val data: GETBankDetails) : SavedBankState()
    }

    // ── Withdrawal history state ───────────────────────────
    private val _withdrawalHistoryState = MutableStateFlow<WithdrawalHistoryState>(WithdrawalHistoryState.Idle)
    val withdrawalHistoryState: StateFlow<WithdrawalHistoryState> = _withdrawalHistoryState

    sealed class WithdrawalHistoryState {
        object Idle : WithdrawalHistoryState()
        object Loading : WithdrawalHistoryState()
        data class Error(val message: String) : WithdrawalHistoryState()
        data class Success(val items: List<PaymentHistoryItem>) : WithdrawalHistoryState()
    }

    fun resetState() {
        _bankDetailsState.value = null
        Log.d(TAG, "resetState: State cleared")
    }

    // ══════════════════════════════════════════════════════════
    //  ADD FUNDS
    // ══════════════════════════════════════════════════════════
    fun addFunds(token: String?, amount: Int) {
        if (token.isNullOrBlank()) {
            _bankDetailsState.value = BankDetailsState.Error("Authentication token missing. Please login again.")
            return
        }
        if (amount <= 0) {
            _bankDetailsState.value = BankDetailsState.Error("Invalid amount.")
            return
        }
        viewModelScope.launch {
            _bankDetailsState.value = BankDetailsState.Loading
            try {
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = RetrofitClient.instance.AddFunds(authHeader, addfundsRequest(amount))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _bankDetailsState.value = BankDetailsState.Success(body.message ?: "Funds added successfully!")
                } else {
                    _bankDetailsState.value = BankDetailsState.Error(
                        when (response.code()) {
                            401  -> "Session expired. Please login again."
                            422  -> "Invalid amount or request data."
                            500  -> "Server error. Please try again later."
                            else -> "Something went wrong (code: ${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _bankDetailsState.value = BankDetailsState.Error(e.localizedMessage ?: "Network error.")
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  SAVE BANK DETAILS
    // ══════════════════════════════════════════════════════════
    fun saveBankDetails(token: String, bank_name: String, account_number: String, ifsc_code: String) {
        if (token.isBlank()) {
            _bankDetailsState.value = BankDetailsState.Error("Authentication token missing. Please login again.")
            return
        }
        if (bank_name.isBlank() || account_number.isBlank() || ifsc_code.isBlank()) {
            _bankDetailsState.value = BankDetailsState.Error("All fields are required.")
            return
        }
        viewModelScope.launch {
            _bankDetailsState.value = BankDetailsState.Loading
            try {
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = RetrofitClient.instance.bankdetails(
                    authHeader,
                    BankDetailsRequest(bank_name, account_number, ifsc_code)
                )
                if (response.isSuccessful && response.body() != null) {
                    _bankDetailsState.value = BankDetailsState.Success(
                        response.body()!!.message ?: "Bank details saved successfully!"
                    )
                } else {
                    _bankDetailsState.value = BankDetailsState.Error(
                        when (response.code()) {
                            401  -> "Session expired. Please login again."
                            409  -> "Bank details already exist."
                            422  -> "Invalid bank details."
                            500  -> "Server error. Please try again later."
                            else -> "Something went wrong (code: ${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                _bankDetailsState.value = BankDetailsState.Error(e.localizedMessage ?: "Network error.")
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  GET SAVED BANK DETAILS  (auto-fill jab Bank select ho)
    // ══════════════════════════════════════════════════════════
    fun getSavedBankDetails(token: String?) {
        if (token.isNullOrBlank()) {
            Log.e(TAG, "getSavedBankDetails: Token missing")
            _savedBankState.value = SavedBankState.Error("Token missing.")
            return
        }
        viewModelScope.launch {
            _savedBankState.value = SavedBankState.Loading
            try {
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                Log.d(TAG, "getSavedBankDetails: Calling GET api/bank-details...")
                val response = RetrofitClient.instance.getBankDetails(authHeader)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    if (data != null) {
                        Log.d(TAG, "getSavedBankDetails: ✅ bank=${data.bank_name}, acc=${data.account_number}")
                        _savedBankState.value = SavedBankState.Success(data)
                    } else {
                        Log.w(TAG, "getSavedBankDetails: data is null — no bank details saved yet")
                        _savedBankState.value = SavedBankState.Error("No bank details saved yet.")
                    }
                } else {
                    Log.e(TAG, "getSavedBankDetails: ❌ code=${response.code()}")
                    _savedBankState.value = SavedBankState.Error(
                        when (response.code()) {
                            401  -> "Session expired."
                            404  -> "No bank details found."
                            else -> "Error fetching bank details."
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "getSavedBankDetails: Exception → ${e.localizedMessage}")
                _savedBankState.value = SavedBankState.Error(e.localizedMessage ?: "Network error.")
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  WITHDRAWAL
    // ══════════════════════════════════════════════════════════
    fun withdrawal(
        token: String?,
        amount: Int,
        mobile: String,
        paymentMode: String,
        bankName: String?      = null,
        accountNumber: String? = null,
        ifscCode: String?      = null
    ) {
        if (token.isNullOrBlank()) {
            _bankDetailsState.value = BankDetailsState.Error("Authentication token missing. Please login again.")
            return
        }
        if (amount <= 0) {
            _bankDetailsState.value = BankDetailsState.Error("Invalid amount.")
            return
        }
        if (mobile.isBlank()) {
            _bankDetailsState.value = BankDetailsState.Error("Phone number is required.")
            return
        }
        if (paymentMode.isBlank()) {
            _bankDetailsState.value = BankDetailsState.Error("Please select a payment mode.")
            return
        }
        viewModelScope.launch {
            _bankDetailsState.value = BankDetailsState.Loading
            try {
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = RetrofitClient.instance.withdrawal(
                    authHeader,
                    withdrawalRequest(
                        amount         = amount,
                        mobile         = mobile,
                        payment_mode   = paymentMode,
                        bank_name      = bankName,
                        account_number = accountNumber,
                        ifsc_code      = ifscCode
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "withdrawal: ✅ ${response.body()!!.message}")
                    _bankDetailsState.value = BankDetailsState.Success(
                        response.body()!!.message ?: "Withdrawal request submitted successfully!"
                    )
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e(TAG, "withdrawal: ❌ code=${response.code()}, body=$errorBody")
                    _bankDetailsState.value = BankDetailsState.Error(
                        when (response.code()) {
                            401  -> "Session expired. Please login again."
                            400  -> "Invalid withdrawal request."
                            422  -> "Invalid amount or details."
                            500  -> "Server error. Please try again later."
                            else -> "Something went wrong (code: ${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "withdrawal: ❌ ${e.localizedMessage}")
                _bankDetailsState.value = BankDetailsState.Error(e.localizedMessage ?: "Network error.")
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  WITHDRAWAL HISTORY   (GET api/wallet/withdraws)
    // ══════════════════════════════════════════════════════════
    fun withdrawalHistory(token: String?) {
        if (token.isNullOrBlank()) {
            _withdrawalHistoryState.value = WithdrawalHistoryState.Error("Authentication token missing.")
            return
        }
        viewModelScope.launch {
            _withdrawalHistoryState.value = WithdrawalHistoryState.Loading
            try {
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                Log.d(TAG, "withdrawalHistory: Calling GET api/wallet/withdraws...")
                val response = RetrofitClient.instance.withdrawalHistory(authHeader)
                if (response.isSuccessful && response.body() != null) {
                    val items = response.body()!!.data ?: emptyList()
                    Log.d(TAG, "withdrawalHistory: ✅ ${items.size} records")
                    _withdrawalHistoryState.value = WithdrawalHistoryState.Success(items)
                } else {
                    Log.e(TAG, "withdrawalHistory: ❌ code=${response.code()}")
                    _withdrawalHistoryState.value = WithdrawalHistoryState.Error(
                        when (response.code()) {
                            401  -> "Session expired. Please login again."
                            500  -> "Server error. Please try again later."
                            else -> "Something went wrong (code: ${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "withdrawalHistory: ❌ ${e.localizedMessage}")
                _withdrawalHistoryState.value = WithdrawalHistoryState.Error(e.localizedMessage ?: "Network error.")
            }
        }
    }
}