package com.bmdu.dhanlaxmi.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.R
import com.bmdu.dhanlaxmi.viewModel.AuthViewModel

@Composable
fun SignupScreen(navController: NavController) {

    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf("") }
    var mobileError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }  // ✅ API error message

    var isTermsAccepted by remember { mutableStateOf(false) }  // ✅ Terms checkbox state

    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // ✅ Handle auth state changes
    LaunchedEffect(state) {
        when (state) {
            is AuthViewModel.AuthState.Success -> {
                Toast.makeText(context, "🎉 Account created successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
                viewModel.resetState()
            }
            is AuthViewModel.AuthState.Error -> {
                val msg = (state as AuthViewModel.AuthState.Error).message
                val toastMsg = when {
                    msg.contains("422")             -> "📱 Mobile number already registered!"
                    msg.contains("name")            -> "👤 Please enter a valid name"
                    msg.contains("mobile")          -> "📞 Please enter a valid mobile number"
                    msg.contains("password")        -> "🔒 Password must be at least 6 characters"
                    msg.contains("500")             -> "🔧 Server error. Please try again later"
                    msg.contains("timeout")         -> "⏱ Request timed out. Check your internet"
                    msg.contains("Unable to resolve")-> "🌐 No internet connection"
                    else                            -> "❌ Signup failed. Please try again"
                }
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                errorMessage = toastMsg
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // ✅ Validation function
    fun validate(): Boolean {
        var isValid = true

        usernameError = when {
            username.isBlank() -> { isValid = false; "Username is required" }
            else -> ""
        }

        mobileError = when {
            mobile.isBlank() -> { isValid = false; "Mobile number is required" }
            mobile.length != 10 -> { isValid = false; "Enter a valid 10-digit mobile number" }
            !mobile.all { it.isDigit() } -> { isValid = false; "Mobile number must contain only digits" }
            else -> ""
        }

        passwordError = when {
            password.isBlank() -> { isValid = false; "Password is required" }
            password.length < 6 -> { isValid = false; "Password must be at least 6 characters" }
            else -> ""
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
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.appblack)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                // ✅ Back button — now actually navigates back
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(id = R.color.textsecondary),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 40.dp, start = 25.dp)
                            .size(28.dp)
                            .clickable { navController.popBackStack() }  // ✅ Fixed
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(shape = CircleShape)
                        .background(color = Color(0xFF000000))
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
                    text = "Sign up",
                    fontSize = 24.sp,
                    color = colorResource(id = R.color.textsecondary),
                    fontWeight = FontWeight.W500
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ✅ Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = ""
                        errorMessage = ""
                    },
                    placeholder = { Text("User name", color = Color.White) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    isError = usernameError.isNotEmpty(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.textsecondary),
                        unfocusedBorderColor = colorResource(id = R.color.textsecondary),
                        errorBorderColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                )
                // ✅ Username error text
                if (usernameError.isNotEmpty()) {
                    Text(
                        text = usernameError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp)
                    )
                }

                // ✅ Mobile field
                OutlinedTextField(
                    value = mobile,
                    onValueChange = {
                        mobile = it
                        mobileError = ""
                        errorMessage = ""
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Mobile", color = Color.White) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    isError = mobileError.isNotEmpty(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.textsecondary),
                        unfocusedBorderColor = colorResource(id = R.color.textsecondary),
                        errorBorderColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 15.dp)
                )
                // ✅ Mobile error text
                if (mobileError.isNotEmpty()) {
                    Text(
                        text = mobileError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp)
                    )
                }

                // ✅ Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = ""
                        errorMessage = ""
                    },
                    placeholder = { Text("Password", color = Color.White) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),  // ✅ Hides password
                    isError = passwordError.isNotEmpty(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.textsecondary),
                        unfocusedBorderColor = colorResource(id = R.color.textsecondary),
                        errorBorderColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 15.dp)
                )
                // ✅ Password error text
                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp)
                    )
                }

                // ✅ Terms & Conditions — now functional
                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isTermsAccepted,
                        onCheckedChange = {
                            isTermsAccepted = it
                            errorMessage = ""
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(id = R.color.textsecondary),
                            uncheckedColor = Color.White
                        )
                    )
                    Text(
                        text = "Agree with ",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                    Text(
                        text = "Terms and Conditions",
                        color = colorResource(id = R.color.textsecondary),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ✅ API error message shown to user
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                // ✅ Loading indicator
                if (state is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = colorResource(id = R.color.textsecondary),
                        modifier = Modifier
                            .size(28.dp)
                            .padding(top = 6.dp)
                    )
                }

                // ✅ Register button — validates terms + form before calling API
                Button(
                    onClick = {
                        errorMessage = ""
                        if (!isTermsAccepted) {
                            errorMessage = "Please accept Terms and Conditions"
                        } else if (validate()) {
                            viewModel.signup(username, mobile, password)
                        }
                    },
                    enabled = state !is AuthViewModel.AuthState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF3EE06),
                                        Color(0xFFB28E2D)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register Now",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "Contact Us",
                    color = colorResource(id = R.color.textsecondary),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Divider(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                    color = colorResource(id = R.color.textsecondary),
                    thickness = 1.dp,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 86.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.Blue,
                        modifier = Modifier.size(24.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.whatsapplogo),
                        contentDescription = null,
                        tint = colorResource(id = R.color.background),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}