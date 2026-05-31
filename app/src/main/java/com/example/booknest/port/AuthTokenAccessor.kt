package com.example.booknest.port

/**
 * Read-only access to bearer credentials for the network stack.
 * Keeps OkHttp types from depending on the full session implementation.
 */
interface AuthTokenAccessor {
    fun getToken(): String
    fun getRefreshToken(): String
}
