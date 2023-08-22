package kebriel.ctf.event.async;

import kebriel.ctf.Constants;
import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.game.Teams;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncPlayerKillEvent extends AsyncEvent {

    @Override
    public void postProcess() {
        killer.incrementStat(Stat.GAME_KILLS);
        killer.incrementStat(Stat.KILLS);
        killer.addToStat(Stat.GAME_GOLD, goldReward.get());
        killer.addToStat(Stat.GOLD, goldReward.get());
        killer.addToStat(Stat.XP, xpReward.get());

        GameMessage killMessage = holdingFlag ? GameMessage.GAME_YOU_KILLED_HOLDING_FLAG : GameMessage.GAME_YOU_KILLED;
        killer.send(killMessage.fillNext(killed.getNameFull())
                .fillNext(goldReward.get())
                .fillNext(xpReward.get()));

        GameMessage.GAME_PLAYER_KILLED_PLAYER.sendGlobalPrefixed();

        killer.play(GameSound.MENU_SUCCESS);
    }

    @Override
    public void preProcess() {
        // Double the base gold & xp rewards
        if(holdingFlag) {
            goldReward.getAndUpdate((value) -> Constants.BASE_FLAGHOLDER_MULT*value);
            xpReward.getAndUpdate((value) -> Constants.BASE_FLAGHOLDER_MULT*value);
        }
    }

    public enum PlayerKillType {
        MELEE_ATTACK, ARROW, KNOCKBACK_MELEE, KNOCKBACK_PROJECTILE, TICK_FIRE, TICK_WITHER, LIFE_DRAIN,
        GENERIC_KILL
    }

    private final CTFPlayer killer;
    private final CTFPlayer killed;
    private final double finalDamage;
    private final AtomicInteger goldReward;
    private final AtomicInteger xpReward;
    private final ItemStack with;
    private final boolean holdingFlag;
    private final PlayerKillType cause;

    public AsyncPlayerKillEvent(CTFPlayer killer, CTFPlayer killed, double finalDamage, PlayerKillType cause) {
        super(killer);
        this.killer = killer;
        this.killed = killed;
        this.finalDamage = finalDamage;
        this.cause = cause;

        holdingFlag = killed.getState().isHoldingFlag();

        goldReward = new AtomicInteger(Constants.BASE_KILL_GOLD);
        xpReward = new AtomicInteger(Constants.BASE_KILL_XP);

        with = killer.getBukkitPlayer().getItemInHand();
    }

    public CTFPlayer getKiller() {
        return killer;
    }

    public CTFPlayer getWhoDied() {
        return killed;
    }

    public boolean didFlagDrop() {
        return holdingFlag;
    }

    public double getFinalDamage() {
        return finalDamage;
    }

    public ItemStack getItemWith() {
        return with;
    }

    public AtomicInteger getGoldReward() {
        return goldReward;
    }

    public AtomicInteger getXPReward() {
        return xpReward;
    }

    public PlayerKillType getKillType() {
        return cause;
    }
}
