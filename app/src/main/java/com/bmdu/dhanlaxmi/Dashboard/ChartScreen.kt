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

    // ✅ Converts "March" → "3"
    val monthNumber by remember(selectedMonth) {
        derivedStateOf {
            (months.indexOf(selectedMonth) + 1).toString()
        }
    }

    val resultState by gameViewModel.resultState.collectAsState()

    // ✅ Single LaunchedEffect — removed duplicate, passes month/year
    LaunchedEffect(Unit) {
        gameViewModel.getresult(token, null, null)
    }

    // ✅ game names from API response
    val gameNames by remember(resultState) {
        derivedStateOf {
            if (resultState is GameViewModel.ResultState.Success) {
                val data = (resultState as GameViewModel.ResultState.Success).data
                data.flatMap { it.games }.map { it.game_name }.distinct()
            } else emptyList()
        }
    }

    // ✅ Use API data directly — removed local filteredDates
    val dates by remember(resultState) {
        derivedStateOf {
            if (resultState is GameViewModel.ResultState.Success) {
                (resultState as GameViewModel.ResultState.Success).data
            } else emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A8C00))
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
            Text(
                "Chart",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.Black
            )
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {

            // ── Month / Year / Search Row ─────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Month Dropdown
                Box(modifier = Modifier.weight(1.4f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A1A))
                            .clickable { expandMonth = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                selectedMonth,
                                color    = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded         = expandMonth,
                        onDismissRequest = { expandMonth = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text    = { Text(month) },
                                onClick = { selectedMonth = month; expandMonth = false }
                            )
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
                            .background(Color(0xFF1A1A1A))
                            .clickable { expandYear = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                selectedYear,
                                color    = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded         = expandYear,
                        onDismissRequest = { expandYear = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text    = { Text(year) },
                                onClick = { selectedYear = year; expandYear = false }
                            )
                        }
                    }
                }

                // ✅ Search Button — passes monthNumber and selectedYear
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A1A))
                        .clickable {
                            gameViewModel.getresult(token, monthNumber, selectedYear)
                        },
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
                            Text(
                                state.message,
                                color     = Color.White,
                                fontSize  = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            // ✅ Retry also passes month/year
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE6B800))
                                    .clickable {
                                        gameViewModel.getresult(token, monthNumber, selectedYear)
                                    }
                                    .padding(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is GameViewModel.ResultState.Success -> {
                    // ✅ Use dates directly from API — already filtered by month/year
                    if (dates.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No data for $selectedMonth $selectedYear",
                                color     = Color.White,
                                fontSize  = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {

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
                                    Text(
                                        "Chart for $selectedMonth $selectedYear",
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 15.sp,
                                        color      = Color.Black
                                    )
                                }

                                // Header Row
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(hScroll)
                                        .background(Color.White)
                                ) {
                                    ChartCell(
                                        text       = "Date",
                                        width      = DATE_COL_W,
                                        fontWeight = FontWeight.Bold,
                                        color      = Color.Black,
                                        fontSize   = 12,
                                        showBorder = true
                                    )
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

                                // ✅ Data Rows — uses dates from API directly
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 520.dp)
                                ) {
                                    itemsIndexed(dates) { _, dateGroup ->

                                        val dayLabel = extractDay(dateGroup.date)

                                        Row(
                                            modifier = Modifier
                                                .horizontalScroll(hScroll)
                                                .background(Color.White),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ChartCell(
                                                text       = dayLabel,
                                                width      = DATE_COL_W,
                                                fontWeight = FontWeight.Normal,
                                                color      = Color.Black,
                                                fontSize   = 13,
                                                showBorder = true
                                            )

                                            gameNames.forEach { gameName ->
                                                val gameResult = dateGroup.games.find {
                                                    it.game_name.equals(gameName, ignoreCase = true)
                                                }
                                                val answer = gameResult?.correct_answer
                                                    ?.takeIf { it.isNotBlank() } ?: "xx"

                                                ChartCell(
                                                    text       = answer,
                                                    width      = CITY_COL_W,
                                                    fontWeight = FontWeight.Normal,
                                                    color      = if (answer != "xx") Color.Black else Color(0xFFAAAAAA),
                                                    fontSize   = 13,
                                                    showBorder = true
                                                )
                                            }
                                        }
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
    showBorder : Boolean    = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .then(
                if (showBorder) Modifier.border(0.5.dp, Color(0xFFAAAAAA))
                else Modifier
            )
            .background(bgColor)
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