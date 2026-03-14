package com.bmdu.dhanlaxmi.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bmdu.dhanlaxmi.Api.RetrofitClient
import com.bmdu.dhanlaxmi.Model.GameData
import com.bmdu.dhanlaxmi.Model.PlayData
import com.bmdu.dhanlaxmi.Model.PlayRequest
import com.bmdu.dhanlaxmi.Model.ResultDate
import com.bmdu.dhanlaxmi.Model.playAndar
import com.bmdu.dhanlaxmi.Model.playBahar
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
        object Idle : PlayState()
        object Loading : PlayState()
        data class Error(val message: String) : PlayState()
        data class Success(val data: PlayData) : PlayState()
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
                        _playState.value = PlayState.Success(
                            data = body.play
                        )
                        Log.d(TAG, "playGame: ✅ ${body.message}")
                    } else {
                        // HTTP 200 aaya lekin status false
                        _playState.value = PlayState.Error(body.message)
                        Log.e(TAG, "playGame: ❌ API returned status=false: ${body.message}")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "playGame: ❌ code=${response.code()}, body=$err")
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
                    if (body.status){
                        _playState.value = PlayState.Success(
                            data = body.bahardata
                        )

                    }
                    else {
                        // HTTP 200 aaya lekin status false
                        _playState.value = PlayState.Error(body.message)
                        Log.e(TAG, "playGame: ❌ API returned status=false: ${body.message}")
                    }
                }
                else {
                    val err = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "playGame: ❌ code=${response.code()}, body=$err")
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
                        _playState.value = PlayState.Success(
                            data = body.andardata
                        )

                    }
                    else {
                        // HTTP 200 aaya lekin status false
                        _playState.value = PlayState.Error(body.message)
                        Log.e(TAG, "playGame: ❌ API returned status=false: ${body.message}")
                    }
                }
                else {
                    val err = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "playGame: ❌ code=${response.code()}, body=$err")
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

    fun getresult(token: String) {
        viewModelScope.launch {
            _resultState.value = ResultState.Loading
            try {
                Log.d(TAG, "getresult → fetching results")
                val response = RetrofitClient.instance.getResult(token)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _resultState.value = ResultState.Success(body.data)
                        Log.d(TAG, "getresult: ✅ loaded")
                    } else {
                        _resultState.value = ResultState.Error("No results found")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "getresult: ❌ code=${response.code()}, body=$err")
                    _resultState.value = ResultState.Error("Failed to load results")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getresult → Exception: ${e.localizedMessage}", e)
                _resultState.value = ResultState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}