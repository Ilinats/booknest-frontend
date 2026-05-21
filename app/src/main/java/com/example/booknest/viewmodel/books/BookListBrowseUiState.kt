package com.example.booknest.viewmodel.books

/**
 * Browse / search UI state for the book list screen. Filter fields apply when the list is in full browse mode
 * (`category == null`); the search field can still appear when `category == "search"` using route arguments.
 */
data class BookListBrowseUiState(
    val searchQuery: String = "",
    val debouncedSearchQuery: String = "",
    val showFilters: Boolean = false,
    val showRecentSearches: Boolean = false,
    val selectedGenres: Set<Int> = emptySet(),
    val selectedAgeRating: String? = null,
    val selectedDistributionType: String? = null,
    val minRating: Float = 0f,
    val maxRating: Float = 5f,
    val selectedApplicationStatus: String? = null,
    val selectedDeadlineFilter: String? = null,
    val selectedSortBy: String? = null,
)

internal data class BrowseFilterSnapshot(
    val debouncedSearch: String,
    val genres: Set<Int>,
    val ageRating: String?,
    val distributionType: String?,
    val minRating: Float,
    val maxRating: Float,
    val applicationStatus: String?,
    val deadlineFilter: String?,
    val sortBy: String?,
) {
    companion object {
        fun from(ui: BookListBrowseUiState): BrowseFilterSnapshot =
            BrowseFilterSnapshot(
                debouncedSearch = ui.debouncedSearchQuery,
                genres = ui.selectedGenres,
                ageRating = ui.selectedAgeRating,
                distributionType = ui.selectedDistributionType,
                minRating = ui.minRating,
                maxRating = ui.maxRating,
                applicationStatus = ui.selectedApplicationStatus,
                deadlineFilter = ui.selectedDeadlineFilter,
                sortBy = ui.selectedSortBy,
            )
    }
}
