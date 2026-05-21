package com.example.booknest.ui.applications.utils

import com.example.booknest.domain.model.response.ApplicationReaderResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.BookLeakFingerprintResponse

fun formatApplicationReaderDisplay(reader: ApplicationReaderResponse): String {
    val fullName = listOf(reader.firstName, reader.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .takeIf { it.isNotBlank() }

    return when {
        fullName != null && reader.username.isNotBlank() -> "$fullName (@${reader.username})"
        fullName != null -> fullName
        reader.username.isNotBlank() -> "@${reader.username}"
        reader.email.isNotBlank() -> reader.email
        else -> "Reader"
    }
}

fun BookLeakFingerprintResponse.readerDisplayLabel(
    applications: List<ApplicationResponse> = emptyList(),
): String {
    applications.firstOrNull { it.reader?.id == readerId }?.reader?.let {
        return formatApplicationReaderDisplay(it)
    }

    val fullName = listOfNotNull(
        readerFirstName?.takeIf { it.isNotBlank() },
        readerLastName?.takeIf { it.isNotBlank() },
    ).joinToString(" ").takeIf { it.isNotBlank() }

    return when {
        fullName != null && !readerUsername.isNullOrBlank() -> "$fullName (@$readerUsername)"
        fullName != null -> fullName
        !readerUsername.isNullOrBlank() -> "@$readerUsername"
        !readerEmail.isNullOrBlank() -> readerEmail!!
        else -> "Matched reader on this book"
    }
}
