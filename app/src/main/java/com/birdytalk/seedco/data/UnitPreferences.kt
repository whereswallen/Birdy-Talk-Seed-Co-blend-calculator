package com.birdytalk.seedco.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.birdytalk.seedco.domain.units.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "seedco_prefs")

/**
 * Persists the globally-selected [UnitSystem] so the production floor's preferred units survive
 * app restarts.
 */
class UnitPreferences(private val context: Context) {

    private val unitKey = stringPreferencesKey("unit_system")

    val unitSystem: Flow<UnitSystem> = context.dataStore.data.map { prefs ->
        prefs[unitKey]?.let { name ->
            runCatching { UnitSystem.valueOf(name) }.getOrNull()
        } ?: UnitSystem.DECIMAL_POUNDS
    }

    suspend fun setUnitSystem(unit: UnitSystem) {
        context.dataStore.edit { prefs -> prefs[unitKey] = unit.name }
    }
}
