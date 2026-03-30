package com.bmdu.dhanlaxmi.Dashboard

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bmdu.dhanlaxmi.Api.RetrofitClient
import com.bmdu.dhanlaxmi.Model.ContactData
import com.bmdu.dhanlaxmi.Model.GameData
import com.bmdu.dhanlaxmi.Model.TokenManager
import com.bmdu.dhanlaxmi.R
import com.bmdu.dhanlaxmi.components.LogoutConfirmDialog
import com.bmdu.dhanlaxmi.ui.theme.GoldTheme
import com.bmdu.dhanlaxmi.viewModel.GameViewModel
import com.bmdu.dhanlaxmi.viewModel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════
//  COLOR PALETTE
// ══════════════════════════════════════════════════════════

private val DarkGreen     = Color(0xFF002601)
private val MidGreen      = Color(0xFF004D02)
private val BrightGreen   = Color(0xFF00A904)
private val CardGreen     = Color(0xFF005C02)
private val CardGreenDark = Color(0xFF003D01)
val GoldText      = Color(0xFFE7D156)   // bright gold — visible on dark green
private val GoldMid       = Color(0xFFDBB74C)
val BlackText     = Color(0xFF1A1100)   // near-black — visible on bright gold bg

// ══════════════════════════════════════════════════════════
//  DATA MODELS
// ══════════════════════════════════════════════════════════

data class DrawerMenuItem(
    val icon : ImageVector,
    val label: String,
    val route: String
)

val drawerMenuItems = listOf(
    DrawerMenuItem(Icons.Filled.Home,           "Home",            "home"),
    DrawerMenuItem(Icons.Filled.Person,         "User Profile",    "profile_screen"),
    DrawerMenuItem(Icons.Filled.Star,           "Winning History", "winning_history"),
    DrawerMenuItem(Icons.Filled.List,           "Bid History",     "history"),
    DrawerMenuItem(Icons.Filled.AccountBalance, "Banking Details", "bank_details"),
    DrawerMenuItem(Icons.Filled.StarRate,       "Game Rate",       "game_rate"),
    DrawerMenuItem(Icons.Filled.ContactPage,    "Contact Us",      "contact_us"),
//    DrawerMenuItem(Icons.Filled.PlayCircle,     "How to Play",     "how_to_play"),
    DrawerMenuItem(Icons.Filled.Lock,           "Privacy Policy",  "privacy_policy"),
    DrawerMenuItem(Icons.Filled.Logout,         "Logout",          "logout"),
)

val bottomNavItems = listOf(
    Triple("home",    Icons.Default.Home,      "Home"),
    Triple("history", Icons.Default.Replay,    "History"),
    Triple("result",  Icons.Default.AutoGraph, "Result"),
    Triple("chart",   Icons.Default.BarChart,  "Chart"),
    Triple("share",   Icons.Default.Share,     "Share"),
)

// ══════════════════════════════════════════════════════════
//  ROOT
// ══════════════════════════════════════════════════════════

@Composable
fun HomeScreen(navController: NavController,profileViewModel : ProfileViewModel) {

    val drawerState      = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope            = rememberCoroutineScope()
    var selectedRoute    by remember { mutableStateOf("home") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val state by profileViewModel.profileState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    // Read signup data as fallback when no token yet
    val signupName  = prefs.getString("signup_name", null)
    val signupPhone = prefs.getString("signup_phone", null)

    val userName = when (val s = state) {
        is ProfileViewModel.ProfileState.Success -> s.data.data?.name ?: "User"
        else -> "User"
    }
    val userPhone = when (val s = state) {
        is ProfileViewModel.ProfileState.Success -> s.data.data?.phone ?: ""  // dynamic phone
        else -> ""
    }
    val userPoints = when (val s = state) {
        is ProfileViewModel.ProfileState.Success -> s.data.data?.wallet_amount ?: 0
        else -> 0
    }

    LaunchedEffect(Unit) {
        if (!token.isNullOrBlank()) {
            profileViewModel.fetchProfile("Bearer $token")  // ← add Bearer
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                navController.navigate("login") { popUpTo("home") { inclusive = true } }
            },
            onCancel = { showLogoutDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        scrimColor    = Color.Black.copy(alpha = 0.6f),
        drawerContent = {
            DrawerContent(
                userName      = userName,
                userPhone     = userPhone,
                userPoints    = userPoints,
                menuItems     = drawerMenuItems,
                selectedRoute = selectedRoute,
                onItemClick   = { route ->
                    scope.launch { drawerState.close() }
                    when (route) {
                        "logout" -> {
                            TokenManager.clearToken(context)
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        }
                        "home" -> selectedRoute = "home"
                        else   -> { selectedRoute = route; navController.navigate(route) }
                    }
                }
            )
        }
    ) {
        HomeScreenContent(navController = navController, onMenuClick = { scope.launch { drawerState.open() } },profileViewModel = profileViewModel)
    }
    //  Re-fetch profile every time token changes
    LaunchedEffect(Unit) {  // ← Unit triggers every time screen opens
        if (!token.isNullOrBlank()) profileViewModel.fetchProfile(token)
    }
}

// ══════════════════════════════════════════════════════════
//  MAIN CONTENT
// ══════════════════════════════════════════════════════════

@Composable
fun HomeScreenContent(navController: NavController, onMenuClick: () -> Unit,profileViewModel : ProfileViewModel) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val rawToken = prefs.getString("auth_token", null) ?: ""
    val token = "Bearer $rawToken"

    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gamestate.collectAsState()

    val isRefreshing = gameState is GameViewModel.GameState.Loading

    var contact by remember { mutableStateOf<ContactData?>(null) }

    LaunchedEffect(Unit) {
        try {
            val token = "Bearer ${token}" // from DataStore/SharedPrefs
            val response = RetrofitClient.instance.getContacts(token)
            if (response.status) {
                contact = response.data.firstOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) { viewModel.fetchGames(token) }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route ?: "home"

    Scaffold(
        topBar = { TopBar(onMenuClick = onMenuClick, navController = navController, profileViewModel = profileViewModel) },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                navController = navController
            )
        },
        containerColor = DarkGreen
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.fetchGames(token)
                profileViewModel.fetchProfile("Bearer $token")
            },
            indicator = { state, trigger ->
//                SwipeRefreshIndicator(
//                    state = state,
//                    refreshTriggerDistance = trigger,
//                    backgroundColor = CardGreen,
//                    contentColor = GoldText,
//                    scale = true
//                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // Banner directly — no hero Box gap
                item {
                    Spacer(Modifier.height(8.dp))
                    NoticeMarqueeBanner()
                }

                // QuickActionButtons — no offset
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),  // removed offset
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardGreen),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(brush = GoldTheme.metallicBrushHorizontal)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {QuickActionButton(
                                icon = painterResource(id = R.drawable.telegramimg),
                        label = "Telegram",
                        iconTint = Color.Unspecified,
                        iconSize = 25.dp,
                        boxSize = 37.dp,
                        fontSize = 11.sp,
                        onClick = {
                            contact?.telegram_link?.let { link ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                            }
                        }
                        )
                            VDivider()
                            QuickActionButton(
                                icon = painterResource(id = R.drawable.whatsapplogo),
                                label = "WhatsApp",
                                iconTint = Color.Unspecified,
                                iconSize = 25.dp,
                                boxSize = 37.dp,
                                fontSize = 11.sp,
                                onClick = {
                                    contact?.whatsapp_number?.let { number ->
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://wa.me/$number")
                                            )
                                        )
                                    }
                                }
                            )
                            VDivider()
                            QuickActionButton(
                                icon = Icons.Default.AccountBalanceWallet,
                                label = "Add Funds",
                                iconTint = Color(0xFF6EE98A),
                                iconSize = 25.dp,
                                boxSize  = 37.dp,
                                fontSize = 11.sp,
                                onClick = { navController.navigate("add_money") }
                            )
                            VDivider()
                            QuickActionButton(
                                icon = Icons.Default.CurrencyRupee,
                                label = "Withdraw",
                                iconTint = Color(0xFFFC8181),
                                iconSize = 25.dp,
                                boxSize  = 37.dp,
                                fontSize = 11.sp,
//                                onClick = {navController.navigate("withdrawal")}
                                onClick = {
                                    val now = java.util.Calendar.getInstance()
                                    val totalMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                                            now.get(java.util.Calendar.MINUTE)

                                    if (totalMinutes < 420 || totalMinutes > 750) {
                                        Toast.makeText(context, "Withdrawal is only available between 7:00 AM and 12:30 PM", Toast.LENGTH_LONG).show()
                                    } else {
                                        navController.navigate("withdrawal")
                                    }
                                }
                            )
                        }
                    }
                }



                // Live Games label
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.width(4.dp).height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(brush = GoldTheme.metallicBrush)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Live Games",
                            color = GoldText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Game list
                when (val s = gameState) {
                    is GameViewModel.GameState.Loading -> {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            brush = GoldTheme.metallicBrush,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(44.dp),
                                        color = BlackText,
                                        trackColor = Color(0x30000000),
                                        strokeWidth = 4.dp
                                    )
                                }
                            }
                        }
                    }

                    is GameViewModel.GameState.Error -> {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        null,
                                        tint = GoldText.copy(alpha = 0.7f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        s.message,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = GoldTheme.metallicBrush,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.fetchGames(token) }
                                            .padding(horizontal = 24.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Retry",
                                            color = BlackText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is GameViewModel.GameState.Success -> {
                        val sortedGames = s.games.sortedByDescending {
                            it.status.equals("open", ignoreCase = true)
                        }
                        items(sortedGames) { game ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                                ApiGameCard(
                                    game = game,
                                    onCardClick = { navController.navigate("delhi_bazar/${game.id}/${game.game_name}") },
                                    onPlayClick = { navController.navigate("delhi_bazar/${game.id}/${game.game_name}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  GAME CARD
// ══════════════════════════════════════════════════════════

@Composable
fun ApiGameCard(game: GameData, onCardClick: () -> Unit = {}, onPlayClick: () -> Unit = {}) {
    val context = LocalContext.current

    fun formatTime(time: String?): String {
        if (time.isNullOrBlank()) return "--"
        return try {
            val input  = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
            val output = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            output.format(input.parse(time)!!)
        } catch (e: Exception) {
            try {
                val input  = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
                val output = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                output.format(input.parse(time)!!)
            } catch (e2: Exception) {
                time
            }
        }
    }

    val gameName   = game.game_name ?: "Game"
    val status     = game.status ?: ""
    val isPlayable = status.equals("open", ignoreCase = true)
    Log.d("GameCard", "${game.game_name} → status='${game.status}'")

    val formattedOpen  = formatTime(game.open_time)
    val formattedClose = formatTime(game.close_time)

    // ── Number logic ──────────────────────────────────────────────────────────
    // result_status = "NEW"  → number declared today (after 12 PM)
    //                          aaj = number,  kal = XX
    // result_status = "OLD"  → number carried from previous day
    //                          kal = number,  aaj = XX
    // anything else / blank  → both XX (not declared yet)
    // ─────────────────────────────────────────────────────────────────────────
    val number = if (game.number.isNullOrBlank()) "XX" else game.number

    var kalNum = if (game.old_number.isNullOrBlank()) "XX" else game.old_number
    var aajNum = if (game.new_number.isNullOrBlank()) "XX" else game.new_number

//    when (game.result_status?.uppercase()) {
//        "NEW" -> { aajNum = number; kalNum = "XX" }
//        "OLD" -> { kalNum = number; aajNum = "XX" }
//        else  -> { kalNum = "XX";   aajNum = "XX" }
//    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable {
                if (isPlayable) onCardClick()
                else Toast.makeText(
                    context,
                    "This game is closed. Opens at $formattedOpen",
                    Toast.LENGTH_SHORT
                ).show()
            },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF1A4A1A)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        // ── Top gold accent bar ──
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(brush = GoldTheme.metallicBrushHorizontal)
        )

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {

            // ── Left: Kal › Aaj numbers ──
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = kalNum,
                        color      = Color(0xFFA0D0A0),
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Text(
                        text       = "OLD",
                        color      = Color(0xFF6A9A6A),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text     = "›",
                    color    = Color(0xFF3D6B3D),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = aajNum,
                        color      = GoldText,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Text(
                        text       = "NEW",
                        color      = Color(0xFFC8A030),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Vertical divider ──
            Box(
                Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(Color(0xFF2A5A2A))
            )

            // ── Middle: Game name + Open/Close times ──
            Column(modifier = Modifier.weight(1f)) {

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text       = gameName,
                        color      = Color.White,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlayable) Color(0xFF00E676)
                                else Color(0xFFCC4444)
                            )
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Open — left
                    Column {
                        Text(
                            text       = "Open",
                            color      = Color(0xFF8AAA8A),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text       = formattedOpen,
                            color      = Color(0xFFC8D8C8),
                            fontSize   = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Close — right
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text       = "Close",
                            color      = Color(0xFF8AAA8A),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text       = formattedClose,
                            color      = Color(0xFFC8D8C8),
                            fontSize   = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Right: Play Now / Closed — same gold brush, label changes ──
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .shadow(3.dp, RoundedCornerShape(10.dp))
                    .background(
                        brush = GoldTheme.metallicBrush,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        if (isPlayable) onPlayClick()
                        else Toast.makeText(
                            context,
//                            "This game is closed. Opens tomorrow at $formattedOpen",
                            "This game is closed. Opens at $formattedOpen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = if (isPlayable) "Play Now" else "Closed",
                    color      = BlackText,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    maxLines   = 1
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  TOP BAR
// ══════════════════════════════════════════════════════════

@Composable
fun TopBar(onMenuClick: () -> Unit, navController: NavController,profileViewModel : ProfileViewModel) {
    val state by profileViewModel.profileState.collectAsState()

    val amount = when (state) {
        is ProfileViewModel.ProfileState.Success ->
            (state as ProfileViewModel.ProfileState.Success).data.data?.wallet_amount?.toString() ?: "0"
        else -> "0"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = GoldTheme.metallicBrush)
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menu icon on left
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, "Menu", tint = BlackText, modifier = Modifier.size(26.dp))
        }

        //Logo in center
        Box(
            modifier = Modifier
                .size(46.dp)
                .border(2.dp, BlackText.copy(alpha = 0.20f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier           = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
            )
        }

        // Wallet on right (replaced bell)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "Wallet",
                color      = BlackText.copy(alpha = 0.60f),
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "₹ $amount",
                color      = BlackText,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
//  QUICK ACTION BUTTONS
// ══════════════════════════════════════════════════════════

@Composable
fun QuickActionButtons(navController: NavController) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-36).dp),   // overlap hero — reduced gap
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardGreen),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(brush = GoldTheme.metallicBrushHorizontal))
        Row(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            QuickActionButton(
                icon     = painterResource(id = R.drawable.telegramimg),
                label    = "Telegram",
                iconTint = Color.Unspecified,
                onClick  = {}
            )
            VDivider()
            QuickActionButton(
                icon     = painterResource(id = R.drawable.whatsapplogo),
                label    = "WhatsApp",
                iconTint = Color.Unspecified,
                onClick  = {}
            )
            VDivider()

            QuickActionButton(
                icon     = Icons.Default.AccountBalanceWallet,
                label    = "Add Funds",
                iconTint = Color(0xFF6EE98A),
                onClick  = { navController.navigate("add_money") }
            )

            VDivider()

            QuickActionButton(
                icon     = Icons.Default.CurrencyRupee,
                label    = "Withdraw",
                iconTint = Color(0xFFFC8181),
                onClick  = { navController.navigate("withdrawal") }
            )
        }
    }
}

@Composable
private fun VDivider() {
    Box(Modifier.width(1.dp).height(44.dp).background(Color.White.copy(alpha = 0.12f)))
}

@Composable
fun QuickActionButton(icon: Any, label: String,
                      iconSize: Dp = 25.dp, boxSize: Dp = 45.dp,
                      fontSize: TextUnit = 10.sp,
                      iconTint: Color, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable { onClick() }.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier         = Modifier.size(boxSize),

            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(icon, label, tint = iconTint, modifier = Modifier.size(iconSize))
                else           -> Icon(icon as androidx.compose.ui.graphics.painter.Painter, label, tint = iconTint, modifier = Modifier.size(iconSize))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize   = fontSize,
            color      = Color.White,  // white on dark green — readable
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.Center
        )
    }
}

// ══════════════════════════════════════════════════════════
//  BOTTOM NAV
// ══════════════════════════════════════════════════════════

@Composable
fun BottomNavigationBar(currentRoute: String, navController: NavController) {
    val context = LocalContext.current

    val shareMessage = """
        घर बैठे गेम प्ले करो अपने यार दोस्तों को शेयर करो
        सबसे ट्रस्टेड और सबसे ईमानदार धन लक्ष्मी एप्लीकेशन
        960 रेट ✅
        http://65.0.122.72/login  
    """.trimIndent()

    NavigationBar(
        containerColor = Color.Transparent,
        modifier       = Modifier.height(68.dp).background(brush = GoldTheme.metallicBrushHorizontal).navigationBarsPadding()
    ) {
        bottomNavItems.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                icon     = { Icon(icon, label, modifier = Modifier.size(22.dp)) },
                label    = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                selected = isSelected,
                onClick  = {
                    if (!isSelected) when (route) {
                        "home"    -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        "history" -> navController.navigate("history")
                        "chart"   -> navController.navigate("chart")
                        "result"  -> navController.navigate("result")
                        "share"   -> {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        }
                        else -> {}
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = BlackText,               // dark on gold
                    selectedTextColor   = BlackText,
                    unselectedIconColor = BlackText.copy(alpha = 0.45f),
                    unselectedTextColor = BlackText.copy(alpha = 0.45f),
                    indicatorColor      = Color(0x33000000)
                )
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
//  DRAWER
// ══════════════════════════════════════════════════════════

@Composable
fun DrawerContent(
    userName: String, userPhone: String, userPoints: Int,
    menuItems: List<DrawerMenuItem>, selectedRoute: String,
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier             = Modifier.width(280.dp),
        drawerShape          = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        drawerContainerColor = Color(0xFF111111)
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(MidGreen, DarkGreen)))) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(brush = GoldTheme.metallicBrushHorizontal))
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 24.dp)) {
                Box(
                    modifier = Modifier.size(68.dp).border(2.dp, brush = GoldTheme.metallicBrush, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier         = Modifier.size(63.dp).clip(CircleShape).background(DarkGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = GoldText, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(userName,  color = Color.White,                     fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(userPhone, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .background(brush = GoldTheme.metallicBrush, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text("⭐  Points: $userPoints", color = BlackText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
            menuItems.forEach { item ->
                DrawerMenuItemRow(item = item, isSelected = selectedRoute == item.route, onClick = { onItemClick(item.route) })
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun DrawerMenuItemRow(item: DrawerMenuItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor   = if (isSelected) Color(0x22E7D156) else Color.Transparent
    val textColor = if (isSelected) GoldText else Color.White.copy(alpha = 0.85f)
    val iconColor = if (isSelected) GoldText else Color.White.copy(alpha = 0.50f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(start = if (isSelected) 14.dp else 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Box(Modifier.width(3.dp).height(22.dp).clip(RoundedCornerShape(2.dp)).background(brush = GoldTheme.metallicBrush))
            Spacer(Modifier.width(13.dp))
        }
        Icon(item.icon, item.label, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(item.label, color = textColor, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}


@Composable
fun NoticeMarqueeBanner() {

    val notices = listOf(
        "        🙏 A request to all users 🙏 \n After placing a withdrawal request, please do not call or message before 12:00 PM. Your withdrawal will be safely transferred to your account.",
        "       🛡️ Notice (Secure Transactions) \n Dear Customer, The QR code changes every minute. Please do not make payments using old QR codes. Always generate a new QR code."
    )

    val pagerState = rememberPagerState(pageCount = { notices.size })

    // Auto scroll every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val next = (pagerState.currentPage + 1) % notices.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape  = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A5C1A)),
            border = BorderStroke(1.dp, GoldText)
        ) {
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo
                    Image(
                        painter            = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(1.dp, GoldText, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Notice Text
                    Text(
                        text       = notices[page],
                        color      = Color.White,
                        fontSize   = 12.5.sp,
                        lineHeight = 18.sp,
                        maxLines   = 5,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        repeat(notices.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isSelected) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) GoldText
                                        else Color.White.copy(alpha = 0.4f)
                                    )
                                    .clickable {  }
                            )
                        }
                    }

                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

//        // Dots indicator
//        Row(
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment     = Alignment.CenterVertically
//        ) {
//            repeat(notices.size) { index ->
//                val isSelected = pagerState.currentPage == index
//                Box(
//                    modifier = Modifier
//                        .padding(horizontal = 4.dp)
//                        .size(if (isSelected) 10.dp else 7.dp)
//                        .clip(CircleShape)
//                        .background(
//                            if (isSelected) GoldText
//                            else Color.White.copy(alpha = 0.4f)
//                        )
//                        .clickable {  }
//                )
//            }
//        }
    }
}