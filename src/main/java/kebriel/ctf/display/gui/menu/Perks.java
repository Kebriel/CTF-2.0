package kebriel.ctf.display.gui.menu;

import kebriel.ctf.ability.components.AbilityRegistry;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.PagedGUI;
import kebriel.ctf.display.gui.component.inventory.Successor;
import kebriel.ctf.display.gui.menu.pages.SelectablesPage;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;

public class Perks extends PagedGUI implements Successor {

    private final Stat selectSlot;

    public Perks(CTFPlayer player, Stat selectSlot) {
        super(player);
        this.selectSlot = selectSlot;
    }

    @Override
    protected GameGUI createPage(CTFPlayer player, int page) {
        return new SelectablesPage(player, this, selectSlot, 36, AbilityRegistry.get(AbilityType.PERK), page, "Perks");
    }

    @Override
    public int getElementsPerPage() {
        return 14;
    }

    @Override
    public int getTotalElementCount() {
        return AbilityRegistry.get(AbilityType.PERK).size();
    }

    @Override
    public byte glassColor() {
        return 14;
    }

    @Override
    public GameGUI getPrior() {
        return new Overview(getPlayer());
    }
}
