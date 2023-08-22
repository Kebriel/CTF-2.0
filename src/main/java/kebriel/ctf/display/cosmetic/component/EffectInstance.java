package kebriel.ctf.display.cosmetic.component;

import kebriel.ctf.CTFMain;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectInstance {

    private final CTFPlayer player;
    private BukkitRunnable run;

    public EffectInstance(CTFPlayer player) {
        this.player = player;
    }

    public void disable() {
        if(run != null)
            run.cancel();
    }

    public void newEffect(BukkitRunnable run) {
        disable();
        this.run = run;
        run.runTaskTimerAsynchronously(CTFMain.instance, 0, 1);
    }

    public CTFPlayer getPlayer() {
        return player;
    }
}
