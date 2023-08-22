package kebriel.ctf.event.async;

import kebriel.ctf.Constants;
import kebriel.ctf.event.async.components.FlagEvent;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.internal.player.title.GameTitle;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import kebriel.ctf.util.JavaUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncFlagReturnEvent extends FlagEvent {

    @Override
    public void postProcess() {
        CTFPlayer player = getPlayer();
        player.addToStat(Stat.GOLD, goldReward.get());
        player.addToStat(Stat.XP, xpReward.get());

        player.sendTitle(GameTitle.GAME_FLAG_RETURNED_YOU.get());
        GameTitle.GAME_FLAG_RETURNED_YOURS.sendTo(JavaUtil.exemptFromList(player.getTeam().getPlayers(), player));
        GameTitle.GAME_FLAG_RETURNED_ENEMY.sendTo(Teams.getAllPlayersInTeamsBesides(player.getTeam()));
    }

    private final boolean natural;
    private final AtomicInteger xpReward;
    private final AtomicInteger goldReward;

    public AsyncFlagReturnEvent(CTFPlayer player, Flag flag, boolean natural) {
        super(player, flag);
        this.natural = natural;

        goldReward = new AtomicInteger(Constants.BASE_RETURN_GOLD);
        xpReward = new AtomicInteger(Constants.BASE_RETURN_XP);
    }

    public AtomicInteger getGoldReward() {
        return goldReward;
    }

    public AtomicInteger getXPReward() {
        return xpReward;
    }

    public boolean wasNatural() {
        return natural;
    }
}
