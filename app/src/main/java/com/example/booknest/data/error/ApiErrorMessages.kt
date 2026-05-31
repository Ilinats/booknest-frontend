package com.example.booknest.data.error

object ApiErrorMessages {
    const val DEFAULT = "Something went wrong. Please try again."

    private val byCode: Map<String, String> = buildMap {
        // ApplicationErrors
        put("APPLICATION_NOT_FOUND", "Application not found.")
        put("APPLICATION_ALREADY_EXISTS", "You have already applied for this book.")
        put("APPLICATION_NOT_PENDING", "This application is no longer pending.")
        put("APPLICATION_NOT_APPROVED", "This application has not been approved yet.")
        put("APPLICATION_CANNOT_UPDATE", "This application cannot be updated.")
        put("APPLICATION_CANNOT_WITHDRAW", "This application cannot be withdrawn.")
        put("APPLICATION_ACCESS_DENIED", "You don't have permission to access this application.")
        put("APPLICATION_NOT_OWNED_BY_READER", "This application doesn't belong to you.")
        put("APPLICATION_NOT_FOR_AUTHOR_BOOK", "This application doesn't match your book.")
        put("APPLICATION_ADDRESS_REQUIRED", "Please add your address in your profile to apply for physical copies.")
        put("APPLICATION_EMAIL_VERIFICATION_REQUIRED", "Please verify your email address before applying.")
        put("APPLICATION_BOOK_NOT_ACTIVE", "This book is not accepting applications right now.")
        put("APPLICATION_NO_AVAILABLE_COPIES", "This book has no review copies available.")
        put("APPLICATION_DEADLINE_PASSED", "The application deadline for this book has passed.")
        put("APPLICATION_AGE_RESTRICTION_VIOLATION", "You don't meet the age requirement for this book.")
        put("APPLICATION_READER_ID_REQUIRED", "Reader information is required.")
        put("APPLICATION_CANNOT_MANAGE_LOTTERY", "Lottery cannot be run for this book right now.")

        // AuthErrors
        put("USER_ALREADY_EXISTS", "An account with this email or username already exists.")
        put("INVALID_CREDENTIALS", "Email/username or password is incorrect.")
        put("INVALID_REFRESH_TOKEN", "Your session expired. Please sign in again.")
        put("REFRESH_TOKEN_REUSE", "Your session expired. Please sign in again.")
        put("USER_NOT_FOUND", "Account not found.")
        put("EMAIL_NOT_VERIFIED", "Please verify your email address to continue.")
        put("INVALID_VERIFICATION_CODE", "That verification code is invalid.")
        put("VERIFICATION_CODE_EXPIRED", "That verification code has expired. Request a new one.")
        put("PASSWORD_RESET_TOKEN_INVALID", "This password reset link is invalid.")
        put("PASSWORD_RESET_TOKEN_EXPIRED", "This password reset link has expired.")
        put("GOOGLE_AUTH_FAILED", "Google sign-in failed. Please try again.")
        put("EMAIL_ALREADY_VERIFIED", "Your email is already verified.")
        put("ROLE_ACCESS_REQUIRED", "You don't have permission to perform this action.")
        put("AUTHOR_ACCESS_REQUIRED", "This action is only available to authors.")
        put("READER_ACCESS_REQUIRED", "This action is only available to readers.")

        // AuthGuardErrorCode
        put("MISSING_TOKEN", "Your session expired. Please sign in again.")
        put("INVALID_TOKEN", "Your session expired. Please sign in again.")

        // AuthErrorCode — omit generic UNAUTHORIZED; Nest often sends it with a specific code in message
        put("INVALID_OWNERSHIP_RESOURCE_TYPE", "Invalid resource.")

        // AuthorFollowErrorCode
        put("AUTHOR_FOLLOW_CANNOT_FOLLOW_SELF", "You can't follow yourself.")
        put("AUTHOR_FOLLOW_AUTHOR_NOT_FOUND", "Author not found.")
        put("AUTHOR_FOLLOW_ALREADY_FOLLOWING", "You're already following this author.")
        put("AUTHOR_FOLLOW_NOT_FOLLOWING", "You're not following this author.")

        // BookErrors
        put("BOOK_NOT_FOUND", "Book not found.")
        put("BOOK_ALREADY_EXISTS", "A book with these details already exists.")
        put("BOOK_NOT_OWNED_BY_AUTHOR", "You can only manage your own books.")
        put("BOOK_NOT_ACTIVE", "This book is not active.")
        put("BOOK_NO_COPIES_AVAILABLE", "No review copies are available for this book.")
        put("BOOK_INVALID_COPIES", "Please enter a valid number of copies.")
        put("BOOK_INVALID_DEADLINE", "Please enter valid application and review deadlines.")
        put("BOOK_CANNOT_MODIFY_OTHERS", "You can only edit your own books.")
        put("BOOK_CANNOT_DELETE_OTHERS", "You can only delete your own books.")
        put("BOOK_CANNOT_PUBLISH", "This book can't be published yet. Check that all required details are complete.")
        put("BOOK_ALREADY_PUBLISHED", "This book is already published.")
        put("BOOK_FILE_NOT_AVAILABLE", "No downloadable file is available for this book.")
        put("BOOK_PDF_WATERMARK_FAILED", "Could not prepare the PDF for download. Please try again later.")
        put("BOOK_FINGERPRINT_NOT_FOUND", "No verifiable fingerprint was found in this file.")
        put("BOOK_FINGERPRINT_WRONG_BOOK", "This file doesn't match this book.")
        put("BOOK_EPUB_INVALID", "Could not prepare the EPUB for download. Please try again later.")
        put("BOOK_EPUB_FINGERPRINT_FAILED", "Could not prepare the EPUB for download. Please try again later.")
        put("BOOK_INVALID_GENRE_IDS", "One or more selected genres are invalid.")

        // FileErrorCode
        put("FILE_REQUIRED", "Please choose a file to upload.")
        put("FILE_BUFFER_MISSING", "The file could not be read. Please try again.")
        put("FILE_ORIGINAL_NAME_MISSING", "The file could not be read. Please try again.")
        put("FILE_NOT_FOUND", "File not found.")
        put("FILE_ACCESS_DENIED", "You don't have permission to access this file.")
        put("FILE_DELETE_FAILED", "Could not delete the file. Please try again.")

        // FriendErrorCode
        put("FRIEND_CANNOT_FRIEND_SELF", "You can't send a friend request to yourself.")
        put("FRIEND_USER_NOT_FOUND", "User not found.")
        put("FRIEND_ALREADY_FRIENDS", "You're already friends with this user.")
        put("FRIEND_REQUEST_ALREADY_PENDING", "A friend request is already pending.")
        put("FRIEND_REQUEST_BLOCKED", "Friend requests aren't allowed with this user.")
        put("FRIEND_REQUEST_NOT_FOUND", "Friend request not found.")
        put("FRIENDSHIP_NOT_FOUND", "Friendship not found.")
        put("FRIEND_AUTHORS_CANNOT_FRIEND", "Authors can't send friend requests.")

        // ReviewErrorCode
        put("REVIEW_APPLICATION_NOT_FOUND", "Application not found.")
        put("REVIEW_APPLICATION_NOT_APPROVED", "You can only review approved applications.")
        put("REVIEW_APPLICATION_NOT_RECEIVED", "Mark the book copy as received before submitting a review.")
        put("REVIEW_ALREADY_EXISTS", "You've already submitted a review for this book.")
        put("REVIEW_NOT_FOUND", "Review not found.")
        put("REVIEW_ACCESS_DENIED", "You don't have permission to access this review.")
        put("REVIEW_AUTHOR_ACCESS_REQUIRED", "This action is only available to authors.")
        put("REVIEW_NOT_AUTHOR_OF_BOOK", "You can only manage reviews for your own books.")

        // SeriesErrorCode
        put("SERIES_NOT_FOUND", "Series not found.")
        put("SERIES_NOT_OWNED_BY_AUTHOR", "You can only manage your own series.")
        put("SERIES_CANNOT_EDIT_OTHERS", "You can only edit your own series.")
        put("SERIES_CANNOT_DELETE_OTHERS", "You can only delete your own series.")

        // UserAddressErrorCode
        put("ADDRESS_NOT_FOUND", "Address not found.")
        put("ADDRESS_ACCESS_DENIED", "You don't have permission to access this address.")

        // UserProfileErrorCode
        put("USER_PROFILE_USER_NOT_FOUND", "User not found.")
        put("USER_PROFILE_PRIVATE", "This profile is private.")

        // UserErrors
        put("USER_ALREADY_EXISTS", "An account with this email or username already exists.")
        put("USER_EMAIL_NOT_VERIFIED", "Please verify your email address to continue.")
        put("USER_NOT_ACTIVE", "This account is not active.")
        put("USER_NOT_AUTHOR", "This action is only available to authors.")
        put("USER_NOT_READER", "This action is only available to readers.")
        put("USER_ACCESS_DENIED", "You don't have permission to perform this action.")
        put("USER_INVALID_CREDENTIALS", "Email/username or password is incorrect.")
        put("USER_ALREADY_VERIFIED", "Your account is already verified.")
    }

    /** Longest codes first so partial substring matches don't pick the wrong code. */
    private val codesByLength = byCode.keys.sortedByDescending { it.length }

    fun forCode(code: String?): String? {
        if (code.isNullOrBlank()) return null
        val normalized = code.trim().uppercase()
        return byCode[normalized]
    }

    fun looksLikeMachineCode(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return false
        return trimmed == trimmed.uppercase() && trimmed.contains('_') ||
            forCode(trimmed) != null
    }

    fun findInText(text: String): String? {
        val upper = text.uppercase()
        for (code in codesByLength) {
            if (upper.contains(code)) {
                return byCode[code]
            }
        }
        return null
    }

    fun resolve(message: String?, errorCode: String?, rawBody: String? = null): String {
        val trimmedMessage = message?.trim()
        if (!trimmedMessage.isNullOrBlank() && !looksLikeMachineCode(trimmedMessage)) {
            return trimmedMessage
        }

        // Prefer the specific code in message (e.g. INVALID_CREDENTIALS) over the HTTP wrapper (e.g. UNAUTHORIZED).
        trimmedMessage?.let { forCode(it) }?.let { return it }
        forCode(errorCode)?.let { return it }

        rawBody?.let { findInText(it) }?.let { return it }

        return trimmedMessage?.takeIf { it.isNotBlank() } ?: DEFAULT
    }
}
