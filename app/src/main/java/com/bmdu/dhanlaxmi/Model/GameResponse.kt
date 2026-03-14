package com.bmdu.dhanlaxmi.Model

data class GameResponse(
    val success: Boolean,
    val data: List<GameData>
)

data class GameData(
    val id: Int,
    val game_name: String? = "",
    val status: String? = "",
    val result_time: String? = "",
    val close_time: String? = "",
    val play_next_day: String? = "",
    val play_days: List<String>? = emptyList()
)
data class PlayRequest(
    val play_type: String,
    val number: String,
    val amount: Int
)

data class PlayResponse(
    val status: Boolean,
    val message: String,
    val play: PlayData,
    val balance: Int
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
    val data: List<ResultDate>
)

data class ResultDate(
    val date: String,
    val games: List<GameResult>
)

data class GameResult(
    val city_name: String,
    val correct_answer: String
)

data class playAndar(
    val number: String,
    val amount: Int
)

data class playAndarResponse(
    val status: Boolean,
    val message: String,
    val andardata: PlayData,
    val balance: Int
)


data class playBahar(
    val number: String,
    val amount: Int
)

data class playBaharResponse(
    val status: Boolean,
    val message: String,
    val bahardata: PlayData,
    val balance: Int
)

