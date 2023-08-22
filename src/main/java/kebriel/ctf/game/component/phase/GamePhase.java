package kebriel.ctf.game.component.phase;

import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.player.PlayerState;
import org.bukkit.scheduler.BukkitTask;

public interface GamePhase extends EventReactor {

    void start();
    void end();
    void updatePlayerState(PlayerState player);
    GamePhase getNextPhase();
    int getTimer();
    GameMessage getJoinMessage();
    GameMessage getLeaveMessage();
    boolean isCombatAllowed();
    boolean isDamageAllowed();
}
