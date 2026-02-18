package me.xcue.hyloot.listeners

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.EntityEventSystem
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.math.vector.Vector3i
import com.hypixel.hytale.protocol.BlockFlags
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.protocol.packets.asseteditor.AssetInfo
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
import com.hypixel.hytale.server.core.asset.type.item.config.ItemStackContainerConfig
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent
import com.hypixel.hytale.server.core.inventory.Inventory
import com.hypixel.hytale.server.core.inventory.container.ItemContainer
import com.hypixel.hytale.server.core.inventory.container.ItemContainerUtil
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.npc.corecomponents.items.ActionPickUpItem
import com.hypixel.hytale.server.npc.util.InventoryHelper

class PreChestOpenSystem : EntityEventSystem<EntityStore, UseBlockEvent.Pre>(UseBlockEvent.Pre::class.java) {
    companion object {
        private val DENIED_INTERACTION_TYPES = listOf(InteractionType.Primary)
        private val AFFECTED_WORLDS = listOf("resources")
    }

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

        if (!AFFECTED_WORLDS.contains(player.world?.name)) {
            return
        }

        if (!isContainer(blockType)) return
        // Cancel breaking it
        if (interactionType != InteractionType.Use) {
            if (DENIED_INTERACTION_TYPES.contains(interactionType)) {
                e.isCancelled = true
                player.sendMessage(Message.raw("You cannot do that."))
            }
            return
        }

//        debugBlock(blockType, player)

        e.isCancelled = true

        if (canLootContainer(pos, ref, player)) {
            lootContainer(pos, ref, player, buffer)
        } else {
            player.sendMessage(Message.raw("You have already looted that container."))
        }
    }

    private fun canLootContainer(pos: Vector3i, ref: Ref<EntityStore>, player: Player): Boolean {
        // TODO Check some kind of store...
        return true
    }

    private fun lootContainer(
        pos: Vector3i,
        ref: Ref<EntityStore>,
        player: Player,
        buffer: CommandBuffer<EntityStore?>
    ) {
        // TODO update store saying the player HAS looted it now...
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
    }

    private fun isContainer(blockType: BlockType): Boolean {
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
}