package me.xcue.hyloot.listeners

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Component
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
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.npc.corecomponents.items.ActionPickUpItem
import com.hypixel.hytale.server.npc.util.InventoryHelper

class PreChestOpenSystem : EntityEventSystem<EntityStore, UseBlockEvent.Pre>(UseBlockEvent.Pre::class.java) {
    override fun handle(
        p0: Int,
        p1: ArchetypeChunk<EntityStore?>,
        p2: Store<EntityStore?>,
        p3: CommandBuffer<EntityStore?>,
        p4: UseBlockEvent.Pre
    ) {
        onPre(p4)
    }

    fun onPre(e: UseBlockEvent.Pre) {
        val pos = e.targetBlock
        val blockType = e.blockType
        val interactionType = e.interactionType
        val ctx = e.context

        if (interactionType != InteractionType.Use) return
        if (!isContainer(blockType)) return


        val ref = e.context.entity
        val player = ref.store.getComponent(ref, Player.getComponentType()) ?: return
        debugBlock(blockType, player)

        println(blockType.state)
        println("Container data: " + blockType.data.containerData)
        println("Container data: " + blockType.data)
//        WorldChunk worldChunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
//        (ItemContainerState)worldChunk.getState(x, y, z);

        val chunk = player.world?.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(pos.x, pos.z)) ?: return
        val contents = chunk.getState(pos.x, pos.y, pos.z) as ItemContainerState


//        println("Container data: " + blockType.data.)

        val blockStore = player.world?.getBlockComponentHolder(pos.x, pos.y, pos.z) ?: return

        // Damage is POSITIVE - To HEAL need to use NEGATIVE value
//        float amount = 20.0f; // damage amount
//        Damage damage = new Damage(Damage.NULL_SOURCE, DamageCause.PHYSICAL, amount);
//        DamageSystems.executeDamage(player.getReference(), store, damage);


//        player.sendMessage(Message.raw("prefablistassetid: " + e.blockType.))

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