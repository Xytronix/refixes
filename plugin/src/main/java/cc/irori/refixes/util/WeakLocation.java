package cc.irori.refixes.util;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nullable;

public record WeakLocation(String worldName, Vector3d position, Vector3f rotation) {

    public WeakLocation(String worldName, Transform transform) {
        this(worldName, transform.getPosition(), transform.getRotation());
    }

    public @Nullable World getWorld() {
        return Universe.get().getWorld(worldName);
    }

    public Transform getTransform() {
        return new Transform(position, rotation);
    }
}
