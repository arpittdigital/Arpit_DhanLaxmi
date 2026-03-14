package com.d_shield_parent.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController



data class WinningHistoryItem(
    val amountWon: String,
    val dateTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinningHistoryScreen(navController: NavController) {

    val dummyList = listOf(
        WinningHistoryItem("₹500",  "12 Jul 2025, 10:30 AM"),
        WinningHistoryItem("₹1200", "11 Jul 2025, 08:15 PM"),
        WinningHistoryItem("₹300",  "10 Jul 2025, 03:45 PM"),
        WinningHistoryItem("₹750",  "09 Jul 2025, 11:00 AM"),
        WinningHistoryItem("₹2000", "08 Jul 2025, 06:20 PM"),
        WinningHistoryItem("₹450",  "07 Jul 2025, 02:10 PM"),
        WinningHistoryItem("₹900",  "06 Jul 2025, 05:55 PM"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Winning History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A0DAD)
                )
            )
        }
    ) { paddingValues ->

        if (dummyList.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Winnings Yet",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {

            // Total Winnings Summary Card at top
            val totalWon = dummyList.sumOf {
                it.amountWon.replace("₹", "").replace(",", "").toIntOrNull() ?: 0
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF6A0DAD)
                        ),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Winnings",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹$totalWon",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // History Items
                items(dummyList) { item ->
                    WinningHistoryCard(item)
                }
            }
        }
    }
}

@Composable
fun WinningHistoryCard(item: WinningHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFFF3E5F5), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFF6A0DAD),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Date & Time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Amount Won",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.dateTime,
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            // Amount
            Text(
                text = "+ ${item.amountWon}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}