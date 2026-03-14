package com.bmdu.dhanlaxmi.Dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.viewModel.ProfileViewModel

private const val PROFILE_TAG = "ProfileScreen"

// ── Colors ─────────────────────────────────────────────────
private val PGreenDark   = Color(0xFF004D02)
private val PGreenLight  = Color(0xFF00A904)
private val PGreenField  = Color(0xFF0D3D0D)
private val PYellowMain  = Color(0xFFD4A800)
private val PYellowLight = Color(0xFFF0C000)

// ══════════════════════════════════════════════════════════
//  PROFILE SCREEN
// ══════════════════════════════════════════════════════════

@Composable
fun ProfileScreen(navController: NavController) {

    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.profileState.collectAsState()

    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
    val token   = prefs.getString("auth_token", null)

    // ── Fetch profile on first load ──────────────────────
    LaunchedEffect(Unit) {
        if (!token.isNullOrBlank()) {
            Log.d(PROFILE_TAG, "LaunchedEffect: fetching profile")
            viewModel.fetchProfile(token)
        } else {
            Log.e(PROFILE_TAG, "LaunchedEffect: token is null — cannot fetch profile")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(PGreenLight, PGreenDark))
            )
    ) {

        // ── Top Bar ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(colors = listOf(PYellowMain, PYellowLight))
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint               = Color.Black,
                modifier           = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text       = "Profile",
                color      = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp
            )
        }

        // ── Body ─────────────────────────────────────────
        when (val s = state) {

            is ProfileViewModel.ProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PYellowMain)
                }
            }

            is ProfileViewModel.ProfileState.Error -> {
                Log.e(PROFILE_TAG, "Error state: ${s.message}")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text      = s.message,
                            color     = Color.White,
                            fontSize  = 14.sp,
                            modifier  = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { token?.let { viewModel.fetchProfile(it) } },
                            colors  = ButtonDefaults.buttonColors(containerColor = PYellowMain)
                        ) {
                            Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is ProfileViewModel.ProfileState.Success -> {
                val profile = s.data.data
                Log.d(PROFILE_TAG, "Rendering profile: $profile")

                Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(Modifier.height(28.dp))

                    // ── Profile Avatar ────────────────────
                    Box(
                        modifier         = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(PGreenField),
                        contentAlignment = Alignment.Center
                    ) {
//                        if (!profile?.profile_image.isNullOrBlank()) {
//                            AsyncImage(
//                                model             = profile!!.profile_image,
//                                contentDescription = "Profile Photo",
//                                contentScale      = ContentScale.Crop,
//                                modifier          = Modifier.fillMaxSize()
//                            )
//                        }
                      //  else {
                            Icon(
                                imageVector        = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint               = Color.White,
                                modifier           = Modifier.size(50.dp)
                            )

                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Name ─────────────────────────────
                    ProfileFieldYellow(
                        label = "Name",
                        value = profile?.name ?: "—"
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Password (masked) ─────────────────
                    ProfileFieldYellow(
                        label = "Password",
                        value = profile?.password ?: "••••••••"
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Customer ID ───────────────────────
                    ProfileFieldYellow(
                        label = "Customer ID",
                        value = profile?.customer_id ?: profile?.customer_id?.toString() ?: "—"
                    )



                    Spacer(Modifier.height(32.dp))
                }
            }

            null -> {
                // Initial state — show nothing / shimmer placeholder
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PYellowMain)
                }
            }
        }
    }
}

// ── Yellow gradient field matching screenshot ─────────────
@Composable
fun ProfileFieldYellow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = label,
            color      = Color.White,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFD4A800), Color(0xFFF0C000))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text       = value,
                color      = Color.Black,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}