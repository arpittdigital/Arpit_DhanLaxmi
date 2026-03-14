package com.bmdu.dhanlaxmi.Dashboard

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.bmdu.dhanlaxmi.R
import com.bmdu.dhanlaxmi.components.LogoutConfirmDialog
import com.bmdu.dhanlaxmi.viewModel.GameViewModel
import com.bmdu.dhanlaxmi.viewModel.ProfileViewModel
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════
//  DATA MODELS
// ══════════════════════════════════════════════════════════

data class DrawerMenuItem(
    val icon : ImageVector,
    val label: String,
    val route: String
)

// ══════════════════════════════════════════════════════════
//  DRAWER ITEMS
// ══════════════════════════════════════════════════════════

val drawerMenuItems = listOf(
    DrawerMenuItem(Icons.Filled.Home,           "Home",            "home"),
    DrawerMenuItem(Icons.Filled.Person,         "User Profile",    "profile_screen"),
    DrawerMenuItem(Icons.Filled.Star,           "Winning History", "winning_history"),
    DrawerMenuItem(Icons.Filled.List,           "Bid History",     "history"),
    DrawerMenuItem(Icons.Filled.AccountBalance, "Banking Details", "bank_details"),
    DrawerMenuItem(Icons.Filled.StarRate,        "Game Rate","game_rate"),
    DrawerMenuItem(Icons.Filled.ContactPage,    "Contact Us",      "contact_us"),
    DrawerMenuItem(Icons.Filled.PlayCircle, "How to Play",     "how_to_play"),
//    DrawerMenuItem(Icons.Filled.Share,          "Share",           "share"),
    DrawerMenuItem(Icons.Filled.Lock, "Privacy Policy", "privacy_policy"),
    DrawerMenuItem(Icons.Filled.Logout,         "Logout",          "logout"),
)

// Bottom nav items
val bottomNavItems = listOf(
    Triple("home",    Icons.Default.Home,      "Home"),
    Triple("history", Icons.Default.Replay,    "History"),
    Triple("result",  Icons.Default.AutoGraph, "Result"),
    Triple("chart",   Icons.Default.BarChart,  "Chart"),
    Triple("share",   Icons.Default.Share,     "Share"),
)

@Composable
fun HomeScreen(navController: NavController) {

    val drawerState      = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope            = rememberCoroutineScope()
    var selectedRoute    by remember { mutableStateOf("home") }
    var showLogoutDialog by remember { mutableStateOf(false) }


    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.profileState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    val userName = when (val s = state) {
        is ProfileViewModel.ProfileState.Success -> s.data.data?.name ?: "User"
        else -> "User"
    }

    // ── Fetch profile on first load ──────────────────────
    LaunchedEffect(Unit) {
        if (!token.isNullOrBlank()) {

            viewModel.fetchProfile(token)
        } else {
        }
    }


    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            onCancel = { showLogoutDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        scrimColor    = Color.Black.copy(alpha = 0.5f),
        drawerContent = {
            DrawerContent(
                userName      = userName ,
                userPhone     = "9674332246",
                userPoints    = 0,
                menuItems     = drawerMenuItems,
                selectedRoute = selectedRoute,
                onItemClick   = { route ->
                    scope.launch { drawerState.close() }
                    when (route) {
                        "logout" -> showLogoutDialog = true
                        "home"   -> selectedRoute = "home"


                        else     -> {
                            selectedRoute = route
                            navController.navigate(route)
                        }
                    }
                }
            )
        }
    ) {
        HomeScreenContent(
            navController = navController,
            onMenuClick   = { scope.launch { drawerState.open() } }
        )
    }
}

@Composable
fun HomeScreenContent(
    navController: NavController,
    onMenuClick  : () -> Unit
) {
    val context   = LocalContext.current
    val prefs     = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val rawToken = prefs.getString("auth_token", null) ?: ""
    val token = "Bearer $rawToken"
    val viewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val gameState by viewModel.gamestate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchGames(token)
    }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route ?: "home"

    Scaffold(
        topBar        = { TopBar(onMenuClick = onMenuClick,navController) },
        bottomBar     = {
            BottomNavigationBar(
                currentRoute  = currentRoute,
                navController = navController
            )
        },
        containerColor = Color(0xFF004D02)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Gradient header box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF00A904), Color(0xFF004D02))
                        )
                    )
            )

            // Quick Action Buttons
            QuickActionButtons(navController = navController)

            // Game Result Button
            Button(
                onClick  = {
                    navController.navigate("game_result")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3EE06)),
                shape  = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Game Result", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.W600)
            }

            Spacer(Modifier.height(8.dp))

            // ── Games List — API se ───────────────────────────
            when (val s = gameState) {
                is GameViewModel.GameState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF3EE06))
                    }
                }

                is GameViewModel.GameState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text      = s.message,
                                color     = Color.White,
                                fontSize  = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.fetchGames(token) },
                                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3EE06))
                            ) {
                                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is GameViewModel.GameState.Success -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxWidth().weight(1f),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.games) { game ->
                            ApiGameCard(
                                game = game,
                                onCardClick = {
                                    navController.navigate("delhi_bazar/${game.id}/${game.game_name}")
                                },
                                onPlayClick = {
                                    navController.navigate("delhi_bazar/${game.id}/${game.game_name}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ApiGameCard(
    game: GameData,
    onCardClick: () -> Unit = {},
    onPlayClick: () -> Unit = {}
) {

    val gameName   = game.game_name ?: "Game"
    val resultTime = if (game.result_time.isNullOrBlank()) {
        "Not Declared"
    } else {
        game.result_time
    }

    val closeTime  = game.close_time ?: ""
    val status     = game.status ?: ""
    val playDays   = game.play_days?.joinToString(", ") ?: ""

    val isPlayable = status.equals("play", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF005503)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Result Time Circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00A904)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = resultTime ?: "",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Result", color = Color.White, fontSize = 8.sp)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = gameName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Close: $closeTime",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )

                if (playDays.isNotBlank()) {
                    Text(
                        text = "Days: $playDays",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            if (isPlayable) {
                Button(
                    onClick = { onPlayClick() },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3EE06)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Play",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = status.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
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
fun TopBar(
    onMenuClick: () -> Unit,
    navController: NavController
) {
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
            .height(60.dp)
            .background(Color(0xFFD8C715))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = Color.Black,
            modifier = Modifier
                .size(28.dp)
                .clickable { onMenuClick() }
        )

        // 🔥 Center Section (Logo + App Name + Amount)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "₹ $amount",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

        }

        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = Color.Black,
            modifier = Modifier
                .size(28.dp)
                .clickable {
                    navController.navigate("notification")
                }
        )
    }
}

@Composable
fun DrawerContent(
    userName     : String,
    userPhone    : String,
    userPoints   : Int,
    menuItems    : List<DrawerMenuItem>,
    selectedRoute: String,
    onItemClick  : (String) -> Unit
) {
    ModalDrawerSheet(
        modifier             = Modifier.width(280.dp),
        drawerShape          = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        drawerContainerColor = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF00A904), Color(0xFF004D02))
                    )
                )
                .padding(vertical = 24.dp, horizontal = 20.dp)
        ) {
            Column {
                Box(
                    modifier         = Modifier.size(64.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00A904), modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text(userName,  color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(userPhone, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text("Points: $userPoints", color = Color(0xFFF3EE06), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            menuItems.forEach { item ->
                DrawerMenuItemRow(
                    item       = item,
                    isSelected = selectedRoute == item.route,
                    onClick    = { onItemClick(item.route) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════
//  DRAWER ROW
// ══════════════════════════════════════════════════════════

@Composable
fun DrawerMenuItemRow(item: DrawerMenuItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor   = if (isSelected) Color(0xFFE8F5E9) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF00A904) else Color(0xFF333333)
    val iconColor = if (isSelected) Color(0xFF00A904) else Color(0xFF666666)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(
                start  = if (isSelected) 16.dp else 20.dp,
                end    = 20.dp,
                top    = 14.dp,
                bottom = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(4.dp).height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF00A904))
            )
            Spacer(Modifier.width(12.dp))
        }
        Icon(
            imageVector        = item.icon,
            contentDescription = item.label,
            tint               = iconColor,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text       = item.label,
            color      = textColor,
            fontSize   = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
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
            .offset(y = (-70).dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon    = painterResource(id = R.drawable.telegramimg),
                label   = "Telegram",
                color   = Color(0xFF0088CC),
                onClick = { }
            )
            QuickActionButton(
                icon    = painterResource(id = R.drawable.whatsapplogo),
                label   = "Whatsapp",
                color   = Color(0xFF25D366),
                onClick = { }
            )
            QuickActionButton(
                icon    = Icons.Default.Add,
                label   = "Add Funds",
                color   = Color(0xFF00A904),
                onClick = { navController.navigate("add_money") }
            )
            QuickActionButton(
                icon    = Icons.Default.Remove,
                label   = "Withdraw",
                color   = Color.Red,
                onClick = { navController.navigate("withdrawal") }
            )
        }
    }
}

@Composable
fun QuickActionButton(icon: Any, label: String, color: Color, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier         = Modifier.size(50.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
                else           -> Icon(painter = icon as androidx.compose.ui.graphics.painter.Painter, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.W500)
    }
}

// ══════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION BAR
// ══════════════════════════════════════════════════════════

@Composable
fun BottomNavigationBar(
    currentRoute : String,
    navController: NavController
) {
    NavigationBar(
        containerColor = Color(0xFFF3EE06),
        contentColor   = Color.Black,
        modifier       = Modifier.height(70.dp)
    ) {
        bottomNavItems.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route

            NavigationBarItem(
                icon     = {
                    Icon(
                        imageVector        = icon,
                        contentDescription = label,
                        modifier           = Modifier.size(24.dp)
                    )
                },
                label    = {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.W500)
                },
                selected = isSelected,
                onClick  = {
                    if (!isSelected) {
                        when (route) {
                            "home"    -> navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            "history" -> navController.navigate("history")
                            "chart"   -> navController.navigate("chart")
                            "result"  -> { }
                            "share"   -> { }
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color.Black,
                    selectedTextColor   = Color.Black,
                    unselectedIconColor = Color.Black.copy(alpha = 0.6f),
                    unselectedTextColor = Color.Black.copy(alpha = 0.6f),
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}