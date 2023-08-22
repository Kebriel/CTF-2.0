package kebriel.ctf.command;

import kebriel.ctf.game.Game;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShoutCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(!(sender instanceof Player p) || !cmd.getName().equalsIgnoreCase("shout"))
			return false;

		if(!Game.get().isPlaying()) {
			p.sendMessage(ChatColor.RED + "You can only shout during a game");
			return false;
		}

		if(args.length == 0) {
			p.sendMessage(ChatColor.RED + "Usage: /shout <message>");
			return false;
		}

		StringBuilder msg = new StringBuilder();
		for(int i = 0; i < args.length; i++)
			msg.append(i == 0 ? args[i] : " " + args[i]);

		CTFPlayer player = CTFPlayer.get(p);

		String teamPrefix = player.getTeam().getChatColor() + player.getTeam().getTeamColor().toString();

		for(CTFPlayer pl : CTFPlayer.getAllOnline())
			pl.send(Text.get().gray("[").text(teamPrefix).gray("] ")
							.boldColor(ChatColor.AQUA, "[").boldColor(ChatColor.WHITE, player.getStat(Stat.LEVEL)).boldColor(ChatColor.AQUA, "] ")
							.white(player.getNameRaw() + ": " + msg));
		return false;
	}

}
