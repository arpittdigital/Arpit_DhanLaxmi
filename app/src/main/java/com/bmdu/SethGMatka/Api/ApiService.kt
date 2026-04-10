package com.bmdu.SethGMatka.Api

import com.bmdu.SethGMatka.Model.BankDetailsRequest
import com.bmdu.SethGMatka.Model.BankdetailsResponse
import com.bmdu.SethGMatka.Model.ContactResponse
import com.bmdu.SethGMatka.Model.CreatePaymentRequest
import com.bmdu.SethGMatka.Model.CreatePaymentResponse
import com.bmdu.SethGMatka.Model.ForgotRequest
import com.bmdu.SethGMatka.Model.GETBankDetailsResponse
import com.bmdu.SethGMatka.Model.GameResponse
import com.bmdu.SethGMatka.Model.HistoryResponse
import com.bmdu.SethGMatka.Model.LoginRequest
import com.bmdu.SethGMatka.Model.NotificationResponse
import com.bmdu.SethGMatka.Model.PaymentHistoryResponse
import com.bmdu.SethGMatka.Model.PaymentSuccessRequest
import com.bmdu.SethGMatka.Model.PaymentSuccessResponse
import com.bmdu.SethGMatka.Model.PlayRequest
import com.bmdu.SethGMatka.Model.PlayResponse
import com.bmdu.SethGMatka.Model.ProfileResponse
import com.bmdu.SethGMatka.Model.ResultResponse
import com.bmdu.SethGMatka.Model.SignupRequest
import com.bmdu.SethGMatka.Model.SignupResponse
import com.bmdu.SethGMatka.Model.VerifyOtpRequest
import com.bmdu.SethGMatka.Model.WinningHistoryResponse
import com.bmdu.SethGMatka.Model.addfundsRequest
import com.bmdu.SethGMatka.Model.addfundsResponse
import com.bmdu.SethGMatka.Model.forgotResponse
import com.bmdu.SethGMatka.Model.loginresponse
import com.bmdu.SethGMatka.Model.playAndar
import com.bmdu.SethGMatka.Model.playAndarResponse
import com.bmdu.SethGMatka.Model.playBahar
import com.bmdu.SethGMatka.Model.playBaharResponse
import com.bmdu.SethGMatka.Model.resetPasswordRequest
import com.bmdu.SethGMatka.Model.resetPasswordResponse
import com.bmdu.SethGMatka.Model.verifyOtpResponse
import com.bmdu.SethGMatka.Model.withdrawalRequest
import com.bmdu.SethGMatka.Model.withdrawalResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {


    @POST("api/signup")
    suspend fun signup(
        @Body signupRequest: SignupRequest
    ): Response<SignupResponse>


    @POST("api/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<loginresponse>

    @POST("api/forgot-password")
    suspend fun forgotPassword(
        @Body forgotRequest: ForgotRequest
    ): Response<forgotResponse>

    @POST("api/verify-otp")
    suspend fun verifyOtp(
        @Body verifyOtpRequest: VerifyOtpRequest
    ): Response<verifyOtpResponse>

    @POST("api/reset-password")
    suspend fun resetPassword(
        @Body resetPasswordRequest: resetPasswordRequest
    ): Response<resetPasswordResponse>

    @POST("api/bank-details")
    suspend fun bankdetails(
        @Header("Authorization") token: String,
        @Body bankdetailsRequest: BankDetailsRequest
    ): Response<BankdetailsResponse>


    @POST("api/payment-success")
    suspend fun confirmPayment(
        @Header("Authorization") token: String,
        @Body request: PaymentSuccessRequest
    ): Response<PaymentSuccessResponse>


    @GET("api/games")
    suspend fun DashboardGame(
        @Header("Authorization") token: String,
    ): Response<GameResponse>


    @POST("api/wallet/add-funds")
    suspend fun AddFunds(
        @Header("Authorization") token: String?,
        @Body addfundsRequest: addfundsRequest
    ): Response<addfundsResponse>

    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @POST("api/game/{game_id}/play/add")
    suspend fun playGame(
        @Header("Authorization") token: String,
        @Path("game_id") gameId: Int,
        @Body playRequest: PlayRequest
    ): Response<PlayResponse>



    @GET("api/wallet/history")
    suspend fun bidHistory(
        @Header("Authorization") token: String
    ): Response<HistoryResponse>



    @GET("api/correct-answers")
    suspend fun getResult(
        @Header("Authorization") token: String
    ): Response<ResultResponse>

    @POST("api/wallet/withdraw")
    suspend fun withdrawal(
        @Header("Authorization") token: String,
        @Body withdrawalRequest: withdrawalRequest
    ): Response<withdrawalResponse>


    @GET("api/wallet/withdraws")
    suspend fun withdrawalHistory(
        @Header("Authorization") token: String
    ): Response<PaymentHistoryResponse>

    @GET("api/chart/filter")
    suspend fun getChartFilter(
        @Header("Authorization") token: String,
        @Query("month") month: String,
        @Query("year") year: String
    ): Response<ResultResponse>

    @GET("api/bank-details")
    suspend fun getBankDetails(
        @Header("Authorization") token: String
    ): Response<GETBankDetailsResponse>


    @POST("api/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<NotificationResponse>


    @POST("api/play-andar/{game_id}")
    suspend fun playAndar(
        @Header("Authorization") token: String,
        @Path("game_id") gameId: Int,
        @Body playAndar: playAndar
    ): Response<playAndarResponse>


    @POST("api/play-bahar/{game_id}")
    suspend fun playBahar(
        @Header("Authorization") token: String,
        @Path("game_id") gameId: Int,
        @Body playBahar: playBahar
    ): Response<playBaharResponse>

    @POST("api/create-payment")
    suspend fun createPayment(
        @Header("Authorization") token: String,
        @Body request: CreatePaymentRequest
    ): Response<CreatePaymentResponse>

    @GET("api/winning-history")
    suspend fun getWinningHistory(
        @Header("Authorization") token: String
    ): Response<WinningHistoryResponse>

    @GET("api/contacts")
    suspend fun getContacts(
        @Header("Authorization") token: String
    ): ContactResponse

    @GET
    suspend fun checkPaymentStatus(@Url checkLink: String): Response<String>
}


