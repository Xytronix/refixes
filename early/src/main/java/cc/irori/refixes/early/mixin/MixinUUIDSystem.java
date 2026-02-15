package cc.irori.refixes.early.mixin;

import cc.irori.refixes.early.util.Logs;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityStore.UUIDSystem.class)
public class MixinUUIDSystem {

    @Unique
    private static final HytaleLogger refixes$LOGGER = Logs.logger();

    @Unique
    private static final ThreadLocal<UUIDComponent> refixes$UUID_COMPONENT = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    @Redirect(
            method = "onEntityRemove",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hypixel/hytale/component/CommandBuffer;getComponent(Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentType;)Lcom/hypixel/hytale/component/Component;"))
    private <T extends Component<EntityStore>> T refixes$storeUuidComponent(
            CommandBuffer<EntityStore> instance, Ref<EntityStore> ref, ComponentType<EntityStore, ?> componentType) {
        UUIDComponent uuidComponent = (UUIDComponent) instance.getComponent(ref, componentType);
        refixes$UUID_COMPONENT.set(uuidComponent);
        return (T) uuidComponent;
    }

    @Inject(
            method = "onEntityRemove",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hypixel/hytale/component/CommandBuffer;getComponent(Lcom/hypixel/hytale/component/Ref;Lcom/hypixel/hytale/component/ComponentType;)Lcom/hypixel/hytale/component/Component;",
                            shift = At.Shift.AFTER),
            cancellable = true)
    private void refixes$validateUuidComponent(
            Ref<EntityStore> ref,
            RemoveReason reason,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer,
            CallbackInfo ci) {
        UUIDComponent uuidComponent = refixes$UUID_COMPONENT.get();
        if (uuidComponent == null) {
            refixes$LOGGER.atWarning().log("UUIDSystem#onEntityRemove(): UUIDComponent is null");
            ci.cancel();
        }
        refixes$UUID_COMPONENT.remove();
    }
}
