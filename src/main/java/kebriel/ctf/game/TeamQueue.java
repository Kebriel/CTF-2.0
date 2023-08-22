package kebriel.ctf.game;

import kebriel.ctf.Constants;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.component.PlayerCollection;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamQueue extends PlayerCollection {

    private final Team team;

    public TeamQueue(Team team) {
        this.team = team;
    }

    public boolean canJoinQueue(CTFPlayer player) {
        return getPlayers().size() <= ((CTFPlayer.getAllOnline().size() / Teams.getTeams().size()) - Constants.QUEUE_OFFSET) || Teams.isPlayerQueued(player);
    }

    public boolean canJoinQueue(UUID id) {
        return canJoinQueue(CTFPlayer.get(id));
    }

    public Team getCorrespondingTeam() {
        return team;
    }

    @Override
    public void add(CTFPlayer p) {
        if(!GameStage.IS_STARTING.get())
            return;

        if (!canJoinQueue(p))
            return;

        super.add(p);
    }

    public void attemptJoin(CTFPlayer player) {
        if(canJoinQueue(player)) {
            add(player);
            player.send(GameMessage.LOBBY_QUEUED_FOR_TEAM.get().fillNext(team.getNameFull()));
            player.play(GameSound.MENU_YES);
        }else{
            GameMessage msg = containsPlayer(player) ? GameMessage.LOBBY_ALREADY_QUEUED : GameMessage.LOBBY_QUEUE_IS_FULL;
            player.send(msg.get().fillNext(team.getNameFull()));
            player.play(GameSound.MENU_NO);
        }
    }
}
