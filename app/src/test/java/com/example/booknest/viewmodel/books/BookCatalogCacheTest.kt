package com.example.booknest.viewmodel.books

import com.example.booknest.testutil.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookCatalogCacheTest {

    @Test
    fun register_storesBooksById() {
        val cache = BookCatalogCache()
        val book = TestFixtures.book(id = "a", title = "Alpha")

        cache.register(listOf(book))

        assertEquals("Alpha", cache.findBook("a")?.title)
    }

    @Test
    fun register_mergesWithoutRemovingExistingEntries() {
        val cache = BookCatalogCache()
        cache.register(listOf(TestFixtures.book(id = "a", title = "Alpha")))
        cache.register(listOf(TestFixtures.book(id = "b", title = "Beta")))

        assertEquals("Alpha", cache.findBook("a")?.title)
        assertEquals("Beta", cache.findBook("b")?.title)
    }

    @Test
    fun findBook_returnsNullForUnknownId() {
        val cache = BookCatalogCache()
        assertNull(cache.findBook("missing"))
    }

    @Test
    fun register_emptyList_isNoOp() {
        val cache = BookCatalogCache()
        cache.register(emptyList())
        assertNull(cache.findBook("any"))
    }
}
