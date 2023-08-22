package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.ability.components.ComplexDeselect;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.Unlockable;
import kebriel.ctf.display.gui.component.Selectable;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.GameMessage;
import org.bukkit.Sound;

public class GUIButtonSelect extends GUIButton {

	public GUIButtonSelect(Selectable subject, Stat selectSlot, GameGUI gui) {
		super(subject::getMenuItem, gui, player -> {
			boolean alreadySelected = player.getIsSelected(subject.getID());
			if(alreadySelected) {
				player.send(GameMessage.MENU_ALREADY_SELECTED);
				player.play(GameSound.MENU_NO);
				return;
			}

			if(!subject.mustBeUnlocked() || (subject instanceof Unlockable a && a.isUnlocked(player))) {
				if(player.getSelected(selectSlot) instanceof ComplexDeselect de && GameStage.IN_GAME.get())
					de.deselect(player);

				subject.select(player, selectSlot);
				return;
			}

			// Failsafe, all purchaseables should use GUIButtonPurchase
			if(subject instanceof Purchaseable p)
				p.tryPurchase(player);
		});
	}
}
