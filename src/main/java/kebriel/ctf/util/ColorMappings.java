package kebriel.ctf.util;

import kebriel.ctf.game.Teams.TeamColor;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class ColorMappings {

    private static final Map<ChatColor, DyeColor> chatToDyeMap = new HashMap<>();
    private static final Map<TeamColor, ChatColor> teamToChatColorMap = new HashMap<>();


    static {
        chatToDyeMap.put(ChatColor.AQUA, DyeColor.LIGHT_BLUE);
        chatToDyeMap.put(ChatColor.BLACK, DyeColor.BLACK);
        chatToDyeMap.put(ChatColor.BLUE, DyeColor.BLUE);
        chatToDyeMap.put(ChatColor.DARK_AQUA, DyeColor.CYAN);
        chatToDyeMap.put(ChatColor.DARK_BLUE, DyeColor.BLUE);
        chatToDyeMap.put(ChatColor.DARK_GRAY, DyeColor.GRAY);
        chatToDyeMap.put(ChatColor.GREEN, DyeColor.LIME);
        chatToDyeMap.put(ChatColor.DARK_GREEN, DyeColor.GREEN);
        chatToDyeMap.put(ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA);
        chatToDyeMap.put(ChatColor.DARK_PURPLE, DyeColor.PURPLE);
        chatToDyeMap.put(ChatColor.RED, DyeColor.RED);
        chatToDyeMap.put(ChatColor.DARK_RED, DyeColor.RED);
        chatToDyeMap.put(ChatColor.GOLD, DyeColor.ORANGE);
        chatToDyeMap.put(ChatColor.WHITE, DyeColor.WHITE);
        chatToDyeMap.put(ChatColor.YELLOW, DyeColor.YELLOW);

        teamToChatColorMap.put(TeamColor.RED, ChatColor.RED);
        teamToChatColorMap.put(TeamColor.BLUE, ChatColor.BLUE);
    }

    public static DyeColor chatToDyeColor(ChatColor chatColor) {
        return chatToDyeMap.getOrDefault(chatColor, DyeColor.WHITE); // Default to white if no mapping exists
    }

    public static ChatColor teamToChatColor(TeamColor color) {
        return teamToChatColorMap.getOrDefault(color, ChatColor.GRAY);
    }
}
