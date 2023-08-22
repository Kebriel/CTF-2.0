package kebriel.ctf.internal.concurrent;

import java.util.concurrent.ThreadFactory;

public class WorkerThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new WorkerThread(r);
    }
}
