Refixes uses two config files in `mods/IroriPowered_Refixes/`. This page covers the runtime plugin config in `config.json` (created by the server). The Mixin patch toggles live in a separate `Refixes.json`, documented on the [Mixins](Mixins) page.

# Blackbox Integration

- `BlackboxIntegration` (default = `true`): Emit events and gauges to the Blackbox monitoring plugin if it is installed. Harmless no-op when Blackbox is absent.

# Early Patches
Section: `Early`

Boot-time tweaks for chunk streaming, pathfinding, stat recalculation, shutdown, and backpressure. Changes here require a full restart. Several keys only take effect when the matching patch on the [Mixins](Mixins) page is enabled.

| Key | Default | Description |
|-----|---------|-------------|
| `MaxChunksPerSecond` | `36` | Cap chunks generated or loaded per second, smoothing streaming spikes. |
| `MaxChunksPerTick` | `4` | Cap chunks generated or loaded within a single tick. |
| `VanillaKeepSpawnLoaded` | `true` | Keep the vanilla behavior of holding spawn chunks loaded. Set false to let them unload. |
| `UnloadDistanceOffset` | `4` | Extra chunks beyond a player's view radius kept loaded (raises the minimum-loaded-chunk radius). |
| `StatRecalcInterval` | `4` | Recalculate entity stat modifiers at most once per this many ticks. |
| `PathfindingMaxPathLength` | `200` | Max nodes in a computed NPC path before the search gives up. |
| `PathfindingOpenNodesLimit` | `80` | Max simultaneously-open frontier nodes per A* search. |
| `PathfindingTotalNodesLimit` | `400` | Max total nodes a single A* search may expand. |
| `PathfindingMaxNewSearchesPerTick` | `8` | Max new pathfinding searches started per tick. |
| `PathfindingMaxNodeExpansionsPerTick` | `600` | Max total A* node expansions per tick across all searches. |
| `ShutdownSaveTimeoutSeconds` | `10` | Max seconds to wait for saving during shutdown before proceeding. |
| `BackpressureMaxOutboundBytes` | `16777216` | Per-connection outbound buffer high-watermark (16 MiB) before backpressure applies. |
| `BackpressureGraceMs` | `10000` | Grace period in ms a stalled connection may persist before being force-closed. |

## Cylinder Visibility
Section: `Early.CylinderVisibility`

Narrows entity visibility collection to a cylinder to cut unnecessary vertical matches.

| Key | Default | Description |
|-----|---------|-------------|
| `HeightMultiplier` | `2.0` | Cylinder vertical half-height as a multiple of the horizontal view radius. Lower values restrict vertical matching further. |

## K-d Tree Optimization
Section: `Early.KDTreeOptimization`

Tunes the spatial KD-tree rebuild.

| Key | Default | Description |
|-----|---------|-------------|
| `SpatialFastSortThreshold` | `64` | Below this entity count, rebuild uses a cheap coordinate sort instead of the Morton sort. |

# Listeners
Section: `Listeners`

Event-driven helper listeners.

| Key | Default | Description |
|-----|---------|-------------|
| `UnknownBlockCleaner` | `false` | Scan newly pre-loaded chunks and clear blocks whose type is no longer known, for example from removed mods or content. |
| `UnknownBlockCleanerExclude` | `[]` | List of ID prefixes that are never cleaned, even when unknown (applies to blocks, fluids, container items and player inventory items). For example `dynamicseasons:` protects all content from that mod. |
| `UnknownBlockCleanerBudgetMs` | `10` | Per-run time budget in ms for the unknown-block cleaner. |
| `UnknownBlockCleanerIntervalMs` | `50` | Interval in ms between cleaner passes (floored to 20 ms). |

# Systems
Section: `Systems`

ECS fix systems.

| Key | Default | Description |
|-----|---------|-------------|
| `CraftingManager` | `true` | Rescue crafting benches left in a stuck or stale bound state so they can be used again. |
| `EntityDespawnTimer` | `true` | Remove dropped items, loose block entities, and projectiles after a timeout. |

## Entity Despawn Timer
Section: `Systems.EntityDespawnTimerConfig`

Per-category timeouts, effective only when `EntityDespawnTimer` is on.

The engine already gives most dropped items, loose block entities, and projectiles their own despawn timer, which is respected by default. As a result the per-category seconds below normally only take effect when repairing a null timer, when `AddTimerWhenMissing` is on, or when `OverrideExistingTimers` is on.

| Key | Default | Description |
|-----|---------|-------------|
| `ItemDespawnSeconds` | `300` | Seconds a dropped item persists before despawning. Set to `0` to never despawn items. |
| `BlockEntityDespawnSeconds` | `300` | Seconds a loose block entity persists before despawning. Set to `0` to never despawn block entities. |
| `ProjectileDespawnSeconds` | `60` | Seconds a projectile persists before despawning. Set to `0` to never despawn projectiles. |
| `AddTimerWhenMissing` | `false` | When on, also give a despawn timer to freshly spawned items/block entities/projectiles that have none. Off by default because a missing despawn timer is the engine's "persist forever" signal (e.g. intentionally permanent dropped items), so enabling this can make such entities despawn. Existing timers are always respected and null timers are always repaired regardless of this setting. |
| `OverrideExistingTimers` | `false` | When on, replace a newly spawned entity's existing despawn timer with the configured per-category value (set the category to `0` to strip the timer and make it permanent). Off by default, which respects the lifetime the engine or another mod already set. Applied at spawn only; saved entities keep their timer on reload. |

# Services
Section: `Services`

Per-service performance and lifecycle managers. Each is a nested section.

## AI Tick Throttler
Section: `Services.AiTickThrottler`

Throttles NPC AI tick rate by distance from players, freezing or slowing distant NPCs.

| Key | Default | Description |
|-----|---------|-------------|
| `Enabled` | `false` | Master switch. |
| `UpdateIntervalMs` | `150` | Milliseconds between throttle re-evaluation passes. |
| `MaxCycleMs` | `30` | Per-cycle time budget in ms. `0` disables it. |
| `ScanShards` | `1` | Split the scan across this many ticks. `1` scans all every cycle. Higher lowers per-tick cost but slows reaction. |
| `NearChunks` | `2` | Within this chunk distance, full tick rate (about 64 blocks). |
| `MidChunks` | `4` | Within this chunk distance, mid tick rate (about 128 blocks). |
| `FarChunks` | `6` | Within this chunk distance, far tick rate (about 192 blocks). |
| `MidTickSeconds` | `0.2` | Tick interval for mid-distance NPCs. |
| `FarTickSeconds` | `0.5` | Tick interval for far NPCs. |
| `VeryFarTickSeconds` | `1.0` | Tick interval for NPCs beyond the far band. |
| `MinTickSeconds` | `0.05` | Fastest allowed throttled interval. |
| `ActivationHysteresisChunks` | `0` | Extra margin before re-tiering, to prevent flapping. |
| `MaxUnfreezesPerTick` | `10` | Cap NPCs unfrozen per tick. |
| `MaxFreezesPerTick` | `20` | Cap NPCs frozen per tick. |
| `ThrottleExcludedNpcTypes` | `[]` | NPC types exempt from throttling. |
| `ThrottleExcludeMounts` | `true` | Exempt mounts (ridden entities). |
| `ThrottleExcludeFlying` | `false` | Exempt flying NPCs. |
| `CleanupFrozenEntities` | `false` | On load, release NPCs left stranded frozen by the throttler. |
| `CleanupExcludedNpcTypes` | `[]` | NPC types exempt from that cleanup. |
| `LegacyCleanup` | `false` | Enable the legacy orphan-frozen cleanup path. |
| `LegacyCleanupExcludedNpcTypes` | `[]` | NPC types exempt from legacy cleanup. |

## Per-Player Hot Radius
Section: `Services.PerPlayerHotRadius`

Dynamically adjusts each player's hot (fully-ticked) chunk radius based on per-world TPS: a laggy world shrinks its own players' simulation radius without affecting calm worlds. At healthy TPS the radius sits at `MaxRadius`, which defaults to the engine's own hot-chunk cap (8), so a healthy server is left untouched. Optionally guards against memory pressure: when the heap stays high after a major garbage collection it lowers the server's max view radius, then recovers it as memory frees up. Detection works across HotSpot collectors (G1, Parallel, ZGC, Shenandoah). The memory guard is off by default and reacts to GC events, so it costs nothing on servers that never run low on memory.

| Key | Default | Description |
|-----|---------|-------------|
| `Enabled` | `true` | Master switch. |
| `CheckIntervalMs` | `5000` | Milliseconds between adjustments. |
| `MinRadius` | `2` | Lowest hot-chunk radius (used at or below `TPSLow`). |
| `MaxRadius` | `8` | Highest hot-chunk radius. Matches the engine default, so healthy servers are unchanged. |
| `TPSLowFraction` | `0.75` | Fraction of the world's target TPS at or below which the radius shrinks to `MinRadius` (e.g. 0.75 = 22.5 TPS on a 30-TPS world). |
| `TPSHighFraction` | `0.90` | Fraction of the world's target TPS at or above which the radius grows to `MaxRadius` (e.g. 0.90 = 27 TPS on a 30-TPS world). |
| `MemoryGuardEnabled` | `false` | Reduce the server max view radius under memory pressure and restore it on recovery/shutdown. Opt-in. |
| `MemoryHeapThreshold` | `0.85` | After-GC heap usage ratio that counts as memory pressure. |
| `MemoryMinViewRadius` | `2` | Lowest view radius the guard will drop to. |
| `MemoryViewDecreaseFactor` | `0.75` | Multiplier applied to the view radius on each reduction step. |
| `MemoryRecoveryWaitSeconds` | `60` | Seconds of relieved pressure before the view radius grows back by 1. |

## Idle Player Handler
Section: `Services.IdlePlayerHandler`

Reduces the ticking and view footprint of players who stop moving for a set time.

| Key | Default | Description |
|-----|---------|-------------|
| `Enabled` | `false` | Master switch. |
| `IdleTimeoutSeconds` | `90` | Inactivity before a player is treated as idle. |
| `CheckIntervalSeconds` | `10` | Seconds between idle checks. |
| `ReduceViewRadius` | `true` | Shrink an idle player's view radius. |
| `IdleViewRadius` | `4` | View radius applied while idle. |
| `ReduceHotRadius` | `true` | Shrink an idle player's hot radius. |
| `IdleHotRadius` | `3` | Hot radius applied while idle. |
| `ReduceMinLoadedRadius` | `true` | Shrink an idle player's minimum-loaded-chunk radius. |
| `IdleMinLoadedRadius` | `2` | Minimum-loaded-chunk radius applied while idle. |
| `MovementThreshold` | `0.5` | Blocks a player must move to count as active and reset the idle timer. |

## Idle World Pause
Section: `Services.IdleWorldPause`

Pauses ticking of worlds that have no players.

| Key | Default | Description |
|-----|---------|-------------|
| `Enabled` | `false` | Master switch. |
| `CheckIntervalMs` | `10000` | Milliseconds between empty-world checks. |
| `ExcludedWorlds` | `[]` | Worlds never paused when empty. |

# Chunk Loader
Section: `ChunkLoader`

Admin command (`/chunkloader`) to keep selected chunks permanently loaded.

| Key | Default | Description |
|-----|---------|-------------|
| `Enabled` | `true` | Register the `/chunkloader` command and apply kept chunks on world load. When off, persisted chunk loaders are left on disk but not applied. |

# Shared Instance Worlds
Section: `SharedInstanceWorlds`

Controls reuse and reset behavior for shared (persistent) instance worlds such as dungeons.

| Key | Default | Description |
|-----|---------|-------------|
| `ExcludedPrefixes` | `[]` | World-name prefixes excluded from shared-instance handling. |
| `ResetOnEmpty` | `false` | Reset a shared instance world once it becomes empty of players. |

# Watchdog
Section: `Watchdog`

Monitors world threads for stalls and crashes.

| Key | Default | Description |
|-----|---------|-------------|
| `Enabled` | `true` | Master switch. |
| `ShutdownOnDefaultWorldCrash` | `true` | Shut the server down if the default world thread crashes. |
| `AutoRestartWorlds` | `false` | Automatically restart a world after a detected failure (save-gated). |
| `DumpAllThreads` | `false` | On detection, dump all JVM threads rather than just the stalled one. |
| `ActivationDelayMs` | `10000` | Delay after startup before monitoring begins. |
| `ThreadTimeoutMs` | `30000` | Time a world thread may be unresponsive before it is flagged stalled. |
| `ShutdownTimeoutMs` | `60000` | Time allowed for a watchdog-triggered shutdown to complete. |
| `RestartSaveTimeoutMs` | `15000` | Time allowed for the pre-restart save. Restart aborts if it cannot finish. |
| `AutoRestartingWorldFilter` | `[]` | Worlds eligible for auto-restart. Empty applies the default policy. |

# Experimental
Section: `Experimental`

| Key | Default | Description |
|-----|---------|-------------|
| `ParallelSteeringThreshold` | `64` | Minimum chunk entity count for NPC steering to run in parallel. Only takes effect when parallel entity ticking is enabled. |
