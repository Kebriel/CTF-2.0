package kebriel.ctf.game.component;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class WrappedDamage {

    private double totalDamage;
    private double lastDamage;
    private DamageCause cause;

    public WrappedDamage(double damage, DamageCause cause) {
        lastDamage = damage;
        this.cause = cause;
    }

    public WrappedDamage add(double damage, DamageCause cause) {
        lastDamage = damage;
        totalDamage+=damage;
        this.cause = cause;
        return this;
    }

    public double getLastDamage() {
        return lastDamage;
    }

    public double getTotalDamage() {
        return totalDamage;
    }

    public DamageCause getCause() {
        return cause;
    }
}
