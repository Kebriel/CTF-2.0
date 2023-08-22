package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.Successor;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;

import kebriel.ctf.player.item.CTFItem;

public class GUIButtonPrior extends GUIButton {
	
	public GUIButtonPrior(GameGUI gui) {
		super(CTFItem.newItem(Material.WORKBENCH).setName(Text.get().yellow("Go back").toString())
				.addLore(Text.get().red("Click to return to the previous menu").toString()).build(), gui, player -> {
					if(gui instanceof Successor g)
						player.openMenu(g.getPrior());
				});
	}

}
