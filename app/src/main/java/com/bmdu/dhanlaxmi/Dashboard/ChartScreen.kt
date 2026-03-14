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

    val resultState by gameViewModel.resultState.collectAsState()

    LaunchedEffect(Unit) {
        gameViewModel.getresult(token)
    }

    // City names from API data
    val cityNames by remember(resultState) {
        derivedStateOf {
            if (resultState is GameViewModel.ResultState.Success) {
                val data = (resultState as GameViewModel.ResultState.Success).data
                data.flatMap { it.games }.map { it.city_name }.distinct()
            } else emptyList()
        }
    }

    // Filter by selected month + year
    val filteredDates by remember(resultState, selectedMonth, selectedYear) {
        derivedStateOf {
            if (resultState is GameViewModel.ResultState.Success) {
                val data = (resultState as GameViewModel.ResultState.Success).data
                val monthIndex = (months.indexOf(selectedMonth) + 1).toString().padStart(2, '0')
                data.filter { dateGroup ->
                    val prefix = "$selectedYear-$monthIndex"
                    dateGroup.date.startsWith(prefix)
                }
            } else emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF00A904), GreenDark)
                )
            )
    ) {

        // ── Top Bar ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(YellowMain)
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
                "Record Chart",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.Black
            )
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {

            // ── Month / Year / Search Row ─────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Month Dropdown
                Box(modifier = Modifier.weight(1.4f)) {
                    OutlinedButton(
                        onClick = { expandMonth = true },
                        shape   = RoundedCornerShape(10.dp),
                        colors  = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            selectedMonth,
                            color    = Color.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint     = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
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
                    OutlinedButton(
                        onClick = { expandYear = true },
                        shape   = RoundedCornerShape(10.dp),
                        colors  = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            selectedYear,
                            color    = Color.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint     = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
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

                // Search Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(YellowMain)
                        .clickable { gameViewModel.getresult(token) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black)
                }
            }

            Spacer(Modifier.height(12.dp))

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
                            Text(state.message, color = Color.White, fontSize = 15.sp,
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { gameViewModel.getresult(token) },
                                colors  = ButtonDefaults.buttonColors(containerColor = YellowMain)
                            ) {
                                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is GameViewModel.ResultState.Success -> {
                    if (filteredDates.isEmpty()) {
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

                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(12.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column {

                                // ── Table Title ──────────────────────
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(YellowMain)
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Chart — $selectedMonth $selectedYear",
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 14.sp,
                                        color      = Color.Black
                                    )
                                }

                                // ── Sticky Header + Scrollable Rows ──
                                Column {

                                    // Header (scrolls horizontally WITH rows via shared hScroll)
                                    Row(
                                        modifier = Modifier
                                            .horizontalScroll(hScroll)
                                            .background(HeaderBg)
                                            .padding(vertical = 10.dp)
                                    ) {
                                        // Date header
                                        ChartCell(
                                            text       = "Date",
                                            width      = DATE_COL_W,
                                            fontWeight = FontWeight.Bold,
                                            color      = Color.Black,
                                            fontSize   = 12
                                        )
                                        // City headers
                                        cityNames.forEach { city ->
                                            ChartCell(
                                                text       = city.uppercase(),
                                                width      = CITY_COL_W,
                                                fontWeight = FontWeight.Bold,
                                                color      = Color(0xFF333333),
                                                fontSize   = 11
                                            )
                                        }
                                    }

                                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                                    // Data rows — LazyColumn for performance
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 520.dp)
                                    ) {
                                        itemsIndexed(filteredDates) { rowIndex, dateGroup ->

                                            // ── Day number extraction ────────
                                            val dayLabel = extractDay(dateGroup.date)

                                            Row(
                                                modifier = Modifier
                                                    .horizontalScroll(hScroll)
                                                    .background(
                                                        if (rowIndex % 2 == 0) Color.White
                                                        else AltRowBg
                                                    )
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Day cell
                                                ChartCell(
                                                    text       = dayLabel,
                                                    width      = DATE_COL_W,
                                                    fontWeight = FontWeight.Bold,
                                                    color      = Color(0xFF1A1A1A),
                                                    fontSize   = 13,
                                                    bgColor    = Color(0xFFFFF9C4)   // light yellow highlight
                                                )

                                                // Result per city
                                                cityNames.forEach { cityName ->
                                                    val gameResult = dateGroup.games.find {
                                                        it.city_name.equals(cityName, ignoreCase = true)
                                                    }
                                                    val answer    = gameResult?.correct_answer?.trim()
                                                        .takeIf { !it.isNullOrBlank() } ?: "-"
                                                    val isPending = answer == "-"

                                                    ChartCell(
                                                        text       = answer,
                                                        width      = CITY_COL_W,
                                                        fontWeight = if (!isPending) FontWeight.Bold else FontWeight.Normal,
                                                        color      = if (!isPending) Color(0xFF1A1A1A) else Color(0xFFBBBBBB),
                                                        fontSize   = 13
                                                    )
                                                }
                                            }

                                            if (rowIndex < filteredDates.size - 1) {
                                                HorizontalDivider(
                                                    thickness = 0.5.dp,
                                                    color     = Color(0xFFEEEEEE)
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

// ── Reusable Table Cell ────────────────────────────────────
@Composable
private fun ChartCell(
    text       : String,
    width      : Dp,
    fontWeight : FontWeight = FontWeight.Normal,
    color      : Color      = Color.Black,
    fontSize   : Int        = 12,
    bgColor    : Color      = Color.Transparent
) {
    Box(
        modifier = Modifier
            .width(width)
            .background(bgColor)
            .padding(horizontal = 4.dp),
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