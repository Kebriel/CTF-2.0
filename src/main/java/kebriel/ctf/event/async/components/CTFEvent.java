package kebriel.ctf.event.async.components;

import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class CTFEvent extends Event {

    private CTFPlayer player;

    private static final HandlerList handlers = new HandlerList();

    public static void fireEvent(CTFEvent event) {
        if(event instanceof AsyncEvent async)
            fireEventAsync(async);

        // Make sure events are always called on the main thread
        if(!MinecraftUtil.isMainThread()) {
            MinecraftUtil.runOnMainThread(() -> Bukkit.getServer().getPluginManager().callEvent(event));
            return;
        }

        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    /**
     * For custom events that don't need to be passed through
     * Bukkit's system whatsoever
     * @param event
     */
    private static void fireEventAsync(AsyncEvent event) {
        EventReaction.processEvent(event);
    }

    public CTFPlayer getPlayer() {
        return player;
    }

    public CTFEvent(CTFPlayer player) {
        this.player = player;
    }

    public CTFEvent() {}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
