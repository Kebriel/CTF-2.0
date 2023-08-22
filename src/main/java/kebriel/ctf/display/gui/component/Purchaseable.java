package kebriel.ctf.display.gui.component;

import kebriel.ctf.display.gui.menu.Confirmation;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;

public interface Purchaseable extends Unlockable {

    int getCost();

    @Override
    default Text getSubtext() {
        return Text.get().red("Click to purchase for ").gold(getCost() + "g");
    }

    default boolean tryPurchase(CTFPlayer player) {
        if(((int)player.getStat(Stat.GOLD)) < getCost()) {
            player.send(GameMessage.MENU_YOU_CANNOT_AFFORD_THIS);
            player.play(GameSound.MENU_NO);
            return false;
        }else {
            player.openMenu(new Confirmation(player, player.getCurrentMenu(), this));
            return true;
        }
    }

    default void purchase(CTFPlayer player) {
        player.subtractFromStat(Stat.GOLD, getCost());
        player.unlock(getUnlockData());
        player.send(GameMessage.MENU_PURCHASED);
        player.play(GameSound.MENU_SUCCESS);
    }
}
