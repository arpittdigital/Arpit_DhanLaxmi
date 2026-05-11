package com.bmdu.SethGMatka.Dashboard

import android.R.attr.color
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DividerDefaults.color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import com.bmdu.SethGMatka.Api.RetrofitClient
import com.bmdu.SethGMatka.Model.ContactData
import com.bmdu.SethGMatka.Model.GameData
import com.bmdu.SethGMatka.Model.TokenManager
import com.bmdu.SethGMatka.components.LogoutConfirmDialog
import com.bmdu.SethGMatka.ui.theme.GoldTheme
import com.bmdu.SethGMatka.viewModel.GameViewModel
import com.bmdu.SethGMatka.viewModel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.bmdu.SethGMatka.R
import kotlin.text.toFloat

// ══════════════════════════════════════════════════════════
//  COLOR PALETTE
// ══════════════════════════════════════════════════════════

// ===== Royal Navy + Gold Theme =====

val GoldText      = Color(0xFFF4C542)   // premium gold text
private val GoldLight   = Color(0xFFFFD76A)

val PageNavy    = Color(0xFF07152B)   // main dark navy background
val TopBarBlack         = Color(0xFF050B18)   // deep navy-black top/bottom bar

private val CardNavy    = Color(0xFF0E1B32)   // main game cards
//private val CardDark    = Color(0xFF13233F)   // quick action cards

val YellowText          = Color(0xFFF4C542)   // luxury gold on dark
private val CardBlack2  = Color(0xFF1A2A45)   // secondary card tone

val BlackText           = Color(0xFFF8F8F8)   // light text for dark bg

// Optional extra colors
val BorderGold          = Color(0xFFD4A73A)
val SoftGold            = Color(0xFFFFE08A)
val DimText             = Color(0xFFB8C1CC)
val GreenDot            = Color(0xFF38D26E)
   // near-black — visible on bright gold bg

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
    DrawerMenuItem(Icons.Filled.History, "Transactions", "transactions"),
    DrawerMenuItem(Icons.Filled.List,           "Bid History",     "history"),
    DrawerMenuItem(Icons.Filled.AccountBalance, "Banking Details", "bank_details"),
    DrawerMenuItem(Icons.Filled.StarRate,       "Game Rate",       "game_rate"),
//    DrawerMenuItem(Icons.Filled.ContactPage,    "Contact Us",      "contact_us"),
//    DrawerMenuItem(Icons.Filled.PlayCircle,     "How to Play",     "how_to_play"),
    DrawerMenuItem(Icons.Filled.Lock,           "Privacy Policy",  "privacy_policy"),
    DrawerMenuItem(Icons.Filled.Logout,         "Logout",          "logout"),
)

val bottomNavItems = listOf(
    Triple("home", R.drawable.home_png, "Home"),
    Triple("history", R.drawable.history_png, "History"),
    Triple("result", R.drawable.result_png, "Result"),
    Triple("chart", R.drawable.chart_png, "Chart"),
    Triple("share", R.drawable.share_png, "Share")
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
        is ProfileViewModel.ProfileState.Success ->  s.data.data?.wallet_amount?.toFloat()?.toInt() ?: 0
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
                // Clear auth token from prefs
                val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                prefs.edit().remove("auth_token").apply()
                // Navigate to login, wipe entire back stack
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
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
                onItemClick = { route ->
                    scope.launch {
                        drawerState.close()
                        delay(150)
                        when (route) {
                            "logout" -> {
                                TokenManager.clearToken(context)
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                            }
                            "home" -> selectedRoute = "home"
                            else -> {
                                selectedRoute = route
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
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
        topBar = { TopBar(onMenuClick = onMenuClick, navController = navController, profileViewModel = profileViewModel)},
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                navController = navController
            )
        },
        containerColor = PageNavy,
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
                .padding(
                    padding
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(PageNavy),
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Add Funds
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(id = R.drawable.add_funds),
                            label = "Add Cash",
                            onClick = { navController.navigate("add_money") }
                        )

                        // Withdraw
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(id = R.drawable.withdrw_png),
                            label = "Withdraw",
                            onClick = {
                                val now = java.util.Calendar.getInstance()
                                val totalMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                                        now.get(java.util.Calendar.MINUTE)
                                if (totalMinutes < 540 || totalMinutes > 840) {
                                    Toast.makeText(
                                        context,
                                        "Withdrawal is only available between 9:00 AM to 2:00 PM",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    navController.navigate("withdrawal")
                                }
                            }
                        )

                        // WhatsApp
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(id = R.drawable.whtsp_png),
                            label = "WhatsApp",
                            onClick = {
                                contact?.whatsapp_number?.let { number ->
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number"))
                                    )
                                }
                            }
                        )
                        //telegram
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(id = R.drawable.tele_png),
                            label = "Telegram",
                            onClick = {
                                contact?.telegram_link?.let { link ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                }
                            }
                        )
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
                                .background(TopBarBlack)
                        )
                        Spacer(Modifier.width(10.dp))
                        val GoldBrush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF7D5700),
                                Color(0xFFC5A059),
                                Color(0xFFFCF6BA),
                                Color(0xFFD4AF37),
                                Color(0xFF7D5700)
                            )
                        )

                        Text(
                            text = "Live Games",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            style = TextStyle(
                                brush = GoldBrush
                            )
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
                                        tint = Color(0xFFFFE500).copy(alpha = 0.7f),
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
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
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
                        color      = Color(0xFFAAAAAA),
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Text(
                        text       = "OLD",
                        color      = Color(0xFF666666),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text     = "›",
                    color    = Color(0xFF444444),
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
                        color      = Color(0xFFFFD600),
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
                    .background(Color(0xFF2A2A2A))
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
                            color      = Color(0xFF888888),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text       = formattedOpen,
                            color      = Color(0xFFCCCCCC),
                            fontSize   = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Close — right
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text       = "Close",
                            color      = Color(0xFF888888),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text       = formattedClose,
                            color      = Color(0xFFCCCCCC),
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

    val amount = when (val s = state) {
        is ProfileViewModel.ProfileState.Success ->
            s.data.data?.wallet_amount?.let {
                String.format("%.2f", it)  // 2284.55 → "2284.55"
            } ?: "0.00"
        else -> "0.00"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarBlack)
            .statusBarsPadding()
            .height(64.dp)

            .padding(horizontal = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menu icon on left
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, "Menu", tint = YellowText, modifier = Modifier.size(26.dp))
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
                    .size(55.dp)
                    .clip(CircleShape)
            )
        }

        // Wallet on right (replaced bell)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(
                    width = 1.0.dp,
                    brush = GoldTheme.metallicBrush,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = Color(0xFF0A0A0A),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
//            Spacer(modifier = Modifier.width(8.dp))
            Column {

                Text(
                    text = "Wallet",
                    color = Color(0xFFC89738),
                    fontSize = 11.sp
                )

                Text(
                    "₹ $amount",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  QUICK ACTION BUTTONS
// ══════════════════════════════════════════════════════════
@Composable
private fun VDivider() {
    Box(Modifier.width(1.dp).height(44.dp).background(Color.White.copy(alpha = 0.12f)))
}
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: Any,
    label: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = GoldTheme.metallicBrushHorizontal)
            .padding(2.dp) // border thickness
    )
    {
        Card(
            modifier = modifier
                .height(100.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (icon) {
                        is ImageVector -> Icon(
                            icon, label,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(48.dp)
                        )

                        else -> Icon(
                            icon as androidx.compose.ui.graphics.painter.Painter, label,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
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
        सबसे ट्रस्टेड और सबसे ईमानदार Baba G Matka एप्लीकेशन
        960 रेट ✅
        https://sethgmatkagame.com/
    """.trimIndent()

    NavigationBar(
        containerColor = TopBarBlack,
        tonalElevation = 12.dp,
        modifier = Modifier
            .height(78.dp)
            .navigationBarsPadding()
            .clip(
                RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            )
            .border(
                width = 1.dp,
                color = BorderGold.copy(alpha = 0.6f),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            )
    ) {
        bottomNavItems.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,

                icon = {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Transparent)
                    )
                },

                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected)
                            FontWeight.Bold
                        else
                            FontWeight.SemiBold
                    )
                },

                onClick = {

                    if (!isSelected) {

                        when (route) {

                            "home" -> {
                                navController.navigate("home") {
                                    popUpTo("home") {
                                        inclusive = true
                                    }
                                }
                            }


                            "chart" -> {
                                navController.navigate("chart")
                            }

                            "result" -> {
                                navController.navigate("result")
                            }
                            "history" -> {
                                navController.navigate("history")
                            }

                            "share" -> {

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                                }

                                context.startActivity(
                                    Intent.createChooser(intent, "Share via")
                                )
                            }
                        }
                    }
                },

                colors = NavigationBarItemDefaults.colors(

                    selectedIconColor = GoldText,
                    selectedTextColor = GoldText,

                    unselectedIconColor = SoftGold.copy(alpha = 0.70f),
                    unselectedTextColor = SoftGold.copy(alpha = 0.70f),

                    indicatorColor = Color(0x22D4A73A)
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
        drawerContainerColor = Color(0xFF0A0A0A)
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF111111), Color(0xFF0A0A0A))))) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(brush = GoldTheme.metallicBrushHorizontal))
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 24.dp)) {
                Box(
                    modifier = Modifier.size(68.dp).border(2.dp, brush = GoldTheme.metallicBrush, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier         = Modifier.size(63.dp).clip(CircleShape).background(Color(0xFF0A0A0A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = YellowText, modifier = Modifier.size(40.dp))
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
    val banners = listOf(
        R.drawable.banr_1,
        R.drawable.banr_2
    )
//    val notices = listOf(
//
//        "🙏 सभी यूज़र्स से अनुरोध 🙏\n" +
//                "Withdrawal request करने के बाद कृपया 12 बजे से पहले कॉल या मैसेज न करें। आपका पैसा सुरक्षित रूप से आपके अकाउंट में ट्रांसफर कर दिया जाएगा।",
//
//        "🛡️ नोटिस (सुरक्षित ट्रांजैक्शन)\n" +
//                "प्रिय ग्राहक, QR कोड हर मिनट बदलता है। कृपया पुराने QR कोड से पेमेंट न करें। हमेशा नया QR कोड जनरेट करें।\n" +
//                "धन्यवाद 🙏"
//    )

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto Slider
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)

            val nextPage =
                (pagerState.currentPage + 1) % banners.size

            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp),

            shape = RoundedCornerShape(24.dp),

            colors = CardDefaults.cardColors(
                containerColor = CardNavy
            ),
            border = BorderStroke(
                1.8.dp,
                BorderGold.copy(alpha = 0.9f)
            )
        ) {
            Box {
                // Soft Glow Effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    GoldText.copy(alpha = 0.9f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) { page ->

                    Image(
                        painter = painterResource(id = banners[page]),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }
        }
    }
}