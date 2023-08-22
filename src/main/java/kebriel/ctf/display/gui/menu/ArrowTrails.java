package kebriel.ctf.display.gui.menu;

import kebriel.ctf.display.cosmetic.component.CosmeticRegistry;
import kebriel.ctf.display.cosmetic.component.CosmeticType;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.PagedGUI;
import kebriel.ctf.display.gui.component.inventory.Successor;
import kebriel.ctf.display.gui.menu.pages.SelectablesPage;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.ChatColor;

public class ArrowTrails extends PagedGUI implements Successor {

    public ArrowTrails(CTFPlayer player) {
        super(player);
    }

    @Override
    protected GameGUI createPage(CTFPlayer player, int page) {
        return new SelectablesPage(player, this, Stat.SELECTED_TRAIL, 27, CosmeticRegistry.get(CosmeticType.TRAIL), page, ChatColor.GOLD + "Arrow Trails");
    }

    @Override
    public int getElementsPerPage() {
        return 7;
    }

    @Override
    public int getTotalElementCount() {
        return CosmeticRegistry.get(CosmeticType.TRAIL).size();
    }

    @Override
    public byte glassColor() {
        return 2;
    }

    @Override
    public GameGUI getPrior() {
        return new Cosmetics(getPlayer());
    }
}
