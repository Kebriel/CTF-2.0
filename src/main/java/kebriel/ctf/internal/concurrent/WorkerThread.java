package kebriel.ctf.internal.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerThread extends Thread {

    private volatile boolean waiting;
    private volatile boolean stopped;
    private volatile String info;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public WorkerThread(Runnable run) {
        super(run);
    }

    protected WorkerThread() {}

    @Override
    public void run() {
        while(!stopped && !Thread.interrupted()) {
            super.run();
        }
    }

    public synchronized void lazyWait() {
        if(waiting)
            return;
        try {
            waiting = true;
            wait();
        } catch(InterruptedException ignored) {
            interrupt();
        }
    }

    public synchronized void lazyWait(long seconds) {
        if(waiting)
            return;

        try {
            waiting = true;
            wait(seconds*1000);
        } catch(InterruptedException ignored) {
            interrupt();
        }
    }

    public synchronized void waitForInfo(String expectedMessage) {
        lock.lock();
        waiting = true;
        try {
            while(info == null || !info.equals(expectedMessage)) {
                try {
                    condition.await();
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            lock.unlock();
        }

        info = null;
    }

    public synchronized String waitForInfo() {
        lock.lock();
        waiting = true;
        try {
            while(info == null) {
                try {
                    condition.await();
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            lock.unlock();
        }

        // Clear for next waiting operation
        String result = info;
        info = null;
        return result;
    }

    public synchronized void waitForInfoTimed(String expectedMessage, long seconds) {
        lock.lock();
        waiting = true;
        try {
            try {
                AsyncExecutor.doAfterDelay(condition::signal, seconds);
                // Will wait until condition is signaled either by time elapsing, or expected info being sent
                condition.await();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } finally {
            lock.unlock();
        }
    }

    public synchronized void sendInfo(String message) {
        if(!lock.isLocked())
            return;

        this.info = message;
        condition.signal();
    }

    public synchronized void safeNotify() {
        if(!waiting)
            return;

        notify();
        waiting = false;
    }

    public synchronized void safeJoin() {
        try {
            join();
        } catch(InterruptedException ignored) {
            interrupt();
        }
    }

    /**
     * Returns the message passed to this thread
     */
    public String getCachedInfo() {
        return info;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stopGracefully() {
        stopped = true;
        interrupt();
    }
}
