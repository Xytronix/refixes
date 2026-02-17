package cc.irori.refixes.early.mixin;

import cc.irori.refixes.early.util.Logs;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.concurrent.ForkJoinWorkerThread;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.spongepowered.asm.mixin.*;

/**
 * Relaxes the thread assertion to allow parallel entity ticking.
 * This patch allows ForkJoinWorkerThread through the assertion,
 * while still catching genuine off-thread access from other contexts.
 */
@Mixin(Store.class)
public abstract class MixinStore<ECS_TYPE> {

    @Unique
    private static final HytaleLogger refixes$LOGGER = Logs.logger();

    @Shadow
    @Final
    private Thread thread;

    @Shadow
    public abstract <T extends Component<ECS_TYPE>> boolean removeComponentIfExists(
            @NonNullDecl Ref<ECS_TYPE> ref, @NonNullDecl ComponentType<ECS_TYPE, T> componentType);

    // Allow ForkJoinPool worker threads used by parallel entity ticking
    @Overwrite
    public void assertThread() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof ForkJoinWorkerThread) {
            return;
        }
        if (!currentThread.equals(this.thread) && this.thread.isAlive()) {
            throw new IllegalStateException("Assert not in thread! " + this.thread + " but was in " + currentThread);
        }
    }

    @Overwrite
    public <T extends Component<ECS_TYPE>> void tryRemoveComponent(
            @Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
        try {
            removeComponentIfExists(ref, componentType);
        } catch (IllegalStateException e) {
            refixes$LOGGER.atWarning().withCause(e).log("Store#tryRemoveComponent(): Failed to remove component");
        }
    }
}
