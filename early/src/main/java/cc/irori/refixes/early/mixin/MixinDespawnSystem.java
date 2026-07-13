package cc.irori.refixes.early.mixin;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.DespawnSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DespawnSystem.class)
public class MixinDespawnSystem {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void refixes$guardNullDespawn(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            CallbackInfo ci) {
        DespawnComponent despawn = archetypeChunk.getComponent(index, DespawnComponent.getComponentType());
        if (despawn == null || despawn.getDespawn() == null) {
            ci.cancel();
        }
    }
}
