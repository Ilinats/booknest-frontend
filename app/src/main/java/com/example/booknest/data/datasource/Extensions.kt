package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

fun extractErrorMessage(errorBody: String?): String {
    if (errorBody.isNullOrBlank()) return "An error occurred"

    return try {
        val json = Json { ignoreUnknownKeys = true }
        val error = json.decodeFromString<BNError.Generic>(errorBody)

        val message = error.messageString
        if (message != null) {
            if (message.startsWith("[") && message.endsWith("]")) {
                try {
                    val messages = json.decodeFromString<List<String>>(message)
                    messages.joinToString(", ")
                } catch (e: Exception) {
                    message
                }
            } else {
                message
            }
        } else {
            error.error ?: "An error occurred"
        }
    } catch (e: Exception) {
        try {
            val arrayRegex = Regex(""""message"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
            val arrayMatch = arrayRegex.find(errorBody)
            if (arrayMatch != null) {
                val messages = arrayMatch.groupValues[1]
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
                if (messages.isNotEmpty()) {
                    return messages.joinToString(", ")
                }
            }

            val messageRegex = Regex(""""message"\s*:\s*"([^"]+)"""")
            messageRegex.find(errorBody)?.groupValues?.get(1)
                ?: when {
                    errorBody.contains("already applied", ignoreCase = true) ||
                            errorBody.contains("APPLICATION_ALREADY_EXISTS", ignoreCase = true) -> {
                        "You have already applied for this book"
                    }

                    errorBody.contains("email verification", ignoreCase = true) -> {
                        "Please verify your email address before applying"
                    }

                    errorBody.contains("address", ignoreCase = true) &&
                            errorBody.contains("physical", ignoreCase = true) -> {
                        "Please add your address in your profile to apply for physical copies"
                    }

                    errorBody.contains("REVIEW_APPLICATION_NOT_RECEIVED", ignoreCase = true) -> {
                        "You must mark the book copy as received before submitting a review"
                    }

                    errorBody.contains("APPLICATION_NOT_RECEIVED", ignoreCase = true) -> {
                        "You must mark the book copy as received before submitting a review"
                    }

                    errorBody.contains("BOOK_FILE_NOT_AVAILABLE", ignoreCase = true) -> {
                        "No downloadable file is available for this book"
                    }

                    errorBody.contains("BOOK_PDF_WATERMARK_FAILED", ignoreCase = true) -> {
                        "Could not prepare the PDF for download. Please try again later."
                    }

                    errorBody.contains("BOOK_EPUB_FINGERPRINT_FAILED", ignoreCase = true) ||
                        errorBody.contains("BOOK_EPUB_INVALID", ignoreCase = true) -> {
                        "Could not prepare the EPUB for download. Please try again later."
                    }

                    errorBody.contains("BOOK_FINGERPRINT_NOT_FOUND", ignoreCase = true) -> {
                        "No verifiable fingerprint was found in this file"
                    }

                    errorBody.contains("BOOK_FINGERPRINT_WRONG_BOOK", ignoreCase = true) -> {
                        "This file does not match this book"
                    }

                    else -> "An error occurred"
                }
        } catch (ex: Exception) {
            "An error occurred"
        }
    }
}

internal fun mapNetworkOrUnknown(e: Exception): Throwable = when (e) {
    is BNError -> e
    is IOException -> BNError.Network(
        messageString = "Unable to reach the server. Check your connection and try again."
    )
    else -> BNError.Generic(
        messageString = e.message ?: "Request failed",
        error = null,
        statusCode = null
    )
}

suspend fun <T> runSuspendRequest(block: suspend () -> Response<T>): Result<T> {
    return try {
        requestBody(block())
    } catch (e: Exception) {
        Result.failure(mapNetworkOrUnknown(e))
    }
}

suspend fun <T> runSuspendRequestPaginated(
    block: suspend () -> Response<com.example.booknest.domain.model.response.PaginatedResponse<T>>
): Result<List<T>> {
    return try {
        requestPaginatedBody(block())
    } catch (e: Exception) {
        Result.failure(mapNetworkOrUnknown(e))
    }
}

suspend fun runSuspendRequestUnit(block: suspend () -> Response<Unit>): Result<Unit> {
    return try {
        requestBodyUnit(block())
    } catch (e: Exception) {
        Result.failure(mapNetworkOrUnknown(e))
    }
}

fun <T> requestBody(request: Response<T>): Result<T> {
    return try {
        if (request.isSuccessful) {
            request.body()?.let {
                Result.success(it)
            } ?: Result.failure(Throwable("Empty response body"))
        } else {
            val errorBody = request.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            val errorWithMessage = BNError.Generic(
                messageString = errorMessage,
                error = null,
                statusCode = request.code()
            )
            Result.failure(errorWithMessage)
        }
    } catch (ex: Exception) {
        Result.failure(Throwable(ex.message))
    }
}

fun <T> resultBody(result: Result<T>): Result<T> {
    return result.fold(
        onSuccess = {
            Result.success(it)
        },
        onFailure = {
            Result.failure(it)
        }
    )
}

fun requestBodyUnit(request: Response<Unit>): Result<Unit> {
    return try {
        if (request.isSuccessful) {
            Result.success(Unit)
        } else {
            val errorBody = request.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            val errorWithMessage = BNError.Generic(
                messageString = errorMessage,
                error = null,
                statusCode = request.code()
            )
            Result.failure(errorWithMessage)
        }
    } catch (ex: Exception) {
        Result.failure(Throwable(ex.message))
    }
}

fun <T> requestPaginatedBody(request: Response<com.example.booknest.domain.model.response.PaginatedResponse<T>>): Result<List<T>> {
    return try {
        if (request.isSuccessful) {
            request.body()?.let {
                Result.success(it.data)
            } ?: Result.failure(Throwable("Empty response body"))
        } else {
            val errorBody = request.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            val errorWithMessage = BNError.Generic(
                messageString = errorMessage,
                error = null,
                statusCode = request.code()
            )
            Result.failure(errorWithMessage)
        }
    } catch (ex: Exception) {
        Result.failure(Throwable(ex.message))
    }
}

fun <T, R> requestApiBody(
    request: Response<com.example.booknest.domain.model.response.ApiResponse<T>>,
    transform: (T) -> R
): Result<R> {
    return try {
        if (request.isSuccessful && request.body() != null) {
            val apiResponse = request.body()!!
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(transform(apiResponse.data))
            } else {
                Result.failure(
                    BNError.Generic(
                        messageString = apiResponse.message ?: "Request failed",
                        error = null,
                        statusCode = apiResponse.statusCode
                    )
                )
            }
        } else {
            val errorBody = request.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            val errorWithMessage = BNError.Generic(
                messageString = errorMessage,
                error = null,
                statusCode = request.code()
            )
            Result.failure(errorWithMessage)
        }
    } catch (ex: Exception) {
        Result.failure(Throwable(ex.message))
    }
}

fun <T> requestApiBody(
    request: Response<com.example.booknest.domain.model.response.ApiResponse<T>>
): Result<T> {
    return requestApiBody(request) { it }
}

fun requestApiBodyUnit(
    request: Response<com.example.booknest.domain.model.response.ApiResponse<Unit>>
): Result<Unit> {
    return try {
        if (request.isSuccessful && request.body() != null) {
            val apiResponse = request.body()!!
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    BNError.Generic(
                        messageString = apiResponse.message ?: "Request failed",
                        error = null,
                        statusCode = apiResponse.statusCode
                    )
                )
            }
        } else {
            val message = request.errorBody()?.string()
            Result.failure(
                BNError.Generic(
                    messageString = message ?: "Request failed",
                    error = null,
                    statusCode = request.code()
                )
            )
        }
    } catch (ex: Exception) {
        Result.failure(Throwable(ex.message))
    }
}

