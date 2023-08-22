package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GUIButton {

	private int[] slot;
	private final Function<CTFPlayer, ItemStack> pendingItem;
	private ItemStack item;
	private final GameGUI gui;
	private final Consumer<CTFPlayer> func;

	public GUIButton(Function<CTFPlayer, ItemStack> pendingItem, GameGUI gui, Consumer<CTFPlayer> func) {
		this.pendingItem = pendingItem;
		this.gui = gui;
		this.func = func;
	}

	public GUIButton(ItemStack item, GameGUI gui, Consumer<CTFPlayer> func) {
		this((Function<CTFPlayer, ItemStack>) null, gui, func);
		this.item = item;
	}
	
	public void setItem(CTFPlayer player) {
		ItemStack set = pendingItem == null ? item : pendingItem.apply(player);
		for(int i : slot)
			gui.getInventory().setItem(i, set);
	}

	public GUIButton setActiveSlots(int... slots) {
		slot = slots;
		return this;
	}

	public int[] getActiveSlots() {
		return slot;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public GameGUI getGUI() {
		return gui;
	}
	
	public void onClick(InventoryClickEvent event) {
		func.accept(CTFPlayer.get((Player) event.getWhoClicked()));
	}

}
