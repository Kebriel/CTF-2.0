package kebriel.ctf.display.gui.menu;

import kebriel.ctf.ability.components.AbilityRegistry;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.PagedGUI;
import kebriel.ctf.display.gui.component.inventory.Successor;
import kebriel.ctf.display.gui.menu.pages.SelectablesPage;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;

public class Items extends PagedGUI implements Successor {

    public Items(CTFPlayer player) {
        super(player);
    }

    @Override
    protected GameGUI createPage(CTFPlayer player, int page) {
        return new SelectablesPage(player, this, Stat.SELECTED_EXTRA_ITEM1, 36, AbilityRegistry.get(AbilityType.ITEM), page, "Extra Items");
    }

    @Override
    public int getElementsPerPage() {
        return 14;
    }

    @Override
    public int getTotalElementCount() {
        return AbilityRegistry.get(AbilityType.ITEM).size();
    }

    @Override
    public byte glassColor() {
        return 4;
    }

    @Override
    public GameGUI getPrior() {
        return new Overview(getPlayer());
    }
}
