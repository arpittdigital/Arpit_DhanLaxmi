package com.bmdu.dhanlaxmi.Dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.viewModel.BankDetailsViewModel

private const val TAG = "BankWithdrawScreens"

private val GreenMid    = Color(0xFF006400)
private val YellowMain  = Color(0xFFD4A800)
private val YellowLight = Color(0xFFF0C000)
private val CardBg      = Color(0xFF0A2E0A)
private val FieldBg     = Color(0xFF0D380D)

// ══════════════════════════════════════════════════════════
//  SCREEN 1 — BANK DETAILS (save)
// ══════════════════════════════════════════════════════════

@Composable
fun BankDetailsScreen(navController: NavController) {

    var bankName      by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode      by remember { mutableStateOf("") }

    val viewModel: BankDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.bankDetailsState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    LaunchedEffect(state) {
        if (state is BankDetailsViewModel.BankDetailsState.Success) {
            viewModel.resetState()
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    DisposableEffect(Unit) { onDispose { viewModel.resetState() } }

    Column(modifier = Modifier.fillMaxSize().background(GreenMid)) {

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(YellowMain, YellowLight)))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black,
                modifier = Modifier.size(24.dp).clickable { navController.popBackStack() })
            Spacer(Modifier.width(12.dp))
            Text("Bank Details", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error Banner
            if (state is BankDetailsViewModel.BankDetailsState.Error) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB00020)),
                    shape = RoundedCornerShape(8.dp)) {
                    Text((state as BankDetailsViewModel.BankDetailsState.Error).message,
                        color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(6.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Bank Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(28.dp))

                    BankInputField("Bank Name",      bankName,      { bankName = it })
                    Spacer(Modifier.height(16.dp))
                    BankInputField("Account Number", accountNumber, { accountNumber = it })
                    Spacer(Modifier.height(16.dp))
                    BankInputField("IFSC Code",      ifscCode,      { ifscCode = it.uppercase() })
                    Spacer(Modifier.height(28.dp))

                    val isLoading   = state is BankDetailsViewModel.BankDetailsState.Loading
                    val isFormValid = bankName.isNotBlank() && accountNumber.isNotBlank() && ifscCode.isNotBlank()

                    Button(
                        onClick = {
                            if (!token.isNullOrBlank())
                                viewModel.saveBankDetails(token, bankName.trim(), accountNumber.trim(), ifscCode.trim())
                        },
                        enabled  = isFormValid && !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = YellowMain,
                            disabledContainerColor = Color(0xFF888800)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading)
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else
                            Text("Save Bank Details", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  SCREEN 2 — WITHDRAWAL MONEY
// ══════════════════════════════════════════════════════════

@Composable
fun WithdrawalMoneyScreen(navController: NavController) {

    var amount       by remember { mutableStateOf("") }
    var phoneNumber  by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Select Mode") }
    var dropdownOpen by remember { mutableStateOf(false) }

    // Bank fields — auto-filled from API, user can still edit
    var bankName      by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode      by remember { mutableStateOf("") }

    // Display label → exact API value (confirmed via Postman)
    val paymentModes = linkedMapOf(
        "Paytm"      to "paytm",
        "Google Pay" to "google pay",
        "PhonePe"    to "phonepay",
        "UPI"        to "upi",
        "Bank"       to "bank"
    )

    val isBankMode = selectedMode == "Bank"

    val viewModel: BankDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state          by viewModel.bankDetailsState.collectAsState()
    val savedBankState by viewModel.savedBankState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    // ── Auto-fill bank fields jab API se data aaye ───────
    LaunchedEffect(savedBankState) {
        if (savedBankState is BankDetailsViewModel.SavedBankState.Success) {
            val d = (savedBankState as BankDetailsViewModel.SavedBankState.Success).data
            bankName      = d.bank_name      ?: ""
            accountNumber = d.account_number ?: ""
            ifscCode      = d.ifsc_code      ?: ""
            Log.d(TAG, "Auto-filled → bank=${d.bank_name}")
        }
    }

    // ── Success / Error handling ─────────────────────────
    LaunchedEffect(state) {
        when (val s = state) {
            is BankDetailsViewModel.BankDetailsState.Success -> {
                android.widget.Toast.makeText(context, s.message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            else -> Unit
        }
    }

    DisposableEffect(Unit) { onDispose { viewModel.resetState() } }

    Column(modifier = Modifier.fillMaxSize().background(GreenMid)) {

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(YellowMain, YellowLight)))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black,
                modifier = Modifier.size(24.dp).clickable { navController.popBackStack() })
            Spacer(Modifier.width(12.dp))
            Text("Withdrawal Money", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Error Banner
            if (state is BankDetailsViewModel.BankDetailsState.Error) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB00020)),
                    shape = RoundedCornerShape(8.dp)) {
                    Text((state as BankDetailsViewModel.BankDetailsState.Error).message,
                        color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                }
            }

            WithdrawTextField(amount,      "Amount",       { amount = it })
            Spacer(Modifier.height(14.dp))
            WithdrawTextField(phoneNumber, "Phone Number", { phoneNumber = it })
            Spacer(Modifier.height(14.dp))

            // ── Payment Mode Dropdown ────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(
                            width = if (dropdownOpen) 2.dp else 0.dp,
                            color = if (dropdownOpen) YellowMain else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { dropdownOpen = !dropdownOpen }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = selectedMode,
                        color = if (selectedMode == "Select Mode") Color.Gray else Color.Black,
                        fontSize = 15.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                }

                DropdownMenu(
                    expanded         = dropdownOpen,
                    onDismissRequest = { dropdownOpen = false },
                    modifier         = Modifier.fillMaxWidth(0.88f).background(Color.White)
                ) {
                    paymentModes.keys.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(label, color = Color.Black, fontSize = 15.sp) },
                            onClick = {
                                selectedMode = label
                                dropdownOpen = false
                                if (label == "Bank") {
                                    // Bank select → saved details fetch karo (auto-fill)
                                    viewModel.getSavedBankDetails(token)
                                } else {
                                    // Dusra mode → bank fields clear + state reset
                                    bankName      = ""
                                    accountNumber = ""
                                    ifscCode      = ""
                                }
                                Log.d(TAG, "Mode selected → $label (api=${paymentModes[label]})")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── Bank Details Card (Bank mode mein dikhega) ─
            if (isBankMode) {
                Spacer(Modifier.height(14.dp))
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Bank Details", color = YellowMain, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))

                        when (savedBankState) {
                            is BankDetailsViewModel.SavedBankState.Loading -> {
                                // API call chal rahi hai — loader dikhao
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = YellowMain,
                                        modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                                }
                            }
                            is BankDetailsViewModel.SavedBankState.Error -> {
                                // No saved data — user manually fill kare
                                Text(
                                    text     = "No saved bank details found. Please enter manually.",
                                    color    = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                                WithdrawTextField(bankName,      "Bank Name",      { bankName = it })
                                Spacer(Modifier.height(10.dp))
                                WithdrawTextField(accountNumber, "Account Number", { accountNumber = it })
                                Spacer(Modifier.height(10.dp))
                                WithdrawTextField(ifscCode,      "IFSC Code",      { ifscCode = it.uppercase() })
                            }
                            else -> {
                                // Success ya Idle — fields dikhao (auto-filled ya empty)
                                WithdrawTextField(bankName,      "Bank Name",      { bankName = it })
                                Spacer(Modifier.height(10.dp))
                                WithdrawTextField(accountNumber, "Account Number", { accountNumber = it })
                                Spacer(Modifier.height(10.dp))
                                WithdrawTextField(ifscCode,      "IFSC Code",      { ifscCode = it.uppercase() })
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            val isLoading = state is BankDetailsViewModel.BankDetailsState.Loading
            val isBankLoading = savedBankState is BankDetailsViewModel.SavedBankState.Loading
            val isBankValid = !isBankMode ||
                    (bankName.isNotBlank() && accountNumber.isNotBlank() && ifscCode.isNotBlank())

            val isWithdrawValid = amount.isNotBlank() && phoneNumber.isNotBlank() &&
                    selectedMode != "Select Mode" && isBankValid && !isLoading && !isBankLoading

            Button(
                onClick = {
                    val parsedAmount = amount.trim().toIntOrNull() ?: 0
                    val apiMode      = paymentModes[selectedMode] ?: selectedMode.lowercase()
                    viewModel.withdrawal(
                        token         = token,
                        amount        = parsedAmount,
                        mobile        = phoneNumber.trim(),
                        paymentMode   = apiMode,
                        bankName      = if (isBankMode) bankName.trim()      else null,
                        accountNumber = if (isBankMode) accountNumber.trim() else null,
                        ifscCode      = if (isBankMode) ifscCode.trim()      else null
                    )
                },
                enabled  = isWithdrawValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = YellowMain,
                    disabledContainerColor = Color(0xFF888800)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading)
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else
                    Text("Withdrawal Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = { navController.navigate("withdraw_history") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = YellowLight),
                shape    = RoundedCornerShape(26.dp)
            ) {
                Text("Withdraw History", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun WithdrawalHistoryScreen(navController: NavController) {

    val viewModel: BankDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val historyState by viewModel.withdrawalHistoryState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    LaunchedEffect(Unit) {
        viewModel.withdrawalHistory(token)
    }

    Column(modifier = Modifier.fillMaxSize().background(GreenMid)) {

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(YellowMain, YellowLight)))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black,
                modifier = Modifier.size(24.dp).clickable { navController.popBackStack() })
            Spacer(Modifier.width(12.dp))
            Text("Withdrawal History", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        when (val s = historyState) {

            is BankDetailsViewModel.WithdrawalHistoryState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YellowMain)
                }
            }

            is BankDetailsViewModel.WithdrawalHistoryState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = Color.White, fontSize = 15.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                }
            }

            is BankDetailsViewModel.WithdrawalHistoryState.Success -> {
                if (s.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No withdrawal history found.", color = Color.White, fontSize = 15.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(s.items) { item ->
                            Card(
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(12.dp),
                                colors    = CardDefaults.cardColors(containerColor = CardBg),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                                    // Amount + Status
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {

                                        Text("₹${item.amount}", color = YellowMain,
                                            fontWeight = FontWeight.Bold, fontSize = 20.sp)

                                        val (chipColor, statusText) = when (item.status.lowercase()) {
                                            "approved" -> Color(0xFF2E7D32) to "Approved"
                                            "rejected" -> Color(0xFFB00020) to "Rejected"
                                            else       -> Color(0xFF795548) to "Pending"
                                        }
                                        Surface(shape = RoundedCornerShape(20.dp), color = chipColor) {
                                            Text(statusText, color = Color.White, fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    Spacer(Modifier.height(8.dp))

                                    HistoryRow("Mode",   item.payment_mode.replaceFirstChar { it.uppercase() })
                                    HistoryRow("Mobile", item.mobile)
                                    item.bank_name?.let      { HistoryRow("Bank",    it) }
                                    item.account_number?.let { HistoryRow("Acc No.", it) }
                                    item.ifsc_code?.let      { HistoryRow("IFSC",    it) }
                                    item.created_at?.let     { HistoryRow("Date",    it.take(10)) }
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

// ─── Helper composables ────────────────────────────────────

@Composable
private fun HistoryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp,
            modifier = Modifier.width(70.dp))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WithdrawTextField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 15.sp) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor      = YellowMain,
            unfocusedBorderColor    = Color.Transparent,
            focusedTextColor        = Color.Black,
            unfocusedTextColor      = Color.Black,
            cursorColor             = YellowMain
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun BankInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = FieldBg,
            unfocusedContainerColor = FieldBg,
            focusedBorderColor      = YellowMain,
            unfocusedBorderColor    = Color.White.copy(alpha = 0.3f),
            focusedTextColor        = Color.White,
            unfocusedTextColor      = Color.White,
            cursorColor             = YellowMain,
            focusedLabelColor       = YellowMain
        ),
        shape = RoundedCornerShape(10.dp)
    )
}