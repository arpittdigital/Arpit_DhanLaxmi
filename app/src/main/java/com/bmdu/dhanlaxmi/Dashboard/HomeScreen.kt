package com.bmdu.dhanlaxmi.Dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bmdu.dhanlaxmi.Model.GameData
import com.bmdu.dhanlaxmi.Model.TokenManager
import com.bmdu.dhanlaxmi.R
import com.bmdu.dhanlaxmi.components.LogoutConfirmDialog
import com.bmdu.dhanlaxmi.ui.theme.GoldTheme
import com.bmdu.dhanlaxmi.viewModel.GameViewModel
import com.bmdu.dhanlaxmi.viewModel.ProfileViewModel
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════
//  COLOR PALETTE
// ══════════════════════════════════════════════════════════

private val DarkGreen     = Color(0xFF002601)
private val MidGreen      = Color(0xFF004D02)
private val BrightGreen   = Color(0xFF00A904)
private val CardGreen     = Color(0xFF005C02)
private val CardGreenDark = Color(0xFF003D01)
private val GoldText      = Color(0xFFE7D156)   // bright gold — visible on dark green
private val GoldMid       = Color(0xFFDBB74C)
private val BlackText     = Color(0xFF1A1100)   // near-black — visible on bright gold bg

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
    DrawerMenuItem(Icons.Filled.PlayCircle,     "How to Play",     "how_to_play"),
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
fun HomeScreen(navController: NavController) {
    val drawerState      = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope            = rememberCoroutineScope()
    var selectedRoute    by remember { mutableStateOf("home") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.profileState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    // ✅ Read signup data as fallback when no token yet
    val signupName  = prefs.getString("signup_name", null)
    val signupPhone = prefs.getString("signup_phone", null)

    val userName = when (val s = state) {
        is ProfileViewModel.ProfileState.Success -> s.data.data?.name ?: "User"
        else -> "User"
    }
    val userPhone = when (val s = state) {
        is ProfileViewModel.ProfileState.Success -> s.data.data?.phone ?: ""  // ✅ dynamic phone
        else -> ""
    }
    val userPoints = when (val s = state) {
        is ProfileViewModel.ProfileState.Success -> s.data.data?.wallet_amount?.toInt() ?: 0
        else -> 0
    }

    LaunchedEffect(Unit) {
        if (!token.isNullOrBlank()) viewModel.fetchProfile(token)
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
        HomeScreenContent(navController = navController, onMenuClick = { scope.launch { drawerState.open() } })
    }
    // ✅ Re-fetch profile every time token changes
    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) viewModel.fetchProfile(token)
    }
}

// ══════════════════════════════════════════════════════════
//  MAIN CONTENT
// ══════════════════════════════════════════════════════════

@Composable
fun HomeScreenContent(navController: NavController, onMenuClick: () -> Unit) {
    val context  = LocalContext.current
    val prefs    = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val rawToken = prefs.getString("auth_token", null) ?: ""
    val token    = "Bearer $rawToken"

    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gamestate.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchGames(token) }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route ?: "home"

    Scaffold(
        topBar        = { TopBar(onMenuClick = onMenuClick, navController = navController) },
        bottomBar     = { BottomNavigationBar(currentRoute = currentRoute, navController = navController) },
        containerColor = DarkGreen
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Slim hero strip — only 70dp, card will overlap it
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(Brush.verticalGradient(listOf(MidGreen, DarkGreen)))
            )

            // Quick action card overlaps the hero
            QuickActionButtons(navController = navController)

            // Tighten the gap left by the negative offset
            Spacer(Modifier.height((-24).dp))

            // Game Result button — no extra top gap
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .background(brush = GoldTheme.metallicBrush, shape = RoundedCornerShape(12.dp))
                    .clickable { navController.navigate("game_result") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint     = BlackText,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Game Result",
                        color         = BlackText,      // ✅ BLACK on gold — fully visible
                        fontSize      = 16.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Live Games label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp).height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(brush = GoldTheme.metallicBrush)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Live Games",
                    color         = GoldText,           // ✅ bright gold on dark green — visible
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Games list
            when (val s = gameState) {
                is GameViewModel.GameState.Loading -> {
                    Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(brush = GoldTheme.metallicBrush, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(44.dp),
                                color       = BlackText,
                                trackColor  = Color(0x30000000),
                                strokeWidth = 4.dp
                            )
                        }
                    }
                }

                is GameViewModel.GameState.Error -> {
                    Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, null, tint = GoldText.copy(alpha = 0.7f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(s.message, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .background(brush = GoldTheme.metallicBrush, shape = RoundedCornerShape(8.dp))
                                    .clickable { viewModel.fetchGames(token) }
                                    .padding(horizontal = 24.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Retry", color = BlackText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is GameViewModel.GameState.Success -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxWidth().weight(1f),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(s.games) { game ->
                            ApiGameCard(
                                game        = game,
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

// ══════════════════════════════════════════════════════════
//  GAME CARD
// ══════════════════════════════════════════════════════════

@Composable
fun ApiGameCard(game: GameData, onCardClick: () -> Unit = {}, onPlayClick: () -> Unit = {}) {
    val gameName   = game.game_name ?: "Game"
    val resultTime = if (game.result_time.isNullOrBlank()) "Pending" else game.result_time
    val closeTime  = game.close_time ?: "--"
    val status     = game.status ?: ""
    val playDays   = game.play_days?.joinToString(" · ") ?: ""
    val isPlayable = status.equals("play", ignoreCase = true)

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onCardClick() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardGreen),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        // Gold top accent line
        Box(Modifier.fillMaxWidth().height(2.dp).background(brush = GoldTheme.metallicBrushHorizontal))

        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Result circle — gold ring border
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .border(2.dp, brush = GoldTheme.metallicBrush, shape = CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier         = Modifier.fillMaxSize().clip(CircleShape).background(CardGreenDark),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = resultTime,
                            color      = GoldText,       // ✅ bright gold on dark — visible
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )
                        Text("Result", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
                    }
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text       = gameName,
                    color      = Color.White,            // ✅ white on green — visible
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = GoldText.copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Close: $closeTime",
                        color    = Color.White.copy(alpha = 0.80f),  // ✅ white — readable
                        fontSize = 12.sp
                    )
                }
                if (playDays.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        playDays,
                        color    = GoldText.copy(alpha = 0.80f),     // ✅ gold — readable
                        fontSize = 10.sp
                    )
                }
            }

            // Play / Closed
            if (isPlayable) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .shadow(3.dp, RoundedCornerShape(10.dp))
                        .background(brush = GoldTheme.metallicBrush, shape = RoundedCornerShape(10.dp))
                        .clickable { onPlayClick() }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Play",
                        color      = BlackText,          // ✅ dark on gold — clearly visible
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33FFFFFF))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(
                        status.uppercase(),
                        color         = Color.White.copy(alpha = 0.65f),
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//  TOP BAR
// ══════════════════════════════════════════════════════════

@Composable
fun TopBar(onMenuClick: () -> Unit, navController: NavController) {
    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.profileState.collectAsState()

    val amount = when (state) {
        is ProfileViewModel.ProfileState.Success ->
            (state as ProfileViewModel.ProfileState.Success).data.data?.wallet_amount?.toString() ?: "0"
        else -> "0"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(brush = GoldTheme.metallicBrush)
            .padding(horizontal = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, "Menu", tint = BlackText, modifier = Modifier.size(26.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Logo with subtle border ring
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .border(2.dp, BlackText.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier           = Modifier.size(42.dp).clip(CircleShape)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Wallet",
                    color      = BlackText.copy(alpha = 0.60f),  // ✅ dark muted on gold
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "₹ $amount",
                    color      = BlackText,                       // ✅ solid dark on gold
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(onClick = { navController.navigate("notification") }) {
            Icon(Icons.Default.Notifications, "Notifications", tint = BlackText, modifier = Modifier.size(26.dp))
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
            .offset(y = (-38).dp),   // overlap hero — reduced gap
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
                bgColor  = Color(0xFF0D4F73),
                iconTint = Color.Unspecified,
                onClick  = {}
            )
            VDivider()
            QuickActionButton(
                icon     = painterResource(id = R.drawable.whatsapplogo),
                label    = "WhatsApp",
                bgColor  = Color(0xFF0E5C2A),
                iconTint = Color.Unspecified,
                onClick  = {}
            )
            VDivider()
            QuickActionButton(
                icon     = Icons.Default.Add,
                label    = "Add Funds",
                bgColor  = Color(0xFF1A5C1A),
                iconTint = Color(0xFF6EE98A),
                onClick  = { navController.navigate("add_money") }
            )
            VDivider()
            QuickActionButton(
                icon     = Icons.Default.Remove,
                label    = "Withdraw",
                bgColor  = Color(0xFF5C1A1A),
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
fun QuickActionButton(icon: Any, label: String, bgColor: Color, iconTint: Color, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable { onClick() }.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier         = Modifier.size(50.dp).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(icon, label, tint = iconTint, modifier = Modifier.size(26.dp))
                else           -> Icon(icon as androidx.compose.ui.graphics.painter.Painter, label, tint = iconTint, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize   = 10.sp,
            color      = Color.White.copy(alpha = 0.90f),  // ✅ white on dark green — readable
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
    NavigationBar(
        containerColor = Color.Transparent,
        modifier       = Modifier.height(68.dp).background(brush = GoldTheme.metallicBrushHorizontal)
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
                        else      -> {}
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = BlackText,               // ✅ dark on gold
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