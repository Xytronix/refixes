package cc.irori.refixes.early.util;

import com.hypixel.hytale.logger.HytaleLogger;

public final class Logs {

    private static final String LOGGER_NAME = "Refixes-Early";

    // Private constructor to prevent instantiation
    private Logs() {}

    public static HytaleLogger logger() {
        return HytaleLogger.get(LOGGER_NAME);
    }
}
