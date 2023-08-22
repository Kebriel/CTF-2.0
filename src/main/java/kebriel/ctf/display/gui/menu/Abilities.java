package kebriel.ctf.display.gui.menu;

import kebriel.ctf.ability.components.AbilityRegistry;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.inventory.Successor;
import kebriel.ctf.display.gui.menu.pages.SelectablesPage;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.PagedGUI;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;

public class Abilities extends PagedGUI implements Successor {

    private final Stat selectSlot;

    public Abilities(CTFPlayer player, Stat selectSlot) {
        super(player);
        this.selectSlot = selectSlot;
    }

    @Override
    protected GameGUI createPage(CTFPlayer player, int page) {
        return new SelectablesPage(player, this, selectSlot, 54, AbilityRegistry.get(AbilityType.ABILITY), page, "Abilities");
    }

    @Override
    public int getElementsPerPage() {
        return 42;
    }

    @Override
    public int getTotalElementCount() {
        return AbilityRegistry.get(AbilityType.ABILITY).size();
    }

    @Override
    public byte glassColor() {
        return 3;
    }

    @Override
    public GameGUI getPrior() {
        return new Overview(getPlayer());
    }
}
