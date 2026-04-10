package com.bmdu.SethGMatka.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {

    val sections = listOf(
        PrivacySection(
            title = "1. Information We Collect",
            content = "We collect information you provide directly to us, such as your name, phone number, email address, and payment details when you register or use our services."
        ),
        PrivacySection(
            title = "2. How We Use Your Information",
            content = "We use the information we collect to operate, maintain, and improve our services, process transactions, send you technical notices, and respond to your comments and questions."
        ),
        PrivacySection(
            title = "3. Information Sharing",
            content = "We do not share, sell, or rent your personal information to third parties except as described in this policy or with your consent. We may share data with trusted partners to help perform statistical analysis or provide customer support."
        ),
        PrivacySection(
            title = "4. Data Security",
            content = "We take reasonable measures to help protect your personal information from loss, theft, misuse, unauthorized access, disclosure, alteration, and destruction."
        ),
        PrivacySection(
            title = "5. Cookies & Tracking",
            content = "We may use cookies and similar tracking technologies to track activity on our app and hold certain information to improve and analyze our service."
        ),
        PrivacySection(
            title = "6. Third-Party Services",
            content = "Our app may contain links to third-party websites or services. We are not responsible for the privacy practices of those third parties and encourage you to read their privacy policies."
        ),
        PrivacySection(
            title = "7. Children's Privacy",
            content = "Our services are not directed to individuals under the age of 18. We do not knowingly collect personal information from children."
        ),
        PrivacySection(
            title = "8. Changes to This Policy",
            content = "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new policy on this page with an updated effective date."
        ),
        PrivacySection(
            title = "9. Contact Us",
            content = "If you have any questions about this Privacy Policy, please contact us."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A0DAD)

                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF6A0DAD)),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Privacy Policy",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Effective Date: 01 January 2025",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Policy Sections
            items(sections) { section ->
                PrivacySectionCard(section)
            }

            // Footer
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "© 2025 Seth G Matka. All rights reserved.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class PrivacySection(
    val title: String,
    val content: String
)

@Composable
fun PrivacySectionCard(section: PrivacySection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF6A0DAD), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = section.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF6A0DAD)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = section.content,
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}