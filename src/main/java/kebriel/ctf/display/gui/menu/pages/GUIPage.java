package kebriel.ctf.display.gui.menu.pages;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.PagedGUI;
import kebriel.ctf.player.CTFPlayer;

public abstract class GUIPage extends GameGUI {

    private final int page;
    private final PagedGUI pgui;
    private final String name;

    public GUIPage(CTFPlayer player, PagedGUI paged, String name, int page) {
        super(player);
        pgui = paged;
        this.page = page;
        this.name = name;
    }

    public PagedGUI getPagedGUI() {
        return pgui;
    }

    public int getPage() {
        return page;
    }

    @Override
    public String getName() {
        return name + " Page " + (page + 1);
    }
}
