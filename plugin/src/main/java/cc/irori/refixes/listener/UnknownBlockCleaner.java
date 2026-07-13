package cc.irori.refixes.listener;

import cc.irori.refixes.compat.BlackboxBridge;
import cc.irori.refixes.config.impl.ListenerConfig;
import cc.irori.refixes.early.duck.UnknownFluidScannable;
import cc.irori.refixes.util.Logs;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public final class UnknownBlockCleaner {

    private static final HytaleLogger LOGGER = Logs.logger();
    private static final Queue<WorldChunk> pendingChunks = new ConcurrentLinkedQueue<>();

    private UnknownBlockCleaner() {}

    public static void registerEvents(JavaPlugin plugin) {
        plugin.getEventRegistry()
                .registerGlobal(ChunkPreLoadProcessEvent.class, event -> pendingChunks.add(event.getChunk()));

        plugin.getEventRegistry()
                .registerGlobal(AddPlayerToWorldEvent.class, UnknownBlockCleaner::cleanPlayerInventory);

        int intervalMs = Math.max(20, ListenerConfig.get().getValue(ListenerConfig.UNKNOWN_BLOCK_CLEANER_INTERVAL_MS));
        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                () -> {
                    try {
                        drainQueue();
                    } catch (Exception e) {
                        LOGGER.atSevere().withCause(e).log("Error in unknown block cleaner");
                    }
                },
                1000,
                intervalMs,
                TimeUnit.MILLISECONDS);
    }

    private static void drainQueue() {
        if (pendingChunks.isEmpty()) {
            return;
        }

        Map<World, List<WorldChunk>> byWorld = new HashMap<>();
        WorldChunk chunk;
        while ((chunk = pendingChunks.poll()) != null) {
            World world = chunk.getWorld();
            if (world != null) {
                byWorld.computeIfAbsent(world, k -> new ArrayList<>()).add(chunk);
            }
        }

        for (Map.Entry<World, List<WorldChunk>> entry : byWorld.entrySet()) {
            List<WorldChunk> chunks = entry.getValue();
            entry.getKey().execute(() -> processChunks(chunks));
        }
    }

    private static void processChunks(List<WorldChunk> chunks) {
        int budgetMs = Math.max(1, ListenerConfig.get().getValue(ListenerConfig.UNKNOWN_BLOCK_CLEANER_BUDGET_MS));
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(budgetMs);
        for (int i = 0; i < chunks.size(); i++) {
            cleanChunk(chunks.get(i));
            if (System.nanoTime() > deadline && i + 1 < chunks.size()) {
                for (int j = i + 1; j < chunks.size(); j++) {
                    pendingChunks.add(chunks.get(j));
                }
                return;
            }
        }
    }

    private static void cleanChunk(WorldChunk chunk) {
        Map<String, Integer> removedCounts = new HashMap<>();
        String[] exclude = ListenerConfig.get().getValue(ListenerConfig.UNKNOWN_BLOCK_CLEANER_EXCLUDE);

        BlockChunk blockChunk = chunk.getBlockChunk();
        if (blockChunk != null) {
            for (int sectionY = 0; sectionY < ChunkUtil.HEIGHT; sectionY += ChunkUtil.SIZE) {
                BlockSection section = blockChunk.getSectionAtBlockY(sectionY);
                if (section == null || section.isSolidAir()) {
                    continue;
                }
                cleanSection(chunk, sectionY, removedCounts, section, exclude);
            }
        } else {
            cleanAll(chunk, removedCounts, exclude);
        }

        cleanFluids(chunk, removedCounts, exclude);
        cleanContainers(chunk, removedCounts, exclude);

        if (!removedCounts.isEmpty()) {
            chunk.markNeedsSaving();
            int total =
                    removedCounts.values().stream().mapToInt(Integer::intValue).sum();
            LOGGER.atInfo().log(
                    "Cleaned %d unknown blocks (%d types) from chunk (%d, %d) in world '%s': %s",
                    total,
                    removedCounts.size(),
                    chunk.getX(),
                    chunk.getZ(),
                    chunk.getWorld().getName(),
                    removedCounts);
            BlackboxBridge.count("UnknownBlockCleaner removed", total);
        }
    }

    private static void cleanSection(
            WorldChunk chunk,
            int sectionStartY,
            Map<String, Integer> removedCounts,
            BlockSection section,
            String[] exclude) {
        if (section != null && !sectionHasUnknown(section)) {
            return;
        }
        int sectionEndY = sectionStartY + ChunkUtil.SIZE;
        for (int x = 0; x < ChunkUtil.SIZE; x++) {
            for (int z = 0; z < ChunkUtil.SIZE; z++) {
                for (int y = sectionStartY; y < sectionEndY; y++) {
                    try {
                        BlockType blockType = chunk.getBlockType(x, y, z);
                        if (blockType != null && blockType.isUnknown() && !isExcluded(blockType.getId(), exclude)) {
                            removedCounts.merge(blockType.getId(), 1, Integer::sum);
                            chunk.setBlock(x, y, z, BlockType.EMPTY_KEY);
                        }
                    } catch (Throwable t) {
                        int blockX = chunk.getX() * ChunkUtil.SIZE + x;
                        int blockZ = chunk.getZ() * ChunkUtil.SIZE + z;
                        LOGGER.atWarning().withCause(t).log(
                                "Error cleaning block at (%d, %d, %d) in world '%s'",
                                blockX, y, blockZ, chunk.getWorld().getName());
                    }
                }
            }
        }
    }

    private static void cleanAll(WorldChunk chunk, Map<String, Integer> removedCounts, String[] exclude) {
        for (int sectionY = 0; sectionY < ChunkUtil.HEIGHT; sectionY += ChunkUtil.SIZE) {
            cleanSection(chunk, sectionY, removedCounts, null, exclude);
        }
    }

    private static void cleanFluids(WorldChunk chunk, Map<String, Integer> removedCounts, String[] exclude) {
        if (!ListenerConfig.get().getValue(ListenerConfig.UNKNOWN_BLOCK_CLEANER_SCAN_FLUIDS)) {
            return;
        }
        Ref<ChunkStore> columnRef = chunk.getReference();
        if (columnRef == null || !columnRef.isValid()) {
            return;
        }
        Store<ChunkStore> store = columnRef.getStore();
        ChunkColumn column = store.getComponent(columnRef, ChunkColumn.getComponentType());
        if (column == null) {
            return;
        }
        for (Ref<ChunkStore> sectionRef : column.getSections()) {
            if (sectionRef == null || !sectionRef.isValid()) {
                continue;
            }
            FluidSection fluidSection = store.getComponent(sectionRef, FluidSection.getComponentType());
            if (fluidSection == null || fluidSection.isEmpty()) {
                continue;
            }
            if (fluidSection instanceof UnknownFluidScannable scannable && !scannable.refixes$hasUnknownFluid()) {
                continue;
            }
            for (int x = 0; x < ChunkUtil.SIZE; x++) {
                for (int z = 0; z < ChunkUtil.SIZE; z++) {
                    for (int y = 0; y < ChunkUtil.SIZE; y++) {
                        try {
                            Fluid fluid = fluidSection.getFluid(x, y, z);
                            if (fluid != null && fluid.isUnknown() && !isExcluded(fluid.getId(), exclude)) {
                                removedCounts.merge("fluid/" + fluid.getId(), 1, Integer::sum);
                                fluidSection.setFluid(x, y, z, Fluid.EMPTY, (byte) 0);
                            }
                        } catch (Throwable t) {
                            LOGGER.atWarning().withCause(t).log(
                                    "Error cleaning fluid at section-local (%d, %d, %d) in world '%s'",
                                    x, y, z, chunk.getWorld().getName());
                        }
                    }
                }
            }
        }
    }

    private static void cleanContainers(WorldChunk chunk, Map<String, Integer> removedCounts, String[] exclude) {
        if (!ListenerConfig.get().getValue(ListenerConfig.UNKNOWN_BLOCK_CLEANER_SCAN_CONTAINERS)) {
            return;
        }
        Ref<ChunkStore> chunkRef = chunk.getReference();
        if (chunkRef == null || !chunkRef.isValid()) {
            return;
        }
        Store<ChunkStore> store = chunkRef.getStore();
        BlockComponentChunk blockComponents = store.getComponent(chunkRef, BlockComponentChunk.getComponentType());
        if (blockComponents == null) {
            return;
        }
        for (Ref<ChunkStore> blockRef : blockComponents.getEntityReferences().values()) {
            if (blockRef == null || !blockRef.isValid()) {
                continue;
            }
            ItemContainerBlock containerBlock = store.getComponent(blockRef, ItemContainerBlock.getComponentType());
            if (containerBlock == null) {
                continue;
            }
            cleanItemContainer(
                    containerBlock.getItemContainer(),
                    removedCounts,
                    chunk.getWorld().getName(),
                    exclude);
        }
    }

    private static void cleanPlayerInventory(AddPlayerToWorldEvent event) {
        if (!ListenerConfig.get().getValue(ListenerConfig.UNKNOWN_BLOCK_CLEANER_SCAN_PLAYER_INVENTORIES)) {
            return;
        }
        World world = event.getWorld();
        Holder<EntityStore> holder = event.getHolder();
        if (world == null || holder == null) {
            return;
        }
        Map<String, Integer> removedCounts = new HashMap<>();
        String[] exclude = ListenerConfig.get().getValue(ListenerConfig.UNKNOWN_BLOCK_CLEANER_EXCLUDE);
        for (ComponentType<EntityStore, ? extends InventoryComponent> type : InventoryComponent.EVERYTHING) {
            InventoryComponent inventory = holder.getComponent(type);
            if (inventory != null) {
                cleanItemContainer(inventory.getInventory(), removedCounts, world.getName(), exclude);
            }
        }
        if (!removedCounts.isEmpty()) {
            int total =
                    removedCounts.values().stream().mapToInt(Integer::intValue).sum();
            LOGGER.atInfo().log(
                    "Cleaned %d unknown items (%d types) from a player's inventory in world '%s': %s",
                    total, removedCounts.size(), world.getName(), removedCounts);
            BlackboxBridge.count("UnknownBlockCleaner removed", total);
        }
    }

    private static void cleanItemContainer(
            ItemContainer container, Map<String, Integer> removedCounts, String worldName, String[] exclude) {
        if (container == null) {
            return;
        }
        short capacity = container.getCapacity();
        for (short slot = 0; slot < capacity; slot++) {
            try {
                ItemStack stack = container.getItemStack(slot);
                if (stack != null
                        && !stack.isEmpty()
                        && stack.getItem() == Item.UNKNOWN
                        && !isExcluded(stack.getItemId(), exclude)) {
                    removedCounts.merge("item/" + stack.getItemId(), Math.max(1, stack.getQuantity()), Integer::sum);
                    container.setItemStackForSlot(slot, ItemStack.EMPTY);
                }
            } catch (Throwable t) {
                LOGGER.atWarning().withCause(t).log("Error cleaning item slot %d in world '%s'", slot, worldName);
            }
        }
    }

    private static boolean isExcluded(String id, String[] exclude) {
        if (id == null || exclude == null || exclude.length == 0) {
            return false;
        }
        for (String prefix : exclude) {
            if (prefix != null && !prefix.isEmpty() && id.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sectionHasUnknown(BlockSection section) {
        BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
        for (int blockId : section.values()) {
            if (blockId == 0) {
                continue;
            }
            BlockType blockType = assetMap.getAsset(blockId);
            if (blockType == null || blockType.isUnknown()) {
                return true;
            }
        }
        return false;
    }
}
