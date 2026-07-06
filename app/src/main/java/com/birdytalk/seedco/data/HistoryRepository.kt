package com.birdytalk.seedco.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.birdytalk.seedco.domain.persistence.BatchHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

/** Reactive, persisted log of past calculations, newest first. */
class HistoryRepository(private val context: Context) {

    private val key = stringPreferencesKey("history")
    private val serializer = ListSerializer(BatchHistoryEntry.serializer())
    private val maxEntries = 200

    val entries: Flow<List<BatchHistoryEntry>> = context.appDataStore.data.map { prefs ->
        prefs[key]?.let { stored ->
            runCatching { SeedCoJson.decodeFromString(serializer, stored) }.getOrNull()
        }.orEmpty().sortedByDescending { it.timestampEpochMillis }
    }

    suspend fun current(): List<BatchHistoryEntry> = entries.first()

    /** Prepends an entry, capping the log at [maxEntries]. */
    suspend fun add(entry: BatchHistoryEntry) {
        save((listOf(entry) + current()).take(maxEntries))
    }

    suspend fun delete(id: String) = save(current().filterNot { it.id == id })

    suspend fun clear() = save(emptyList())

    suspend fun replaceAll(entries: List<BatchHistoryEntry>) = save(entries)

    private suspend fun save(list: List<BatchHistoryEntry>) {
        context.appDataStore.edit { prefs ->
            prefs[key] = SeedCoJson.encodeToString(serializer, list)
        }
    }
}
