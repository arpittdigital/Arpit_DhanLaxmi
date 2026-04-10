package com.bmdu.SethGMatka.Dashboard


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bmdu.SethGMatka.viewModel.ProfileViewModel

// ─── Colors (same theme) ───────────────────────────────────
private val NotiYellowMain  = Color(0xFFD4A800)
private val NotiYellowLight = Color(0xFFF0C000)
private val NotiGreenMid    = Color(0xFF006400)
private val NotiCardBg      = Color(0xFF0A2E0A)
private val NotiUnreadBg    = Color(0xFF0F3B0F)
private val NotiReadBg      = Color(0xFF0A2E0A)

@Composable
fun NotificationScreen(navController: NavController) {

    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val notificationState by viewModel.notificationState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    // Fetch on screen open
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NotiGreenMid)
    ) {

        // ── Top Bar ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(NotiYellowMain, NotiYellowLight)))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint               = Color.Black,
                modifier           = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text       = "Notifications",
                color      = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp
            )

            // Unread badge
            if (notificationState is ProfileViewModel.NotificationState.Success) {
                val unreadCount = (notificationState as ProfileViewModel.NotificationState.Success)
                    .items.count { it.is_read == 0 }
                if (unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color.Red, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color    = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── Content ─────────────────────────────────────
        when (val s = notificationState) {

            is ProfileViewModel.NotificationState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NotiYellowMain)
                }
            }

            is ProfileViewModel.NotificationState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = Icons.Default.Notifications,
                            contentDescription = null,
                            tint               = Color.White.copy(alpha = 0.3f),
                            modifier           = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text      = s.message,
                            color     = Color.White.copy(alpha = 0.7f),
                            fontSize  = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.fetchNotifications(token) },
                            colors  = ButtonDefaults.buttonColors(containerColor = NotiYellowMain),
                            shape   = RoundedCornerShape(10.dp)
                        ) {
                            Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is ProfileViewModel.NotificationState.Success -> {
                if (s.items.isEmpty()) {
                    // ── Empty State ──────────────────────
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector        = Icons.Default.Notifications,
                                contentDescription = null,
                                tint               = Color.White.copy(alpha = 0.3f),
                                modifier           = Modifier.size(72.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text      = "No notifications yet",
                                color     = Color.White.copy(alpha = 0.6f),
                                fontSize  = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text     = "We'll notify you when something arrives",
                                color    = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // ── Notification List ────────────────
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(s.items, key = { it.id }) { item ->

                            val isUnread  = item.is_read == 0
                            val cardColor = if (isUnread) NotiUnreadBg else NotiReadBg

                            Card(
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(12.dp),
                                colors    = CardDefaults.cardColors(containerColor = cardColor),
                                elevation = CardDefaults.cardElevation(if (isUnread) 6.dp else 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // ── Icon circle ─────────────────────
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(
                                                color  = if (isUnread) NotiYellowMain else Color.White.copy(alpha = 0.15f),
                                                shape  = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector        = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint               = if (isUnread) Color.Black else Color.White.copy(alpha = 0.6f),
                                            modifier           = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(14.dp))

                                    // ── Text content ─────────────────────
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text       = item.title,
                                                color      = if (isUnread) NotiYellowMain else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize   = 14.sp,
                                                modifier   = Modifier.weight(1f)
                                            )

                                            // Unread dot
                                            if (isUnread) {
                                                Spacer(Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(NotiYellowMain, CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text     = item.message,
                                            color    = Color.White.copy(alpha = 0.8f),
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        // Date
                                        item.created_at?.let { dateStr ->
                                            Text(
                                                text     = formatNotificationDate(dateStr),
                                                color    = Color.White.copy(alpha = 0.4f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Unread left border accent
                                if (isUnread) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(NotiYellowMain, NotiYellowLight, Color.Transparent)
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}

// ─── Date formatter ────────────────────────────────────────
// "2026-02-17T05:41:19.000000Z"  →  "17 Feb 2026, 05:41"
private fun formatNotificationDate(isoDate: String): String {
    return try {
        val parts = isoDate.split("T")
        val datePart = parts[0]   // 2026-02-17
        val timePart = parts.getOrNull(1)?.take(5) ?: ""  // 05:41

        val (year, month, day) = datePart.split("-")
        val monthName = listOf("", "Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec")
            .getOrElse(month.toIntOrNull() ?: 0) { month }

        "$day $monthName $year, $timePart"
    } catch (e: Exception) {
        isoDate.take(10)
    }
}