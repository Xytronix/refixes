This page covers the Mixin-based early patch toggles in `mods/IroriPowered_Refixes/Refixes.json` (written by the Refixes early plugin, separate from the server-created `config.json`). These require booting the server via Hyinit. Each key enables or disables a single patch. For the runtime plugin config, see [Configuration](Configuration).

# Mixins
Section: `Mixins`

## Optimizations
Section: `Mixins.Optimizations`

Performance patches that are safe to leave on. They speed up chunk loading, collision, spatial queries, and entity tracking without changing gameplay.

| Key | Default | Description |
|-----|---------|-------------|
| `FluidPlugin` | `true` | Speeds up chunk loading by skipping the fluid module's per-chunk pre-load processing handler. |
| `BlockModule` | `true` | Speeds up chunk loading by skipping the block module's per-chunk pre-load processing handler. |
| `CollectVisible` | `true` | Reduces entity-tracking cost in tall worlds by collecting visible entities with a vertical-limited cylinder query instead of a full sphere. |
| `CollisionConfig` | `true` | Speeds up collision checks by caching the last looked-up fluid section so repeated checks on the same chunk avoid re-fetching it. |
| `CollisionAirFastPath` | `true` | Speeds up collision checks by short-circuiting block, filler, and rotation lookups to air when a section is entirely air. |
| `SkipSystemMetrics` | `false` | Stops recording per-system timing metrics to save a little per-tick CPU. Turning this on hides those metrics. |
| `KDTree` | `true` | Speeds up spatial-index rebuilds by using a cheaper coordinate sort instead of the Morton sort when the entity count is small. |
| `ChunkUnloadingSystem` | `true` | Fixes a bug where chunks near paused players unload after 7.5 seconds, and keeps the world-spawn chunk at origin loaded. |
| `PlayerChunkTrackerSystems` | `true` | Applies configurable per-player chunk streaming limits (max chunks per second and per tick, minimum loaded radius) when a player joins. |
| `SpawnManagerRecalc` | `true` | Reduces spawn-system CPU spikes by coalescing redundant environment-change spawn recalculations into one deferred pass per world. |
| `AStarBase` | `true` | Speeds up NPC pathfinding with a binary-search open-node insert and makes its node limits configurable to keep runaway searches from hurting performance. |
| `RepulsionTicker` | `true` | Reduces memory churn from entity push and repulsion handling by reusing a pooled buffer each tick. |

## Experimental
Section: `Mixins.Experimental`

Opt-in performance experiments, disabled by default. They can improve throughput but carry stability or behavioral risk. Enable one at a time and test.

| Key | Default | Description |
|-----|---------|-------------|
| `ShutdownSaveTimeout` | `false` | Prevents a hung world save from blocking shutdown by capping the save wait at a configurable timeout. |
| `PathfindingBudget` | `false` | Caps NPC A* pathfinding work per tick to smooth tick spikes, exempting NPCs that are attacking or have a marked target. |
| `BlockSectionCache` | `false` | Speeds up repeated block lookups by caching the last block section returned for a given Y in a chunk. |
| `SkipEmptyLightSections` | `false` | Speeds up lighting by skipping light propagation out of fully-air sections that hold no light. |
| `SharedInstances` | `false` | Makes instance worlds such as dungeons shared and reused across players instead of spawning a fresh copy each time, with a configurable prefix exclusion list. Can break dungeons that rely on consumable content. |
| `ConnectionBackpressure` | `false` | Adds a per-connection outbound write-buffer watermark and backpressure guard to limit memory growth from slow clients. |
| `TickSurvival` | `false` | Wraps each ticking system's `tick` call so an uncaught exception is logged (rate-limited, with the culprit plugin if identifiable) and skipped instead of killing the world's tick thread. Keeps the world alive at the cost of skipping the failing system's work for that tick; masks genuine bugs, so leave off unless a buggy system is freezing a world. Save/persistence systems (chunk, player, world-config) are handled specially: a transient save failure is tolerated and retried on the next save interval (the world keeps running), but if a save fails repeatedly it is allowed to crash so the watchdog can roll back to the last good save rather than running unsaved indefinitely. |

### Parallel
Section: `Mixins.Experimental.Parallel`

Parallel entity-ticking toggles, all disabled by default. Split into two groups by safety:

**Safe subset** — each parallelizes a single self-contained system that only reads shared read-only data and writes its own per-entity state. These do **not** need `RelaxStoreAsserts` and can be enabled on their own. Inert below the parallelism threshold, so small servers are unaffected; the win shows up on high player-count single-world servers.

| Key | Default | Description |
|-----|---------|-------------|
| `CollectVisible` | `false` | Runs per-viewer entity-visibility collection across threads. Each task only reads the shared spatial index and writes its own viewer's `visible` set. |
| `SpatialCollection` | `false` | Collects entity positions for the spatial index in parallel across chunks (into thread-local buffers) before the single-threaded tree rebuild. |

**Advanced (relaxed) group** — these parallelize systems that write through the entity store or access world chunks on worker threads. They require `RelaxStoreAsserts`, which **downgrades** the engine's thread-safety checks to rate-limited warnings (it does not make the access safe). To avoid a broken combination, the early plugin auto-enables the required dependencies at boot (and logs which ones): turning on `AllSystems` force-enables `RelaxStoreAsserts` plus both `*ReplicateChanges` companions, and turning on `Steering` force-enables `RelaxStoreAsserts`. Enable one change at a time and watch the log for relaxed-assert call sites.

| Key | Default | Description |
|-----|---------|-------------|
| `AllSystems` | `false` | Global switch: makes every entity-ticking system that delegates to `maybeUseParallel` run in parallel on large chunks. Auto-enables `RelaxStoreAsserts` and both `*ReplicateChanges` companions. Supersedes `CollectVisible` (already covered). |
| `Steering` | `false` | Forces NPC steering parallel (the engine disables this by default because it touches world chunks). Auto-enables `RelaxStoreAsserts`. |
| `RelaxStoreAsserts` | `false` | Relaxes the entity store's thread-safety assertions so the above can run, logging each relaxed call site (rate-limited) instead of crashing. Auto-enabled by `AllSystems`/`Steering`; useless on its own. |
| `FluidReplicateChanges` | `false` | Makes fluid-change replication safe under parallel ticking by deferring client packet sends to the merge phase. Companion to `AllSystems` (auto-enabled with it). |
| `ChunkReplicateChanges` | `false` | Makes block-change replication safe under parallel ticking by deferring client packet sends to the merge phase. Companion to `AllSystems` (auto-enabled with it). |

## Crashfixes
Section: `Mixins.Crashfixes`

High-severity patches that stop the server or individual worlds from crashing on corrupt data, invalid entity references, NaN positions, and cross-world teleport races.

| Key | Default | Description |
|-----|---------|-------------|
| `BlockSectionSafety` | `true` | Prevents a crash when a chunk's block-section data is corrupt by leaving the affected section empty instead of throwing. |
| `MotionControllerBase` | `true` | Stops NPCs from corrupting movement and crashing when their motion goes non-finite (NaN) by resetting it to zero. |
| `Player` | `true` | Fixes the player-ready event firing on the wrong thread, which could cause errors or lost connections as a player finishes loading in. |
| `TurnOffTeleportersSystem` | `true` | Prevents a freeze or crash by deferring teleporter and portal block updates off the chunk load/unload callback. |
| `EntityChunkLoadingSystem` | `true` | Fixes a crash when loading chunks with broken or empty entity records by skipping the bad entities and re-saving the chunk. |
| `CollisionModule` | `true` | Prevents a crash from collision against invalid (NaN) positions or empty or inverted hitboxes, such as an NPC with an unloaded model. |
| `HideEntitySystems` | `true` | Stops a crash from invalid entity references while a player is mid-teleport between worlds by skipping that tick. |
| `TriggerVolumesPlugin` | `true` | Prevents a crash when removing a world whose entity store never finished initializing. |
| `VoiceModule` | `true` | Avoids errors in proximity-voice position updates for a player who has already moved to another world. |
| `ChunkLightDataSerializeSafety` | `true` | Prevents a crash when saving or sending chunk lighting that overflows its buffer by writing a safe neutral-light fallback. |
| `PageManager` | `true` | Stops a crash from an unexpected client UI page acknowledgement by ignoring the stray event. |
| `TeleportToPlayerCommand` | `true` | Fixes a cross-world crash in the built-in `/tp <player>` command where the teleport-history update hit an invalidated reference. |
| `DeployableOwnerComponent` | `true` | Prevents a world crash from stale deployable references such as totems after the owner changes worlds by pruning invalid entries. |
| `MountPlugin` | `true` | Prevents a world crash when a mount's rider reference becomes invalid, such as a fast dismount into an unloaded area. |
| `DespawnSystem` | `true` | Prevents a world-thread crash when an entity's despawn timer has no instant set by skipping that entity instead of dereferencing null. |

## Helpers
Section: `Mixins.Helpers`

Supporting patches that enable, back, or harden the main plugin's fixes. These are accessors, behavior tweaks, and defensive guards rather than standalone features.

| Key | Default | Description |
|-----|---------|-------------|
| `ArchetypeChunk` | `true` | Prevents a crash when reading or copying a component for an out-of-bounds entity index, logging and skipping instead of throwing. |
| `BeaconAddRemoveSystem` | `true` | Despawns an NPC instead of crashing when its beacon spawn controller is missing during entity-add. |
| `BlockComponentChunk` | `true` | Tolerates duplicate block-component references and holders in a chunk by logging and ignoring them instead of throwing. |
| `BlockHealthSystem` | `true` | Wraps the block-health tick so a NullPointerException is caught and logged instead of killing the world thread. |
| `CommandBuffer` | `true` | Makes buffered component and entity removal safe against already-invalid references, fixing "Invalid entity reference" crashes during deferred removals. |
| `EntityViewer` | `true` | Fixes the "Entity is not visible!" crash by skipping queued updates and removals for entities not in the viewer's visible set. |
| `GamePacketHandler` | `true` | Wraps the client-movement packet handler so a NullPointerException is caught and logged instead of crashing the connection. |
| `HytaleServer` | `true` | Warns in the log at boot when the Refixes main plugin is missing, so admins know some runtime fixes will not apply. |
| `InteractionChain` | `true` | Fixes interaction-sync data corruption by handling negative indices and offset gaps when building interaction chains. |
| `MarkerAddRemoveSystem` | `true` | Wraps spawn-marker add-on-load and removal so an exception discards the marker instead of crashing. |
| `NPCKillsEntitySystem` | `true` | Guards against an invalid killer reference when an NPC kills an entity, returning null instead of crashing. |
| `Options` | `true` | Registers Refixes' own launch options into the server's argument parser so its config flags are recognized. |
| `PlayerViewRadius` | `true` | Keeps a player's minimum-loaded-chunk radius in sync with their client view radius plus a configurable offset to prevent premature chunk unloading. |
| `PortalDeviceSummonPage` | `true` | Prevents duplicate return portals in shared-instance worlds and supplies a fallback spawn when the portal transform is null. |
| `PortalWorldAccessor` | `true` | Exposes a portal world's removal-condition field so other Refixes patches can read it. |
| `PrefabListExtraRoots` | `true` | Adds a browsable, searchable "WorldGen" root to the builder-tools prefab browser and opens it at the top level. |
| `RemovalSystem` | `true` | Keeps shared-instance portal worlds alive until their remaining lifetime expires instead of removing them early. |
| `ServerAuthManager` | `true` | Enables OAuth token persistence and refresh for external-session auth so a self-hosted server stays logged in across restarts. |
| `SetMemoriesCapacityInteraction` | `true` | Fails the "set memories capacity" interaction gracefully when a player's memories component is no longer valid. |
| `SpawnMarkerBlockStateHeartbeat` | `true` | Recovers stale spawn-marker references by re-resolving them via UUID, keeping block-state spawn markers working. |
| `StateSupport` | `true` | Prevents NPC role updates from crashing on an "Incorrect store for entity reference" error by catching and discarding the bad update. |
| `TickingSpawnMarkerSystem` | `true` | Despawns the offending NPC instead of crashing when a spawn-marker tick hits an "Incorrect store for entity reference" error. |
| `TickSleep` | `true` | Reserves the last 1ms of the tick budget and lets the world tick loop briefly park instead of busy-spinning, for more precise pacing at lower CPU. |
| `TickingThread` | `true` | Treats parallel-ticking worker threads as valid tick threads (required by Parallel Entity Ticking). |
| `TickingThreadAssert` | `true` | Suppresses a harmless tick-thread assertion error that can fire during shutdown. |
| `TrackedPlacementAccessor` | `true` | Internal accessor exposing a tracked block's name to support `TrackedPlacementOnAddRemove`. No standalone effect. |
| `TrackedPlacementOnAddRemove` | `true` | Hardens block-placement tracking on entity removal so a null placement or block name is logged and skipped instead of crashing. |
| `UUIDSystem` | `true` | Guards entity removal against a missing UUID component, skipping the step instead of throwing. |
| `UpdateCheckCommand` | `true` | Lets `/update check` authenticate with an identity token, not only a session token. |
| `UpdateDownloadCommand` | `true` | Lets `/update download` authenticate with an identity token, not only a session token. |
| `UpdateModule` | `true` | Lets the module-level update check authenticate with an identity token, not only a session token. |
| `WorldPauseCommand` | `true` | Allows `/pause` to toggle pause on an empty (zero-player) world on a multiplayer server. |
| `World` | `true` | Adds world-lifecycle safeguards: null-safe `getPlayers()`, a retry that resolves a player-join race, a 10-second shutdown-save timeout, and per-tick pathfinding-budget reset and cleanup. |
| `WorldConfig` | `true` | Marks the world config dirty when its spawn provider changes so the change is persisted. |
| `WorldMapTracker` | `true` | Stops world-map image unloading from crashing on a null reference and defers off-thread map-settings resends to the next tick. |
| `WorldSpawningSystem` | `true` | Catches errors during random spawn-chunk selection so a failure returns no chunk instead of crashing the spawning system. |

# Hypixel Services
Section: `HypixelServices`

| Key | Default | Description |
|-----|---------|-------------|
| `LiveConfig` | `false` | Skips the LiveConfig remote refresh on startup, falling back to local feature-flag defaults. |
