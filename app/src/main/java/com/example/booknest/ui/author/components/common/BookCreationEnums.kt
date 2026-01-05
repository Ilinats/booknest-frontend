package com.example.booknest.ui.author.components.common

enum class AgeRating(val value: String, val displayName: String) {
    ALL("all", "All Ages"),
    THIRTEEN_PLUS("13+", "13+"),
    SIXTEEN_PLUS("16+", "16+"),
    EIGHTEEN_PLUS("18+", "18+")
}

enum class DistributionType(val value: String, val displayName: String) {
    DIGITAL("digital", "Digital"),
    PHYSICAL("physical", "Physical"),
    BOTH("both", "Both")
}

enum class SelectionMethod(val value: String, val displayName: String) {
    AUTHOR_SELECTS("author_selects", "Author Selects"),
    FIRST_COME("first_come", "First Come First Served"),
    RANDOM("lottery", "Random Selection")
}
