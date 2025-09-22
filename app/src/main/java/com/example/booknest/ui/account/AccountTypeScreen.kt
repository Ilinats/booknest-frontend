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
fun AccountTypeScreen(navController: NavController, viewModel: SignupViewModel) {
    var selected by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Choose Account Type",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { selected = "reader" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == "reader") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Reader")
                }
                Button(
                    onClick = { selected = "author" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == "author") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Author")
                }
            }
            if (selected != null) {
                Button(
                    onClick = {
                        viewModel.updateAccountType(selected!!)
                        navController.navigate(Screen.PersonalInfo.route)
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Next")
                }
            }
        }
    }
}
