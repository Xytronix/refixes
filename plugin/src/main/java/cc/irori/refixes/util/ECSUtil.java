package cc.irori.refixes.util;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nullable;

public final class ECSUtil {

    // Private constructor to prevent instantiation
    private ECSUtil() {}

    public static @Nullable UUID getPlayerUuid(Holder<EntityStore> holder) {
        UUIDComponent uuidComponent = holder.getComponent(UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return null;
        }
        return uuidComponent.getUuid();
    }
}
