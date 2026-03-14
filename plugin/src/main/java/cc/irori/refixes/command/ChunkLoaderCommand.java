package cc.irori.refixes.command;

import cc.irori.refixes.Refixes;
import cc.irori.refixes.service.ChunkLoaderService;
import cc.irori.refixes.util.Logs;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.entity.player.Transform;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;

public class ChunkLoaderCommand extends CommandBase {

    private static final HytaleLogger LOGGER = Logs.logger();
    private final ChunkLoaderService service;

    public ChunkLoaderCommand(ChunkLoaderService service) {
        super("chunkloader", "refixes.commands.chunkloader.desc");
        this.service = service;
    }

    @Override
    protected void executeSync(CommandContext context) {
        String[] args = context.getArgs();

        if (args.length == 0) {
            context.sendMessage("Usage: /refixes chunkloader <add|remove|list> [x] [z]");
            return;
        }

        String subCommand = args[0].toLowerCase();
        World world = context.getSender().getWorld();

        if (world == null) {
            context.sendMessage("§cNo world context available");
            return;
        }

        switch (subCommand) {
            case "add":
                handleAdd(context, world, args);
                break;
            case "remove":
                handleRemove(context, world, args);
                break;
            case "list":
                handleList(context, world);
                break;
            default:
                context.sendMessage("§cUnknown subcommand: " + subCommand);
                context.sendMessage("Usage: /refixes chunkloader <add|remove|list> [x] [z]");
        }
    }

    private int[] getChunkCoords(CommandContext context, String[] args, int startIndex) {
        if (args.length >= startIndex + 2) {
            try {
                return new int[]{
                        Integer.parseInt(args[startIndex]),
                        Integer.parseInt(args[startIndex + 1]),
                        startIndex + 2
                };
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (!context.isPlayer()) {
            return null;
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        Transform transform = context.getSender().getWorld().getEntityStore().getStore()
                .getComponent(playerRef, Transform.getComponentType());

        if (transform == null) {
            return null;
        }

        return new int[]{
                ChunkUtil.chunkCoordinate((int) transform.getPosition().getX()),
                ChunkUtil.chunkCoordinate((int) transform.getPosition().getZ()),
                startIndex
        };
    }

    private String extractLabel(String[] args, int startIndex) {
        if (startIndex >= args.length) {
            return null;
        }
        StringBuilder label = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) label.append(" ");
            label.append(args[i]);
        }
        return label.toString();
    }

    private void handleAdd(CommandContext context, World world, String[] args) {
        CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand("refixes.chunkloader.add"));

        int[] coords = getChunkCoords(context, args, 1);
        if (coords == null) {
            context.sendMessage("§cUsage: /refixes chunkloader add [x] [z] [label]");
            context.sendMessage("§cProvide coordinates or use current chunk");
            return;
        }

        String label = extractLabel(args, coords[2]);
        service.addChunk(world, coords[0], coords[1], label);

        String message = "§aAdded chunk loader at " + coords[0] + ", " + coords[1];
        if (label != null && !label.isEmpty()) {
            message += " (" + label + ")";
        }
        context.sendMessage(message);
    }

    private void handleRemove(CommandContext context, World world, String[] args) {
        CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand("refixes.chunkloader.remove"));

        int[] coords = getChunkCoords(context, args, 1);

        if (coords == null) {
            // Try to find by label
            if (args.length < 2) {
                context.sendMessage("§cUsage: /refixes chunkloader remove [x] [z] OR /refixes chunkloader remove <label>");
                context.sendMessage("§cProvide coordinates, label, or use current chunk");
                return;
            }

            String label = extractLabel(args, 1);
            Long chunkIndex = service.findChunkByLabel(world.getName(), label);

            if (chunkIndex == null) {
                context.sendMessage("§cNo chunk loader found with label: " + label);
                return;
            }

            int x = ChunkUtil.chunkX(chunkIndex);
            int z = ChunkUtil.chunkZ(chunkIndex);
            service.removeChunk(world, x, z);
            context.sendMessage("§aRemoved chunk loader at " + x + ", " + z + " (" + label + ")");
            return;
        }

        service.removeChunk(world, coords[0], coords[1]);
        context.sendMessage("§aRemoved chunk loader at " + coords[0] + ", " + coords[1]);
    }

    private void handleList(CommandContext context, World world) {
        CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand("refixes.chunkloader.list"));

        Map<Long, String> chunks = service.getKeptChunks(world.getName());

        if (chunks.isEmpty()) {
            context.sendMessage("§eNo chunk loaders in this world");
            return;
        }

        context.sendMessage("§aChunk loaders in " + world.getName() + ":");
        for (Map.Entry<Long, String> entry : chunks.entrySet()) {
            int x = ChunkUtil.chunkX(entry.getKey());
            int z = ChunkUtil.chunkZ(entry.getKey());
            String label = entry.getValue();

            String message = "  - " + x + ", " + z;
            if (label != null && !label.isEmpty()) {
                message += " (" + label + ")";
            }
            context.sendMessage(message);
        }
    }
}
