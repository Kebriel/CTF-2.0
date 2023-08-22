package kebriel.ctf.display.gui.menu.pages;

import kebriel.ctf.display.gui.component.Selectable;
import kebriel.ctf.display.gui.component.button.GUIButtonClearSelection;
import kebriel.ctf.display.gui.component.inventory.PagedGUI;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.item.CTFItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectablesPage extends GUIPage {

    private final Collection<? extends Selectable> contents;
    private final int size;
    private final Stat selectSlot;

    public SelectablesPage(CTFPlayer player, PagedGUI pgui, Stat selectSlot, int size, Collection<? extends Selectable> contents, int page, String name) {
        super(player, pgui, name, page);
        this.contents = contents;
        this.size = size;
        this.selectSlot = selectSlot;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Object[][] draw() {
        int rows = getSize()/9;
        Object[][] draw = new Object[rows][9];

        draw[0] = new Object[]{"-", "-", "-", "-", "C", "-", "-", "-", "-"};
        draw[rows-1] = new Object[]{"-", "-", "-", "-", "-", "-", "-", "-", "-"};
        for(int i = 1; i < rows-1; i++)
            draw[i] = new Object[]{"|", "*", "*", "*", "*", "*", "*", "*", "|"};

        return draw;

        /*
         * Drawing this, but dynamically
                {"-", "-", "-", "-", "-", "-", "-", "-", "-"},
                {"|", "*", "*", "*", "*", "*", "*", "*", "|"},
                {"|", "*", "*", "*", "*", "*", "*", "*", "|"},
                {"|", "*", "*", "*", "*", "*", "*", "*", "|"},
                {"|", "*", "*", "*", "*", "*", "*", "*", "|"},
                {"-", "-", "-", "-", "-", "-", "-", "-", "-"}
         *
         */
    }

    @Override
    public void setPlaceholders() {
        ItemStack borderGlass = CTFItem.newItem(Material.STAINED_GLASS_PANE).setData(getPagedGUI().glassColor()).build();
        setPlaceholder("-", borderGlass);
        setPlaceholder("|", borderGlass);

        List<Selectable> sorted = new ArrayList<>();
        for(Selectable s : contents)
            if(!s.mustBeUnlocked())
                sorted.add(s);
        for(Selectable s : contents)
            if(!sorted.contains(s))
                sorted.add(s);
        List<ItemStack> icons = new ArrayList<>();

        int elements = getPagedGUI().getElementsPerPage();

        // Account for pages
        for(int i = elements * getPage(); i < sorted.size() && i < elements; i++)
            icons.add(sorted.get(i).getMenuItem(getPlayer()));
        setPlaceholder("*", icons);

        // Clear selection
        setPlaceholder("C", new GUIButtonClearSelection(selectSlot, this).setActiveSlots(4));
    }
}
