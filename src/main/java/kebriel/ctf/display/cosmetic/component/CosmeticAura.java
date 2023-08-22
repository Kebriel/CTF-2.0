package kebriel.ctf.display.cosmetic.component;

import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.scheduler.BukkitRunnable;

public interface CosmeticAura extends Cosmetic {

    BukkitRunnable getAura(PlayerState player);

    @Override
    default void apply(CTFPlayer player) {
        player.setEffect(this);
    }

    @Override
    default void select(CTFPlayer player, Stat selectSlot) {
        Cosmetic.super.select(player, selectSlot);
        apply(player);
    }
}
