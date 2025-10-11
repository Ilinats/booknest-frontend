package com.example.booknest.data

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.booknest.network.ApiService
import com.example.booknest.network.GoogleAuthRequest
import com.example.booknest.network.GoogleAuthResponse
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.booknest.BuildConfig

class GoogleAuthManager(
    private val context: Context,
    private val apiService: ApiService
) {
    
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient
    
    private val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    
    init {
        initializeGoogleSignIn()
    }
    
    private fun initializeGoogleSignIn() {
        try {
            println("DEBUG: Initializing Google Sign-In...")
            println("DEBUG: Web Client ID: $webClientId")
            
            // Configure Google Sign-In to always show account picker
            val gso = GoogleSignInOptions.Builder()
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build()
            
            println("DEBUG: Google Sign-In Options built successfully")
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            oneTapClient = Identity.getSignInClient(context)
            
            println("DEBUG: Google Sign-In client initialized successfully")
        } catch (e: Exception) {
            println("ERROR: Failed to initialize Google Sign-In: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Creates an ActivityResultLauncher for Google Sign-In
     */
    fun createSignInLauncher(
        activity: ComponentActivity,
        onResult: (GoogleSignInResult) -> Unit
    ): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                onResult(GoogleSignInResult.Success(account))
            } catch (e: ApiException) {
                onResult(GoogleSignInResult.Error(e.message ?: "Google Sign-In failed"))
            }
        }
    }
    
    /**
     * Creates an ActivityResultLauncher for One Tap Sign-In
     */
    fun createOneTapLauncher(
        activity: ComponentActivity,
        onResult: (GoogleSignInResult) -> Unit
    ): ActivityResultLauncher<IntentSenderRequest> {
        return activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    // Create a mock GoogleSignInAccount for consistency
                    val account = GoogleSignInAccount.createDefault()
                    onResult(GoogleSignInResult.Success(account))
                } else {
                    onResult(GoogleSignInResult.Error("No ID token received"))
                }
            } catch (e: Exception) {
                onResult(GoogleSignInResult.Error(e.message ?: "One Tap Sign-In failed"))
            }
        }
    }
    
    /**
     * Initiates Google Sign-In flow
     */
    fun signIn(): Intent {
        println("DEBUG: GoogleAuthManager.signIn() called")
        println("DEBUG: Web Client ID: $webClientId")
        
        // Sign out first to force account picker to show
        googleSignInClient.signOut()
        
        val intent = googleSignInClient.signInIntent
        println("DEBUG: Generated sign-in intent: $intent")
        return intent
    }
    
    /**
     * Initiates One Tap Sign-In flow
     */
    suspend fun signInWithOneTap(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
    
    /**
     * Authenticates with backend using Google ID token
     */
    suspend fun authenticateWithBackend(
        idToken: String,
        userType: String
    ): Result<GoogleAuthResponse> {
        return try {
            val request = GoogleAuthRequest(idToken = idToken, userType = userType)
            val response = apiService.authenticateWithGoogle(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.success) {
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception(authResponse.message ?: "Authentication failed"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Authentication failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Signs out from Google
     */
    suspend fun signOut() {
        try {
            googleSignInClient.signOut()
            oneTapClient.signOut()
        } catch (e: Exception) {
            // Handle sign out error if needed
        }
    }
    
    /**
     * Gets the last signed-in account
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    /**
     * Checks if user is already signed in
     */
    fun isSignedIn(): Boolean {
        return getLastSignedInAccount() != null
    }
    
    /**
     * Revokes access and signs out
     */
    suspend fun revokeAccess() {
        try {
            googleSignInClient.revokeAccess()
        } catch (e: Exception) {
            // Handle revoke error if needed
        }
    }
}

/**
 * Result wrapper for Google Sign-In operations
 */
sealed class GoogleSignInResult {
    data class Success(val account: GoogleSignInAccount) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}
