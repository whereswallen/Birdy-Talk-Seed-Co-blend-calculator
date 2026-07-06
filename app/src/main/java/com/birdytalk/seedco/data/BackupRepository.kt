package com.birdytalk.seedco.data

import com.birdytalk.seedco.domain.persistence.AppBackup

/**
 * Produces and consumes a single JSON [AppBackup] bundling recipes, inventory, and history. The
 * actual file read/write (via the Storage Access Framework) happens in the UI; this class only
 * serializes and applies the snapshot.
 */
class BackupRepository(
    private val blendRepository: BlendRepository,
    private val inventoryRepository: InventoryRepository,
    private val historyRepository: HistoryRepository,
) {

    /** Serializes the current app state to a pretty-printed JSON string. */
    suspend fun exportJson(): String {
        val backup = AppBackup(
            blends = blendRepository.current(),
            inventory = inventoryRepository.current(),
            history = historyRepository.current(),
        )
        return prettyJson.encodeToString(AppBackup.serializer(), backup)
    }

    /** Parses and applies a backup, replacing all current data. Returns failure on invalid JSON. */
    suspend fun importJson(text: String): Result<Unit> = runCatching {
        val backup = SeedCoJson.decodeFromString(AppBackup.serializer(), text)
        blendRepository.replaceAll(backup.blends)
        inventoryRepository.replaceAll(backup.inventory)
        historyRepository.replaceAll(backup.history)
    }

    private companion object {
        val prettyJson = kotlinx.serialization.json.Json {
            prettyPrint = true
            encodeDefaults = true
        }
    }
}
