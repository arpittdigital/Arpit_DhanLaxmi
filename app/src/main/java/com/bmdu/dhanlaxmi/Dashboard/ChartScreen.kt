package com.bmdu.dhanlaxmi.Dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.viewModel.GameViewModel
import com.google.common.collect.Multimaps.index

// ── Constants ─────────────────────────────────────────────
private val YellowMain  = Color(0xFFF3EE06)
private val GreenDark   = Color(0xFF004D02)
private val GreenMid    = Color(0xFF006400)
private val HeaderBg    = Color(0xFFEEEEEE)
private val AltRowBg    = Color(0xFFF5F5F5)

private val DATE_COL_W : Dp = 46.dp   // "Date" column width
private val CITY_COL_W : Dp = 70.dp   // Each city column width

private fun extractDay(rawDate: String): String {
    return try {
        rawDate.take(10).split("-").lastOrNull()?.trimStart('0') ?: rawDate
    } catch (e: Exception) {
        rawDate
    }
}

// Full date label for title: "2026-02-17..." → "Feb 2026"
private fun extractMonthYear(rawDate: String): String {
    return try {
        val parts = rawDate.take(10).split("-")
        val year  = parts.getOrNull(0) ?: ""
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val monthName = listOf("","Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec").getOrElse(month) { "" }
        "$monthName $year"
    } catch (e: Exception) { "" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current

    val token = remember {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("auth_token", "") ?: ""
        if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    val months = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )
    val years = (2023..2026).map { it.toString() }

    val currentMonth = remember {
        val cal = java.util.Calendar.getInstance()
        months[cal.get(java.util.Calendar.MONTH)]
    }
    val currentYear = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
    }

    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear  by remember { mutableStateOf(currentYear) }
    var expandMonth   by remember { mutableStateOf(false) }
    var expandYear    by remember { mutableStateOf(false) }

    val monthNumber by remember(selectedMonth) {
        derivedStateOf { (months.indexOf(selectedMonth) + 1).toString() }
    }

    val resultState by gameViewModel.resultState.collectAsState()

    //FIX 1: Load with actual current month/year on first launch
    LaunchedEffect(Unit) {
        gameViewModel.getresult(token, monthNumber, currentYear)
    }

    val gameNames by remember(resultState) {
        derivedStateOf {
            (resultState as? GameViewModel.ResultState.Success)
                ?.data
                ?.flatMap { it.games }
                ?.map { it.game_name }
                ?.distinct()
                ?: emptyList()
        }
    }

    val dates by remember(resultState) {
        derivedStateOf {
            (resultState as? GameViewModel.ResultState.Success)?.data ?: emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A4A1A))
    ) {

        // ── Top Bar ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE6B800))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable { navController.navigateUp() },
                tint = Color.Black
            )
            Spacer(Modifier.width(12.dp))
            Text("Chart", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {

            // ── Month / Year / Search Row ─────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month Dropdown
                Box(modifier = Modifier.weight(1.4f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A2E))  //0xFF1A1A2E
                            .clickable { expandMonth = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                selectedMonth, color = Color.White, fontSize = 14.sp,
                                modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    DropdownMenu(expanded = expandMonth, onDismissRequest = { expandMonth = false }) {
                        months.forEach { month ->
                            DropdownMenuItem(text = { Text(month) }, onClick = { selectedMonth = month; expandMonth = false })
                        }
                    }
                }

                // Year Dropdown
                Box(modifier = Modifier.weight(0.9f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A2E))
                            .clickable { expandYear = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedYear, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    DropdownMenu(expanded = expandYear, onDismissRequest = { expandYear = false }) {
                        years.forEach { year ->
                            DropdownMenuItem(text = { Text(year) }, onClick = { selectedYear = year; expandYear = false })
                        }
                    }
                }

                // Search Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A2E))
                        .clickable { gameViewModel.getresult(token, monthNumber, selectedYear) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Chart Table ───────────────────────────────
            when (val state = resultState) {

                is GameViewModel.ResultState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is GameViewModel.ResultState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE6B800))
                                    .clickable { gameViewModel.getresult(token, monthNumber, selectedYear) }
                                    .padding(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is GameViewModel.ResultState.Success -> {
                    if (dates.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No data for $selectedMonth $selectedYear", color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
                        }
                    } else {

                        // FIX 2: Single shared scroll state for header + rows
                        val hScroll = rememberScrollState()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                        ) {
                            Column {

                                // Yellow Title Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFE600))
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Chart for $selectedMonth $selectedYear", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                                }

                                // ── Header Row ───────────────────────────────
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ) {
                                    // Date header — fixed, does NOT scroll
                                    ChartCell(
                                        text       = "Date",
                                        width      = DATE_COL_W,
                                        fontWeight = FontWeight.Bold,
                                        color      = Color.Black,
                                        fontSize   = 12,
                                        showBorder = true
                                    )

                                    // Game name headers — scrollable
                                    Row(modifier = Modifier.horizontalScroll(hScroll)) {
                                        gameNames.forEach { game ->
                                            ChartCell(
                                                text       = game.uppercase(),
                                                width      = CITY_COL_W,
                                                fontWeight = FontWeight.Bold,
                                                color      = Color.Black,
                                                fontSize   = 11,
                                                showBorder = true
                                            )
                                        }
                                    }
                                }

// ── Data Rows ────────────────────────────────
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 520.dp)
                                ) {
                                    itemsIndexed(dates) { idx, dateGroup ->
                                        val dayLabel = extractDay(dateGroup.date)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (idx % 2 == 0) Color(0xFFF5F5F5) else Color.White),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            //Date cell — fixed, does NOT scroll
                                            ChartCell(
                                                text = dayLabel,
                                                width = DATE_COL_W,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.Black,
                                                fontSize = 13,
                                                showBorder = true,
                                                rowIndex = idx
                                            )

                                            //Game result cells — scrollable
                                            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                                                gameNames.forEach { gameName ->
                                                    val answer = dateGroup.games
                                                        .find {
                                                            it.game_name.equals(
                                                                gameName,
                                                                ignoreCase = true
                                                            )
                                                        }
                                                        ?.correct_answer
                                                        ?.takeIf { it.isNotBlank() }
                                                        ?: "xx"

                                                    ChartCell(
                                                        text = answer,
                                                        width = CITY_COL_W,
                                                        fontWeight = FontWeight.Normal,
                                                        color = if (answer != "xx") Color.Black else Color(
                                                            0xFFAAAAAA
                                                        ),
                                                        fontSize = 13,
                                                        showBorder = true,
                                                        rowIndex = idx
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }  }
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable Table Cell ──────────────────────────────────────
@Composable
private fun ChartCell(
    text       : String,
    width      : Dp,
    fontWeight : FontWeight = FontWeight.Normal,
    color      : Color      = Color.Black,
    fontSize   : Int        = 12,
    bgColor    : Color      = Color.Transparent,
    showBorder : Boolean    = false,
    rowIndex   : Int        = -1   // -1 = header, 0+ = data rows
) {
    // Alternating row shading; header stays white
    val resolvedBg = when {
        bgColor != Color.Transparent -> bgColor          // caller override wins
        rowIndex < 0                 -> Color.White      // header row
        rowIndex % 2 == 0            -> Color(0xFFF5F5F5) // even rows — light grey
        else                         -> Color.White      // odd rows — white
    }

    Box(
        modifier = Modifier
            .width(width)
            .then(
                if (showBorder) Modifier.border(0.5.dp, Color(0xFFAAAAAA))
                else Modifier
            )
            .background(resolvedBg)   // ✅ was always Transparent before
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = text,
            fontSize   = fontSize.sp,
            fontWeight = fontWeight,
            textAlign  = TextAlign.Center,
            color      = color,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}