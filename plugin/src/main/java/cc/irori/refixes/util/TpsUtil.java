package cc.irori.refixes.util;

import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.commands.world.perf.WorldPerfCommand;

public final class TpsUtil {

    // Private constructor to prevent instantiation
    private TpsUtil() {}

    public static double getWorldTps(World world) {
        long tickStepNanos = world.getTickStepNanos();
        HistoricMetric metrics = world.getBufferedTickLengthMetricSet();
        return WorldPerfCommand.tpsFromDelta(metrics.getAverage(0), tickStepNanos);
    }

    public static double getTargetTps(World world) {
        return 1_000_000_000.0 / world.getTickStepNanos();
    }
}
