package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.display.gui.component.inventory.GUIBase;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import org.bukkit.inventory.ItemStack;

public class GUIButtonPortal extends GUIButton {

	public GUIButtonPortal(ItemStack stack, GameGUI thisGUI, GUIBase open) {
		super(stack, thisGUI, player -> player.openMenu(open));
	}
}
