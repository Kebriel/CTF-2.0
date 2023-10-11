package kebriel.ctf.event.reaction;

import kebriel.ctf.CTFMain;
import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventReaction implements Listener {

    private static final CTFMain main = CTFMain.instance;

    private static final Map<ReactPriority, List<EventMethod>> reactCache = new HashMap<>();


    static {
        for(ReactPriority prio : ReactPriority.values())
            reactCache.put(prio, new ArrayList<>());
    }

    public static void register(EventReactor eventReactor) {
        // If this isn't part of the initial plugin loading progress, take responsibility off of main thread
        if(main.isEnabled()) {
            AsyncExecutor.doTask(() -> loadEventMethods(eventReactor));
        }else{
            loadEventMethods(eventReactor);
        }
    }

    private static void loadEventMethods(EventReactor reactor) {
        for(Method method : reactor.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(EventReact.class)) {
                if(method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    EventReact a = method.getAnnotation(EventReact.class);
                    reactCache.get(a.priority()).add(new EventMethod(reactor, method, (Class<? extends Event>) method.getParameterTypes()[0], a));
                }else{
                    throw new IllegalArgumentException("EventReact annotation must be placed above a method with a single Event parameter");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(Event e) {
        processEvent(e);
    }

    public static <T extends Event> void processEvent(T event) {
        if(event instanceof AsyncEvent async) {
            async.preProcess();
            acceptEvent(event);
            async.postProcess();
            return;
        }
        acceptEvent(event);
    }

    private static <T extends Event> void acceptEvent(T event) {
        AsyncExecutor.doAsyncIfNot(() -> { // All iteration done off main thread for best performance
            for(ReactPriority prio : ReactPriority.values())
                for(EventMethod method : reactCache.get(prio)) {
                    if(method.hasExpired())
                        continue;
                    if(method.getEventType().equals(event.getClass()))
                        method.run(event);
                }
        });
    }

    /*
     * Use and explanation can be found in BackgroundProcess.java
     */
    public static void purgeExpired() {
        for(ReactPriority prio : ReactPriority.values())
            reactCache.get(prio).removeIf(EventMethod::hasExpired);
    }

    protected static class EventMethod {

        private final WeakReference<EventReactor> instance;
        private final Method m;
        private final Class<? extends Event> type;
        private final ThreadControl thread;
        private final GameStage[] allowedPhase;

        public EventMethod(EventReactor instance, Method m, Class<? extends Event> type, EventReact react) {
            this.instance = new WeakReference<>(instance);
            this.m = m;
            this.type = type;

            thread = react.thread();
            allowedPhase = react.allowedWhen();
        }

        public synchronized <T extends Event> void run(T event) {
            for(GameStage stage : allowedPhase)
                if(!stage.get())
                    return;

            if(event instanceof AsyncEvent) {
                AsyncExecutor.doAsyncIfNot(() -> {
                    try {
                        m.invoke(instance, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
                return;
            }
            thread.accept(() -> {
                try {
                    m.invoke(instance, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public Class<? extends Event> getEventType() {
            return type;
        }

        public ThreadControl getThread() {
            return thread;
        }

        public GameStage[] getAllowedPhases() {
            return allowedPhase;
        }

        public boolean isAsync() {
            return thread == ThreadControl.ASYNC;
        }

        public boolean hasExpired() {
            return instance.get() == null;
        }
    }
}
