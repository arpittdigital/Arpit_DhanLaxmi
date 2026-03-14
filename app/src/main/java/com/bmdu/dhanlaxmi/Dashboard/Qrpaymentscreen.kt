package com.bmdu.dhanlaxmi.Dashboard


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
import com.bmdu.dhanlaxmi.viewModel.BankDetailsViewModel
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap

// ─── QR Code Generator using ZXing ───────────────────────
// Add to build.gradle:
// implementation 'com.google.zxing:core:3.5.2'
// implementation 'androidx.compose.ui:ui-graphics'

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

private const val QR_TAG = "QRPaymentScreen"

// ── UPI QR Data — replace with your actual UPI ID ────────
private const val UPI_ID    = "yourname@upi"
private const val UPI_NAME  = "Dhan Laxmi"
private const val CURRENCY  = "INR"

// ══════════════════════════════════════════════════════════
//  QR PAYMENT SCREEN
// ══════════════════════════════════════════════════════════

@Composable
fun QRPaymentScreen(
    navController: NavController,
    amount: Int
) {
    val viewModel: BankDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.bankDetailsState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    val minDeposit = 50
    val maxDeposit = 10000

    // ── Generate UPI deep-link for QR ───────────────────
    // Format: upi://pay?pa=UPI_ID&pn=NAME&am=AMOUNT&cu=INR
    val upiString = "upi://pay?pa=$UPI_ID&pn=${UPI_NAME.replace(" ", "%20")}&am=$amount&cu=$CURRENCY"
    val qrBitmap: ImageBitmap? = remember(upiString) {
        generateQRBitmap(upiString, 600)
    }

    // ── Navigate home on success ─────────────────────────
    LaunchedEffect(state) {
        when (val s = state) {
            is BankDetailsViewModel.BankDetailsState.Success -> {
                Log.d(QR_TAG, "✅ addFunds success: ${s.message} — going home")
                viewModel.resetState()
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is BankDetailsViewModel.BankDetailsState.Error -> {
                Log.e(QR_TAG, "❌ addFunds error: ${s.message}")
            }
            else -> Unit
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    val isLoading = state is BankDetailsViewModel.BankDetailsState.Loading

    // ── UI ───────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF00A904), Color(0xFF004D02))
                )
            )
    ) {

        // ── Top Bar ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3EE06))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint     = Color.Black,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = !isLoading) { navController.navigateUp() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Payment",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.Black
            )
        }

        // ── Body ─────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Min / Max Row ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(text = "Min. Deposit : $minDeposit", modifier = Modifier.weight(1f))
                InfoChip(text = "Max. Deposit : $maxDeposit", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(32.dp))

            // ── Amount label ──────────────────────────────
            Text(
                text       = "Pay ₹$amount",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFF3EE06)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text     = "Scan QR code to complete payment",
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(24.dp))

            // ── QR Code Card ──────────────────────────────
            Card(
                modifier  = Modifier.size(220.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap             = qrBitmap,
                            contentDescription = "UPI QR Code",
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Fit
                        )
                    } else {
                        // Fallback if ZXing not available
                        CircularProgressIndicator(color = Color(0xFF006400))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text     = "UPI ID: $UPI_ID",
                fontSize = 12.sp,
                color    = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(32.dp))

            // ── Error Banner ──────────────────────────────
            if (state is BankDetailsViewModel.BankDetailsState.Error) {
                val errorMsg = (state as BankDetailsViewModel.BankDetailsState.Error).message
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB00020)),
                    shape  = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text     = errorMsg,
                        color    = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ── "I have paid" Button — triggers addFunds API ──
            Button(
                onClick = {
                    Log.d(QR_TAG, "I have paid clicked → calling addFunds, amount=$amount")
                    viewModel.addFunds(token, amount)
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Color(0xFF002800),
                    disabledContainerColor = Color(0xFF555555)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "I HAVE PAID",
                        fontSize      = 16.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Cancel Button ─────────────────────────────
            OutlinedButton(
                onClick  = {
                    Log.d(QR_TAG, "Cancel clicked — going back")
                    navController.navigateUp()
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF004D00)),
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
    }
}

fun generateQRBitmap(content: String, size: Int): ImageBitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bits  = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp   = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bmp.asImageBitmap()
    } catch (e: Exception) {
        Log.e(QR_TAG, "generateQRBitmap: Failed to generate QR → ${e.message}", e)
        null
    }
}