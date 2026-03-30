package com.bmdu.dhanlaxmi.Dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.Model.GameResult
import com.bmdu.dhanlaxmi.Model.ResultDate
import com.bmdu.dhanlaxmi.viewModel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current

    val token = remember {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("auth_token", "") ?: ""
        if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    val resultState by gameViewModel.resultState.collectAsState()

    LaunchedEffect(Unit) {
        gameViewModel.getresult(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ── Top Bar ───────────────────────────────────────────────────────────
        TopAppBar(
            title = {
                Text("Result", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE6B800),
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )

        // ── Content ───────────────────────────────────────────────────────────
        when (val state = resultState) {

            is GameViewModel.ResultState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE53935))
                }
            }

            is GameViewModel.ResultState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.Gray, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { gameViewModel.getresult(token , null, null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }

            is GameViewModel.ResultState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    state.data.forEach { dateGroup ->
                        // ── Date Header ───────────────────────────────────────
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8E8E8))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(18.dp)
                                            .background(Color(0xFFE53935), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        dateGroup.date,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE53935)
                                    )
                                }
                            }
                        }

                        // ── Game Result Rows ──────────────────────────────────
                        items(dateGroup.games) { game ->
                            ResultRow(game)
                            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

// ── Single Result Row ─────────────────────────────────────────────────────────
@Composable
fun ResultRow(game: GameResult) {
    // correct_answer format assume: "39-65" (open-close) ya single "39"
    val parts = game.correct_answer?.split("-")
    val openNum  = parts?.getOrNull(0)?.trim() ?: "XX"
    val closeNum = parts?.getOrNull(1)?.trim() ?: "XX"

    // Highlight agar current time ke aas paas ho (optional — abhi static)
    val isHighlighted = false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHighlighted) Color(0xFFFFC107) else Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: City name + time
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.game_name?.uppercase() ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = "at ${game.open_time ?: ""}",
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Record Chart",
                    fontSize = 12.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right: Open & Close numbers
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = openNum,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = closeNum,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (closeNum == "XX") Color(0xFF9E9E9E) else Color(0xFF1A1A1A)
            )
        }
    }
}