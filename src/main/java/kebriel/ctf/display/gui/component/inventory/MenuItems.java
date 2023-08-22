package kebriel.ctf.display.gui.component.inventory;

import kebriel.ctf.player.item.CTFItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MenuItems {

    // TODO -- Add more

    WHITE_STAINED_GLASS(Material.STAINED_GLASS_PANE, 0, " "),
    YELLOW_STAINED_GLASS(Material.STAINED_GLASS_PANE, 4, " "),
    BLUE_STAINED_GLASS(Material.STAINED_GLASS_PANE, 11, " ")
    //...
    ;

    private final Material mat;
    private final int id;
    private final String name;

    MenuItems(Material mat, int id, String name) {
        this.mat = mat;
        this.id = id;
        this.name = name;
    }

    public Material getMaterial() {
        return mat;
    }

    public byte getID() {
        return (byte) id;
    }

    public String getName() {
        return name;
    }

    public ItemStack get() {
        return CTFItem.newItem(mat).setName(name).setData(getID()).build();
    }
}
