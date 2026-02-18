package cc.irori.refixes.system;

import cc.irori.refixes.util.Logs;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Ensures dropped items, block entities and projectiles have a DespawnComponent.
 *
 * Items → 5 minutes
 * Block entities → 5 minutes
 * Projectiles → 1 minute
 * All other entity types are left untouched.
 */
public class EntityDespawnTimerSystem extends RefSystem<EntityStore> {

    private static final HytaleLogger LOGGER = Logs.logger();
    private static final int ITEM_DESPAWN_SECONDS = 300;
    private static final int BLOCK_ENTITY_DESPAWN_SECONDS = 300;
    private static final int PROJECTILE_DESPAWN_SECONDS = 60;

    @Override
    public void onEntityAdded(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl AddReason addReason,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        if (store.getComponent(ref, Player.getComponentType()) != null) {
            return;
        }

        // For loaded entities, only fix null despawn timers (prevents engine NPE)
        if (addReason == AddReason.LOAD) {
            DespawnComponent existing = store.getComponent(ref, DespawnComponent.getComponentType());
            if (existing != null && existing.getDespawn() == null) {
                int seconds = resolveTimerSeconds(store, ref);
                if (seconds > 0) {
                    TimeResource time = commandBuffer.getResource(TimeResource.getResourceType());
                    commandBuffer.putComponent(
                            ref, DespawnComponent.getComponentType(), DespawnComponent.despawnInSeconds(time, seconds));
                }
            }
            return;
        }

        int seconds = resolveTimerSeconds(store, ref);
        if (seconds <= 0) {
            return;
        }

        TimeResource time = commandBuffer.getResource(TimeResource.getResourceType());
        DespawnComponent despawn = DespawnComponent.despawnInSeconds(time, seconds);
        commandBuffer.putComponent(ref, DespawnComponent.getComponentType(), despawn);
    }

    private int resolveTimerSeconds(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (store.getComponent(ref, ItemComponent.getComponentType()) != null) {
            return ITEM_DESPAWN_SECONDS;
        }

        if (store.getComponent(ref, BlockEntity.getComponentType()) != null) {
            return BLOCK_ENTITY_DESPAWN_SECONDS;
        }

        if (store.getComponent(ref, ProjectileComponent.getComponentType()) != null
                || store.getComponent(ref, Projectile.getComponentType()) != null) {
            return PROJECTILE_DESPAWN_SECONDS;
        }

        return -1;
    }

    @Override
    public void onEntityRemove(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl RemoveReason removeReason,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {}

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        // Match all entities that have a UUID
        return UUIDComponent.getComponentType();
    }
}
