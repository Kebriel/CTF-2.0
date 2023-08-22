package kebriel.ctf.display.cosmetic.component;

import kebriel.ctf.display.gui.component.Unlockable;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.Stat;

import java.util.function.Function;

public interface Cosmetic extends Unlockable {

    CosmeticType getType();
    Function<CTFPlayer, Boolean> getUnlockCriteria();
    @Override
    Text getSubtext();
    void apply(CTFPlayer player);

    @Override
    default Stat getUnlockData() {
        return null;
    }

    @Override
    default boolean isUnlocked(CTFPlayer player) {
        return getUnlockCriteria().apply(player);
    }
}
