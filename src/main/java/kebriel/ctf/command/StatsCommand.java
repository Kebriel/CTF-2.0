package kebriel.ctf.command;

import kebriel.ctf.internal.APIQuery;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.Future;

public class StatsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(!(sender instanceof Player p) || !cmd.getName().equalsIgnoreCase("stats"))
			return false;

		CTFPlayer player = CTFPlayer.get(p);

		if(args.length == 0) {
			displayStats(player);
		}else{
			if(args[0].length() > 16) {
				p.sendMessage(ChatColor.RED + "Player name '" + args[0] + "' has too many characters");
				return false;
			}

			/*
			 * Offline stat searching by way of queried API data, rather than direct database searches
			 * of the given player name, is preferred as it increases accuracy in the event of a player's
			 * name having changed.
			 *
			 * For example, say Player A is friends with Player B, but Player B hasn't
			 * logged in since they changed their name from "Bob" to "Jim". If we save all player names
			 * as a field the database, then our database will still consider Player B's UUID as
			 * mapping to the previously-saved name "Bob". If Player A then types /stats Jim, aware
			 * of their friend's new username, Player B's stats won't be found. However, if we
			 * first query Mojang's API  for the name "Jim", we'll find Jim's UUID, which is Player B's
			 * UUID in our database, and the stats will be loaded despite Player B never having
			 * logged into the server with their new name.
			 *
			 * TODO Add limiter to number of times this command can be run by a certain player in
			 *  a period of time, and/or capped frequency at which it can be run
			 */

			// Target is currently online
			for(CTFPlayer pl : CTFPlayer.getAllOnline())
				if(pl.getNameRaw().equalsIgnoreCase(args[0])) {
					displayStats(pl);
					return false;
				}

			// Target is not online
			AsyncExecutor.doTask(() -> {
				// Begin by querying Mojang's API to verify that this is a real player
				APIQuery query = APIQuery.queryUUIDUsername(args[0]);

				// Current thread will complete query before proceeding

				// At API request cap
				if(query.wasDisallowed()) {
					MinecraftUtil.runOnMainThread(() -> p.sendMessage(ChatColor.RED + "Too many requests, try again later!"));
					return;
				}

				if(query.getResult() == null) { // The entered player doesn't exist whatsoever
					MinecraftUtil.runOnMainThread(() -> p.sendMessage(ChatColor.RED + "Player '" + args[0] + "' does not seem to be a valid player. Did you enter their name correctly?"));
					player.play(GameSound.MENU_NO);
					return;
				}

				// The player exists, lets see if they have data -- data queried based on player's UUID fetched from API
				MinecraftUtil.runOnMainThread(() -> p.sendMessage(ChatColor.AQUA + "Player '" + args[0] + "' exists, grabbing their stats..."));
				CTFPlayer other = CTFPlayer.getOffline(query);

				if(other == null) { // The player is real, but we don't have any data for them
					MinecraftUtil.runOnMainThread(() -> p.sendMessage(ChatColor.RED + "No data found for " + args[0] + "! They have likely never joined the server before"));
					player.play(GameSound.MENU_NO);
					return;
				}

				// Data properly found, show the queried player's stats
				displayStats(other);
			});
		}
		return false;
	}

	private void displayStats(CTFPlayer player) {
		player.send(Text.get().strikethroughColor(ChatColor.GOLD, "=============================================")
				.newLine().green(player.getNameRaw() + "'s Stats:")
				.newLine().aqua("Games Played: ").gold(player.getStat(Stat.GAMES)).aqua(" Wins: ").gold(player.getStat(Stat.WINS)).aqua(" Losses: ").gold(player.getStat(Stat.LOSSES)).aqua(" WLR: ").gold(player.calcWlr())
				.newLine().aqua("Flags Captured: ").gold(player.getStat(Stat.FLAGS_CAPTURED)).aqua(" Flag Carriers Killed: ").gold(player.getStat(Stat.FLAG_CARRIER_KILLS))
				.newLine().aqua("Assists: ").gold(player.getStat(Stat.ASSISTS)).aqua(" Kills: ").gold(Stat.KILLS).aqua(" Deaths: ").gold(player.getStat(Stat.DEATHS)).aqua(" KDR: ").gold(player.calcKdr())
				.newLine().aqua("Level: ").gold(player.getStat(Stat.LEVEL)).aqua(" XP: ").gold(player.getStat(Stat.XP)).aqua(" XP Needed: ").gold(player.getXPToNext()).aqua(" Gold: ").gold(player.getStat(Stat.GOLD))
				.newLine().aqua("Challeneges Completed: ").gold(player.getStat(Stat.CHALLENEGES_COMPLETED)).aqua(" Timed Played: ").gold(player.getTimePlayed())
				.newLine().strikethroughColor(ChatColor.GOLD, "============================================="));
		player.play(GameSound.MENU_SUCCESS);
	}

}
 