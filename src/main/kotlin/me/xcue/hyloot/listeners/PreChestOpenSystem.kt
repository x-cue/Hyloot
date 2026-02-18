package me.xcue.hyloot.listeners

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.EntityEventSystem
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.math.vector.Vector3i
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import me.xcue.hyloot.Hyloot
import me.xcue.hyloot.components.HylootChestStorage
import java.util.UUID

class PreChestOpenSystem : EntityEventSystem<EntityStore, UseBlockEvent.Pre>(UseBlockEvent.Pre::class.java) {
    companion object {
        private val WORLD_STORAGE = mutableMapOf<UUID, HylootChestStorage>()
    }

    private val locationsAboutToBreak = mutableListOf<Vector3i>()

    override fun handle(
        p0: Int,
        p1: ArchetypeChunk<EntityStore?>,
        p2: Store<EntityStore?>,
        p3: CommandBuffer<EntityStore?>,
        p4: UseBlockEvent.Pre
    ) {
        onPre(p4, p3)
    }

    fun onPre(e: UseBlockEvent.Pre, buffer: CommandBuffer<EntityStore?>) {
        val pos = e.targetBlock
        val blockType = e.blockType
        val interactionType = e.interactionType
        val ref = e.context.entity
        val player = ref.store.getComponent(ref, Player.getComponentType()) ?: return
        val playerRef = ref.store.getComponent(ref, PlayerRef.getComponentType()) ?: return

        if (!Hyloot.AFFECTED_WORLDS.contains(player.world?.name)) {
            return
        }

        if (!isContainer(blockType)) return
        // Cancel breaking it
        if (interactionType != InteractionType.Use) {
            if (interactionType == InteractionType.Primary) {
                locationsAboutToBreak.add(pos)
                e.isCancelled = true
            }
            return
        }

//        debugBlock(blockType, player)

        e.isCancelled = true


        if (canLootContainer(pos, playerRef)) {
            lootContainer(pos, ref, player, playerRef, buffer)
        } else {
            player.sendMessage(Message.raw("You have already looted that container."))
        }
    }

    private fun getWorldStore(ref: PlayerRef): HylootChestStorage? {
        val worldUuid = ref.worldUuid ?: return null

        return WORLD_STORAGE[worldUuid] ?: HylootChestStorage(worldUuid).also {
            it.load()
        }
    }

    private fun canLootContainer(pos: Vector3i, ref: PlayerRef): Boolean {
        val store = getWorldStore(ref) ?: return false
        return !store.hasLooted(pos, ref.uuid)
    }

    private fun lootContainer(
        pos: Vector3i,
        ref: Ref<EntityStore>,
        player: Player,
        playerRef: PlayerRef,
        buffer: CommandBuffer<EntityStore?>
    ) {
        val store = getWorldStore(playerRef) ?: return
        store.markLooted(pos, playerRef.uuid)

        val chunk = player.world?.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(pos.x, pos.z)) ?: return
        val contents = chunk.getState(pos.x, pos.y, pos.z) as ItemContainerState
        val inv = player.inventory.storage

        val finalCont = contents.itemContainer.clone()
        contents.itemContainer.forEach { slot, stack ->
            if (inv.canAddItemStack(stack)) {
                inv.addItemStack(stack)
                player.notifyPickupItem(ref, stack, pos.toVector3d(), buffer)
                finalCont.removeItemStackFromSlot(slot)
            }
        }

        if (!finalCont.isEmpty) {
            // Drop remaining
            finalCont.dropAllItemStacks()
        }

        store.save()
    }

    fun isContainer(blockType: BlockType): Boolean {
        return blockType.state?.id == "container"
    }

    override fun getQuery(): Query<EntityStore?> {
        return PlayerRef.getComponentType()
    }

    private fun debugBlock(blockType: BlockType, player: Player) {
        player.sendMessage(Message.raw("block is: " + blockType.id));
        player.sendMessage(Message.raw("block state is: " + blockType.state.id));
        player.sendMessage(Message.raw("block group is: " + blockType.group));
        player.sendMessage(Message.raw("block list id is: " + blockType.blockListAssetId));
        player.sendMessage(Message.raw("block data: " + blockType.data));
        player.sendMessage(Message.raw("block ent is: " + blockType.blockEntity));
        player.sendMessage(Message.raw("block model: " + blockType.customModel));
        player.sendMessage(Message.raw("block flags: " + blockType.flags));
        player.sendMessage(Message.raw("block iHint: " + blockType.interactionHint));
        player.sendMessage(Message.raw("block iType?: " + blockType.interactionHitboxType));
        player.sendMessage(Message.raw("block is trigger?: " + blockType.isTrigger));
        player.sendMessage(Message.raw("prefablistassetid: " + blockType.prefabListAssetId))
    }

    fun preventBlockBreak(e: BreakBlockEvent) {
        if (locationsAboutToBreak.contains(e.targetBlock)) {
            e.isCancelled = true
        }
    }
}