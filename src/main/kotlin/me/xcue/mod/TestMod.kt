package me.xcue.mod

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit

/* This is the main class: the entry point for your plugin.
 * Use the setup function to register commands or event listeners.
 */

class TestMod(init: JavaPluginInit) : JavaPlugin(init) {
    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    }

    init {
        LOGGER.atInfo().log("Hello from ${this.name} version ${this.manifest.version}")
    }

    override fun setup() {
        LOGGER.atInfo().log("Setting up plugin ${this.name}")
    }
}

