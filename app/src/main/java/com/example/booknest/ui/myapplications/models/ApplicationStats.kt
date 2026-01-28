package com.example.booknest.ui.myapplications.models

data class ApplicationStats(
    val total: Int,
    val approvalRate: Double,
    val reviewsThisMonth: Int,
    val pendingReviews: Int
)

