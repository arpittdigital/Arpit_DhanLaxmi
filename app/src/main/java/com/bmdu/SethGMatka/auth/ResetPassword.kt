package com.bmdu.SethGMatka.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bmdu.SethGMatka.R
import com.bmdu.SethGMatka.viewModel.AuthViewModel

// ─────────────────────────────────────────────
//  RESET PASSWORD SCREEN
// ─────────────────────────────────────────────
@Composable
fun ResetPassword(navController: NavController,
                  viewModel: AuthViewModel) {

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (state) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate("resetsuccess") {
                    popUpTo("resetpassword") { inclusive = true }
                }
                viewModel.resetState()
            }
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(
                    context,
                    (state as AuthViewModel.AuthState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00A904), Color(0xFF004D02), Color(0xFF004302))
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.appblack))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                // ── Back Arrow ──
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 16.dp, start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.textsecondary),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Logo ──
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "Reset Password",
                    fontSize = 24.sp,
                    color = colorResource(id = R.color.textsecondary),
                    fontWeight = FontWeight.W600
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Create new password",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.W400
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your new password must be different from\nyour previous used password",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.W300,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── Password Field ──
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.White
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.textsecondary),
                        unfocusedBorderColor = colorResource(id = R.color.textsecondary),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Confirm Password Field ──
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirm Password", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = Color.White
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.textsecondary),
                        unfocusedBorderColor = colorResource(id = R.color.textsecondary),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── Reset Password Button ──
                Button(
                    onClick = {
                        when {
                            password.isEmpty() || confirmPassword.isEmpty() -> {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            }
                            password.length < 6 -> {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            }
                            password != confirmPassword -> {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // Mobile ViewModel se internally le lega (savedMobile)
                                viewModel.resetPassword(password, confirmPassword)
                            }
                        }
                    },
                    enabled = state !is AuthViewModel.AuthState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFF3EE06), Color(0xFFB28E2D))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state is AuthViewModel.AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Reset Password",
                                color = Color.Black,
                                fontWeight = FontWeight.W600,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  RESET SUCCESS SCREEN
// ─────────────────────────────────────────────
@Composable
fun ResetSuccessScreen(navController: NavController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00A904), Color(0xFF004D02), Color(0xFF004302))
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF062A0D))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Back Arrow ──
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF3EE06),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // ── Center Content ──
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // ── Logo Circle ──
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Successfully Reset\nYour password",
                        color = Color(0xFFF3EE06),
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Password Updated!",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your password has been updated.\nYou can now log in to your account.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // ── Go to Login Circle Button ──
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate("login") {
                                    popUpTo("forgotpassword") { inclusive = true }
                                }
                            }
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFF3EE06), Color(0xFFB28E2D))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Go to Login",
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tap to Login",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}