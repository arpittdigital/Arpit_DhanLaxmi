package com.bmdu.SethGMatka.Dashboard


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bmdu.SethGMatka.viewModel.BankDetailsViewModel
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap

// ─── QR Code Generator using ZXing ───────────────────────
// Add to build.gradle:
// implementation 'com.google.zxing:core:3.5.2'
// implementation 'androidx.compose.ui:ui-graphics'

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bmdu.SethGMatka.viewModel.PaymentViewModel
import com.bmdu.SethGMatka.viewModel.ProfileViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter


// ── UPI QR Data — replace with your actual UPI ID ────────
//private const val UPI_ID    = "yourname@upi"
//private const val UPI_NAME  = "Dhan Laxmi"
//private const val CURRENCY  = "INR"

// ══════════════════════════════════════════════════════════
//  QR PAYMENT SCREEN
// ══════════════════════════════════════════════════════════

private const val QR_TAG = "QRPaymentScreen"

@Composable
fun QRPaymentScreen(
    navController: NavController,
    amount: Int,
    profileViewModel : ProfileViewModel
) {
    // ── Use new PaymentViewModel instead of BankDetailsViewModel ──
    val viewModel: PaymentViewModel = viewModel()
    val state   by viewModel.state.collectAsState()
    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = prefs.getString("auth_token", null)
    val bearerToken = if (token?.startsWith("Bearer ") == true) token else "Bearer $token"
    Log.d("PAYMENT", "token = '$token'")
    Log.d("PAYMENT", "bearerToken = '$bearerToken'")
    val profileState by profileViewModel.profileState.collectAsState()
    val customerId = when (val s = profileState) {
        is ProfileViewModel.ProfileState.Success ->
            s.data.data?.customer_id
        else -> null
    }

//    val minDeposit = 50
    val minDeposit = 50
    val maxDeposit = 10000

    // ── Call create-payment API when screen opens ────────
    LaunchedEffect(Unit) {

        Log.d("PAYMENT", "token = '$token'")
        Log.d("PAYMENT", "bearerToken = '$bearerToken'")
        Log.d("PAYMENT", "amount = $amount")
        viewModel.setUserId(customerId)
        viewModel.createPayment(bearerToken, amount)
    }

    // ── Start polling when user returns from payment page ─
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val s = viewModel.state.value
                if (s is PaymentViewModel.PaymentState.ReadyToPay) {
                    viewModel.startPolling(
                        checkLink = s.result.checkLink,
                        token     = bearerToken   // ← add token

                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.resetState()
        }
    }

    // ── Navigate home on success ─────────────────────────
    LaunchedEffect(state) {
        when (val s = state) {
            is PaymentViewModel.PaymentState.ReadyToPay -> {
                // Start polling as soon as QR is ready (no need to wait for ON_RESUME)
                viewModel.startPolling(
                    checkLink = s.result.checkLink,
                    token = bearerToken
                )
            }
            is PaymentViewModel.PaymentState.Success -> {
                Log.d("PAYMENT", "Payment success → navigating home")
                profileViewModel.fetchProfile(bearerToken)
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is PaymentViewModel.PaymentState.Error -> {
                Log.e("PAYMENT", "❌ Payment failed: ${s.message}")
            }
            else -> Unit
        }
    }

    val isLoading = state is PaymentViewModel.PaymentState.Loading

    // ── UI ───────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFFFE500)
            )
    ) {

        // ── Top Bar ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background (Color(0xFF222222))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint     = Color(0xFFFFE500),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = !isLoading) { navController.navigateUp() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Payment",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFFFE500)
            )
        }

        // ── Body ─────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Min / Max Row ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(text = "Min. Deposit : ₹$minDeposit", modifier = Modifier.weight(1f))
                InfoChip(text = "Max. Deposit : ₹$maxDeposit", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(32.dp))

            when (val s = state) {

                // ── Creating payment ──────────────────────
                is PaymentViewModel.PaymentState.Loading -> {
                    CircularProgressIndicator(color = Color(0xFFF3EE06))
                    Spacer(Modifier.height(16.dp))
                    Text("Creating payment...", color = Color.White)
                }

                // ── Ready — show PAY NOW button ───────────
                is PaymentViewModel.PaymentState.ReadyToPay -> {

                    // ── UPI Launcher — returns user back automatically ──
                    val upiLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val response = result.data?.getStringExtra("response") ?: ""
                        when {
                            response.contains("SUCCESS", ignoreCase = true) ||
                                    response.contains("PAID",    ignoreCase = true) -> {
                                viewModel.startPolling(
                                    checkLink = s.result.checkLink,
                                    token     = bearerToken,  // ← add
                                          // ← add
                                )
                            }
                            response.contains("FAILURE", ignoreCase = true) ||
                                    response.contains("FAILED",  ignoreCase = true) -> {
                                viewModel.startPolling(
                                    checkLink = s.result.checkLink,
                                    token     = bearerToken,  // ← add
                                          // ← add
                                )// server is source of truth
                            }
                            else -> {
                                // user cancelled or closed the app — do nothing
                            }
                        }
                    }

                    Text("Pay ₹$amount", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF3EE06))
                    Spacer(Modifier.height(8.dp))
                    Text("Choose payment method", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(32.dp))

                    // ── GPay ──────────────────────────────────────────
                    PaymentAppButton(
                        label = "Google Pay",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(s.result.bhimLink))
                                upiLauncher.launch(intent)
                            } catch (e: Exception) {
                                // fallback if no app handles it
                                Toast.makeText(context, "No app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    // ── PhonePe ───────────────────────────────────────
                    PaymentAppButton(
                        label = "PhonePe",
                        onClick = {
                            try {
                                val phonepeLink = s.result.bhimLink.replace("upi://", "phonepe://")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(phonepeLink))
                                upiLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Fallback to generic UPI link if PhonePe not installed
                                Toast.makeText(context, "No app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

// ── Paytm ─────────────────────────────────────────
                    PaymentAppButton(
                        label = "Paytm",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(s.result.paytmLink))
                                upiLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Fallback to generic UPI link if Paytm not installed
                                Toast.makeText(context, "No app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    // ── Other UPI Apps — Chrome Tab (ON_RESUME handles polling) ──
                    PaymentAppButton(
                        label = "Other UPI App",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(s.result.paymentUrl))
                            context.startActivity(intent)  // ← regular browser, ON_RESUME fires correctly
                        }
                    )

                    Spacer(Modifier.height(24.dp))
                    Text("Order ID: ${s.result.orderId}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick  = { navController.navigateUp() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(10.dp),
                        border   = BorderStroke(1.dp, Color(0xFF004D00)),
                        colors   = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF003300))
                    ) {
                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF3EE06))
                    }
                }

                // ── Verifying payment after returning ─────
                is PaymentViewModel.PaymentState.Idle -> {
                    CircularProgressIndicator(color = Color(0xFFF3EE06))
                    Spacer(Modifier.height(16.dp))
                    Text("Verifying payment...", color = Color.White)
                }

                // ── Error ─────────────────────────────────
                is PaymentViewModel.PaymentState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(containerColor = Color(0xFFB00020)),
                        shape    = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            s.message,
                            color    = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick  = { viewModel.createPayment(token, amount) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF002800)
                        )
                    ) {
                        Text(
                            "Retry",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick  = { navController.navigateUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape  = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF004D00)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF003300)
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFFF3EE06)
                        )
                    }
                }

                else -> Unit
            }
        }
    }
}
// Opens UPI app directly
fun openUpiApp(context: Context, upiLink: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiLink))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        // App not installed — fallback to Chrome Tab
        Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
    }
}

// Reusable button composable
@Composable
fun PaymentAppButton(label: String, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF002800))
    ) {
        Text(
            label,
            fontSize      = 16.sp,
            fontWeight    = FontWeight.Bold,
            color         = Color.White,
            letterSpacing = 1.sp
        )
    }
}



// ── Generate UPI deep-link for QR ───────────────────
//    // Format: upi://pay?pa=UPI_ID&pn=NAME&am=AMOUNT&cu=INR
//    val upiString = "upi://pay?pa=$UPI_ID&pn=${UPI_NAME.replace(" ", "%20")}&am=$amount&cu=$CURRENCY"
//    val qrBitmap: ImageBitmap? = remember(upiString) {
//        generateQRBitmap(upiString, 600)
//    }


//fun generateQRBitmap(content: String, size: Int): ImageBitmap? {
//    return try {
//        val hints = mapOf(EncodeHintType.MARGIN to 1)
//        val bits  = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
//        val bmp   = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
//        for (x in 0 until size) {
//            for (y in 0 until size) {
//                bmp.setPixel(x, y, if (bits[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
//            }
//        }
//        bmp.asImageBitmap()
//    } catch (e: Exception) {
//        Log.e(QR_TAG, "generateQRBitmap: Failed to generate QR → ${e.message}", e)
//        null
//    }
//}