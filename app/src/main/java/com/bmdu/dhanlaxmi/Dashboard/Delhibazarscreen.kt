package com.bmdu.dhanlaxmi.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.graphics.component1
import com.bmdu.dhanlaxmi.viewModel.GameViewModel
import com.bmdu.dhanlaxmi.viewModel.ProfileViewModel

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelhiBazarScreen(
    navController: NavController,
    gameId: Int,
    gameName: String,
//    walletBalance: Int,
    profileViewModel: ProfileViewModel,
    gameViewModel: GameViewModel = viewModel(),

//    onBidSuccess  : () -> Unit
) {
    val context = LocalContext.current
    val profileState by profileViewModel.profileState.collectAsState()
    val walletBalance = when (val s = profileState) {
        is ProfileViewModel.ProfileState.Success ->
            s.data.data?.wallet_amount
                ?.toFloat()
                ?.toInt() ?: 0   // "1700.00" → 1700.0f → 1700
        else -> 0
    }

    val token = remember {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("auth_token", "") ?: ""
        if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    val tabLabels = listOf("Jodi", "Crossing", "Copy Paste")
    val playTypes = listOf("jodi", "crossing", "copy_paste")

    var selectedTab by remember { mutableStateOf(0) }

    val numbers = (0..99).toList()
    val amountMap = remember { mutableStateMapOf<Int, String>() }
    val totalAmount by derivedStateOf {
        amountMap.values.sumOf { it.toIntOrNull() ?: 0 }
    }

    val playState by gameViewModel.playState.collectAsState()
    val isLoading = playState is GameViewModel.PlayState.Loading

    var showBidReceivedDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab) { amountMap.clear() }

    LaunchedEffect(playState) {
        when (val s = playState) {
            is GameViewModel.PlayState.Success -> {
                if (selectedTab != 0) {              // ← only show for Crossing/CopyPaste
                    showBidReceivedDialog = true
                }
                profileViewModel.fetchProfile(token)
                gameViewModel.resetPlayState()
            }
            is GameViewModel.PlayState.Error -> {
                if (selectedTab != 0) {              // ← only show for Crossing/CopyPaste
                    errorMessage = s.message
                    showErrorDialog = true
                }
                gameViewModel.resetPlayState()
            }
            else -> Unit
        }
    }

    val bgDark = Color(0xFF1B1B1B)
    val redBtn = Color(0xFFE53935)

    // ── Success Dialog ────────────────────────────────────
    if (showBidReceivedDialog) {
        Dialog(onDismissRequest = { showBidReceivedDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bid Received!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B1B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aapki bid successfully place ho gayi hai.\nResult baad mein declare hoga.",
                        fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showBidReceivedDialog = false; amountMap.clear() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // ── Error Dialog ──────────────────────────────────────
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = redBtn)
                ) { Text("OK", color = Color.White) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(bgDark)) {

        // ── Top Bar ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgDark)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text("←", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(gameName.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("₹$walletBalance", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium) // ← real balance
            }
        }

        // ── Tabs ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabLabels.forEachIndexed { index, label ->
                Button(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) redBtn else Color(0xFF333333)
                    )
                ) {
                    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        // ── Content + Bottom Bar ──────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // Content takes all remaining space
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> JodiScreen(
                        numbers       = numbers,
                        gameId        = gameId,
                        gameName      = gameName,
                        walletBalance = walletBalance,  // ← pass balance
                        onBidSuccess  = { profileViewModel.fetchProfile(token) },
                        gameViewModel = gameViewModel
                    )
                    1 -> CrossingScreen(amountMap)
                    2 -> CopyPasteScreen(amountMap)
                }
            }

            // ── Bottom Bar (only Crossing & CopyPaste) ────
            if (selectedTab != 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            totalAmount.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B)
                        )
                        Text("Total Amount", fontSize = 13.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            if (!isLoading && totalAmount > 0) {
                                amountMap.forEach { (number, amtStr) ->
                                    val amt = amtStr.toIntOrNull() ?: 0
                                    if (amt > 0) {
                                        gameViewModel.playGame(
                                            token    = token,
                                            gameId   = gameId,
                                            playType = playTypes[selectedTab],
                                            number   = number.toString().padStart(2, '0'),
                                            amount   = amt
                                        )
                                    }
                                }
                            }
                        },
                        enabled  = !isLoading && totalAmount > 0,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = redBtn,
                            disabledContainerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text("Place Bet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// ════════════════════════════════════════════════════════════════════════════════
// ══════════════════════ JODI SCREEN - WITH ANDAR & BAHAR ═══════════════════════
// ════════════════════════════════════════════════════════════════════════════════

@SuppressLint("UnrememberedMutableState")
@Composable
private fun JodiScreen(
    numbers: List<Int>,
    gameId: Int,
    gameName: String,
    walletBalance: Int,
    gameViewModel: GameViewModel = viewModel(),
    onBidSuccess: () -> Unit
) {
    val darkBg = Color(0xFF1B1B1B)
    val redBtn = Color(0xFFE53935)

    // Separate state maps for each play type
    val jodiAmountMap = remember { mutableStateMapOf<Int, String>() }
    val andarAmountMap = remember { mutableStateMapOf<Int, String>() }
    val baharAmountMap = remember { mutableStateMapOf<Int, String>() }

    val balanceFromGame by gameViewModel.balance.collectAsState()
    val currentBalance = if (balanceFromGame > 0) balanceFromGame else walletBalance

    val totalAmount by derivedStateOf {
        val jodi = jodiAmountMap.values.sumOf { it.toIntOrNull() ?: 0 }
        val andar = andarAmountMap.values.sumOf { it.toIntOrNull() ?: 0 }
        val bahar = baharAmountMap.values.sumOf { it.toIntOrNull() ?: 0 }
        jodi + andar + bahar
    }

    val playState by gameViewModel.playState.collectAsState()
    val context = LocalContext.current
    val isLoading = playState is GameViewModel.PlayState.Loading

    val token = remember {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("auth_token", "") ?: ""
        val finalToken = if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
        finalToken
    }

    var showBidReceivedDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(playState) {
        when (val s = playState) {
            is GameViewModel.PlayState.Success -> {
                showBidReceivedDialog = true
                onBidSuccess()
                gameViewModel.resetPlayState()
            }

            is GameViewModel.PlayState.Error -> {
                errorMessage = s.message
                showErrorDialog = true
                gameViewModel.resetPlayState()
            }

            else -> Unit
        }
    }

    if (showBidReceivedDialog) {
        Dialog(onDismissRequest = { showBidReceivedDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✓",
                            fontSize = 36.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Bid Received!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aapki bid successfully place ho gayi hai.\nResult baad mein declare hoga.",
                        fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showBidReceivedDialog = false
                            jodiAmountMap.clear()
                            andarAmountMap.clear()
                            baharAmountMap.clear()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "OK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = redBtn)
                ) { Text("OK", color = Color.White) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // ── Scrollable Content ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // ── JODI Numbers Grid (00-99) ──────────────────────────────────
            Text(
                "Jodi Numbers (00 - 99)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                val rows = numbers.chunked(5)
                rows.forEach { rowNumbers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowNumbers.forEach { number ->
                            val displayNum = number.toString().padStart(2, '0')
                            JodiInputCard(
                                number = displayNum,
                                value = jodiAmountMap[number] ?: "",
                                onValueChange = { jodiAmountMap[number] = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowNumbers.size < 5) {
                            repeat(5 - rowNumbers.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }

            // ── ANDAR Haruf (अंदर) ─────────────────────────────────────────
            Text(
                "Andar Haruf (अंदर)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (0..9).forEach { digit ->
                    JodiInputCard(
                        number = digit.toString(),
                        value = andarAmountMap[digit] ?: "",
                        onValueChange = { andarAmountMap[digit] = it },
                        modifier = Modifier.weight(1f),
                        isSingleDigit = true
                    )
                }
            }

            // ── BAHAR Haruf (बाहार) ────────────────────────────────────────
            Text(
                "Bahar Haruf (बाहार)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (0..9).forEach { digit ->
                    JodiInputCard(
                        number = digit.toString(),
                        value = baharAmountMap[digit] ?: "",
                        onValueChange = { baharAmountMap[digit] = it },
                        modifier = Modifier.weight(1f),
                        isSingleDigit = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── FIXED Place Bet Bottom Bar ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    totalAmount.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B)
                )
                Text("Total Amount", fontSize = 13.sp, color = Color.Gray)
            }

            Button(
                onClick = {
                    if (!isLoading && totalAmount > 0) {

                        // ← Balance check here
                        if (totalAmount > currentBalance) {
                            errorMessage = "Insufficient balance!"
                            showErrorDialog = true
                            return@Button
                        }

                        jodiAmountMap.forEach { (number, amtStr) ->
                            val amt = amtStr.toIntOrNull() ?: 0
                            if (amt > 0) {
                                gameViewModel.playGame(
                                    token = token,
                                    gameId = gameId,
                                    playType = "jodi",
                                    number = number.toString().padStart(2, '0'),
                                    amount = amt
                                )
                            }
                        }
                        andarAmountMap.forEach { (number, amtStr) ->
                            val amt = amtStr.toIntOrNull() ?: 0
                            if (amt > 0) {
                                gameViewModel.andarPlayGame(
                                    token = token,
                                    gameId = gameId,
                                    number = number.toString(),
                                    amount = amt
                                )
                            }
                        }
                        baharAmountMap.forEach { (number, amtStr) ->
                            val amt = amtStr.toIntOrNull() ?: 0
                            if (amt > 0) {
                                gameViewModel.baharPlayGame(
                                    token = token,
                                    gameId = gameId,
                                    number = number.toString(),
                                    amount = amt
                                )
                            }
                        }
                    }
                },
                enabled = !isLoading && totalAmount > 0,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = redBtn,
                    disabledContainerColor = Color(0xFFBDBDBD)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Place Bet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        }
    }


@Composable
private fun JodiInputCard(
    number: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSingleDigit: Boolean = false
) {
    val blueHeader = Color(0xFF1976D2)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(blueHeader)
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            BasicAmountInput(
                value = value,
                onValueChange = {
                    if (it.length <= 5 && (it.isEmpty() || it.all { ch -> ch.isDigit() })) {
                        onValueChange(it)
                    }
                }
            )
        }
    }
}

@Composable
private fun BasicAmountInput(value: String, onValueChange: (String) -> Unit) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF1B1B1B),
            fontWeight = FontWeight.Medium
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                inner()
            }
        }
    )
}

// ════════════════════════════════════════════════════════════════════════════════
// ══════════════════════ CROSSING SCREEN ════════════════════════════════════════
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun CrossingScreen(amountMap: MutableMap<Int, String>) {
    val yellowBtn = Color(0xFFFDD835)
    val bgColor = Color(0xFFFFFFFF)
    val textColor = Color(0xFF1B1B1B)

    var digitInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // ── Inputs ────────────────────────────────────────
        Text("Enter Digit", fontSize = 14.sp, color = textColor,
            fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))

        OutlinedTextField(
            value = digitInput,
            onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) digitInput = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), // ← no height()
            placeholder = { Text("00", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        Text("Amount", fontSize = 14.sp, color = textColor,
            fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))

        OutlinedTextField(
            value = amountInput,
            onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 6) amountInput = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), // ← no height()
            placeholder = { Text("0", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        Button(
            onClick = {
                if (digitInput.isNotEmpty() && amountInput.isNotEmpty()) {
                    val digits = digitInput // e.g. "12"

                    // Generate all combinations of each digit with every other digit
                    digits.forEach { d1 ->
                        digits.forEach { d2 ->
                            val num = "$d1$d2".toIntOrNull() ?: 0
                            amountMap[num] = amountInput
                        }
                    }

                    digitInput = ""
                    amountInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = yellowBtn)
        ) {
            Text("Add", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── List ──────────────────────────────────────────
        if (amountMap.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(yellowBtn)
                    .padding(vertical = 8.dp)
            ) {
                Text("Number", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Amount", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Action", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }

            // ← LazyColumn with weight takes remaining space
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(amountMap.toList()) { (number, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (amountMap.keys.indexOf(number) % 2 == 0)
                                Color(0xFFF5F5F5) else Color.White)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(number.toString().padStart(2, '0'), fontSize = 13.sp,
                            color = textColor, modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        Text("₹$amount", fontSize = 13.sp, color = textColor,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Button(
                            onClick = { amountMap.remove(number) },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text("Delete", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun CopyPasteScreen(amountMap: SnapshotStateMap<Int, String>) {
    val yellowBtn = Color(0xFFFDD835)
    val bgColor = Color(0xFFFFFFFF)
    val textColor = Color(0xFF1B1B1B)
    val context = LocalContext.current

    var digitInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var withPalti by remember { mutableStateOf(true) }
    // Replace single amountMap with two separate maps
    val paltiAmountMap = remember { mutableStateMapOf<Int, String>() }
    val nonPaltiAmountMap = remember { mutableStateMapOf<Int, String>() }
    val currentMap = if (withPalti) paltiAmountMap else nonPaltiAmountMap

    fun syncToParent(isPalti: Boolean = withPalti) {
        amountMap.clear()
        amountMap.putAll(if (isPalti) paltiAmountMap else nonPaltiAmountMap)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // ── Inputs ────────────────────────────────────────
        Text("Enter Number", fontSize = 14.sp, color = textColor,
            fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))

        OutlinedTextField(
            value = digitInput,
            onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) digitInput = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), // ← no height()
            placeholder = { Text("00", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        Text("Amount", fontSize = 14.sp, color = textColor,
            fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))

        OutlinedTextField(
            value = amountInput,
            onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 6) amountInput = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), // ← no height()
            placeholder = { Text("0", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        // ── Palti Toggle ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { withPalti = true; syncToParent(true) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (withPalti) yellowBtn else Color(0xFFEEEEEE))
            ) {
                Text("✓ With Palti",
                    color = if (withPalti) Color.Black else Color.Gray,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { withPalti = false; syncToParent(false) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!withPalti) yellowBtn else Color(0xFFEEEEEE))
            ) {
                Text("✓ Without Palti",
                    color = if (!withPalti) Color.Black else Color.Gray,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = {
                when {
                    digitInput.isEmpty() || amountInput.isEmpty() ->
                        Toast.makeText(context, "Please enter number and amount", Toast.LENGTH_SHORT).show()
                    digitInput.length < 2 ->
                        Toast.makeText(context, "Please enter a 2-digit number", Toast.LENGTH_SHORT).show()
                    else -> {
                        if (withPalti) {
                            val d1 = digitInput[0].toString()
                            val d2 = digitInput[1].toString()
                            val num1 = (d1 + d2).toInt()
                            val num2 = (d2 + d1).toInt()
                            paltiAmountMap[num1] = amountInput
                            if (num2 != num1) paltiAmountMap[num2] = amountInput
                        } else {
                            val digit = digitInput.toIntOrNull() ?: 0
                            nonPaltiAmountMap[digit] = amountInput
                        }
                        syncToParent()  // ← sync to parent
                        digitInput = ""
                        amountInput = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = yellowBtn)
        ) {
            Text("Add", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── List ──────────────────────────────────────────


        if (currentMap.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(yellowBtn)
                    .padding(vertical = 8.dp)
            ) {
                Text("Number", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Amount", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Action", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }

            // ← weight(1f) so list takes remaining space
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(currentMap.toList()) { (number, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (currentMap.keys.indexOf(number) % 2 == 0)
                                Color(0xFFF5F5F5) else Color.White)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(number.toString().padStart(2, '0'), fontSize = 13.sp,
                            color = textColor, modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        Text("₹$amount", fontSize = 13.sp, color = textColor,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Button(
                            onClick = {
                                currentMap.remove(number)
                                syncToParent()
                                      },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).height(32.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text("Delete", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}