package kebriel.ctf.display.gui.component;

import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;

public interface Unlockable extends Selectable {

    Stat getUnlockData();

    default boolean isUnlocked(CTFPlayer player) {
        return player.getIsUnlocked(getUnlockData());
    }
}
