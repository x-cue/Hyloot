package me.xcue.hyloot

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import me.xcue.hyloot.listeners.PreChestOpenSystem

/* This is the main class: the entry point for your plugin.
 * Use the setup function to register commands or event listeners.
 */

class Hyloot(init: JavaPluginInit) : JavaPlugin(init) {
    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    }

    init {
        LOGGER.atInfo().log("Hello from ${this.name} version ${this.manifest.version}")
    }

    override fun setup() {
//       entityStoreRegistry.registerSystem(ChestOpenListener())
       entityStoreRegistry.registerSystem(PreChestOpenSystem())



//        eventRegistry.register(UseBlockEvent.Post::class.java) {
//            println("POST")
//            val p = it.context.entity.store.getComponent(it.context.entity, Player.getComponentType()) ?: return@register
//            val world = p.world ?: return@register
//
//            val blockType = world.getBlockType(it.targetBlock)
//            p.sendMessage(Message.raw("Yuhhh"))
//            p.sendMessage(Message.raw("World: " + world.name))
//            p.sendMessage(Message.raw("$blockType"))
//        }
//
//        eventRegistry.register(UseBlockEvent.Pre::class.java) {
//            println("PRE")
//            val p = it.context.entity.store.getComponent(it.context.entity, Player.getComponentType()) ?: return@register
//            val world = p.world ?: return@register
//
//            val blockType = world.getBlockType(it.targetBlock)
//            p.sendMessage(Message.raw("Yuhhh"))
//            p.sendMessage(Message.raw("World: " + world.name))
//            p.sendMessage(Message.raw("$blockType"))
//            // TODO detect if specifically a chest block
//        }
    }
}

