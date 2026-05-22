package com.example.booknest.viewmodel.author

import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.testutil.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthorBookFilterLogicTest {

    @Test
    fun tabCounts_excludeArchivedBooks() {
        val books = listOf(
            TestFixtures.bookDetails(id = "1", status = "draft"),
            TestFixtures.bookDetails(id = "2", status = "active"),
            TestFixtures.bookDetails(id = "3", status = "archived"),
        )
        val nonArchived = books.filter { it.status != BookStatus.ARCHIVED.value }

        val counts = mapOf(
            0 to nonArchived.size,
            1 to nonArchived.count { it.status == BookStatus.DRAFT.value },
            2 to nonArchived.count { it.status == BookStatus.ACTIVE.value },
        )

        assertEquals(2, counts[0])
        assertEquals(1, counts[1])
        assertEquals(1, counts[2])
    }

    @Test
    fun bookStats_effectiveTotalApplications_prefersTotalApplications() {
        val stats = TestFixtures.bookStats(totalApplications = 12, pendingApplications = 3)
        assertEquals(12, stats.effectiveTotalApplications)
    }

    @Test
    fun bookStats_effectiveTotalApplications_fallsBackToApplicants() {
        val stats = BookStatsResponse(
            totalApplications = null,
            totalApplicants = 7,
            approvedReaders = 2,
            pendingApplications = 1,
        )
        assertEquals(7, stats.effectiveTotalApplications)
    }
}
