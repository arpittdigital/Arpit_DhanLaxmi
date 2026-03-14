package com.bmdu.dhanlaxmi.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bmdu.dhanlaxmi.R
import com.bmdu.dhanlaxmi.viewModel.AuthViewModel

@Composable
fun SignupScreen(navController: NavController){

    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobilerror   by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }


    val scrollState = rememberScrollState()
    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.authState.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            is AuthViewModel.AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo("signup") { inclusive = true }
                }
                viewModel.resetState()
            }
            is AuthViewModel.AuthState.Error -> {
                viewModel.resetState()
            }
            else -> {}
        }
    }

    fun validate(): Boolean {
        var isValid = true

        when {
            mobile.isBlank() -> {
                mobilerror = "Mobile number is required"
                isValid = false
            }
            mobile.length != 10 -> {
                mobilerror = "Enter a valid 10-digit mobile number"
                isValid = false
            }
            !mobile.all { it.isDigit() } -> {
                mobilerror = "Mobile number must contain only digits"
                isValid = false
            }
            else -> mobilerror = ""
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


        when {
            username.isBlank() -> {
                usernameError = "Username is required"
                isValid = false
            }

            else -> usernameError = ""
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
    ){

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
                    modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {

                    Box(modifier = Modifier.fillMaxWidth()) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = colorResource(id = R.color.textsecondary),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    top = 40.dp,
                                    start = 25.dp
                                )
                                .size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(shape = CircleShape)
                            .background(color = Color(0xFF000000))
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    Text(
                        text = "Sign up", fontSize = 24.sp,
                        color = colorResource(id = R.color.textsecondary),
                        fontWeight = FontWeight.W500
                    )

                    OutlinedTextField(

                        value = username,
                        onValueChange = { username = it },

                        placeholder = {
                            Text("User name", color = Color.White)
                        },

                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
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

                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                    )

                    OutlinedTextField(

                        value = mobile,
                        onValueChange = { mobile = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),

                        placeholder = {
                            Text("Mobile", color = Color.White)
                        },

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

                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 15.dp)
                    )

                    OutlinedTextField(

                        value = password,
                        onValueChange = { password = it },

                        placeholder = {
                            Text("Password", color = Color.White)
                        },

                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
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

                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 15.dp)
                    )
                    Row(
                        modifier = Modifier.padding(
                            start = 10.dp,
                            top = 10.dp
                        ).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = {},

                            )


                        Text(
                            text = "Agree with",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W400,
                            modifier = Modifier
                                .padding(top = 15.dp, end = 5.dp),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "Terms and Conditions",
                            color = colorResource(id = R.color.textsecondary),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 15.dp, end = 20.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    if (state is AuthViewModel.AuthState.Loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (validate()){viewModel.signup(username, mobile, password) }},
                        enabled = state !is AuthViewModel.AuthState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                    shape = RoundedCornerShape(12.dp) // same shape
                                )
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Register Now",
                                color = Color.Black
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = "Contact Us", color = colorResource(id = R.color.textsecondary),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
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
