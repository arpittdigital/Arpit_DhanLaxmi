package com.bmdu.dhanlaxmi

import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen(navController: NavController, context: Context) {

    LaunchedEffect(Unit) {
        delay(2000)

        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)

        if (token != null) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00A904),
                        Color(0xFF004D02),
                        Color(0xFF002601)
                    )
                )
            )
    ) {

        // Background decorative canvas
        Canvas(modifier = Modifier.fillMaxSize()) {

            // Top-left coin rings
            drawCircle(
                color = Color(0x14FFD700),
                radius = 200.dp.toPx(),
                center = Offset(-80.dp.toPx(), 140.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0x0BFFD700),
                radius = 130.dp.toPx(),
                center = Offset(-80.dp.toPx(), 140.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Bottom-right coin rings
            drawCircle(
                color = Color(0x14FFD700),
                radius = 220.dp.toPx(),
                center = Offset(size.width + 80.dp.toPx(), size.height - 120.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0x0BFFD700),
                radius = 140.dp.toPx(),
                center = Offset(size.width + 80.dp.toPx(), size.height - 120.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Diagonal gold shimmer band
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x00FFD700),
                        Color(0x12FFD700),
                        Color(0x00FFD700)
                    ),
                    start = Offset(0f, size.height * 0.25f),
                    end = Offset(size.width, size.height * 0.65f)
                ),
                size = size
            )
        }

        // ₹ watermark top-right
        Text(
            text = "₹",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 24.dp),
            fontSize = 130.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0x0FFFD700)
        )

        // $ watermark bottom-left
        Text(
            text = "$",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 68.dp, start = 20.dp),
            fontSize = 110.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0x0DFFD700)
        )

        // Logo — centered, large, no background
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0x00FFD700),
                                Color(0x55FFD700),
                                Color(0xCCFFD700),
                                Color(0x55FFD700),
                                Color(0x00FFD700)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner ring
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0x22FFD700),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Logo — big, clean, no background circle
                    Image(
                        modifier = Modifier
                            .size(240.dp),
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "App Logo",
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}