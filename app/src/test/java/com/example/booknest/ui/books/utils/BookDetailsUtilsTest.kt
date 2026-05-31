package com.example.booknest.ui.books.utils

import com.example.booknest.testutil.TestFixtures
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookDetailsUtilsTest {

    @Test
    fun isFullyBooked_trueWhenNoAvailableCopies() {
        val book = TestFixtures.bookDetails().copy(totalCopies = 10, availableCopies = 0)
        assertTrue(book.isFullyBooked())
    }

    @Test
    fun isFullyBooked_falseWhenCopiesRemain() {
        val book = TestFixtures.bookDetails().copy(totalCopies = 10, availableCopies = 3)
        assertFalse(book.isFullyBooked())
    }

    @Test
    fun isFullyBooked_falseWhenCopyCountsMissing() {
        assertFalse(TestFixtures.bookDetails().isFullyBooked())
    }
}
