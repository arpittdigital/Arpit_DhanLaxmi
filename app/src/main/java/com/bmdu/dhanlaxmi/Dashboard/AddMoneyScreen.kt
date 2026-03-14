package com.bmdu.dhanlaxmi.Dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private const val ADD_MONEY_TAG = "AddMoneyScreen"

@Composable
fun AddMoneyScreen(navController: NavController) {

    var enteredAmount by remember { mutableStateOf("") }
    val quickAmounts  = listOf(50, 100, 200, 500, 1000, 2000, 5000, 10000)
    val minDeposit    = 50
    val maxDeposit    = 10000

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    val isValid = (enteredAmount.toIntOrNull() ?: 0) in minDeposit..maxDeposit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF00A904), Color(0xFF004D02))
                )
            )
    ) {

        // ── Top Bar ───────────────────────────────────────
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
                    .clickable { navController.navigateUp() }
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Add Money",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(12.dp))

            // ── Min / Max Row ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(text = "Min. Deposit : ₹$minDeposit", modifier = Modifier.weight(1f))
                InfoChip(text = "Max. Deposit : ₹$maxDeposit", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(60.dp))

            // ── Title ─────────────────────────────────────
            Text(
                "Add Money",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFF3EE06)
            )

            Spacer(Modifier.height(20.dp))

            // ── Amount Input ──────────────────────────────
            OutlinedTextField(
                value         = enteredAmount,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() } && (it.toIntOrNull() ?: 0) <= maxDeposit) {
                        enteredAmount = it
                    }
                },
                placeholder     = { Text("Enter Amount", color = Color.Gray) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(30.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    focusedTextColor        = Color.Black,
                    unfocusedTextColor      = Color.Black,
                    cursorColor             = Color.Black
                )
            )

            Spacer(Modifier.height(16.dp))

            // ── Quick Amount Buttons ──────────────────────
            quickAmounts.chunked(4).forEach { rowItems ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { amount ->
                        val isSelected = enteredAmount == amount.toString()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFD4A000) else Color(0xFFF3EE06))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { enteredAmount = amount.toString() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = "₹$amount",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.Black,
                                textAlign  = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ── NEXT Button → Navigate to QR Payment Screen ──
            Button(
                onClick = {
                    val amountInt = enteredAmount.toIntOrNull()
                    if (amountInt == null) {
                        Log.e(ADD_MONEY_TAG, "Invalid amount: '$enteredAmount'")
                        return@Button
                    }
                    if (token.isNullOrBlank()) {
                        Log.e(ADD_MONEY_TAG, "Token is null — user not logged in")
                        return@Button
                    }
                    Log.d(ADD_MONEY_TAG, "Navigating to QR screen → amount=$amountInt")
                    navController.navigate("qr_payment/$amountInt")
                },
                enabled  = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Color(0xFF002800),
                    disabledContainerColor = Color(0xFF555555)
                )
            ) {
                Text(
                    "NEXT",
                    fontSize      = 16.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White,
                    letterSpacing = 2.sp
                )
            }

            // ── Validation message ────────────────────────
            if (enteredAmount.isNotEmpty() && !isValid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Amount must be between ₹$minDeposit and ₹$maxDeposit",
                    color    = Color.Red,
                    fontSize = 12.sp
                )
            }

            if (token.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚠ Not logged in. Please login again.",
                    color    = Color(0xFFFFCC00),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun InfoChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF003300))
            .border(1.dp, Color(0xFFF3EE06), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}