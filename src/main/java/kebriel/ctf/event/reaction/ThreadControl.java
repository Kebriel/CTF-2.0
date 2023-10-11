package kebriel.ctf.event.reaction;

import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.Bukkit;

public enum ThreadControl {

    MAIN, BUKKIT_ASYNC, ASYNC;

    public void accept(Runnable run) {
        switch(this) {
            case MAIN -> {
                MinecraftUtil.doSyncIfNot(run);
                return;
            }
            case BUKKIT_ASYNC -> {
                if(!AsyncExecutor.isWorkerThread() && !Bukkit.isPrimaryThread()) {
                    MinecraftUtil.runBukkitAsync(run);
                    return;
                }
            }
            case ASYNC -> {
                AsyncExecutor.doAsyncIfNot(run);
                return;
            }
        }
        run.run();
    }
}
