package com.example.surakshasetu.ui.fakecall

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeCallScreen(navController: NavController) {
    var callerName by remember { mutableStateOf("Mom") }
    var callerNumber by remember { mutableStateOf("+91 9876543210") }
    var delaySeconds by remember { mutableStateOf(5f) }
    var isTimerRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Fake Call") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = callerName,
                onValueChange = { callerName = it },
                label = { Text("Caller Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = callerNumber,
                onValueChange = { callerNumber = it },
                label = { Text("Caller Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Trigger in ${delaySeconds.toInt()} seconds",
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = delaySeconds,
                onValueChange = { delaySeconds = it },
                valueRange = 5f..60f,
                steps = 11
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isTimerRunning = true
                    scope.launch {
                        delay(delaySeconds.toLong() * 1000)
                        val encodedName = Uri.encode(callerName)
                        val encodedNumber = Uri.encode(callerNumber)
                        navController.navigate("incoming_call/$encodedName/$encodedNumber")
                        isTimerRunning = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isTimerRunning
            ) {
                if (isTimerRunning) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Incoming in ${delaySeconds.toInt()}s...")
                } else {
                    Text("Start Fake Call", fontSize = 18.sp)
                }
            }
        }
    }
}
