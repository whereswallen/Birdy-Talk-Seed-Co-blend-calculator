package com.birdytalk.seedco.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.birdytalk.seedco.domain.persistence.InventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import java.math.BigDecimal

/**
 * Reactive, persisted on-hand inventory keyed by ingredient name (in pounds). Ingredients with no
 * entry are simply untracked.
 */
class InventoryRepository(private val context: Context) {

    private val key = stringPreferencesKey("inventory")
    private val serializer = ListSerializer(InventoryItem.serializer())

    val items: Flow<List<InventoryItem>> = context.appDataStore.data.map { prefs ->
        prefs[key]?.let { stored ->
            runCatching { SeedCoJson.decodeFromString(serializer, stored) }.getOrNull()
        } ?: emptyList()
    }

    /** Convenience view: ingredient name → pounds on hand. */
    val onHand: Flow<Map<String, BigDecimal>> = items.map { list ->
        list.associate { it.name to it.onHandPounds }
    }

    suspend fun current(): List<InventoryItem> = items.first()

    /** Sets (or, for null/non-positive, clears) the on-hand pounds for [name]. */
    suspend fun setOnHand(name: String, pounds: BigDecimal?) {
        val list = current().filterNot { it.name == name }.toMutableList()
        if (pounds != null && pounds.signum() > 0) list.add(InventoryItem(name, pounds))
        save(list)
    }

    suspend fun replaceAll(items: List<InventoryItem>) = save(items)

    private suspend fun save(list: List<InventoryItem>) {
        context.appDataStore.edit { prefs ->
            prefs[key] = SeedCoJson.encodeToString(serializer, list.sortedBy { it.name })
        }
    }
}
