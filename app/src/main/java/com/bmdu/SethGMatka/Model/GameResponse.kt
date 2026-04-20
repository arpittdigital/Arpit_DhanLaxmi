package com.bmdu.SethGMatka.Model

data class GameResponse(
    val success: Boolean,
    val data: List<GameData>
)

data class GameData(
    val id: Int?,
    val game_name: String?,
    val status: String?,
    val result_time: String?,
    val open_time: String?,
    val close_time: String?,
    val play_next_day: String?,
    val play_days: List<String>?,
    val old_number: String?,
    val new_number: String?,
    val number: String?,          // the declared result number
    val result_status: String?,   // "NEW", "OLD", or null/empty
    val andar: String?,
    val bahar: String?
)
data class PlayRequest(
    val play_type: String,
    val number: String,
    val amount: Int,
    val palti: Boolean
)

data class PlayResponse(
    val status: Boolean,
    val message: String,
    val plays   : List<PlayData>?,
    val balance : String
)

data class PlayData(
    val user_id: Int,
    val game_id: String,
    val play_type: String,
    val number: String,
    val amount: Int,
    val status: String,
    val id: Int,
    val created_at: String,
    val updated_at: String
)


data class ResultResponse(
    val success: Boolean,
    val month: String? = null,
    val year: String? = null,
    val data: List<ResultDate>
)

data class ResultDate(
    val date: String,
    val games: List<GameResult>
)

data class GameResult(
    val game_name: String,
    val correct_answer: String?
)

data class playAndar(
    val number: String,
    val amount: Int
)

data class AndarPlayData(
    val user_id    : Int,
    val game_id    : String,
    val number     : String,   // ← String (safe for both)
    val amount     : String,   // ← String (safe for both)
    val status     : String,
    val id         : Int,
    val created_at : String,
    val updated_at : String
)

data class playAndarResponse(
    val status   : Boolean,
    val message  : String,
    val data     : AndarPlayData?,  // ← "data" not "andardata", nullable
    val balance  : String           // ← String not Int
)


data class playBahar(
    val number: String,
    val amount: Int
)

data class playBaharResponse(
    val status   : Boolean,
    val message  : String,
    val data     : AndarPlayData?,  // ← same "data" key, reuse AndarPlayData
    val balance  : String           // ← String not Int
)

