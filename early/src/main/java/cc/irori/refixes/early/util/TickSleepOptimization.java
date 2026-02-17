package cc.irori.refixes.early.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.thread.TickingThread;

public final class TickSleepOptimization {

    private static final HytaleLogger LOGGER = Logs.logger();

    public static boolean enabled = false;

    // Private constructor to prevent instantiation
    private TickSleepOptimization() {}

    public static void updateSleepOffset(boolean enable, long spinThresholdNanos) {
        enabled = enable;
        if (enable) {
            TickingThread.SLEEP_OFFSET = 1_000_000L; // 1 ms
            LOGGER.atInfo().log(
                    "TickingThread tick sleep optimization enabled (SLEEP_OFFSET=1ms, spinThreshold=%dns)",
                    spinThresholdNanos / 1000);
        }
    }
}
