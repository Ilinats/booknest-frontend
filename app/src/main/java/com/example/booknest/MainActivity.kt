package com.example.booknest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.booknest.data.AuthManager
import com.example.booknest.navigation.NavGraph
import com.example.booknest.network.RetrofitInstance
import com.example.booknest.viewmodel.LoginViewModel
import com.example.booknest.viewmodel.LoginViewModelFactory
import com.example.booknest.viewmodel.SignupViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AuthManager
        authManager = AuthManager.getInstance(this)
        
        // Initialize RetrofitInstance with AuthManager for token refresh
        RetrofitInstance.initialize(authManager)
        
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                
                // Create ViewModels with factories
                val signupViewModel: SignupViewModel by viewModels()
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(authManager)
                )
                
                NavGraph(
                    navController = navController,
                    signupViewModel = signupViewModel,
                    loginViewModel = loginViewModel,
                    authManager = authManager
                )
            }
        }
    }
}
