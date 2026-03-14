package cc.irori.refixes.listener;

import cc.irori.refixes.service.ChunkLoaderService;
import com.hypixel.hytale.server.core.event.EventHandler;
import com.hypixel.hytale.server.core.event.Listener;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;

public class ChunkLoaderWorldListener implements Listener {

    private final ChunkLoaderService chunkLoaderService;

    public ChunkLoaderWorldListener(ChunkLoaderService chunkLoaderService) {
        this.chunkLoaderService = chunkLoaderService;
    }

    @EventHandler
    public void onWorldStart(StartWorldEvent event) {
        chunkLoaderService.loadWorld(event.getWorld());
    }
}
