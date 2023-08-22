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

public class AsyncFlagCaptureEvent extends FlagEvent {

    @Override
    public void postProcess() {
        CTFPlayer player = getPlayer();
        player.incrementStat(Stat.FLAGS_CAPTURED);
        player.incrementStat(Stat.GAME_CAPTURES);
        player.addToStat(Stat.GOLD, goldReward.get());
        player.addToStat(Stat.XP, xpReward.get());

        // The player who captured
        player.sendTitle(GameTitle.GAME_FLAG_CAPTURED_YOU.get());
        // All players on opponent's team
        GameTitle.GAME_FLAG_CAPTURED_YOURS.sendTo(flag.getTeam().getPlayers());
        // All players on capturer's team except capturer themselves
        GameTitle.GAME_FLAG_CAPTURED_ENEMY.sendTo(JavaUtil.exemptFromList(player.getTeam().getPlayers(), player));
        // To all other teams, if there are any (3-4 team games)
        GameTitle.GAME_FLAG_CAPTURED_ENEMY_OTHER.sendTo(Teams.getAllPlayersInTeamsBesides(player.getTeam(), flag.getTeam()));
    }

    private final AtomicInteger xpReward;
    private final AtomicInteger goldReward;
    private final Flag flag;

    public AsyncFlagCaptureEvent(CTFPlayer player, Flag flag) {
        super(player, flag);
        this.flag = flag;

        goldReward = new AtomicInteger(Constants.BASE_CAPTURE_GOLD);
        xpReward = new AtomicInteger(Constants.BASE_CAPTURE_XP);
    }

    public AtomicInteger getGoldReward() {
        return goldReward;
    }

    public AtomicInteger getXPReward() {
        return xpReward;
    }

}
