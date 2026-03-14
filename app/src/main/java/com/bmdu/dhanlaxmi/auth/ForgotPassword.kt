package com.bmdu.dhanlaxmi.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.R
import com.bmdu.dhanlaxmi.viewModel.AuthViewModel

// ─────────────────────────────────────────────
//  FORGOT PASSWORD SCREEN
// ─────────────────────────────────────────────
@Composable
fun ForgotPassword(navController: NavController,
                   viewModel: AuthViewModel) {

    var mobile by remember { mutableStateOf("") }
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (state) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate("verifypassword") {
                    popUpTo("forgotpassword") { inclusive = false }
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

    val gradientBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF00A904), Color(0xFF004D02), Color(0xFF004302))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBg)
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
                    text = "Forgot Password",
                    fontSize = 24.sp,
                    color = colorResource(id = R.color.textsecondary),
                    fontWeight = FontWeight.W600
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Find your account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your registered phone number\nto receive OTP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W300,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )

                Spacer(modifier = Modifier.height(35.dp))

                // ── Mobile Input ──
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("+91 | Mobile number", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
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

                Spacer(modifier = Modifier.height(40.dp))

                // ── Get OTP Button ──
                Button(
                    onClick = {
                        if (mobile.length == 10) {
                            viewModel.forgotPassword(mobile)
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter a valid 10-digit mobile number",
                                Toast.LENGTH_SHORT
                            ).show()
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
                                text = "Get OTP",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.W600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerifyOtpScreen(navController: NavController,
                    viewModel: AuthViewModel) {

    var otp1 by remember { mutableStateOf("") }
    var otp2 by remember { mutableStateOf("") }
    var otp3 by remember { mutableStateOf("") }
    var otp4 by remember { mutableStateOf("") }

    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }

    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        focusRequester1.requestFocus()
    }

    LaunchedEffect(state) {
        when (state) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate("resetpassword") {
                    popUpTo("verifypassword") { inclusive = false }
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
                    text = "Verify your Phone number",
                    fontSize = 22.sp,
                    color = colorResource(id = R.color.textsecondary),
                    fontWeight = FontWeight.W600,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Verify that's you",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We sent a verification code to your phone\nnumber. Please enter the code",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W300,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )

                Spacer(modifier = Modifier.height(50.dp))

                // ── OTP Fields ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OtpTextField(
                        value = otp1,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp1 = newValue
                                if (newValue.isNotEmpty()) focusRequester2.requestFocus()
                            }
                        },
                        focusRequester = focusRequester1
                    )
                    OtpTextField(
                        value = otp2,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp2 = newValue
                                if (newValue.isNotEmpty()) focusRequester3.requestFocus()
                                else if (newValue.isEmpty()) focusRequester1.requestFocus()
                            }
                        },
                        focusRequester = focusRequester2
                    )
                    OtpTextField(
                        value = otp3,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp3 = newValue
                                if (newValue.isNotEmpty()) focusRequester4.requestFocus()
                                else if (newValue.isEmpty()) focusRequester2.requestFocus()
                            }
                        },
                        focusRequester = focusRequester3
                    )
                    OtpTextField(
                        value = otp4,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otp4 = newValue
                                if (newValue.isEmpty()) focusRequester3.requestFocus()
                            }
                        },
                        focusRequester = focusRequester4
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))

                // ── Verify Button ──
                if (state is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Button(
                    onClick = {
                        val fullOtp = otp1 + otp2 + otp3 + otp4
                        if (fullOtp.length == 4) {
                            viewModel.verifyOtp(fullOtp)
                        } else {
                            Toast.makeText(context, "Please enter complete OTP", Toast.LENGTH_SHORT).show()
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
                                text = "Verify",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W600,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OtpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    BasicTextField(
        value = value,
        onValueChange = { if (it.all { ch -> ch.isDigit() }) onValueChange(it) },
        modifier = Modifier
            .width(60.dp)
            .height(60.dp)
            .focusRequester(focusRequester)
            .border(
                width = 1.dp,
                color = Color(0xFFF3EE06),
                shape = RoundedCornerShape(12.dp)
            )
            .background(color = Color.Transparent, shape = RoundedCornerShape(12.dp)),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                innerTextField()
            }
        }
    )
}