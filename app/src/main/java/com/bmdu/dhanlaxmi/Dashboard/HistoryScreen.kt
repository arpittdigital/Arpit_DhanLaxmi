package com.bmdu.dhanlaxmi.Dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.Model.HistoryData
import com.bmdu.dhanlaxmi.viewModel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    navController: NavController,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current

    // Token — same as AuthViewModel saves it
    val token = remember {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("auth_token", "") ?: ""
        if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    val historyState by historyViewModel.historyState.collectAsState()

    // API call on first load
    LaunchedEffect(Unit) {
        historyViewModel.fetchHistory(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF00A904), Color(0xFF004D02))
                )
            )
    ) {
        // ── Top Bar ───────────────────────────────────────────────────────────
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
                modifier = Modifier.clickable { navController.navigateUp() },
                tint = Color.Black
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "Bid History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // ── Content ───────────────────────────────────────────────────────────
        when (val state = historyState) {

            is HistoryViewModel.HistoryState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            is HistoryViewModel.HistoryState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.White, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { historyViewModel.fetchHistory(token) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3EE06))
                        ) {
                            Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is HistoryViewModel.HistoryState.Success -> {
                if (state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No history found", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
fun HistoryCard(item: HistoryData) {
    val isCredit = item.type.equals("credit", ignoreCase = true)

    // Format date: "2026-02-21T11:31:12.000000Z" → "21 Feb 2026, 05:01 PM"
    val formattedDate = remember(item.created_at) {
        try {
            val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            inputFmt.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFmt.parse(item.created_at)
            val outputFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            outputFmt.timeZone = TimeZone.getDefault()
            outputFmt.format(date ?: Date())
        } catch (e: Exception) {
            item.created_at
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF005503)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCredit) "Amount Added" else "Bet Placed",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCredit) Color(0xFF1B5E20) else Color(0xFFB71C1C))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${item.amount}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}