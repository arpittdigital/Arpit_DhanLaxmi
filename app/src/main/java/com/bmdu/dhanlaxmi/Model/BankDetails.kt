
package com.bmdu.dhanlaxmi.Model

data class BankDetailsRequest(
    val bank_name: String,
    val account_number: String,
    val ifsc_code: String
)

data class BankdetailsResponse(
    val success: Boolean,
    val message: String?,
    val data: BankDetails?
)

data class BankDetails(
    val user_id: Int,
    val bank_name: String,
    val account_number: String,
    val id: Int,
    val ifsc_code: String
)

data class addfundsRequest(
    val amount: Int
)

data class addfundsResponse(
    val success: Boolean,
    val amount: Int?,
    val message: String?
)

data class withdrawalRequest(
    val amount: Int,
    val mobile: String,
    val payment_mode: String,
    val bank_name: String?      = null,
    val account_number: String? = null,
    val ifsc_code: String?      = null
)

data class withdrawalResponse(
    val success: Boolean,
    val message: String?
)

// GET bank-details response
data class GETBankDetailsResponse(
    val success: Boolean,
    val data: GETBankDetails?
)

data class GETBankDetails(
    val id: Int?,
    val user_id: Int?,
    val bank_name: String?,
    val account_number: String?,
    val ifsc_code: String?,
    val created_at: String?,
    val updated_at: String?
)

// Withdrawal history response  (GET api/wallet/withdraws)
data class PaymentHistoryResponse(
    val success: Boolean,
    val message: String?,
    val data: List<PaymentHistoryItem>?
)

data class PaymentHistoryItem(
    val id: Int,
    val user_id: Int,
    val amount: Int,
    val mobile: String,
    val payment_mode: String,
    val status: String,               // "pending" | "approved" | "rejected"
    val bank_name: String?      = null,
    val account_number: String? = null,
    val ifsc_code: String?      = null,
    val created_at: String?     = null,
    val updated_at: String?     = null
)

//package com.bmdu.dhanlaxmi.Model
//
//data class BankDetailsRequest(
//    val bank_name: String,
//    val account_number: String,
//    val ifsc_code: String
//)
//
//data class BankdetailsResponse(
//    val success: Boolean,
//    val message: String?,
//    val data: BankDetails?
//)
//
//data class BankDetails(
//    val user_id: Int,
//    val bank_name: String,
//    val account_number: String,
//    val id: Int,
//    val ifsc_code: String
//)
//
//data class addfundsRequest(
//    val amount: Int
//)
//
//data class addfundsResponse(
//    val success: Boolean,
//    val amount: Int?,
//    val message: String?
//)
//
//data class withdrawalRequest(
//    val amount: Int,
//    val mobile: String,
//    val payment_mode: String,
//    val bank_name: String?       = null,
//    val account_number: String?  = null,
//    val ifsc_code: String?       = null
//)
//
//data class withdrawalResponse(
//    val success: Boolean,
//    val message: String?
//)
//
//
//data class BankDetailsResponse(
//    val success: Boolean,
//    val data: GETBankDetails?
//)
//
//data class GETBankDetails(
//    val id: Int?,
//    val user_id: Int?,
//    val bank_name: String?,
//    val account_number: String?,
//    val ifsc_code: String?,
//    val created_at: String?,
//    val updated_at: String?
//)