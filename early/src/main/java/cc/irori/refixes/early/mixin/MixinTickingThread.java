package cc.irori.refixes.early.mixin;

import com.hypixel.hytale.server.core.util.thread.TickingThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TickingThread.class)
public class MixinTickingThread {

    @Shadow
    private Thread thread;

    // Relaxes isInThread() to also return true for parallel entity ticking worker threads.
    @Overwrite
    public boolean isInThread() {
        Thread current = Thread.currentThread();
        if (current.equals(this.thread)) {
            return true;
        }
        if (current instanceof java.util.concurrent.ForkJoinWorkerThread fjwt) {
            return fjwt.getPool() == java.util.concurrent.ForkJoinPool.commonPool();
        }
        return false;
    }
}
