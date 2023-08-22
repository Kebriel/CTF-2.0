package kebriel.ctf.internal.concurrent;

import java.util.concurrent.SynchronousQueue;

public class SequentialThread extends WorkerThread {

    private final SynchronousQueue<Runnable> tasks = new SynchronousQueue<>();

    @Override
    public void run() {
        while(!interrupted() && !isWaiting() && !isStopped()) {
            try {
                Runnable r = tasks.take(); // Will wait here for new task
                r.run();
            } catch(InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }

    public void give(Runnable task) {
        try {
            tasks.put(task);
        } catch(InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
