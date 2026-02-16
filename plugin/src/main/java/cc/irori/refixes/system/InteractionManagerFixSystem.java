package cc.irori.refixes.system;

import cc.irori.refixes.util.Logs;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class InteractionManagerFixSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = Logs.logger();

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer) {
        try {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            InteractionManager interactionManager =
                    archetypeChunk.getComponent(index, InteractionModule.get().getInteractionManagerComponent());
            if (interactionManager == null) return;

            Map<Integer, InteractionChain> chains = interactionManager.getChains();
            if (chains.isEmpty()) return;

            List<Integer> chainsToRemove = new ArrayList<>();
            List<Integer> seen = new ArrayList<>();
            for (Map.Entry<Integer, InteractionChain> entry : chains.entrySet()) {
                int id = entry.getKey();
                InteractionChain chain = entry.getValue();
                if (chain == null) {
                    chainsToRemove.add(id);
                    continue;
                }

                InteractionContext context = chain.getContext();
                if (context == null) {
                    chainsToRemove.add(id);
                    LOGGER.atWarning().log("Found an interaction chain with null context");
                    continue;
                }

                Ref<EntityStore> ownerRef = context.getOwningEntity();
                // noinspection ConstantConditions
                if (ownerRef == null || !ownerRef.isValid()) {
                    chainsToRemove.add(id);
                    LOGGER.atWarning().log("Found an interaction chain with null or invalid owning entity");
                    continue;
                }

                if (!chainsToRemove.isEmpty()) {
                    for (int removeId : chainsToRemove) {
                        chains.remove(removeId);
                        LOGGER.atWarning().log("Removed interaction chain with id %d", removeId);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Failed to apply interaction manager fix");
        }
    }
}
