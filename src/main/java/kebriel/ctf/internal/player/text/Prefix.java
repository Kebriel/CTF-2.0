package kebriel.ctf.internal.player.text;

import org.bukkit.ChatColor;

public enum Prefix {

    GAME_GENERIC(Text.get().color(ChatColor.RED).text("[").boldColor(ChatColor.GOLD, "CTF").color(ChatColor.RED).text("]")),

    ;

    private final Text contents;

    Prefix(Text contents) {
        this.contents = contents;
    }

    public String get() {
        return contents.toString();
    }

    @Override
    public String toString() {
        return get();
    }
}
