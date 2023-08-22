package kebriel.ctf.event.listeners;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

public class onInventoryClick implements Listener {

	@EventHandler
	public void onInv(InventoryClickEvent e) {
		// Players are allowed to interact with their inventories (besides armor) in-game
		if(GameStage.IN_MAP.get() && e.getInventory() instanceof PlayerInventory && (e.getRawSlot() < 100 || e.getRawSlot() > 103))
			return;

		if(e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof GameGUI gui)
			gui.registerClick(e);

		e.setCancelled(true);
	}

}
