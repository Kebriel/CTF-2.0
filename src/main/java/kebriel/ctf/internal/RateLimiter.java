package kebriel.ctf.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RateLimiter {

    private final Semaphore semaphore;
    private final Queue<Long> requestTimes;
    private final int maxRequests;
    private final long timeWindow;

    public RateLimiter(int maxRequests, long timeWindow, TimeUnit unit) {
        this.maxRequests = maxRequests;
        this.timeWindow = unit.toMillis(timeWindow);
        this.semaphore = new Semaphore(maxRequests);
        this.requestTimes = new ConcurrentLinkedQueue<>();
    }

    public boolean tryAcquire() {
        long currentMillis = System.currentTimeMillis();

        while(!requestTimes.isEmpty() && currentMillis - requestTimes.peek() > timeWindow) {
            requestTimes.poll();
            semaphore.release();
        }

        if (semaphore.tryAcquire()) {
            requestTimes.add(currentMillis);
            return true;
        } else {
            return false;
        }
    }

    public boolean atCapacity() {
        return semaphore.availablePermits() == 0;
    }
}
