package me.xcue.hyloot.components

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hypixel.hytale.math.vector.Vector3i
import me.xcue.hyloot.Hyloot
import java.io.File
import java.util.UUID

class HylootChestStorage(worldUUID: UUID) {

    private val gson = Gson()

    // Map of chest positions to list of player UUIDs
    private val chestsLooted: MutableMap<Vector3i, MutableList<UUID>> = mutableMapOf()

    // JSON file stored inside the data folder, per-world
    private val file: File = File(Hyloot.instance.dataDirectory.toFile(), "${worldUUID}_chests.json")
        .also { it.parentFile.mkdirs() }

    private fun getOrCreateChestsLooted(pos: Vector3i): MutableList<UUID> {
        if (!chestsLooted.containsKey(pos)) {
            chestsLooted[pos] = mutableListOf()
        }

        return chestsLooted[pos]!!
    }

    fun markLooted(pos: Vector3i, players: MutableList<UUID>) {
        chestsLooted[pos] = players
    }

    fun markLooted(pos: Vector3i, playerId: UUID) {
       getOrCreateChestsLooted(pos).add(playerId)
    }

    fun hasLooted(pos: Vector3i, playerId: UUID): Boolean {
        return getOrCreateChestsLooted(pos).contains(playerId)
    }

    fun save() {
        // Convert Vector3i keys to string for JSON
        val mapForJson = chestsLooted.mapKeys { "${it.key.x},${it.key.y},${it.key.z}" }
        file.writeText(gson.toJson(mapForJson))
    }

    fun load() {
        if (!file.exists()) return
        val type = object : TypeToken<Map<String, MutableList<UUID>>>() {}.type
        val mapFromJson: Map<String, MutableList<UUID>> = gson.fromJson(file.readText(), type)
        chestsLooted.clear()
        mapFromJson.forEach { (key, uuids) ->
            val coords = key.split(",").map { it.toInt() }
            chestsLooted[Vector3i(coords[0], coords[1], coords[2])] = uuids
        }
    }
}
