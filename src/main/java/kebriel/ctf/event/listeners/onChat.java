package kebriel.ctf.event.listeners;

import kebriel.ctf.game.Game;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import kebriel.ctf.player.CTFPlayer;

public class onChat implements Listener {

	/*
	 * Game chat is team-only by default, global chat is handled by /shout
	 */
	public void onChat(AsyncPlayerChatEvent e) {
		e.setCancelled(true);
		CTFPlayer player = CTFPlayer.get(e.getPlayer());

		if(Game.get().isPlaying()) {
			for(CTFPlayer pl : player.getTeam().getPlayers())
				pl.getBukkitPlayer().sendMessage(
						Text.get().boldColor(ChatColor.DARK_GRAY, "<").boldColor(ChatColor.GRAY, "TEAM CHAT").boldColor(ChatColor.DARK_GRAY, "> ")
								.boldColor(ChatColor.AQUA, "[").boldColor(ChatColor.WHITE, player.getStat(Stat.LEVEL)).boldColor(ChatColor.AQUA, "] ")
								.white(player.getNameFull() + ": " + e.getMessage()).build());
			return;
		}

		for(CTFPlayer pl : CTFPlayer.getAllOnline())
			pl.getBukkitPlayer().sendMessage(Text.get().boldColor(ChatColor.AQUA, "[").boldColor(ChatColor.WHITE, player.getStat(Stat.LEVEL)).boldColor(ChatColor.AQUA, "], ")
					.white(player.getNameFull() + ": " + e.getMessage()).build());
	}

}
