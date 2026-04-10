package com.bmdu.SethGMatka.Dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bmdu.SethGMatka.R

// ─── Shared Colors ─────────────────────────────────────────
private val YellowMain  = Color(0xFFD4A800)
private val YellowLight = Color(0xFFF0C000)
private val GreenMid    = Color(0xFF006400)
private val CardDark    = Color(0xFF0A2E0A)
private val RowBg       = Color(0xFF0D1F0D)

// ══════════════════════════════════════════════════════════
//  SCREEN 1 — GAME RATES
// ══════════════════════════════════════════════════════════

@Composable
fun GameRatesScreen(navController: NavController) {

    val rates = listOf(
        "Haruf Andar "  to "10-96",
        "Haruf Bahar"  to "10-96",
        "Jodi Digit"   to "10-960"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE500))
    ) {
        // ── Top Bar ─────────────────────────────────────
        TopBar(title = "Game Rates", onBack = { navController.popBackStack() })

        Spacer(Modifier.height(20.dp))

        // ── Rates Card ──────────────────────────────────
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = CardDark),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Card Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF1A3A1A),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "Game Rates",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }

                // Rate Rows
                rates.forEachIndexed { index, (label, rate) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) CardDark else RowBg)
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(label, color = Color.White, fontSize = 15.sp)
                        Text(rate,  color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    if (index < rates.lastIndex)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  SCREEN 2 — HOW TO PLAY
// ══════════════════════════════════════════════════════════

@Composable
fun HowToPlayScreen(navController: NavController) {

    val options = listOf(
        "How to Play Game",
        "How to Deposit",
        "How to withdraw"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenMid)
    ) {
        // ── Top Bar ─────────────────────────────────────
        TopBar(title = "How to play", onBack = { navController.popBackStack() })

        Spacer(Modifier.height(20.dp))

        // ── Option Buttons ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            options.forEach { label ->
                Button(
                    onClick = {
                        when (label) {
                            "How to Play Game" -> navController.navigate("how_to_play_game")
                            "How to Deposit"   -> navController.navigate("how_to_deposit")
                            "How to withdraw"  -> navController.navigate("how_to_withdraw")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                    shape  = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(
                        text       = label,
                        color      = Color.White,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  SCREEN 3 — CONTACT US
// ══════════════════════════════════════════════════════════

@Composable
fun ContactUsScreen(navController: NavController) {


    val phoneNumber = "tel:+919999999999"
    val context     = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenMid)
    ) {
        // ── Top Bar ─────────────────────────────────────
        TopBar(title = "Contact Us", onBack = { navController.popBackStack() })

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Support Image Card ───────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Replace R.drawable.contact_support with your actual drawable name
                    androidx.compose.foundation.Image(
                        painter            = painterResource(id = R.drawable.contact_support),
                        contentDescription = "Support",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Info / Message Box ───────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = CardDark),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center) {
                    Text(
                        text      = "For any queries or support,\nplease contact us.",
                        color     = Color.White.copy(alpha = 0.8f),
                        fontSize  = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Call Now Button ──────────────────────────
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowMain
                ),
                shape = RoundedCornerShape(30.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(
                    text       = "Call Now",
                    color      = Color.Black,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  SHARED — Top Bar Composable
// ══════════════════════════════════════════════════════════

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint               = Color(0xFFFFE500),
            modifier           = Modifier
                .size(24.dp)
                .clickable { onBack() }
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text       = title,
            color      = Color(0xFFFFE500),
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp
        )
    }
}