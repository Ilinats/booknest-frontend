package com.example.booknest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.booknest.navigation.NavGraph
import com.example.booknest.viewmodel.SignupViewModel
import com.example.booknest.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val signupViewModel: SignupViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    signupViewModel = signupViewModel,
                    loginViewModel = loginViewModel
                )
            }
        }
    }
}
