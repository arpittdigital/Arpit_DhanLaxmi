package com.bmdu.SethGMatka

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bmdu.SethGMatka.Dashboard.*
import com.bmdu.SethGMatka.Model.TokenManager
import com.bmdu.SethGMatka.auth.*
import com.bmdu.SethGMatka.presentation.DelhiBazarScreen
import com.bmdu.SethGMatka.screens.SignupScreen
import com.bmdu.SethGMatka.ui.theme.DhanLaxmiTheme
import com.bmdu.SethGMatka.viewModel.AuthViewModel
import com.bmdu.SethGMatka.viewModel.ProfileViewModel
import com.d_shield_parent.presentation.auth.WinningHistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        setIntent(intent)
        setContent {


            val context = LocalContext.current
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()

            val profileViewModel: ProfileViewModel = viewModel()
            val profileState by profileViewModel.profileState.collectAsState()

            val walletBalance = when (val s = profileState) {
                is ProfileViewModel.ProfileState.Success ->
                    s.data.data?.wallet_amount?.toInt() ?: 0

                else -> 0
            }

            val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("auth_token", "") ?: ""

            LaunchedEffect(token) {
                if (token.isNotBlank()) {
                    profileViewModel.fetchProfile("Bearer $token")
                }
            }
            RequestNotificationPermission()
            //notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* granted or not */ }
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }


            // Handle notification tap (app killed or backgrounded)
            LaunchedEffect(navController) {
                val navigateTo = intent?.getStringExtra("navigateTo") ?: ""
                val id = intent?.getStringExtra("id") ?: ""

                if (navigateTo.isNotBlank()) {
                    when (navigateTo) {
                        "order_detail" -> navController.navigate("order/$id")
                        "task_detail" -> navController.navigate("task/$id")
                        "payment" -> navController.navigate("payment")
                        "profile" -> navController.navigate("profile")
                    }
                }
            }


            DhanLaxmiTheme {
                Box (modifier = Modifier.fillMaxSize(),
//                        contentWindowInsets = WindowInsets(0.dp)
                )
                {
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
//                        enterTransition = {
//                            fadeIn(animationSpec = tween(300))
//                        },
//                        exitTransition = {
//                            fadeOut(animationSpec = tween(300))
//                        }

                        ) {
                            composable("splash") {
                                SplashScreen(
                                    navController,
                                    context = applicationContext
                                )
                            }
                            composable("login") { LoginScreen(navController) }
                            composable("register") { SignupScreen(navController) }
                            composable("forgotpassword") {
                                ForgotPassword(navController, authViewModel)
                            }
                            composable("verifypassword") {
                                VerifyOtpScreen(navController, authViewModel)
                            }
                            composable("resetpassword") {
                                ResetPassword(navController, authViewModel)
                            }
                            composable("resetsuccess") { ResetSuccessScreen(navController) }

                            // ── Main Screens ─────────────────────────────
                            composable("home") {
                                HomeScreen(
                                    navController    = navController,
                                    profileViewModel = profileViewModel  // ← pass same instance
                                )
                            }
                            composable("profile_screen") {
                                ProfileScreen(
                                    navController    = navController,
                                    profileViewModel = profileViewModel  // ← pass same instance
                                )
                            }
                            composable("add_money") { AddMoneyScreen(navController) }
                            composable("withdrawal") { WithdrawalMoneyScreen(navController) }
                            composable(
                                route = "qr_payment/{amount}",
                                arguments = listOf(
                                    navArgument("amount") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val amount = backStackEntry.arguments?.getInt("amount") ?: 0
                                QRPaymentScreen(
                                    navController = navController,
                                    amount = amount,
                                    profileViewModel = profileViewModel
                                )
                            }
                            composable("bank_details") { BankDetailsScreen(navController) }
                            composable("winning_history") {
                                val context = LocalContext.current
                                val token = TokenManager.getToken(context)

                                WinningHistoryScreen(navController = navController, token = token)
                            }
                            composable("privacy_policy") {
                                PrivacyPolicyScreen(navController = navController)
                            }
                            composable("history") {
                                val sharedPref =
                                    context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                                val token = sharedPref.getString("auth_token", "") ?: ""
                                Log.d("NAV", "History token: $token")
                                HistoryScreen(
                                    token = "Bearer $token",
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("chart") { ChartScreen(navController) }
                            composable("result") { ResultScreen(navController) }
                            composable("withdraw_history") { WithdrawalHistoryScreen(navController) }
                            composable("how_to_play") { HowToPlayScreen(navController) }
                            composable("game_rate") { GameRatesScreen(navController) }
                            composable("contact_us") { ContactUsScreen(navController) }
                            composable("notification") { NotificationScreen(navController) }
//                        composable("game_result")  {"" }

                            composable("delhi_bazar/{gameId}/{gameName}") { backStack ->
                                val gameId =
                                    backStack.arguments?.getString("gameId")?.toIntOrNull() ?: 0
                                val gameName = backStack.arguments?.getString("gameName") ?: "Game"
                                DelhiBazarScreen(
                                    navController = navController,
                                    gameId = gameId,
                                    gameName = gameName,
                                    profileViewModel = profileViewModel

                                )
                            }
                        }
                    }
                }
            }
        }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // updates the intent so LaunchedEffect re-reads it
    }
    }
@Composable
fun RequestNotificationPermission() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // optional: show a snackbar or rationale dialog
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
