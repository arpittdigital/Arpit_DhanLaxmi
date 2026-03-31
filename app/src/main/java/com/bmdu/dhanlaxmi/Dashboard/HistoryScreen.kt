package com.bmdu.dhanlaxmi.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bmdu.dhanlaxmi.Model.HistoryItem
import com.bmdu.dhanlaxmi.ui.theme.GoldTheme
import com.bmdu.dhanlaxmi.viewModel.HistoryViewModel


fun formatHistoryDate(dateStr: String): String {
    return try {
        val input  = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.US)
        val output = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US)
        output.format(input.parse(dateStr)!!)
    } catch (e: Exception) {
        dateStr
    }
}
@Composable
fun HistoryScreen(
    token: String,
    onBack: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchHistory(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1F0D))
    ) {

        // ── Top bar ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = GoldTheme.metallicBrush)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint               = Color(0xFF1A0800)
                )
            }
            Text(
                text       = "Bid History",
                color      = Color(0xFF1A0800),
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.align(Alignment.Center)
            )
        }

        // ── Body — driven by HistoryState ──
        when (val state = historyState) {

            is HistoryViewModel.HistoryState.Loading -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldText)
                }
            }

            is HistoryViewModel.HistoryState.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text     = state.message,
                            color    = Color(0xFF7A9A7A),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .shadow(3.dp, RoundedCornerShape(10.dp))
                                .background(
                                    brush = GoldTheme.metallicBrush,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.fetchHistory(token) }
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = "Retry",
                                color      = BlackText,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }
            }

            is HistoryViewModel.HistoryState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", fontSize = 40.sp)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text       = "No bids placed yet",
                                color      = Color(0xFF4A6A4A),
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                 else {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text          = "RECENT BETS",
                                color         = Color(0xFF5A8A5A),
                                fontSize      = 13.sp,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier      = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(state.data) { item ->
                            HistoryCard(item)
                        }
                    }
                }
            }
        }
    }
}


// ── History Card ──────────────────────────────────────────────────────────────

@Composable
fun HistoryCard(item: HistoryItem) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF1A4A1A)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        // Gold top accent bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(brush = GoldTheme.metallicBrushHorizontal)
        )

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Row 1: Game name + play type | Amount ──
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text(
                        text       = item.game.game_name,
                        color      = Color.White,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text       = item.game.play_type.replaceFirstChar { it.uppercase() },
                        color      = Color(0xFF8AAA8A),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = "₹${item.amount}",
                        color      = GoldText,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text       = "Bet Amount",
                        color      = Color(0xFF6A9A6A),
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Divider
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF2A5A2A))
            )

            // ── Row 2: Date | Number | Status ──
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Date + time
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint               = Color(0xFF5A8A5A),
                        modifier           = Modifier.size(13.dp)
                    )
                    Text(
                        text       = formatHistoryDate(item.date),
                        color      = Color(0xFF7A9A7A),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Number pill
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "Number",
                        color      = Color(0xFF5A7A5A),
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0D2B0D), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF2A5A2A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text       = item.game.number,
                            color      = Color(0xFFC8D8C8),
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Status badge
                HistoryStatusBadge(item.game.status)
            }
        }
    }
}


// ── Status Badge ──────────────────────────────────────────────────────────────

@Composable
fun HistoryStatusBadge(status: String) {
    val bgColor     : Color
    val textColor   : Color
    val borderColor : Color
    val label       : String

    when (status.lowercase()) {
        "win"  -> {
            bgColor     = Color(0xFF00E676).copy(alpha = 0.12f)
            textColor   = Color(0xFF00E676)
            borderColor = Color(0xFF00E676).copy(alpha = 0.3f)
            label       = "Win"
        }
        "lose" -> {
            bgColor     = Color(0xFFCC4444).copy(alpha = 0.12f)
            textColor   = Color(0xFFFF6B6B)
            borderColor = Color(0xFFCC4444).copy(alpha = 0.3f)
            label       = "Loss"
        }
        else   -> {  // pending
            bgColor     = Color(0xFFF5C842).copy(alpha = 0.12f)
            textColor   = Color(0xFFF5C842)
            borderColor = Color(0xFFF5C842).copy(alpha = 0.3f)
            label       = "Pending"
        }
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text          = label,
            color         = textColor,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}