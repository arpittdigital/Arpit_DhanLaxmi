package com.bmdu.dhanlaxmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bmdu.dhanlaxmi.Dashboard.*
import com.bmdu.dhanlaxmi.Model.TokenManager
import com.bmdu.dhanlaxmi.auth.*
import com.bmdu.dhanlaxmi.presentation.DelhiBazarScreen
import com.bmdu.dhanlaxmi.screens.SignupScreen
import com.bmdu.dhanlaxmi.ui.theme.DhanLaxmiTheme
import com.bmdu.dhanlaxmi.viewModel.AuthViewModel
import com.d_shield_parent.presentation.auth.WinningHistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()
            DhanLaxmiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController    = navController,
                        startDestination = "splash",
                        modifier         = Modifier.padding(innerPadding)
                    ) {
                        composable("splash")        { SplashScreen(navController,context = applicationContext) }
                        composable("login")         { LoginScreen(navController) }
                        composable("register")      { SignupScreen(navController) }
                        composable("forgotpassword"){
                            ForgotPassword(navController, authViewModel)
                        }
                        composable("verifypassword"){
                            VerifyOtpScreen(navController, authViewModel)
                        }
                        composable("resetpassword"){
                            ResetPassword(navController, authViewModel)
                        }
                        composable("resetsuccess")  { ResetSuccessScreen(navController) }

                        // ── Main Screens ─────────────────────────────
                        composable("home")          { HomeScreen(navController) }
                        composable("profile_screen"){ ProfileScreen(navController) }
                        composable("add_money")     { AddMoneyScreen(navController) }
                        composable("withdrawal")    { WithdrawalMoneyScreen(navController) }
                        composable(
                            route = "qr_payment/{amount}",
                            arguments = listOf(
                                navArgument("amount") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val amount = backStackEntry.arguments?.getInt("amount") ?: 0
                            QRPaymentScreen(
                                navController = navController,
                                amount        = amount
                            )
                        }
                        composable("bank_details")  { BankDetailsScreen(navController) }
                        composable("winning_history") {
                            val context = LocalContext.current
                            val token   = TokenManager.getToken(context)

                            WinningHistoryScreen(navController = navController, token = token)
                        }
                        composable("privacy_policy") {
                            PrivacyPolicyScreen(navController = navController)
                        }
                        composable("history")       { HistoryScreen(navController) }
                        composable("chart")         { ChartScreen(navController) }
                        composable("result") { ResultScreen(navController)}
                        composable("withdraw_history"){ WithdrawalHistoryScreen(navController) }
                        composable("how_to_play")  { HowToPlayScreen(navController) }
                        composable("game_rate"){ GameRatesScreen(navController)}
                        composable("contact_us")   { ContactUsScreen(navController) }
                        composable("notification") {NotificationScreen(navController)}

                        composable("delhi_bazar/{gameId}/{gameName}") { backStack ->
                            val gameId = backStack.arguments?.getString("gameId")?.toIntOrNull() ?: 0
                            val gameName = backStack.arguments?.getString("gameName") ?: "Game"
                            DelhiBazarScreen(
                                navController = navController,
                                gameId = gameId,
                                gameName = gameName
                            )
                        }
                    }
                }
            }
        }
    }
}