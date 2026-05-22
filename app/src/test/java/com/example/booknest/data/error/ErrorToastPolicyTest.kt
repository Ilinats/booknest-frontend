package com.example.booknest.data.error

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorToastPolicyTest {

    @Test
    fun shouldShowErrorToast_returnsFalseFor500GenericError() {
        val error = BNError.Generic(
            messageString = "Server blew up",
            error = null,
            statusCode = 500,
        )

        assertFalse(shouldShowErrorToast(throwable = error))
    }

    @Test
    fun shouldShowErrorToast_returnsFalseForInternalServerErrorMessage() {
        assertFalse(shouldShowErrorToast(message = "Internal Server Error"))
    }

    @Test
    fun shouldShowErrorToast_returnsTrueForRegularClientError() {
        val error = BNError.Generic(
            messageString = "Validation failed",
            error = null,
            statusCode = 400,
        )

        assertTrue(shouldShowErrorToast(throwable = error))
    }

    @Test
    fun shouldShowErrorToast_returnsTrueForNetworkError() {
        assertTrue(shouldShowErrorToast(throwable = BNError.Network(messageString = "timeout")))
    }
}
