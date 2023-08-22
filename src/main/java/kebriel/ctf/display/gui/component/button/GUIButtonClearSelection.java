package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class GUIButtonClearSelection extends GUIButton {
	
	public GUIButtonClearSelection(Stat selectSlot, GameGUI gui) {
		super(CTFItem.newItem(Material.HOPPER).setName(ChatColor.RED + "Clear Selection").addLore(ChatColor.YELLOW + "Click to clear your current selection").build(), gui, player -> {
            if(player.getSelected(selectSlot) != null) {
                player.setSelected(null, selectSlot);
                player.play(GameSound.MENU_YES);
                player.send(GameMessage.MENU_SELECTION_WIPED);
                player.refreshMenu();
            }else {
                player.play(GameSound.MENU_NO);
                player.send(GameMessage.MENU_SELECTION_WIPE_FAILURE);
            }
        });
	}

}
