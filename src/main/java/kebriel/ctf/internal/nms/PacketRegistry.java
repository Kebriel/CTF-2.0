package kebriel.ctf.internal.nms;

import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.ThreadControl;
import kebriel.ctf.event.async.AsyncPlayerMoveChunkEvent;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class, in conjunction with RenderablePacket, allows for certain 'constant' packets
 * (such as a packet-rendered entity) to be reliably rendered for players. This is
 * only if players are within a relevant proximity of the packet -- and this is accomplished
 * without needing to do constant checks or blind re-sending of packets which can
 * be very taxing on performance, and be far more likely to result in certain bugs.
 */
public class PacketRegistry implements EventReactor {

    static {
        EventReaction.register(new PacketRegistry());
    }

    /*
     * Set of CTFPlayer represents players who are currently set as seeing the given packet
     *
     * Weak references in Set allow for auto-clearing once a player is reaped from the main
     * CTFPlayer cache
     */
    private static final Map<GamePacket, CopyOnWriteArraySet<WeakReference<CTFPlayer>>> cache = Collections.synchronizedMap(new HashMap<>());

    /**
     * Raw render method with failsafes. Will create an empty Set for players
     * as the Map's value if one doesn't exist, and will add the player to
     * the Set and render the packet for them, if they don't already have it
     * rendered.
     * @param packet
     * @param player
     */
    private static void renderFor(GamePacket packet, CTFPlayer player) {
        registerPacket(packet);
        if(!cache.containsKey(packet)) // Unsuccessfully registered
            return;
        cache.compute(packet, (key, value) -> {
            if(value == null) value = new CopyOnWriteArraySet<>();
            if(!cacheContains(value, player)) {
                value.add(player.asWeakRef());
                packet.sendFor(player);
            }

            return value;
        });
    }

    private static boolean cacheContains(Set<WeakReference<CTFPlayer>> set, CTFPlayer player) {
        for(WeakReference<CTFPlayer> ref : set)
            return ref.refersTo(player);
        return false;
    }

    private static void unrenderFor(GamePacket packet, CTFPlayer player) {
        unregisterPacket(packet);
        cache.computeIfPresent(packet, (key, value) -> {
            if(cacheContains(value, player)) {
                if(packet instanceof Revertable r)
                    r.undo();
                value.remove(player.asWeakRef());
            }
            return value;
        });
    }

    private static void updateFor(GamePacket packet, CTFPlayer player) {
        if(!cache.containsKey(packet))
            return;

        cache.computeIfPresent(packet, (key, value) -> {
            // Refresh packet for those who should see
            // Those who should no longer will only have it unrendered/cleared
            unrenderFor(packet, player);
            if(packet.shouldSend(player)) {
                renderFor(packet, player);
            }
            return value;
        });
    }

    /**
     * Updates the render of any/all packets that have been rendered for this player
     */
    public static void updateAll(CTFPlayer player) {
        AsyncExecutor.doAsyncIfNot(() -> {
            for(GamePacket packet : getAllRenderedFor(player)) {
                updateFor(packet, player);
            }
        });
    }

    /**
     * Unrenders any/all packets that have been rendered for this player
     */
    public static void unrenderAllFor(CTFPlayer player) {
        AsyncExecutor.doAsyncIfNot(() -> {
            for(GamePacket packet : getAllRenderedFor(player)) {
                unrenderFor(packet, player);
            }
        });
    }

    /**
     * Returns a new Set containing any/all renderable packets that have
     * been rendered for this player
     * @return returns a HashSet<RenderablePacket>, this Set is just a copy
     * and also is not necessarily thread safe
     */
    public static Set<GamePacket> getAllRenderedFor(CTFPlayer player) {
        Set<GamePacket> result = new HashSet<>();
        for(Map.Entry<GamePacket, CopyOnWriteArraySet<WeakReference<CTFPlayer>>> entry : cache.entrySet()) {
            if(entry.getValue().contains(player.asWeakRef())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Renders the given packet for any number of players
     * @param packet the packet to render, if it hasn't been already
     * @param players NMSPlayer varargs
     */
    public static void render(GamePacket packet, CTFPlayer... players) {
        AsyncExecutor.doAsyncIfNot(() -> {
            for(CTFPlayer player : players) {
                renderFor(packet, player);
            }
        });
    }

    /**
     * Unrenders the given packet for any number of players
     * @param packet the packet to unrender, if it hasn't been already
     * @param players NMSPlayer varargs
     */
    public static void unrender(GamePacket packet, CTFPlayer... players) {
        AsyncExecutor.doAsyncIfNot(() -> {
            for(CTFPlayer player : players) {
                unrenderFor(packet, player);
            }
        });
    }

    public static void unrender(GamePacket packet, Collection<WeakReference<CTFPlayer>> players) {
        AsyncExecutor.doAsyncIfNot(() -> {
            for(WeakReference<CTFPlayer> player : players) {
                unrenderFor(packet, player.get());
            }
        });
    }

    /**
     * Updates the render of the given packet for any number of players
     * @param packet the packet to update, if it hasn't been already
     * @param players NMSPlayer varargs
     */
    public static void update(GamePacket packet, CTFPlayer... players) {
        AsyncExecutor.doAsyncIfNot(() -> {
            for(CTFPlayer player : players) {
                updateFor(packet, player);
            }
        });
    }

    /**
     * Utility method to make sure all get() operations executed against the
     * cache are safe from NullPointerExceptions
     * @param packet the packet whose entry you want to get
     * @return returns a functionally thread-safe reference to the Set of players
     * who have had this packet rendered for them
     */
    private static Set<WeakReference<CTFPlayer>> safeGet(GamePacket packet) {
        if(cache.get(packet) == null) cache.put(packet, new CopyOnWriteArraySet<>());
        return cache.get(packet);
    }

    /**
     * Fully renders the packet to all players who are marked as being intended
     * to receive it
     * @param packet the RenderablePacket to render
     */
    protected static void renderThis(GamePacket packet) {
        render(packet, JavaUtil.typeArray(packet.getReceivers(), CTFPlayer.class));
    }

    /**
     * Fully unrenders the packet to all players who are currently seeing it
     * @param packet the RenderablePacket to unrender
     */
    public static void derenderThis(GamePacket packet) {
        // Clears all players who see the packet, in case some non-receivers still see it
        unrender(packet, safeGet(packet));
    }

    public static void updateThis(GamePacket packet) {
        update(packet, JavaUtil.typeArray(CTFPlayer.getAllOnline(), CTFPlayer.class));
    }

    public static void refreshThis(GamePacket packet) {
        derenderThis(packet);
        renderThis(packet);
    }

    /**
     * @param packet the packet to check
     * @return returns whether this packet is currently rendered for any players
     */
    public static boolean isRendered(GamePacket packet) {
        return cache.get(packet) != null && !cache.get(packet).isEmpty();
    }

    /**
     * Registers, but does not render, this packet in the registry, if it
     * hasn't been registered already
     */
    private static void registerPacket(GamePacket packet) {
        if(packet.getPolicy() == Policy.SEND_ONLY)
            return;

        cache.computeIfAbsent(packet, key -> new CopyOnWriteArraySet<>());
    }

    /**
     * Unregisters AND unrenders this packet from the registry, if it
     * exists in the registry
     */
    private static void unregisterPacket(GamePacket packet) {
        if(cache.containsKey(packet)) {
            derenderThis(packet);
            cache.remove(packet);
        }
    }

    private static void updateLocationals(CTFPlayer moved) {
        MinecraftUtil.ensureAsync();
        // Do this better? Performance-wise
        for(GamePacket packet : cache.keySet())
            if(packet.getPolicy() == Policy.LOCATIONAL)
                updateThis(packet);
    }

    private static void updateStatics(CTFPlayer player) {
        MinecraftUtil.ensureAsync();

        // Note: player should be added as a receiver for certain group-specific packets BEFORE this is run
        for(GamePacket packet : cache.keySet())
            if(packet.getPolicy() == Policy.STATIC && (packet.getReceivers().contains(player) || packet.isUbiquitous()))
                renderFor(packet, player);
    }

    private static void updateReceiverLists() {
        for(GamePacket packet : cache.keySet())
            if(packet.shouldCheckReceivers())
                packet.updateReceivers();
    }

    @EventReact(thread = ThreadControl.ASYNC)
    public void onChunkMove(AsyncPlayerMoveChunkEvent event) {
        updateLocationals(event.getPlayer());
    }

    @EventReact(thread = ThreadControl.ASYNC)
    public void onJoin(PlayerJoinEvent event) {
        CTFPlayer player = CTFPlayer.get(event.getPlayer());
        updateReceiverLists(); // This is run after a player joining mid-game is sorted to a team, as per react priority
        updateLocationals(player);
        updateStatics(player);
    }

    @EventReact(thread = ThreadControl.ASYNC)
    public void onLeave(PlayerQuitEvent event) {
        CTFPlayer player = CTFPlayer.get(event.getPlayer());
        // Remove player from any packet caches that they're in
        for(GamePacket pack : getAllRenderedFor(player)) {
            cache.compute(pack, (key, value) -> {
                value.remove(player);
                return value;
            });
        }
    }
}
