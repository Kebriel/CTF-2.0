package kebriel.ctf.display.gui.component.inventory;

import kebriel.ctf.display.gui.component.PlaceholderValue;
import kebriel.ctf.display.gui.component.PlaceholderValue.MutablePlaceholderValue;
import kebriel.ctf.display.gui.component.button.GUIButton;
import kebriel.ctf.display.gui.component.button.GUIButtonPrior;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GameGUI extends GUIBase implements InventoryHolder {

    private final Map<Object, PlaceholderValue> placeholders;
    private final List<GUIButton> buttons;
    private final CTFPlayer player;
    private final Inventory inventory;

    {
        placeholders = new HashMap<>();
        buttons = new ArrayList<>();
    }

    public GameGUI(CTFPlayer player) {
        this.player = player;

        setPlaceholders();

        inventory = Bukkit.createInventory(this, getSize(), getName());
        Object[][] draw = draw();
        for(int i = 0; i < draw.length; i++) {
            for(int j = 0; j < draw[i].length; j++) {
                ItemStack fill = placeholders.get(draw[i][j]).getItem();
                inventory.setItem((9*i)+j, fill != null ? fill : new ItemStack(Material.AIR));
            }
        }

        // Add 'previous menu' button in center of final row, if applicable to this menu
        if(this instanceof Successor)
            putButton(new GUIButtonPrior(this).setActiveSlots(getSize() - 5));
    }

    public CTFPlayer getPlayer() {
        return player;
    }

    public abstract String getName();
    public abstract int getSize();
    public abstract Object[][] draw();
    public abstract void setPlaceholders();

    public void setPlaceholder(Object ph, ItemStack value) {
        placeholders.put(ph, new PlaceholderValue(value));
    }

    public void setPlaceholder(Object ph, List<ItemStack> value) {
        placeholders.put(ph, new MutablePlaceholderValue(value));
    }

    public void setPlaceholder(Object ph, GUIButton button) {
        placeholders.put(ph, new PlaceholderValue(button.getItem()));
        buttons.add(button);
    }

    public void set(int index, ItemStack value) {
        inventory.setItem(index, value);
    }

    public void putButton(GUIButton button) {
        for(int i : button.getActiveSlots())
            inventory.setItem(i, button.getItem());
        buttons.add(button);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void open() {
        player.getBukkitPlayer().openInventory(getInventory());
    }

    @Override
    public void refresh(CTFPlayer player) {
        Player p = player.getBukkitPlayer();
        if(this.getClass().isInstance(p.getOpenInventory().getTopInventory().getHolder())) {
            player.closeMenu();
            player.openMenu(this);
        }
    }

    public void registerClick(InventoryClickEvent event) {
        for(GUIButton button : buttons)
            for(int slot : button.getActiveSlots())
                if(slot == event.getSlot()) button.onClick(event);
    }

}
