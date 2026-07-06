package com.birdytalk.seedco.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.serialization.json.Json

/**
 * Single Preferences DataStore holding the app's JSON-serialized data (recipes, inventory,
 * history), separate from the lightweight unit-preference store.
 */
internal val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "seedco_data")

/** Shared, lenient JSON codec for all persisted domain models. */
internal val SeedCoJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
}
