package kebriel.ctf.command;

import kebriel.ctf.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(!(sender instanceof Player p) || !cmd.getName().equalsIgnoreCase("game"))
			return false;

		if(!p.isOp())
			return false;

		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /game <forcestart:reset>");
			return false;
		}

		Game game = Game.get();

		if(args[0].equalsIgnoreCase("forcestart") || args[0].equalsIgnoreCase("start")) {
			if(game.isPlaying()) {
				p.sendMessage(ChatColor.RED + "The game is already started! Use /game reset to stop it");
				return false;
			}

			game.start();
		}else if(args[0].equalsIgnoreCase("reset")) {
			game.resetGame();
			return false;
		}

		sender.sendMessage(ChatColor.RED + "Usage: /game <forcestart:reset>");
		return false;
	}
	
	

}
