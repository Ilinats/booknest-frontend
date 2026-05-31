package com.example.booknest.viewmodel.applications

import com.example.booknest.domain.model.response.ApplicationResponse

fun ApplicationResponse.isCompletedApplication(): Boolean =
    status == "approved" &&
        (readingStatus == "reviewed" || !reviewSubmittedAt.isNullOrBlank())

fun ApplicationResponse.isActiveApprovedApplication(): Boolean =
    status == "approved" && !isCompletedApplication()

fun ApplicationResponse.statusForDisplay(): String =
    if (isCompletedApplication()) "completed" else (status ?: "unknown")

fun ApplicationResponse.isPending(): Boolean =
    status.equals("pending", ignoreCase = true)

fun List<ApplicationResponse>.pendingCount(): Int = count { it.isPending() }
