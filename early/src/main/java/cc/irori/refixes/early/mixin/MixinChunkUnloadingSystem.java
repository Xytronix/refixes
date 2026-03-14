package cc.irori.refixes.early.mixin;

import cc.irori.refixes.early.EarlyOptions;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fixes vanilla bug where paused players' chunks unload after 7.5 seconds
@Mixin(targets = "com.hypixel.hytale.server.core.universe.world.storage.component.ChunkUnloadingSystem")
public class MixinChunkUnloadingSystem {

    @Inject(
            method = "tryUnload",
            at = @At(value = "INVOKE", target = "Lcom/hypixel/hytale/server/core/universe/world/chunk/WorldChunk;pollKeepAlive(I)I"),
            cancellable = true)
    private static void refixes$protectSpawnChunk(
            int index,
            ArchetypeChunk<ChunkStore> archetypeChunk,
            CommandBuffer<ChunkStore> commandBuffer,
            CallbackInfo ci) {
        if (!EarlyOptions.isAvailable() || !EarlyOptions.VANILLA_KEEP_SPAWN_LOADED.get()) {
            return;
        }

        WorldChunk worldChunk = archetypeChunk.getComponent(index, WorldChunk.getComponentType());
        World world = ((ChunkStore) commandBuffer.getStore().getExternalData()).getWorld();

        int spawnX = (int) world.getWorldConfig().getSpawnPosition().getX();
        int spawnZ = (int) world.getWorldConfig().getSpawnPosition().getZ();
        int spawnChunkX = ChunkUtil.chunkCoordinate(spawnX);
        int spawnChunkZ = ChunkUtil.chunkCoordinate(spawnZ);

        if (worldChunk.getX() == spawnChunkX && worldChunk.getZ() == spawnChunkZ) {
            worldChunk.resetKeepAlive();
            ci.cancel();
        }
    }

    @Overwrite
    private static void collectTrackers(
            ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer) {
        Store<ChunkStore> chunkStore =
                ((EntityStore) commandBuffer.getExternalData()).getWorld().getChunkStore().getStore();
        DataAccessor dataResource = (DataAccessor) chunkStore.getResource(ChunkStore.UNLOAD_RESOURCE);

        for (int index = 0; index < archetypeChunk.size(); ++index) {
            ChunkTracker chunkTracker = archetypeChunk.getComponent(index, ChunkTracker.getComponentType());
            if (chunkTracker != null && chunkTracker.shouldBeVisible(chunkTracker.getChunkIndex())) {
                dataResource.getChunkTrackers().add(chunkTracker);
            }
        }
    }

    @Mixin(
            targets =
                    "com.hypixel.hytale.server.core.universe.world.storage.component.ChunkUnloadingSystem$Data")
    interface DataAccessor {
        List<ChunkTracker> getChunkTrackers();
    }
}


