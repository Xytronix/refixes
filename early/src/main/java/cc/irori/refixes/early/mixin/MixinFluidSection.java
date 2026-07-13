package cc.irori.refixes.early.mixin;

import cc.irori.refixes.early.duck.UnknownFluidScannable;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.AbstractSectionPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FluidSection.class)
public abstract class MixinFluidSection implements UnknownFluidScannable {

    @Shadow
    private AbstractSectionPalette typePalette;

    @Override
    @Unique
    public boolean refixes$hasUnknownFluid() {
        AbstractSectionPalette palette = this.typePalette;
        if (palette == null) {
            return false;
        }
        for (int fluidId : palette.values()) {
            if (fluidId == Fluid.EMPTY_ID) {
                continue;
            }
            Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
            if (fluid == null || fluid.isUnknown()) {
                return true;
            }
        }
        return false;
    }
}
