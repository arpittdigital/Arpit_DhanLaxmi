package com.bmdu.dhanlaxmi.Model

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    val success: Boolean,
    val data: List<HistoryData>
)

data class HistoryData(
    val amount: String,
    val type: String,
    val created_at: String
)

data class WinningHistoryResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("data")   val data: List<WinningHistoryItem>
)

data class WinningHistoryItem(
    @SerializedName("game_id")        val gameId: Int,
    @SerializedName("game_name")      val gameName: String,
    @SerializedName("play_type")      val playType: String,
    @SerializedName("number")         val number: String,
    @SerializedName("amount")         val amount: Int,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("played_date")    val playedDate: String,
    @SerializedName("status")         val status: String
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