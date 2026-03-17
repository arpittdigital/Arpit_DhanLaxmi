package com.bmdu.dhanlaxmi.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.R
import com.bmdu.dhanlaxmi.viewModel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController) {

    var mobileno        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ── Validation error states ──────────────────────────
    var mobileError   by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val viewModel: AuthViewModel = viewModel()
    // ✅ Renamed to avoid conflict
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
                viewModel.resetState()
            }
            is AuthViewModel.AuthState.Error -> {
                val errorMsg = (authState as AuthViewModel.AuthState.Error).message
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    fun validate(): Boolean {
        var isValid = true

        when {
            mobileno.isBlank() -> {
                mobileError = "Mobile number is required"
                isValid = false
            }
            mobileno.length != 10 -> {
                mobileError = "Enter a valid 10-digit mobile number"
                isValid = false
            }
            !mobileno.all { it.isDigit() } -> {
                mobileError = "Mobile number must contain only digits"
                isValid = false
            }
            else -> mobileError = ""
        }

        // Password validation
        when {
            password.isBlank() -> {
                passwordError = "Password is required"
                isValid = false
            }
            password.length < 6 -> {
                passwordError = "Password must be at least 6 characters"
                isValid = false
            }
            else -> passwordError = ""
        }

        return isValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00A904),
                        Color(0xFF004D02),
                        Color(0xFF004302)
                    )
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(
                topStart    = 20.dp,
                topEnd      = 20.dp,
                bottomStart = 0.dp,
                bottomEnd   = 0.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.appblack)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(shape = CircleShape)
                        .background(color = Color(0xFF000000))
                ) {
                    Image(
                        modifier           = Modifier.fillMaxSize(),
                        painter            = painterResource(R.drawable.logo),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text       = "Log in",
                    fontSize   = 24.sp,
                    color      = colorResource(id = R.color.textsecondary),
                    fontWeight = FontWeight.W500
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── Mobile Number Field ──────────────────
                OutlinedTextField(
                    value         = mobileno,
                    onValueChange = {
                        // Sirf digits allow karo, max 10
                        if (it.all { c -> c.isDigit() } && it.length <= 10) {
                            mobileno    = it
                            mobileError = ""
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder     = { Text("Mobile number", color = Color.White) },
                    leadingIcon     = {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                    },
                    singleLine      = true,
                    isError         = mobileError.isNotEmpty(),
                    supportingText  = {
                        if (mobileError.isNotEmpty()) {
                            Text(mobileError, color = Color.Red, fontSize = 12.sp)
                        }
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = if (mobileError.isNotEmpty()) Color.Red
                        else colorResource(id = R.color.textsecondary),
                        unfocusedBorderColor    = if (mobileError.isNotEmpty()) Color.Red
                        else colorResource(id = R.color.textsecondary),
                        errorBorderColor        = Color.Red,
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor             = Color.White,
                        focusedTextColor        = Color.White,
                        unfocusedTextColor      = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Password Field ───────────────────────
                OutlinedTextField(
                    value         = password,
                    onValueChange = {
                        password      = it
                        passwordError = ""
                    },
                    placeholder = { Text("Password", color = Color.White) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector        = if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint               = Color.White
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine      = true,
                    isError         = passwordError.isNotEmpty(),
                    supportingText  = {
                        if (passwordError.isNotEmpty()) {
                            Text(passwordError, color = Color.Red, fontSize = 12.sp)
                        }
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = if (passwordError.isNotEmpty()) Color.Red
                        else colorResource(id = R.color.textsecondary),
                        unfocusedBorderColor    = if (passwordError.isNotEmpty()) Color.Red
                        else colorResource(id = R.color.textsecondary),
                        errorBorderColor        = Color.Red,
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor             = Color.White,
                        focusedTextColor        = Color.White,
                        unfocusedTextColor      = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                // Forgot Password
                Text(
                    text       = "Forgot Password?",
                    color      = Color.White,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.W400,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, end = 20.dp)
                        .clickable { navController.navigate("forgotpassword") },
                    textAlign  = TextAlign.End
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(20.dp)
                    )
                }

                // ── Login Button ─────────────────────────
                Button(
                    onClick = {
                        if (validate()) {
                            viewModel.login(mobileno, password)
                        }
                    },
                    enabled         = authState !is AuthViewModel.AuthState.Loading,
                    shape           = RoundedCornerShape(12.dp),
                    contentPadding  = PaddingValues(0.dp),
                    colors          = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier        = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
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
                        Text(text = "Log in", color = Color.Black, fontWeight = FontWeight.W600)
                    }
                }

                Button(
                    onClick        = { navController.navigate("register") },
                    shape          = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp)
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
                        Text(text = "Create an Account", color = Color.Black, fontWeight = FontWeight.W600)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}