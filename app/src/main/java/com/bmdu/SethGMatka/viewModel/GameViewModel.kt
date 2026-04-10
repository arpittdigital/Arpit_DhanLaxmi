package com.bmdu.SethGMatka.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmdu.SethGMatka.Api.RetrofitClient
import com.bmdu.SethGMatka.Model.GameData
import com.bmdu.SethGMatka.Model.PlayData
import com.bmdu.SethGMatka.Model.PlayRequest
import com.bmdu.SethGMatka.Model.ResultDate
import com.bmdu.SethGMatka.Model.playAndar
import com.bmdu.SethGMatka.Model.playBahar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    companion object {
        private const val TAG = "GameViewModel"
    }

    // ══════════════════════════════════════════════════════
    //  GAME LIST STATE
    // ══════════════════════════════════════════════════════
    private val _gamestate = MutableStateFlow<GameState>(GameState.Loading)
    val gamestate: StateFlow<GameState> = _gamestate

    private val _balance = MutableStateFlow(0)
    val balance: StateFlow<Int> = _balance

    sealed class GameState {
        object Loading : GameState()
        data class Error(val message: String) : GameState()
        data class Success(val games: List<GameData>) : GameState()
    }

    fun fetchGames(token: String) {
        viewModelScope.launch {
            _gamestate.value = GameState.Loading
            try {
                val response = RetrofitClient.instance.DashboardGame(token)
                if (response.isSuccessful && response.body() != null) {
                    _gamestate.value = GameState.Success(response.body()!!.data)
                    Log.d(TAG, "fetchGames: ✅ ${response.body()!!.data.size} games loaded")
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "fetchGames: ❌ code=${response.code()}, body=$err")
                    _gamestate.value = GameState.Error("Something went wrong")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchGames → Exception: ${e.localizedMessage}")
                _gamestate.value = GameState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private val _playState = MutableStateFlow<PlayState>(PlayState.Idle)
    val playState: StateFlow<PlayState> = _playState

    sealed class PlayState {
        object Idle    : PlayState()
        object Loading : PlayState()
        data class Error(val message: String)   : PlayState()
        data class Success(val message: String) : PlayState()  // ← String not PlayData?
    }

    fun playGame(
        token: String,
        gameId: Int,
        playType: String,
        number: String,
        amount: Int
    ) {
        viewModelScope.launch {
            _playState.value = PlayState.Loading
            try {
                Log.d(
                    TAG,
                    "playGame → gameId=$gameId, type=$playType, number=$number, amount=$amount"
                )

                val response = RetrofitClient.instance.playGame(
                    token = token,
                    gameId = gameId,
                    playRequest = PlayRequest(
                        play_type = playType,
                        number = number,
                        amount = amount
                    )
                )

                Log.d(TAG, "playGame → HTTP code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d(TAG, "playGame → status=${body.status}, message=${body.message}")

                    // API "status: true" check
                    if (body.status) {
                        _balance.value = body.balance.toDoubleOrNull()?.toInt() ?: 0 // ← parse String
                        _playState.value = PlayState.Success(body.message)
                        Log.d(TAG, "playGame:  ${body.message}")
                    } else {

                        _playState.value = PlayState.Error(body.message)
                        Log.e(TAG, "playGame:  API returned status=false: ${body.message}")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "playGame:  code=${response.code()}, body=$err")
                    _playState.value = PlayState.Error("Failed to place bid. Please try again.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "playGame → Exception: ${e.localizedMessage}", e)
                _playState.value = PlayState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }


    fun baharPlayGame(
        token: String,
        gameId: Int,
        number: String,
        amount: Int
    ){
        viewModelScope.launch {
            _playState.value = PlayState.Loading
            try {
                val response = RetrofitClient.instance.playBahar(
                    token = token,
                    gameId = gameId,
                    playBahar = playBahar(
                        number = number,
                        amount = amount
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status) {
                        _balance.value = body.balance.toDoubleOrNull()?.toInt() ?: 0
                        _playState.value = PlayState.Success(body.message)  // ← same
                    }
                    else {

                        _playState.value = PlayState.Error(body.message)
                        Log.e(TAG, "playGame: API returned status=false: ${body.message}")
                    }
                }
                else {
                    val err = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "playGame: code=${response.code()}, body=$err")
                    _playState.value = PlayState.Error("Failed to place bid. Please try again.")
                }

            }catch (e: Exception){
                _playState.value = PlayState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun andarPlayGame(
        token: String,
        gameId: Int,
        number: String,
        amount: Int
    ){
        viewModelScope.launch {
            _playState.value = PlayState.Loading
            try {
                val response = RetrofitClient.instance.playAndar(
                    token = token,
                    gameId = gameId,
                    playAndar = playAndar(
                        number = number,
                        amount = amount
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status){
                        _balance.value = body.balance.toDoubleOrNull()?.toInt() ?: 0
                        _playState.value = PlayState.Success(body.message)

                    }
                    else {

                        _playState.value = PlayState.Error(body.message)
                        Log.e(TAG, "playGame: API returned status=false: ${body.message}")
                    }
                }
                else {
                    val err = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "playGame: code=${response.code()}, body=$err")
                    _playState.value = PlayState.Error("Failed to place bid. Please try again.")
                }

            }catch (e: Exception){
                _playState.value = PlayState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun resetPlayState() {
        _playState.value = PlayState.Idle
    }

    private val _resultState = MutableStateFlow<ResultState>(ResultState.Loading)
    val resultState: StateFlow<ResultState> = _resultState

    sealed class ResultState {
        object Loading : ResultState()
        data class Success(val data: List<ResultDate>) : ResultState()
        data class Error(val message: String) : ResultState()
    }

    fun getresult(token: String, month: String? = null, year: String? = null) {
        viewModelScope.launch {
            if (token.isBlank() || token == "Bearer ") {
                _resultState.value = ResultState.Error("Not logged in. Please login again.")
                return@launch
            }
            _resultState.value = ResultState.Loading
            try {
                val response = if (month != null && year != null) {
                    RetrofitClient.instance.getChartFilter(token, month, year)
                } else {
                    RetrofitClient.instance.getResult(token)
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _resultState.value = ResultState.Success(body.data)
                    } else {
                        _resultState.value = ResultState.Error("No results found")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    _resultState.value = ResultState.Error("HTTP ${response.code()}: $err")
                }
            } catch (e: Exception) {
                _resultState.value = ResultState.Error("${e.javaClass.simpleName}: ${e.localizedMessage}")
            }
        }
    }
    }