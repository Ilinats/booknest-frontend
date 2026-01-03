package com.example.booknest.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler

@Serializable
data class SearchHistory(
    val recentSearches: List<String> = emptyList()
)

object SearchHistorySerializer : Serializer<SearchHistory> {
    override val defaultValue: SearchHistory = SearchHistory()

    override suspend fun readFrom(input: InputStream): SearchHistory {
        try {
            return Json.decodeFromString(
                SearchHistory.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Unable to read SearchHistory", exception)
        }
    }

    override suspend fun writeTo(t: SearchHistory, output: OutputStream) {
        output.write(
            Json.encodeToString(SearchHistory.serializer(), t).encodeToByteArray()
        )
    }
}

val Context.searchHistoryDataStore: DataStore<SearchHistory> by dataStore(
    fileName = "search_history.pb",
    serializer = SearchHistorySerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { SearchHistory() }
)

class SearchHistoryManager(private val dataStore: DataStore<SearchHistory>) {
    private val maxRecentSearches = 10

    val recentSearches: Flow<List<String>> = dataStore.data.map { it.recentSearches }

    suspend fun addSearch(query: String) {
        if (query.isBlank()) return

        dataStore.updateData { current ->
            val updated = current.recentSearches
                .filter { it.lowercase() != query.lowercase() }
                .toMutableList()
                .apply {
                    add(0, query)
                    if (size > maxRecentSearches) {
                        removeAt(size - 1)
                    }
                }
            current.copy(recentSearches = updated)
        }
    }

    suspend fun clearHistory() {
        dataStore.updateData { SearchHistory() }
    }

    suspend fun getRecentSearches(): List<String> {
        return recentSearches.first()
    }
}

