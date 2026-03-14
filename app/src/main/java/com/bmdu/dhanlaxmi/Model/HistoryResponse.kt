package com.bmdu.dhanlaxmi.Model

data class HistoryResponse(
    val success: Boolean,
    val data: List<HistoryData>
)

data class HistoryData(
    val amount: String,
    val type: String,
    val created_at: String
)

//data class PaymentHistoryResponse(
//    val success: Boolean,
//    val data: List<PaymentHistory>
//)
//
//data class PaymentHistory(
//    val id: Int,
//    val amount: String,
//    val payment_mode: String,
//    val mobile: String,
//    val status: String,
//    val created_at: String
//)