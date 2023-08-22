package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.display.gui.component.inventory.GameGUI;

public class GUIButtonPurchase extends GUIButton {

    public GUIButtonPurchase(Purchaseable purchase, GameGUI gui) {
        super(purchase::getMenuItem, gui, purchase::tryPurchase);
    }
}
