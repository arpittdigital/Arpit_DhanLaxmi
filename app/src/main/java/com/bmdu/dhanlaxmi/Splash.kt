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
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.random.Random
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.input.TextFieldState.Saver.restore
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.cos
import kotlin.math.absoluteValue
import kotlin.math.PI

data class Coin(
    val x: Float,
    val startY: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val flipOffset: Float  // offset so coins don't all flip in sync
)

@Composable
fun SplashScreen(navController: NavController, context: Context) {

    LaunchedEffect(Unit) {
        delay(2000)
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        if (token != null) {
            navController.navigate("home") { popUpTo("splash") { inclusive = true } }
        } else {
            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
        }
    }

    val coins = remember {
        List(28) {
            Coin(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -1.5f,
                size = Random.nextFloat() * 14f + 18f,  // 18–32dp radius
                speed = Random.nextFloat() * 0.35f + 0.2f,
                alpha = Random.nextFloat() * 0.5f + 0.45f,
                flipOffset = Random.nextFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coins")

    // Fall progress 0->1 looping
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    // Flip progress 0->1 looping (faster than fall)
    val flipProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flip"
    )

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

        Canvas(modifier = Modifier.fillMaxSize()) {

            // --- Original decorative rings ---
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
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x00FFD700), Color(0x12FFD700), Color(0x00FFD700)),
                    start = Offset(0f, size.height * 0.25f),
                    end = Offset(size.width, size.height * 0.65f)
                ),
                size = size
            )

            // --- Raining Coins ---
            coins.forEach { coin ->
                val coinY = ((coin.startY + progress * coin.speed * 2.5f) % 1.3f) * size.height
                val coinX = coin.x * size.width
                val r = coin.size.dp.toPx()

                // Fade in/out at edges
                val fadedAlpha = when {
                    coinY < size.height * 0.08f -> coin.alpha * (coinY / (size.height * 0.08f))
                    coinY > size.height * 0.88f -> coin.alpha * (1f - (coinY - size.height * 0.88f) / (size.height * 0.12f))
                    else -> coin.alpha
                }.coerceIn(0f, 1f)

                // Flip: scaleX goes 1 -> 0 -> -1 -> 0 -> 1 using cos
                val flipAngle = ((flipProgress + coin.flipOffset) % 1f) * 2f * PI.toFloat()
                val scaleX = cos(flipAngle)  // -1..1, simulates 3D flip
                val absScaleX = scaleX.absoluteValue  // for width scaling

                // When scaleX > 0 = front (gold), scaleX < 0 = back (darker gold)
                val isFront = scaleX >= 0f
                val coinColor = if (isFront) Color(0xFFFFD700) else Color(0xFFB8860B)
                val rimColor = if (isFront) Color(0xFFDAA520) else Color(0xFF8B6914)
                val shineColor = if (isFront) Color(0xFFFFF9C4) else Color(0xFFCDA434)

                // Scaled width for flip effect
                val scaledR = r * absScaleX.coerceAtLeast(0.08f)

                // Coin body (ellipse to simulate flip)
                drawOval(
                    color = coinColor.copy(alpha = fadedAlpha),
                    topLeft = Offset(coinX - scaledR, coinY - r),
                    size = Size(scaledR * 2f, r * 2f)
                )

                // Rim / edge border
                drawOval(
                    color = rimColor.copy(alpha = fadedAlpha),
                    topLeft = Offset(coinX - scaledR, coinY - r),
                    size = Size(scaledR * 2f, r * 2f),
                    style = Stroke(width = (r * 0.12f))
                )

                // Top shine arc (only visible on front, fades on flip)
                if (isFront && absScaleX > 0.3f) {
                    drawOval(
                        color = shineColor.copy(alpha = fadedAlpha * absScaleX * 0.7f),
                        topLeft = Offset(coinX - scaledR * 0.55f, coinY - r * 0.72f),
                        size = Size(scaledR * 1.1f, r * 0.45f)
                    )
                }

                // ₹ symbol using native Android text paint
                if (absScaleX > 0.25f) {
                    val textPaint = android.graphics.Paint().apply {
                        color = if (isFront)
                            android.graphics.Color.argb((fadedAlpha * absScaleX * 255).toInt(), 0x8B, 0x45, 0x13)
                        else
                            android.graphics.Color.argb((fadedAlpha * absScaleX * 255).toInt(), 0x4A, 0x2A, 0x00)
                        textSize = r * 1.05f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }

                    // Scale canvas horizontally to simulate flip on the text too
                    // With this:
                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.scale(absScaleX, 1f, coinX, coinY)
                    drawContext.canvas.nativeCanvas.drawText(
                        "₹",
                        coinX,
                        coinY + r * 0.38f,
                        textPaint
                    )
                    drawContext.canvas.nativeCanvas.restore()
                }
            }
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

        // Logo centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
                    Image(
                        modifier = Modifier.size(240.dp),
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "App Logo",
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}