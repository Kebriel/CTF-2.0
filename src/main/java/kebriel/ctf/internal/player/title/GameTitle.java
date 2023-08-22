package kebriel.ctf.internal.player.title;

import kebriel.ctf.game.Game;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.ChatColor;

import java.util.Collection;

public enum GameTitle {

    LEVEL_UP_SCREEN(Text.get().boldColor(ChatColor.GOLD, "LEVEL UP"), Text.get().yellow("You are now ").gold("Level: ").boldColor(ChatColor.AQUA, "").placeholder(), 0, 35, 5),
    LOBBY_STARTING_COUNTDOWN(Text.get().boldColor(ChatColor.GOLD, "").insert(() -> Game.get().getTimer()), null, 0, 15, 5),
    PREGAME_GAME_STARTED(Text.get().boldColor(ChatColor.GREEN, "Game started!"),
            Text.get().gold("You are on the ").insert(player -> player.getTeam().getNameFull()),
            0, 40, 10),
    PREGAME_GRACE_PERIOD_COUNTDOWN(Text.get().boldColor(ChatColor.RED, "Grace Period:"),
            Text.get().boldColor(ChatColor.GOLD, "").insert(() -> Game.get().getTimer()), 0, 15, 5),
    PREGAME_GRACE_PERIOD_OVER(Text.get().red("GRACE PERIOD OVER"), Text.get().yellow("Combat is now enabled!"), 0, 10, 3),
    GAME_DEATH_SCREEN(Text.get().boldColor(ChatColor.RED, "YOU DIED"), Text.get().yellow("Respawning in... ").boldColor(ChatColor.AQUA, "").insert(player -> player.getState().getDeathTimer()), 0, 22, 0),
    GAME_FLAG_CAPTURED_YOU(),
    GAME_FLAG_CAPTURED_YOURS(Text.get().green("Flag captured!"), ),
    GAME_FLAG_CAPTURED_ENEMY(),
    GAME_FLAG_CAPTURED_ENEMY_OTHER(),
    GAME_FLAG_TAKEN_YOU(),
    GAME_FLAG_TAKEN_YOURS(),
    GAME_FLAG_TAKEN_ENEMY(),
    GAME_FLAG_RETURNED_YOU(),
    GAME_FLAG_RETURNED_YOURS(),
    GAME_FLAG_RETURNED_ENEMY(),
    GAME_FLAG_DROPPED_YOU_ENEMY(),
    GAME_FLAG_DROPPED_YOU_YOURS(),
    GAME_FLAG_DROPPED_YOURS(),
    GAME_FLAG_DROPPED_ENEMY(),
    GAME_FLAG_RESCUED_YOU(),
    GAME_FLAG_RESCUED_YOURS(),
    GAME_FLAG_RESCUED_ENEMY(),
    GAME_DEATHMATCH_BEGINS(Text.get().text(""), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_YOU_WON(Text.get().text(""), null, 0, 50, 10),
    GAME_VICTORY_YOU_LOST(Text.get().text(""), null, 0, 50, 10),
    GAME_VICTORY_THREE_CAPTURES_TEAM(GAME_VICTORY_YOU_WON.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_THREE_CAPTURES_OTHER(GAME_VICTORY_YOU_LOST.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_DEATHMATCH_CAPTURES_TEAM(GAME_VICTORY_YOU_WON.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_DEATHMATCH_CAPTURES_OTHER(GAME_VICTORY_YOU_LOST.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_MOST_CAPTURES_TEAM(GAME_VICTORY_YOU_WON.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_MOST_CAPTURES_OTHER(GAME_VICTORY_YOU_LOST.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_MOST_KILLS_TEAM(GAME_VICTORY_YOU_WON.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_MOST_KILLS_OTHER(GAME_VICTORY_YOU_LOST.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_LOTTERY_TEAM(GAME_VICTORY_YOU_WON.getTitle(), Text.get().text(""), 0, 50, 10),
    GAME_VICTORY_LOTTERY_OTHER(GAME_VICTORY_YOU_LOST.getTitle(), Text.get().text(""), 0, 50, 10),
    ;

    private final int[] fades;
    private final Text title;
    private final Text subtitle;

    GameTitle(Text title, Text subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        fades = new int[]{fadeIn, stay, fadeOut};
    }

    public boolean playerBased() {
        return (title != null && title.requiresPlayerData()) || (subtitle != null && subtitle.requiresPlayerData());
    }

    public WrappedTitle get() {
        WrappedTitle wrapped;
        if(playerBased()) {
            wrapped = WrappedTitle.forPlayerData(title, subtitle, fades, null);
        }else{
            wrapped = WrappedTitle.wrapBasic(title, subtitle, fades);
        }
        return wrapped;
    }

    public WrappedTitle fillTitleNext(Object obj) {
        return get().titleFillNext(obj);
    }

    public WrappedTitle fillSubtitleNext(Object obj) {
        return get().subtitleFillNext(obj);
    }

    public void sendTo(Collection<CTFPlayer> players) {
        for(CTFPlayer player : players)
            player.sendTitle(get());
    }

    public void sendToLobby() {
        for(CTFPlayer player : CTFPlayer.getAllOnline())
            player.sendTitle(get());
    }

    public Text getTitle() {
        return title.clone();
    }

    public Text getSubtitle() {
        return subtitle.clone();
    }

    protected int[] getFades() {
        return fades;
    }
}
