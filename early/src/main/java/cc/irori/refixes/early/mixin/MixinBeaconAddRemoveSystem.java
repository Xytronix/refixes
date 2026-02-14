package cc.irori.refixes.early.mixin;

import cc.irori.refixes.early.util.Logs;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.SpawnReferenceSystems;
import com.hypixel.hytale.server.spawning.controllers.BeaconSpawnController;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnReferenceSystems.BeaconAddRemoveSystem.class)
public class MixinBeaconAddRemoveSystem {

    @Unique
    private static final HytaleLogger refixes$LOGGER = Logs.logger();

    @Inject(
            method = "onEntityAdded",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hypixel/hytale/server/spawning/controllers/BeaconSpawnController;hasSlots()Z",
                            shift = At.Shift.BEFORE),
            cancellable = true)
    private void refixes$ignoreNullSpawnController(
            Ref<EntityStore> ref,
            AddReason reason,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer,
            CallbackInfo ci,
            @Local(name = "npcComponent") NPCEntity npcComponent,
            @Local(name = "spawnController") BeaconSpawnController spawnController) {
        if (spawnController == null) {
            refixes$LOGGER.atWarning().log(
                    "BeaconAddRemoveSystem#onEntityAdded(): despawning NPC due to null spawnController");
            npcComponent.setToDespawn();
            ci.cancel();
        }
    }
}
