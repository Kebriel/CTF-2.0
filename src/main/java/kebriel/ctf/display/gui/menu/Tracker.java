package kebriel.ctf.display.gui.menu;

import kebriel.ctf.display.gui.component.button.GUIButtonTrack;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.MenuItems;
import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;

public class Tracker extends GameGUI {

    public Tracker(CTFPlayer player) {
        super(player);
    }

    @Override
    public String getName() {
        return "Tracker";
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Object[][] draw() {
        return new Object[][]{
                {"-", "-", "-", "-", "-", "-", "-", "-", "-"},
                {"|", "*", "*", "B", "*", "R", "*", "*", "|"},
                {"-", "-", "-", "-", "-", "-", "-", "-", "-"}
        };
    }

    @Override
    public void setPlaceholders() {
        PlayerState state = getPlayer().getState();
        setPlaceholder("-", MenuItems.YELLOW_STAINED_GLASS.get());
        setPlaceholder("|", MenuItems.YELLOW_STAINED_GLASS.get());
        setPlaceholder("*", MenuItems.WHITE_STAINED_GLASS.get());
        setPlaceholder("B", new GUIButtonTrack(CTFItem.newItem(Material.BANNER).setName(Text.get().blue("Blue Flag")),
                state, this, TeamColor.BLUE).setActiveSlots(12));
        setPlaceholder("B", new GUIButtonTrack(CTFItem.newItem(Material.BANNER).setName(Text.get().red("Red Flag")),
                state, this, TeamColor.RED).setActiveSlots(14));
    }
}
