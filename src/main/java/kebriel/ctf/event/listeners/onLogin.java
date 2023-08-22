package kebriel.ctf.event.listeners;

import kebriel.ctf.player.CTFPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class onLogin implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        CTFPlayer player = CTFPlayer.get(event.getPlayer());
        player.loggedOn();
    }
}
