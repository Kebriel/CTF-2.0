package kebriel.ctf.event.async;

import kebriel.ctf.event.async.components.FlagEvent;
import kebriel.ctf.game.Team;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.game.flag.Flag.FlagStatus;
import kebriel.ctf.internal.player.title.GameTitle;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;

public class AsyncFlagTakeEvent extends FlagEvent {

    @Override
    public void postProcess() {
        CTFPlayer player = getPlayer();
        Team flagTeam = getFlag().getTeam();

        GameTitle forPlayer = player.getTeam().equals(flagTeam) ? GameTitle.GAME_FLAG_RESCUED_YOU : GameTitle.GAME_FLAG_TAKEN_YOU;

        GameTitle forFlagsTeam = friendlyPickup ? GameTitle.GAME_FLAG_RESCUED_YOURS : GameTitle.GAME_FLAG_TAKEN_YOURS;

        GameTitle forAllOthers = friendlyPickup ? GameTitle.GAME_FLAG_RESCUED_ENEMY : GameTitle.GAME_FLAG_TAKEN_ENEMY;

        player.sendTitle(forPlayer.get());
        forFlagsTeam.sendTo(flagTeam.getPlayers());
        forAllOthers.sendTo(JavaUtil.exemptFromSet(Teams.getAllPlayersInTeamsBesides(flagTeam), player));
    }

    private final boolean fromGround;
    private final boolean friendlyPickup;

    public AsyncFlagTakeEvent(Flag flag, CTFPlayer player) {
        super(player, flag);
        fromGround = flag.getStatus() == FlagStatus.DROPPED;
        friendlyPickup = flag.getTeam().equals(player.getTeam());
    }

    public boolean wasPickedUp() {
        return fromGround;
    }

    public boolean wasRecovered() {
        return friendlyPickup;
    }


}
