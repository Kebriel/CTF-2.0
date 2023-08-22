package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GUIButtonRun extends GUIButton {

    public GUIButtonRun(ItemStack item, GameGUI gui, Consumer<CTFPlayer> func) {
        super(item, gui, func);
    }
}
