package cc.irori.refixes.early.mixin;

import com.hypixel.hytale.component.task.ParallelRangeTask;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTrackerSystems.CollectVisible.class)
public class MixinCollectVisibleParallel {

    @Inject(method = "isParallel", at = @At("HEAD"), cancellable = true)
    private void refixes$parallelCollectVisible(
            int archetypeChunkSize, int taskCount, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(taskCount > 0 || archetypeChunkSize > ParallelRangeTask.PARALLELISM);
    }
}
