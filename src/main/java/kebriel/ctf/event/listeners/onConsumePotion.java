package kebriel.ctf.event.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import kebriel.ctf.CTFMain;

public class onConsumePotion implements Listener {
	
	@EventHandler
	public void onConsume(PlayerItemConsumeEvent e) {
		Player p = e.getPlayer();
		if(e.getItem().getType() == Material.POTION) {
			new BukkitRunnable() {

				@Override
				public void run() {
					if(p.isOnline() && p.isValid())
						p.getInventory().remove(Material.GLASS_BOTTLE);
				}
				
			}.runTaskLater(CTFMain.instance, 2);
		}
	}

}
