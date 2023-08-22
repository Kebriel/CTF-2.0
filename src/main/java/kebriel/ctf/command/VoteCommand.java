package kebriel.ctf.command;

import kebriel.ctf.game.Game;
import kebriel.ctf.game.map.GameMaps.GameMap;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kebriel.ctf.game.map.MapVoting;

public class VoteCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(!(sender instanceof Player p) || !cmd.getName().equalsIgnoreCase("vote"))
			return false;

		CTFPlayer player = CTFPlayer.get(p);

		if(!MapVoting.isVoting()) {
			p.sendMessage(ChatColor.RED + "There is no voting going on right now!");
			player.play(GameSound.MENU_NO);
			return false;
		}

		if(args.length < 1) {
			p.sendMessage(ChatColor.RED + "Usage: /vote <#>");
			return false;
		}

		if(MapVoting.hasPlayerVoted(player)) {
			p.sendMessage(ChatColor.RED + "You've already voted");
			player.play(GameSound.MENU_NO);
			return false;
		}

		if(!StringUtils.isNumeric(args[0])) {
			p.sendMessage(ChatColor.RED + "Enter a valid number");
			player.play(GameSound.MENU_NO);
			return false;
		}

		int vote = 0;
		try {
			vote = Integer.parseInt(args[0]);
		} catch(Exception e) {
			p.sendMessage(ChatColor.RED + "Enter a valid number");
			player.play(GameSound.MENU_NO);
			return false;
		}

		if(vote < 1 || vote > MapVoting.getCandidates().size()) {
			p.sendMessage(ChatColor.RED + "Please vote for a number between 1 and " + MapVoting.getCandidates().size());
			return false;
		}

		GameMap map = MapVoting.getCandidates().get(vote-1);
		MapVoting.vote(player, map);
		Game.get().sendGlobal(GameMessage.LOBBY_PLAYER_VOTED.fillNext(map.getName()));

		return false;
	}
	
	

}
