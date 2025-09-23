package com.example.booknest.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.SignupViewModel

@Composable
fun ProfileDetailsScreen(navController: NavController, viewModel: SignupViewModel) {
    var username by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } // "yy-mm-dd"
    var streetAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Profile Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Birth Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            OutlinedTextField(
                value = streetAddress,
                onValueChange = { streetAddress = it },
                label = { Text("Street Address") },
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            OutlinedTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                label = { Text("Postal Code") },
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Country (optional)") },
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Checkbox(
                    checked = isPrimary,
                    onCheckedChange = { isPrimary = it }
                )
                Text("Primary Address")
            }
            Button(
                onClick = {
                    viewModel.updateProfileDetails(
                        username,
                        birthDate,
                        streetAddress,
                        city,
                        postalCode,
                        country.ifBlank { null },
                        isPrimary
                    )
                    navController.navigate(Screen.Bio.route)
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Next")
            }
        }
    }
}
