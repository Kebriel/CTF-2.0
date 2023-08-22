package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.PagedGUI;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import kebriel.ctf.player.item.CTFItem;

public abstract class GUIButtonPage extends GUIButton {
	
	private GUIButtonPage(ItemStack stack, PagedGUI gui, GameGUI page, boolean type) {
		super(stack, page, player -> {
					if(gui.getCurrent() < 0 || !gui.hasNext()) return;
					gui.newPage(type);
				});
	}

	public static class Forward extends GUIButtonPage {

		public Forward(PagedGUI gui, GameGUI page) {
			super(CTFItem.newItem(Material.ARROW).setName(Text.get().yellow("Next Page").toString()).addLore(Text.get().text("Click to go to the next page").toString()).build(), gui, page, true);
		}
	}

	public static class Back extends GUIButtonPage {

		public Back(PagedGUI gui, GameGUI page) {
			super(CTFItem.newItem(Material.ARROW).setName(Text.get().yellow("Previous Page").toString()).addLore(Text.get().text("Click to go to the previous page").toString()).build(), gui, page, false);
		}
	}

}
