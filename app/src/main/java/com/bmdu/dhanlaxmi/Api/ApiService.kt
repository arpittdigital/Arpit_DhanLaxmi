package com.bmdu.dhanlaxmi.Api

import com.bmdu.dhanlaxmi.Model.BankDetailsRequest
import com.bmdu.dhanlaxmi.Model.BankdetailsResponse
import com.bmdu.dhanlaxmi.Model.ContactResponse
import com.bmdu.dhanlaxmi.Model.CreatePaymentRequest
import com.bmdu.dhanlaxmi.Model.CreatePaymentResponse
import com.bmdu.dhanlaxmi.Model.ForgotRequest
import com.bmdu.dhanlaxmi.Model.GETBankDetailsResponse
import com.bmdu.dhanlaxmi.Model.GameResponse
import com.bmdu.dhanlaxmi.Model.HistoryResponse
import com.bmdu.dhanlaxmi.Model.LoginRequest
import com.bmdu.dhanlaxmi.Model.NotificationResponse
import com.bmdu.dhanlaxmi.Model.PaymentHistoryResponse
import com.bmdu.dhanlaxmi.Model.PaymentSuccessRequest
import com.bmdu.dhanlaxmi.Model.PaymentSuccessResponse
import com.bmdu.dhanlaxmi.Model.PlayRequest
import com.bmdu.dhanlaxmi.Model.PlayResponse
import com.bmdu.dhanlaxmi.Model.ProfileResponse
import com.bmdu.dhanlaxmi.Model.ResultResponse
import com.bmdu.dhanlaxmi.Model.SignupRequest
import com.bmdu.dhanlaxmi.Model.SignupResponse
import com.bmdu.dhanlaxmi.Model.VerifyOtpRequest
import com.bmdu.dhanlaxmi.Model.WinningHistoryResponse
import com.bmdu.dhanlaxmi.Model.addfundsRequest
import com.bmdu.dhanlaxmi.Model.addfundsResponse
import com.bmdu.dhanlaxmi.Model.forgotResponse
import com.bmdu.dhanlaxmi.Model.loginresponse
import com.bmdu.dhanlaxmi.Model.playAndar
import com.bmdu.dhanlaxmi.Model.playAndarResponse
import com.bmdu.dhanlaxmi.Model.playBahar
import com.bmdu.dhanlaxmi.Model.playBaharResponse
import com.bmdu.dhanlaxmi.Model.resetPasswordRequest
import com.bmdu.dhanlaxmi.Model.resetPasswordResponse
import com.bmdu.dhanlaxmi.Model.verifyOtpResponse
import com.bmdu.dhanlaxmi.Model.withdrawalRequest
import com.bmdu.dhanlaxmi.Model.withdrawalResponse
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


