package me.xcue.hyloot

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import me.xcue.hyloot.listeners.ChestBreakSystem
import me.xcue.hyloot.listeners.PreChestOpenSystem

/* This is the main class: the entry point for your plugin.
 * Use the setup function to register commands or event listeners.
 */

class Hyloot(init: JavaPluginInit) : JavaPlugin(init) {
    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
        lateinit var instance: Hyloot
        val AFFECTED_WORLDS = listOf("resources")
    }

    init {
        LOGGER.atInfo().log("Hello from ${this.name} version ${this.manifest.version}")
        instance = this
    }

    override fun setup() {
        val chestOpenSystem = PreChestOpenSystem()
        entityStoreRegistry.registerSystem(chestOpenSystem)
        entityStoreRegistry.registerSystem(ChestBreakSystem(chestOpenSystem::isContainer))
    }
}

