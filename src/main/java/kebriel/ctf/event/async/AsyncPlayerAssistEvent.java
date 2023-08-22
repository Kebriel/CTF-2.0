package kebriel.ctf.event.async;

import kebriel.ctf.Constants;
import kebriel.ctf.event.async.AsyncPlayerKillEvent.PlayerKillType;
import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.game.component.WrappedDamage;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncPlayerAssistEvent extends AsyncEvent {

    @Override
    public void postProcess() {
        CTFPlayer player = getPlayer();
        player.incrementStat(Stat.ASSISTS);
        player.setStat(Stat.GOLD, goldReward.get());
        player.setStat(Stat.XP, xpReward.get());

        player.play(GameSound.MENU_SUCCESS);
        player.send(GameMessage.GAME_YOU_ASSISTED);
    }

    private final CTFPlayer assist;
    private final PlayerKillType reason;
    private final double damageDealt;
    private final AtomicInteger goldReward;
    private final AtomicInteger xpReward;

    public AsyncPlayerAssistEvent(CTFPlayer player, CTFPlayer assist, PlayerKillType reason, WrappedDamage damage) {
        super(player);
        this.assist = assist;
        this.reason = reason;

        goldReward = new AtomicInteger(Constants.BASE_ASSIST_GOLD);
        xpReward = new AtomicInteger(Constants.BASE_ASSIST_XP);

        damageDealt = damage.getTotalDamage();
    }

    public CTFPlayer getAssist() {
        return assist;
    }

    public PlayerKillType getCause() {
        return reason;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public AtomicInteger getGoldReward() {
        return goldReward;
    }

    public AtomicInteger getXPReward() {
        return xpReward;
    }
}
