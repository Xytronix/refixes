package cc.irori.refixes.early.mixin;

import cc.irori.refixes.early.util.Logs;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickableSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Store.class)
public abstract class MixinStoreTickGuard {

    @Unique
    private static final HytaleLogger refixes$LOGGER = Logs.logger();

    @Unique
    private static final long refixes$RELOG_INTERVAL_NANOS = 300_000_000_000L;

    @Unique
    private static final Map<String, Long> refixes$lastLogNanos = new ConcurrentHashMap<>();

    // A save throwing once is usually transient (momentary disk-full, a file lock, a brief I/O blip);
    // we keep the world alive and let the next save interval retry. Only a sustained failure escalates
    // to a crash, so the watchdog can reload from the last good save (bounded loss) rather than letting
    // the world run unsaved forever. Window is generous enough that the slowest saver can reach the count.
    @Unique
    private static final long refixes$SAVE_FAILURE_WINDOW_NANOS = 300_000_000_000L;

    @Unique
    private static final int refixes$MAX_SAVE_FAILURES = 3;

    // class name -> {windowStartNanos, failureCount}
    @Unique
    private static final Map<String, long[]> refixes$saveFailureWindows = new ConcurrentHashMap<>();

    @Redirect(
            method = "tickInternal",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hypixel/hytale/component/system/tick/TickableSystem;tick(FILcom/hypixel/hytale/component/Store;)V"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void refixes$guardSystemTick(TickableSystem system, float dt, int systemIndex, Store store) {
        try {
            system.tick(dt, systemIndex, store);
        } catch (Throwable t) {
            if (refixes$isPersistenceSystem(system)) {
                if (refixes$persistenceFailureShouldEscalate(system, t)) {
                    refixes$sneakyThrow(t);
                }
                return;
            }
            refixes$reportFailed(system, t);
        }
    }

    @Unique
    private static boolean refixes$isPersistenceSystem(TickableSystem system) {
        String name = system.getClass().getName();
        return name.contains("Saving") || name.contains("Save");
    }

    // Returns true if the save has failed too many times within the window (caller should rethrow to crash).
    // Otherwise swallows the failure: the world keeps running and the save retries next interval.
    @Unique
    private static boolean refixes$persistenceFailureShouldEscalate(TickableSystem system, Throwable t) {
        String name = system.getClass().getName();
        long now = System.nanoTime();
        long[] window = refixes$saveFailureWindows.computeIfAbsent(name, k -> new long[] {now, 0});
        boolean escalate;
        synchronized (window) {
            if (now - window[0] > refixes$SAVE_FAILURE_WINDOW_NANOS) {
                window[0] = now;
                window[1] = 1;
            } else {
                window[1]++;
            }
            escalate = window[1] >= refixes$MAX_SAVE_FAILURES;
        }
        if (escalate) {
            refixes$saveFailureWindows.remove(name);
            refixes$LOGGER
                    .at(Level.SEVERE)
                    .withCause(t)
                    .log(
                            "%s",
                            "Refixes TickSurvival: persistence system " + name + " failed "
                                    + refixes$MAX_SAVE_FAILURES + " times; letting it crash so the watchdog can"
                                    + " recover the world from the last good save instead of running unsaved.");
        } else {
            refixes$LOGGER
                    .at(Level.WARNING)
                    .withCause(t)
                    .log(
                            "%s",
                            "Refixes TickSurvival: persistence system " + name + " failed to save; keeping the"
                                    + " world alive and retrying next save interval.");
        }
        return escalate;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void refixes$sneakyThrow(Throwable t) throws E {
        throw (E) t;
    }

    @Unique
    private static void refixes$reportFailed(TickableSystem system, Throwable t) {
        String name = system.getClass().getName();
        long now = System.nanoTime();
        Long prev = refixes$lastLogNanos.get(name);
        if (prev != null && now - prev < refixes$RELOG_INTERVAL_NANOS) {
            return;
        }
        refixes$lastLogNanos.put(name, now);
        PluginIdentifier culprit = PluginIdentifier.identifyThirdPartyPlugin(t);
        String suffix = culprit == null ? "" : " (likely caused by plugin " + culprit.getName() + ")";
        refixes$LOGGER
                .at(Level.SEVERE)
                .withCause(t)
                .log(
                        "%s",
                        "Refixes TickSurvival: system " + name + " threw" + suffix
                                + "; skipped this tick to keep the world alive instead of killing the tick thread."
                                + " Fix the system or disable Mixins.Experimental.TickSurvival.");
    }
}
