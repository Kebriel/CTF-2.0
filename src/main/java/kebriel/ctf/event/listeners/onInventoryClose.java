package kebriel.ctf.event.listeners;

import kebriel.ctf.player.CTFPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class onInventoryClose implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        CTFPlayer player = CTFPlayer.get((Player) event.getPlayer());
        if(player.getCurrentMenu() == null)
            return;
        if(event.getInventory().getHolder().getClass().isInstance(player.getCurrentMenu()))
            player.setMenuAsClosed();
    }
}
