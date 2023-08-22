package kebriel.ctf.event;

import kebriel.ctf.ability.AbilityAlchemist;
import kebriel.ctf.event.custom.PlayerAssistEvent;
import kebriel.ctf.event.custom.PlayerDeathEvent;
import kebriel.ctf.event.custom.PlayerDebuffEvent;
import kebriel.ctf.event.custom.PlayerKillEvent;
import kebriel.ctf.game.core.components.Reactor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;

public class EventReaction {

    private static final Set<Reactor> reactInstances = new HashSet<>();

    static {
        reactInstances.add(new AbilityAlchemist());
    }

    public static void onKill(PlayerKillEvent event) {
        for(Reactor react : reactInstances)
            react.onKill(event);
    }

    public static void onAssist(PlayerAssistEvent event) {
        for(Reactor react : reactInstances)
            react.onAssist(event);
    }

    public static void onAttack(EntityDamageByEntityEvent event, Player attacker, Player attacked) {
        for(Reactor react : reactInstances)
            react.onAttack(event, attacker, attacked);
    }

    public static void onDamage(EntityDamageEvent event, Player player) {
        for(Reactor react : reactInstances)
            react.onDamage(event, player);
    }

    public static void onProjectileHit(EntityDamageByEntityEvent event, Player damaged, Player shooter, Projectile proj) {
        for(Reactor react : reactInstances)
            react.onProjectileHit(event, damaged, shooter, proj);
    }

    public static void onBlockPlace(BlockPlaceEvent event) {
        for(Reactor react : reactInstances)
            react.onBlockPlace(event);
    }

    public static void onDie(PlayerDeathEvent event, Player player) {
        for(Reactor react : reactInstances)
            react.onDie(event, player);
    }

    public static void onDebuff(PlayerDebuffEvent event, Player player) {
        for(Reactor react : reactInstances)
            react.onDebuff(event, player);
    }
}
