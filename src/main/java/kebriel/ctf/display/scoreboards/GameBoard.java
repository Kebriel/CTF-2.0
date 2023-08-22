package kebriel.ctf.display.scoreboards;

import kebriel.ctf.Constants;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.game.map.MapVoting;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.nms.GamePacket.CreateScoreboard;
import kebriel.ctf.internal.player.text.ConditionalMessage;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameBoard {

    public static final GameBoard VOTING_BOARD = new GameBoard("voting_board", Text.get().boldColor(ChatColor.GOLD, "Maps"))
            .addLine("map1", Text.get().insert(() -> MapVoting.getMapsSortedByVotes().get(0).getKey()), () -> MapVoting.getMapsSortedByVotes().get(0).getValue())
            .addLine("map2", Text.get().insert(() -> MapVoting.getMapsSortedByVotes().get(1).getKey()), () -> MapVoting.getMapsSortedByVotes().get(1).getValue())
            .addLine("map3", Text.get().insert(() -> MapVoting.getMapsSortedByVotes().get(2).getKey()), () -> MapVoting.getMapsSortedByVotes().get(2).getValue());
    public static final GameBoard GAME_BOARD = new GameBoard("game_board", Text.get().magicColor(ChatColor.BLUE, "===").boldColor(ChatColor.GOLD, " CTF ").magicColor(ChatColor.RED, "==="))
            .addLine("game_state", Text.get().insert(new ConditionalMessage(() -> Game.get().isDeathmatch(), "Deathmatch in: ", "Game ends: ")).insert(() -> Game.get().getTimer()))
            .addLine("your_team", Text.get().gold("Team: ").insert(CTFPlayer::getTeam))
            .addSpace()
            .addLine("blue", Text.get().boldColor(ChatColor.BLUE, "BLUE"))
            .addLine("blue_flag_state", Text.get().gold("Flag: ").white("").insert(() -> Teams.getTeam(TeamColor.BLUE).getFlag().getStatus().toString()))
            .addLine("blue_captures", Text.get().aqua("Flags Captured: ").white("").insert(() -> Teams.getTeam(TeamColor.BLUE).getCaptures()))
            .addSpace()
            .addLine("red", Text.get().boldColor(ChatColor.RED, "RED"))
            .addLine("red_flag_state", Text.get().gold("Flag: ").white("").insert(() -> Teams.getTeam(TeamColor.BLUE).getFlag().getStatus().toString()))
            .addLine("red_captures", Text.get().aqua("Flags Captured: ").white("").insert(() -> Teams.getTeam(TeamColor.RED).getCaptures()))
            .addSpace()
            .addLine(Stat.GAME_KILLS.getID(), Text.get().red("Kills: ").white("").insert(player -> player.getStat(Stat.GAME_KILLS)))
            .addLine(Stat.GAME_GOLD.getID(), Text.get().gold("Gold Earned: ").white("").insert(player -> player.getStat(Stat.GAME_GOLD)));
    public static final GameBoard IDLE_BOARD = new GameBoard("idle_board", Text.get().magicColor(ChatColor.BLUE, "===").boldColor(ChatColor.GOLD, " CTF ").magicColor(ChatColor.RED, "==="))
            .addLine("player_count", Text.get().text("Players: ").aqua("").insert(() -> CTFPlayer.getAllOnline().size()).text("/").text(String.valueOf(Constants.MINIMUM_PLAYERS_TO_START)))
            .addSpace()
            .addLine(Stat.LEVEL.getID(), Text.get().aqua("Level: ").white("").insert(player -> player.getStat(Stat.LEVEL)))
            .addLine(Stat.GOLD.getID(), Text.get().aqua("Gold: ").white("").insert(player -> player.getStat(Stat.GOLD)))
            .addSpace()
            .addLine("abilities", Text.get().yellow("Abilities:"))
            .addLine(Stat.SELECTED_ABILITY1.getID(), Text.get().insert(player -> player.getSelected(Stat.SELECTED_ABILITY1).getName()))
            .addLine(Stat.SELECTED_ABILITY2.getID(), Text.get().insert(player -> player.getSelected(Stat.SELECTED_ABILITY2).getName()))
            .conditionalLine(player -> player.getIsUnlocked(Stat.UNLOCKED_SLOT_ABILITY3), Stat.SELECTED_ABILITY3.getID(), Text.get().insert(player -> player.getSelected(Stat.SELECTED_ABILITY3).getName()))
            .conditionalLine(player -> player.getIsUnlocked(Stat.UNLOCKED_SLOT_ABILITY4), Stat.SELECTED_ABILITY4.getID(), Text.get().insert(player -> player.getSelected(Stat.SELECTED_ABILITY4).getName()))

            ;



    private final Text displayName;
    private final List<ScoreboardLine> lines;
    private final String id;
    private int spaceCount;

    private GameBoard(String boardID, Text displayName) {
        id = boardID;
        this.displayName = displayName;
        this.lines = new ArrayList<>();
    }

    private GameBoard addLine(String lineID, Text contents) {
        lines.add(new ScoreboardLine(id, lineID, contents));
        return this;
    }

    private GameBoard addLine(String lineID, Text contents, Supplier<Integer> score) {
        lines.add(new ScoreboardLine(id, lineID, contents, score));
        return this;
    }

    private GameBoard addSpace() {
        Text space = Text.get().text(" ");
        for(int i = spaceCount; i > 0; i--) space.text(" ");
        lines.add(new ScoreboardLine(id, "space" + spaceCount, space));
        spaceCount++;
        return this;
    }

    private GameBoard conditionalLine(Function<CTFPlayer, Boolean> condition, String lineID, Text contents) {
        lines.add(new ScoreboardLine(id, lineID, contents).makeConditional(condition));
        return this;
    }

    private GameBoard conditionalLine(Function<CTFPlayer, Boolean> condition, String lineID, Text contents, Supplier<Integer> wrappedScore) {
        lines.add(new ScoreboardLine(id, lineID, contents, wrappedScore).makeConditional(condition));
        return this;
    }

    public ScoreboardLine getLine(String id) {
        for(ScoreboardLine line : lines)
            if(line.getID().equals(id))
                return line;
        return null;
    }

    public String getID() {
        return id;
    }

    public void sendRaw(CTFPlayer player) {
        CreateScoreboard display = new CreateScoreboard(id, displayName.toString(), lines);
        display.sendFor(player);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GameBoard board)
            return board.getID().equals(id);
        return false;
    }
}
