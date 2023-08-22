package kebriel.ctf.internal.concurrent;

import kebriel.ctf.internal.sql.WrappedStatement;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncExecutor implements Runnable {

    private static final ExecutorService executorService;
    private static final ScheduledExecutorService scheduledService;

    static {
        executorService = Executors.newCachedThreadPool(new WorkerThreadFactory());
        scheduledService = Executors.newScheduledThreadPool(6, new WorkerThreadFactory());
    }

    private ScheduledFuture<?> instance;
    private Consumer<AsyncExecutor> task;

    public AsyncExecutor(Consumer<AsyncExecutor> task) {
        this.task = task;
    }

    public AsyncExecutor() {}

    // ### Instance Methods ###
    @Override
    public void run() {
        if(task != null)
            task.accept(this);
    }

    public synchronized AsyncExecutor setTask(Consumer<AsyncExecutor> task) {
        this.task = task;
        return this;
    }

    /**
     * Executes whatever bit of code is defined in the lambda a
     * single time
     */
    public synchronized AsyncExecutor doOnce() {
        if(task == null)
            return this;

        executorService.submit(this);
        return this;
    }

    public synchronized AsyncExecutor doRepeating(long delay, long period, TimeUnit time) {
        if(task == null)
            return this;

        instance = scheduledService.scheduleAtFixedRate(this, delay, period, time);
        return this;
    }

    /**
     * @param delay the delay, in seconds, until this task should be run
     *              -- will not occupy a thread while waiting, only when
     *              executing
     */
    public synchronized AsyncExecutor doAfterDelay(long delay, TimeUnit time) {
        if(task == null)
            return this;
        instance = scheduledService.schedule(this, delay, time);
        return this;
    }

    /**
     * Cancels this task, if it can be cancelled
     * @param interrupt whether this task should immediately
     *                  terminate, or finish what it was doing
     *                  and then terminate
     */
    public synchronized void terminate(boolean interrupt) {
        if(instance == null)
            return;
        instance.cancel(interrupt);
    }

    // ### Static Methods ###

    public static boolean isWorkerThread() {
        return Thread.currentThread() instanceof WorkerThread;
    }

    public static void doTask(Runnable task) {
        executorService.submit(task);
    }

    public static void doAfterDelay(Runnable task, long delay, TimeUnit time) {
        scheduledService.schedule(task, delay, time);
    }

    public static void doTimer(Runnable task, long timer) {
        scheduledService.scheduleAtFixedRate(task, 0, timer, TimeUnit.SECONDS);
    }

    public static <T> Future<T> doWithFuture(Supplier<T> task) {
        return executorService.submit(task::get);
    }

    /**
     * Helps to avoid unnecessary context switching
     */
    public static void doAsyncIfNot(Runnable task) {
        if(!isWorkerThread()) {
            doTask(task);
            return;
        }

        task.run();
    }

    public static WrappedStatement doSQLSingle(WrappedStatement statement) {
        doTask(statement::execute);
        return statement;
    }

    public static void shutdown() {
        executorService.shutdown();
        scheduledService.shutdown();
    }

}
