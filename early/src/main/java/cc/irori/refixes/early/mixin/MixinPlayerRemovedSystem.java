package cc.irori.refixes.early.mixin;

import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerSystems.PlayerRemovedSystem.class)
public class MixinPlayerRemovedSystem {}
