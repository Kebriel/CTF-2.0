package kebriel.ctf.event.async;

import kebriel.ctf.event.async.components.FlagEvent;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.internal.player.title.GameTitle;
import kebriel.ctf.player.CTFPlayer;

public class AsyncFlagDropEvent extends FlagEvent {

    @Override
    public void postProcess() {
        Flag flag = getFlag();
        CTFPlayer player = getPlayer();

        // Message differs based on context for player who dropped
        GameTitle forPlayer = flag.getTeam().equals(player.getTeam()) ? GameTitle.GAME_FLAG_DROPPED_YOU_YOURS : GameTitle.GAME_FLAG_DROPPED_YOU_ENEMY;

        // The players on the same team as the flag belongs
        GameTitle.GAME_FLAG_DROPPED_YOURS.sendTo(flag.getTeam().getPlayers());
        // The players on all other teams
        GameTitle.GAME_FLAG_DROPPED_ENEMY.sendTo(Teams.getAllPlayersInTeamsBesides(flag.getTeam()));
    }

    private final boolean droppedByAlly;

    public AsyncFlagDropEvent(CTFPlayer player, Flag flag) {
        super(player, flag);
        droppedByAlly = player.getTeam().equals(flag.getTeam());
    }

    public boolean wasLostByTeam() {
        return droppedByAlly;
    }
}
