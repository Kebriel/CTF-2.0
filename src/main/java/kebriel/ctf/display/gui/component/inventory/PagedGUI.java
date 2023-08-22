package kebriel.ctf.display.gui.component.inventory;

import kebriel.ctf.display.gui.component.button.GUIButtonPage;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class PagedGUI extends GUIBase {

    private final List<GameGUI> pages;
    private final CTFPlayer player;
    private int currentPage;

    public PagedGUI(CTFPlayer player) {
        this.player = player;
        pages = new ArrayList<>();

        int totalPages = Math.max(getTotalElementCount()/getElementsPerPage(), 1);

        for(int i = 0; i < totalPages; i++) {
            GameGUI page = createPage(player, i);
            if (i != 0) // Not first page
                pages.get(i).putButton(new GUIButtonPage.Back(this, page).setActiveSlots(getPageSize() - 6));
            if (i != totalPages - 1) // Not last page
                page.putButton(new GUIButtonPage.Forward(this, page).setActiveSlots(getPageSize() - 4));
            pages.add(page);
        }
    }

    protected abstract GameGUI createPage(CTFPlayer player, int page);
    public abstract int getElementsPerPage();
    public abstract int getTotalElementCount();
    public abstract byte glassColor();

    @Override
    public void open() {
        currentPage = 0;
        player.openMenu(pages.get(getCurrent()));
    }

    @Override
    public void refresh(CTFPlayer player) {
        Player p = player.getBukkitPlayer();
        if(pages.get(getCurrent()).getClass().isInstance(p.getOpenInventory().getTopInventory().getHolder())) {
            player.closeMenu();
            player.openMenu(pages.get(getCurrent()));
        }
    }

    public void newPage(boolean forward) {
        if(currentPage < pages.size() && currentPage > 0)
            player.openMenu(pages.get(currentPage+=forward ? 1 : -1));
    }

    public int getPageSize() {
        return pages.get(currentPage).getSize();
    }

    public boolean hasNext() {
        return currentPage < (pages.size()-1);
    }

    public int getCurrent() {
        return currentPage;
    }

    public CTFPlayer getPlayer() {
        return player;
    }
}
