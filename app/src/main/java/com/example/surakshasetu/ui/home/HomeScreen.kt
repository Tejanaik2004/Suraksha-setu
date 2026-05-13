package com.example.surakshasetu.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.surakshasetu.navigation.Screen
import com.example.surakshasetu.service.SosService
import com.example.surakshasetu.ui.auth.AuthViewModel
import com.example.surakshasetu.ui.theme.DangerRed
import com.example.surakshasetu.ui.theme.PrimaryBlue
import com.example.surakshasetu.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userProfile by authViewModel.userProfile.collectAsState()
    val user = authViewModel.currentUser

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        
        if (locationGranted && micGranted) {
            user?.let {
                val intent = Intent(context, SosService::class.java).apply {
                    putExtra("uid", it.uid)
                    putExtra("userName", userProfile?.name ?: it.displayName ?: "User")
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(20.dp)
    ) {
        GreetingSection(
            name = userProfile?.name ?: user?.displayName ?: "User",
            profilePictureUrl = userProfile?.profilePictureUrl,
            onLogout = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            onProfileClick = {
                navController.navigate(Screen.Profile.route)
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        AiSafetyInsightCard()
        Spacer(modifier = Modifier.height(32.dp))
        SosButtonSection(onTriggerSos = {
            val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            
            if (hasLocation && hasMic) {
                user?.let {
                    val intent = Intent(context, SosService::class.java).apply {
                        putExtra("uid", it.uid)
                        putExtra("userName", userProfile?.name ?: it.displayName ?: "User")
                    }
                    ContextCompat.startForegroundService(context, intent)
                }
            } else {
                permissionsLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
                )
            }
        })
        Spacer(modifier = Modifier.height(32.dp))
        QuickActionsSection(navController)
        Spacer(modifier = Modifier.height(24.dp))
        TrustedCirclesSection(navController, userProfile?.trustedContacts?.map { it.name } ?: emptyList())
    }
}

@Composable
fun GreetingSection(
    name: String, 
    profilePictureUrl: String?, 
    onLogout: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Evening, $name 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                    color = SuccessGreen
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SAFE ZONE",
                    fontSize = 12.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.Gray)
            }
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() },
                color = Color.LightGray
            ) {
                if (!profilePictureUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}

@Composable
fun AiSafetyInsightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "AI Safety Insight",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Maintain awareness in this area. Risk: LOW",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SosButtonSection(onTriggerSos: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(DangerRed, DangerRed.copy(alpha = 0.8f))
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onTriggerSos()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SOS",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Long press",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Column {
        Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem(
                title = "Safe Walk",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                color = Color(0xFF6366F1),
                onClick = { navController.navigate(Screen.Map.route) },
                modifier = Modifier.weight(1f)
            )
            QuickActionItem(
                title = "Fake Call",
                icon = Icons.Default.Call,
                color = Color(0xFFF59E0B),
                onClick = { navController.navigate(Screen.FakeCall.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        }
    }
}

@Composable
fun TrustedCirclesSection(navController: NavController, contacts: List<String>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Circle of Trust", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Manage",
                color = PrimaryBlue,
                fontSize = 14.sp,
                modifier = Modifier.clickable { navController.navigate(Screen.CircleOfTrust.route) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(contacts.size) { index ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color.LightGray
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(16.dp))
                    }
                    Text(text = contacts[index], fontSize = 12.sp)
                }
            }
            if (contacts.size < 5) {
                item {
                    IconButton(
                        onClick = { navController.navigate(Screen.CircleOfTrust.route) },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
        }
    }
}
