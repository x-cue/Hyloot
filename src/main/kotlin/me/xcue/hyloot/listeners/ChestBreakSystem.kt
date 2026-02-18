package me.xcue.hyloot.listeners

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.EntityEventSystem
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import me.xcue.hyloot.Hyloot
import java.util.function.Predicate


class ChestBreakSystem(private val shouldPrevent: Predicate<BlockType>) : EntityEventSystem<EntityStore, BreakBlockEvent>(BreakBlockEvent::class.java) {
    override fun handle(
        p0: Int,
        p1: ArchetypeChunk<EntityStore?>,
        p2: Store<EntityStore?>,
        p3: CommandBuffer<EntityStore?>,
        p4: BreakBlockEvent
    ) {
        val world = p3.externalData.world

        if (Hyloot.AFFECTED_WORLDS.contains(world.name)) {
            if (shouldPrevent.test(p4.blockType)) {
                p4.isCancelled = true
            }

            // If the broken block wasn't a chest, make sure we prevent if the block ABOVE it is a chest too
            val abovePos = p4.targetBlock.clone().add(0, 1, 0)
            val aboveType = world.getBlockType(abovePos) ?: return

            if (shouldPrevent.test(aboveType)) {
                p4.isCancelled = true
            }
        }
    }

    override fun getQuery(): Query<EntityStore?> {
        return PlayerRef.getComponentType()
    }
}