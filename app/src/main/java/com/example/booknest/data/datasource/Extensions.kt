package com.example.booknest.data.datasource

import com.example.booknest.data.error.ApiErrorMessages
import com.example.booknest.data.error.BNError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun extractErrorMessage(errorBody: String?): String {
    if (errorBody.isNullOrBlank()) return ApiErrorMessages.DEFAULT

    return try {
        val json = Json { ignoreUnknownKeys = true }
        val error = json.decodeFromString<BNError.Generic>(errorBody)
        val parsedMessage = parseMessageField(json, error.messageString)
        ApiErrorMessages.resolve(
            message = parsedMessage,
            errorCode = error.error,
            rawBody = errorBody,
        )
    } catch (e: Exception) {
        parseErrorMessageFallback(errorBody)
    }
}

private fun parseMessageField(json: Json, message: String?): String? {
    if (message == null) return null
    if (!message.startsWith("[") || !message.endsWith("]")) return message
    return try {
        json.decodeFromString<List<String>>(message)
            .map { part -> ApiErrorMessages.resolve(message = part, errorCode = null) }
            .joinToString(", ")
    } catch (e: Exception) {
        message
    }
}

private fun parseErrorMessageFallback(errorBody: String): String {
    return try {
        val arrayRegex = Regex(""""message"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
        val arrayMatch = arrayRegex.find(errorBody)
        if (arrayMatch != null) {
            val messages = arrayMatch.groupValues[1]
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
                .map { part -> ApiErrorMessages.resolve(message = part, errorCode = null, rawBody = errorBody) }
            if (messages.isNotEmpty()) {
                return messages.joinToString(", ")
            }
        }

        val errorCodeRegex = Regex(""""error"\s*:\s*"([^"]+)"""")
        val errorCode = errorCodeRegex.find(errorBody)?.groupValues?.get(1)

        val messageRegex = Regex(""""message"\s*:\s*"([^"]+)"""")
        val message = messageRegex.find(errorBody)?.groupValues?.get(1)

        ApiErrorMessages.resolve(message = message, errorCode = errorCode, rawBody = errorBody)
    } catch (ex: Exception) {
        ApiErrorMessages.findInText(errorBody) ?: ApiErrorMessages.DEFAULT
    }
}

internal fun mapNetworkOrUnknown(e: Exception): Throwable {
    if (e is BNError) return e

    val root = e.rootCause()
  return when (root) {
        is SerializationException -> BNError.Generic(
            messageString = "Could not read the server response. Try again.",
            error = null,
            statusCode = null,
        )
        is HttpException -> BNError.Generic(
            messageString = extractErrorMessage(root.response()?.errorBody()?.string())
                .takeIf { it != ApiErrorMessages.DEFAULT }
                ?: "Request failed (${root.code()})",
            error = null,
            statusCode = root.code(),
        )
        is SocketTimeoutException -> BNError.Network(
            messageString = "The server took too long to respond. Try again.",
        )
        is UnknownHostException -> BNError.Network(
            messageString = "Unable to reach the server. Check your connection and try again.",
        )
        is IOException -> BNError.Network(
            messageString = "Unable to reach the server. Check your connection and try again.",
        )
        else -> BNError.Generic(
            messageString = root.message ?: e.message ?: "Request failed",
            error = null,
            statusCode = null,
        )
    }
}

private fun Throwable.rootCause(): Throwable {
    var current: Throwable = this
    while (current.cause != null) {
        current = current.cause!!
    }
    return current
}

suspend fun <T> runSuspendRequest(block: suspend () -> Response<T>): Result<T> {
    return try {
        requestBody(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(mapNetworkOrUnknown(e))
    }
}

suspend fun <T> runSuspendRequestPaginated(
    block: suspend () -> Response<com.example.booknest.domain.model.response.PaginatedResponse<T>>
): Result<List<T>> {
    return try {
        requestPaginatedBody(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(mapNetworkOrUnknown(e))
    }
}

suspend fun runSuspendRequestUnit(block: suspend () -> Response<Unit>): Result<Unit> {
    return try {
        requestBodyUnit(block())
    } catch (e: CancellationException) {
        throw e
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

