package kebriel.ctf.event.listeners;

import kebriel.ctf.event.async.AsyncPlayerMoveBlockEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class onMove implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to.getBlockX() != from.getBlockX() || to.getBlockY() != from.getBlockY() || to.getBlockZ() != from.getBlockZ())
            CTFEvent.fireEvent(new AsyncPlayerMoveBlockEvent(CTFPlayer.get(event.getPlayer()), to, from));
    }
}
