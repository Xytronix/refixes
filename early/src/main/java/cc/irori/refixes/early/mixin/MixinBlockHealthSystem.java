package cc.irori.refixes.early.mixin;

import cc.irori.refixes.early.util.Logs;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule$BlockHealthSystem")
public abstract class MixinBlockHealthSystem {

    @Unique
    private static final HytaleLogger refixes$LOGGER = Logs.logger();

    @Unique
    private static final ThreadLocal<Boolean> refixes$WRAPPING = ThreadLocal.withInitial(() -> false);

    @Shadow
    public abstract void tick(
            float dt,
            int index,
            @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk,
            @NonNullDecl Store<ChunkStore> store,
            @NonNullDecl CommandBuffer<ChunkStore> commandBuffer);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void refixes$wrapTick(
            float dt,
            int index,
            ArchetypeChunk<ChunkStore> archetypeChunk,
            Store<ChunkStore> store,
            CommandBuffer<ChunkStore> commandBuffer,
            CallbackInfo ci) {
        if (refixes$WRAPPING.get()) {
            // Run the original method
            return;
        }

        ci.cancel();
        refixes$WRAPPING.set(true);
        try {
            tick(dt, index, archetypeChunk, store, commandBuffer);
        } catch (NullPointerException e) {
            refixes$LOGGER.atWarning().withCause(e).log("BlockHealthSystem#tick(): Failed to run");
        } finally {
            refixes$WRAPPING.set(false);
        }
    }
}
