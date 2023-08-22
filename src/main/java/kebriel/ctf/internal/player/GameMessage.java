package kebriel.ctf.internal.player;

import kebriel.ctf.Constants;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.map.MapVoting;
import kebriel.ctf.internal.player.text.Prefix;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.internal.player.text.Text.BlurbType;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.ChatColor;

import java.util.Collection;

public enum GameMessage {

    LOBBY_WAITING_FOR_PLAYERS(Text.get().aqua("Waiting for players [")
            .red("").insert(() -> Game.get().getPlayerCount()).boldColor(ChatColor.DARK_AQUA, "/")
            .aqua("").insert(() -> Constants.MINIMUM_PLAYERS_TO_START).aqua("]")),
    LOBBY_GAME_IS_STARTING(Text.get().aqua("The game is starting in ")
            .reset("").insert(() -> Game.get().getTimer()).aqua(" seconds!")),
    LOBBY_QUEUED_FOR_TEAM(Text.get().green("You are now queued for the ").placeholder()),
    LOBBY_ALREADY_QUEUED(Text.get().red("You are already queued for the ").placeholder()),
    LOBBY_QUEUE_IS_FULL(Text.get().red("The queue for the ").placeholder().red(" is full!")),
    LOBBY_CLEARED_QUEUE(Text.get().green("Removed from queue")),
    LOBBY_CANT_CLEAR_QUEUE(Text.get().red("You are not in a queue")),
    GENERIC_PLAYER_LEFT(Text.get().gray("").insert(CTFPlayer::getNameFull).gray(" has left")),
    GENERIC_PLAYER_JOINED(Text.get().gray("").insert(CTFPlayer::getNameFull).gray(" has joined")),
    LOBBY_PLAYER_LEFT_WAITING(Text.get().aqua("").insert(CTFPlayer::getNameFull)
            .gray(" has left ")
            .aqua("[").insert(() -> Game.get().getPlayerCount()).darkAqua("/").aqua("").insert(() -> Constants.MINIMUM_PLAYERS_TO_START).text("]")),
    LOBBY_PLAYER_JOINED_WAITING(Text.get().aqua("").insert(CTFPlayer::getNameFull)
            .gray(" has joined ")
            .aqua("[").insert(() -> Game.get().getPlayerCount()).darkAqua("/").aqua("").insert(() -> Constants.MINIMUM_PLAYERS_TO_START).text("]")),
    LOBBY_PLAYER_VOTED(Text.get().aqua("").insert(CTFPlayer::getNameFull).gray(" voted for ").green("").placeholder()),
    // p.sendMessage(ChatColor.GREEN + "" + c + ". " + ChatColor.GOLD + m.getName());
    LOBBY_MAP_LIST_ENTRY(),
    // p.sendMessage(ChatColor.AQUA + "Vote for a " + ChatColor.YELLOW + "Map:");
    LOBBY_VOTE_FOR_A_MAP(),
    // p.sendMessage(ChatColor.AQUA + "Use " + ChatColor.YELLOW + "/vote <#>" + ChatColor.AQUA + " to vote for a map!");
    LOBBY_USE_VOTECMD_TO_VOTE(),
    PREGAME_GAME_HAS_STARTED(Text.get().darkAqua("The game has begun!")),
    PREGAME_GRACE_PERIOD(Text.get().gold("Be aware though, there is a ").boldColor(ChatColor.GOLD, "")
            .insert(() -> Constants.GRACE_PERIOD_DURATION).text("s").gold(" grace period before combat is allowed or certain abilities can be used")),
    PREGAME_GRACE_PERIOD_ENDING(Text.get().gold("The grace period ends in ").aqua("").insert(() -> Game.get().getTimer()).text("s")),
    PREGAME_GRACE_PERIOD_OVER(Text.get().gold("The grace period has ended!")),
    LOBBY_VOTING_HAS_ENDED(Text.get().aqua("Voting has ended. The map ")
            .green("").insert(() -> MapVoting.getWinner().getName()).aqua(" has won!")),
    GAME_YOU_KILLED(),
    GAME_YOU_ASSISTED(),
    GAME_GENERIC_DEATH(),
    GAME_YOU_KILLED_HOLDING_FLAG(Text.get().aqua("You killed ").placeholder().text(", and they were holding a flag!")
            .placeholder(BlurbType.GOLD_REWARD).text(" ").placeholder(BlurbType.XP_REWARD)),
    GAME_PLAYER_KILLED_PLAYER(),
    GAME_FLAG_CANT_TAKE_YOURS(Text.get().red("You can't take your own flag")),
    GAME_FLAG_ALREADY_HOLDING(Text.get().red("You cannot hold two flags at once!")),
    GAME_FLAG_DROPPED(),
    GAME_VICTORY_CAPTURE_THREE(Text.get().text("")),
    GAME_VICTORY_CAPTURE_DEATHMATCH(Text.get().text("")),
    GAME_VICTORY_MOST_CAPTURES(Text.get().text("")),
    GAME_VICTORY_HIGHEST_KILLS(Text.get().text("")),
    GAME_VICTORY_RANDOMLY_SELECTED(Text.get().text("")),
    MENU_ALREADY_SELECTED(Text.get().red("You already have this selected")),
    MENU_YOU_CANNOT_AFFORD_THIS(Text.get().red("You cannot afford this")),
    MENU_UNLOCK_PREREQ_SLOT(Text.get().red("You must buy the slot before this first")),
    MENU_SELECTION_WIPED(Text.get().green("Selection wiped")),
    MENU_SELECTION_WIPE_FAILURE(Text.get().red("You have nothing selected")),
    MENU_PURCHASED(Text.get().green("Successfully purchased!")),
    MENU_UPCOMING(Text.get().yellow("This is not available yet!")),

    TRACKER_SUCCESSFULLY_TRACKING(Text.get().green("Tracking...")),
    TRACKER_ALREADY_TRACKING(Text.get().red("You are already tracking that flag!")),
    TRACKER_CANNOT_TRACK(Text.get().red("Tracking failed! The player holding that flag has a perk preventing them from being tracked")),
    TRACKER_FAILURE_UNTRACKABLE_CANCEL(Text.get().red("Tracking cancelled! The flag that you were previously tracking was picked up by an untrackable player")),
    /**
     * Args required:
     * - Player with the most kills
     * - (int) # of kills that player got
     * - Player with most flag captures
     * - (int) # of captures that player got
     */
    MULTILINE_ENDGAME_STATS_PLAYERS(Text.get()
            .gold("-----  ").boldColor(ChatColor.DARK_AQUA, "Top Players: ").gold("  -----")
            .newLine().text("  ")
            .newLine().yellow("-").gold("Top Killer: ").placeholder().text(" - ").green("").placeholder().text(" Kills")
            .newLine().yellow("-").gold("Most Flags Captured: ").placeholder().text(" - ").gold("").placeholder().text(" Flags")
            .newLine().text("  ")
            .newLine().gold("---------------------------")),
    MULTILINE_LEVEL_UP(Text.get().strikethroughColor(ChatColor.GOLD, "============================================")
            .newLine().magicColor(ChatColor.GOLD, "                -=").boldColor(ChatColor.GREEN, "Level Up").magicColor(ChatColor.GOLD, "=-                ")
            .newLine().yellow("You are now ").gold("Level: ").boldColor(ChatColor.AQUA, "").placeholder()
            .newLine().strikethroughColor(ChatColor.GOLD, "============================================")),
    ;

    private final Text contents;

    GameMessage(Text contents) {
        this.contents = contents;
    }

    public void sendGlobalPrefixed() {
        Text text = contents.clone().prefix(Prefix.GAME_GENERIC);
        for(CTFPlayer player : CTFPlayer.getAllOnline())
            player.send(text);
    }

    @Override
    public String toString() {
        return contents.clone().toString();
    }

    public Text fillNext(Object obj) {
        return contents.clone().fillNext(obj);
    }

    public Text get() {
        return contents.clone();
    }
}
