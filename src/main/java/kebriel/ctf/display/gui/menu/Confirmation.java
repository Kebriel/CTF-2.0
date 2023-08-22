package kebriel.ctf.display.gui.menu;

import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.display.gui.component.Unlockable;
import kebriel.ctf.display.gui.component.button.GUIButtonRun;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.item.CTFItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Confirmation extends GameGUI {

    private final Purchaseable purchase;
    private final GameGUI previousGUI;

    public Confirmation(CTFPlayer player, GameGUI previousGUI, Purchaseable purchase) {
        super(player);
        this.purchase = purchase;
        this.previousGUI = previousGUI;
    }

    @Override
    public String getName() {
        return "Purchase this?";
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Object[][] draw() {
        return new Object[][]{
                {"Y", "Y", "Y", "Y", "-", "N", "N", "N", "N"},
                {"Y", "Y", "Y", "Y", "-", "N", "N", "N", "N"},
                {"Y", "Y", "Y", "Y", "-", "N", "N", "N", "N"}
        };
    }

    @Override
    public void setPlaceholders() {
        setPlaceholder("-", new ItemStack(Material.STAINED_GLASS_PANE));
        setPlaceholder("Y", new GUIButtonRun(CTFItem.newItem(Material.STAINED_CLAY).setData((byte)5).build(),
                this,
                player -> {
            purchase.purchase(player);
            getPlayer().openMenu(previousGUI);
                }).setActiveSlots(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21));
        setPlaceholder("N", new GUIButtonRun(CTFItem.newItem(Material.STAINED_CLAY).setData((byte)14).build(),
                this,
                player -> player.openMenu(previousGUI)).setActiveSlots(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26));
    }
}
