package com.birdytalk.seedco.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.birdytalk.seedco.domain.data.DefaultBlends
import com.birdytalk.seedco.domain.model.Blend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

/**
 * Reactive, persisted store of the owner's blends. Seeds from [DefaultBlends] on first run, then
 * reflects every edit. Backed by a JSON blob in [appDataStore].
 */
class BlendRepository(private val context: Context) {

    private val key = stringPreferencesKey("blends")
    private val serializer = ListSerializer(Blend.serializer())

    val blends: Flow<List<Blend>> = context.appDataStore.data.map { prefs ->
        prefs[key]?.let { stored ->
            runCatching { SeedCoJson.decodeFromString(serializer, stored) }.getOrNull()
        } ?: DefaultBlends.blends
    }

    suspend fun current(): List<Blend> = blends.first()

    /** Inserts a new blend or replaces the existing one with the same id. */
    suspend fun upsert(blend: Blend) {
        val list = current().toMutableList()
        val index = list.indexOfFirst { it.id == blend.id }
        if (index >= 0) list[index] = blend else list.add(blend)
        save(list)
    }

    suspend fun delete(id: String) = save(current().filterNot { it.id == id })

    /** Duplicates the blend with [id], giving the copy [newId] and a "(Copy)" suffix. */
    suspend fun duplicate(id: String, newId: String) {
        val source = current().firstOrNull { it.id == id } ?: return
        save(current() + source.copy(id = newId, name = "${source.name} (Copy)"))
    }

    /** Restores the six factory recipes. */
    suspend fun resetToDefaults() = save(DefaultBlends.blends)

    /** Wholesale replace (used by backup restore). */
    suspend fun replaceAll(blends: List<Blend>) = save(blends)

    private suspend fun save(list: List<Blend>) {
        context.appDataStore.edit { prefs ->
            prefs[key] = SeedCoJson.encodeToString(serializer, list)
        }
    }
}
