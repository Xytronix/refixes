package cc.irori.refixes.early.mixin;

import com.hypixel.hytale.server.core.util.thread.TickingThread;
import java.util.concurrent.locks.LockSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TickingThread.class)
public class MixinTickSleep {

    // Reserve the last 1ms of the tick budget for the precise spin-wait below.
    @Inject(method = "run", at = @At("HEAD"))
    private void refixes$reserveSpinWindow(CallbackInfo ci) {
        TickingThread.SLEEP_OFFSET = 1_000_000L;
    }

    // Replaces the pure spin-wait in the tick loop with a hybrid spin approach.
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;onSpinWait()V"))
    private void refixes$hybridWait() {
        Thread.onSpinWait();
        LockSupport.parkNanos(100_000L);
    }
}
