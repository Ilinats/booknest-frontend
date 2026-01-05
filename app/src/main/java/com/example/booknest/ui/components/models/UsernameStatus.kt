package com.example.booknest.ui.components.models

sealed class UsernameStatus {
    object Idle : UsernameStatus()
    object TooShort : UsernameStatus()
    object TooLong : UsernameStatus()
    object InvalidFormat : UsernameStatus()
    object ValidFormat : UsernameStatus()
}

