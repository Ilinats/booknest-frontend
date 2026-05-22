package com.example.booknest.data.session

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SearchHistorySerializerTest {

    @Test
    fun writeAndRead_roundTripsSearchHistory() = runTest {
        val original = SearchHistory(recentSearches = listOf("fantasy", "romance", "sci-fi"))

        val output = ByteArrayOutputStream()
        SearchHistorySerializer.writeTo(original, output)

        val restored = SearchHistorySerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, restored)
    }

    @Test
    fun defaultValue_isEmptyHistory() {
        assertEquals(emptyList<String>(), SearchHistorySerializer.defaultValue.recentSearches)
    }
}
