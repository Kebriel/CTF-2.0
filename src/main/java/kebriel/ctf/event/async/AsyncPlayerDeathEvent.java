package kebriel.ctf.event.async;

import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class AsyncPlayerDeathEvent extends AsyncEvent {

    @Override
    public void preProcess() {
        getPlayer().getState().die(this);
    }

    private final double finalDamage;
    private final DamageCause reason;

    public AsyncPlayerDeathEvent(CTFPlayer player, double finalDamage, DamageCause reason) {
        super(player);
        this.finalDamage = finalDamage;
        this.reason = reason;
    }

    public double getFinalDamage() {
        return finalDamage;
    }

    public DamageCause getReason() {
        return reason;
    }
}
