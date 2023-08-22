package kebriel.ctf.event.listeners;

import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.event.async.AsyncPlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import kebriel.ctf.player.CTFPlayer;

public class onLethalDamage implements Listener {

	public void onLethalDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player p && (p.getHealth()-e.getFinalDamage()) <= 0) {
			CTFEvent.fireEvent(new AsyncPlayerDeathEvent(CTFPlayer.get(p), e.getFinalDamage(), e.getCause()));
			e.setCancelled(true);
		}
	}

}
