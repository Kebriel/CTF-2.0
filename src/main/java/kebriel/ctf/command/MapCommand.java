package kebriel.ctf.command;

import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.game.map.MapLocation;
import kebriel.ctf.game.map.MapLocation.LocationType;
import kebriel.ctf.game.map.GameMaps;
import kebriel.ctf.game.map.GameMaps.GameMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand implements CommandExecutor {

	private static final String LIST = "list";
	private static final String CREATE = "create";
	private static final String DELETE = "delete";
	private static final String SET = "set";
	private static final String INVALID_NUM = ChatColor.RED + "Please enter a valid number!";
	private static final String USAGE_MAP = ChatColor.RED + "Usage: /map <list:create:delete:set>";
	private static final String USAGE_CREATE = ChatColor.RED + "Usage: /map create <name>";
	private static final String USAGE_DELETE = ChatColor.RED + "Usage: /map delete <name>";
	private static final String USAGE_SET = ChatColor.RED + "Usage: /map set <name:hub> <team> <spawn:flag:npc>";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(!(sender instanceof Player p) || !cmd.getName().equalsIgnoreCase("map"))
			return false;

		if(!p.isOp())
			return false;

		if(args.length == 0) {
			p.sendMessage(USAGE_MAP);
			return false;
		}

		switch(args[0].toLowerCase()) {
			case LIST -> handleList(p, args);
			case CREATE -> handleCreate(p, args);
			case DELETE -> handleDelete(p, args);
			case SET -> handleSet(p, args);
			default -> p.sendMessage(USAGE_MAP);
		}

		return false;
	}

	private void handleList(Player p, String[] args) {
		if(args.length != 1) {
			p.sendMessage(USAGE_MAP);
			return;
		}

		if(GameMaps.getMapCache().isEmpty()) {
			p.sendMessage(ChatColor.GOLD + "=============================================");
			p.sendMessage(ChatColor.RED + "No maps to display!");
			p.sendMessage(ChatColor.GOLD + "=============================================");
		}else{
			p.sendMessage(ChatColor.GOLD + "=============================================");
			for(GameMap map : GameMaps.getMapCache())
				p.sendMessage(ChatColor.YELLOW + "-Name: " + ChatColor.GREEN + map.getName() + ChatColor.YELLOW + " ID: " + ChatColor.GREEN + map.getID());
			p.sendMessage(ChatColor.GOLD + "=============================================");
		}
	}

	private void handleCreate(Player p, String[] args) {
		if(args.length != 2) {
			p.sendMessage(USAGE_CREATE);
			return;
		}

		String name = args[1];
		if(GameMaps.getMapByName(name) != null) {
			p.sendMessage(ChatColor.RED + "A map with that name or ID already exists!");
			return;
		}

		GameMaps.getNew(name);
		p.sendMessage(ChatColor.GREEN + "Map '" + name + "' created!");
	}

	private void handleDelete(Player p, String[] args) {
		if(args.length != 2) {
			p.sendMessage(USAGE_DELETE);
			return;
		}

		GameMap map = GameMaps.getMapByName(args[1]);
		if(map == null) {
			p.sendMessage(ChatColor.RED + "Invalid map '" + args[1] + "'");
			return;
		}

		map.deleteMap();
		p.sendMessage(ChatColor.GREEN + "Map '" + args[1] + "' deleted!");
	}

	private void handleSet(Player p, String[] args) {
		if(args.length < 2) {
			p.sendMessage(USAGE_SET);
			return;
		}

		if(args[1].equalsIgnoreCase("hub")) {
			MapLocation.setHub(p.getLocation());
			p.sendMessage(ChatColor.GREEN + "Hub set to your location!");
			return;
		}

		if(args.length != 4) {
			p.sendMessage(USAGE_SET);
			return;
		}

		GameMap map = GameMaps.getMapByName(args[1]);
		if(map == null) {
			p.sendMessage(ChatColor.RED + "Invalid map '" + args[1] + "'");
			return;
		}

		TeamColor t = null;
		try {
			t = TeamColor.valueOf(args[2].toUpperCase());
		}catch(IllegalArgumentException e) {
			p.sendMessage(ChatColor.RED + "Invalid team '" + args[2] + "'");
			return;
		}

		LocationType l = null;
		try {
			l = LocationType.valueOf(args[3].toUpperCase());
		}catch(IllegalArgumentException e) {
			p.sendMessage(ChatColor.RED + "Invalid location type '" + args[3] + "'");
			return;
		}

		map.getLocation(t, l).fillLocation(p.getLocation());
		p.sendMessage(ChatColor.GREEN + "Set " + args[2] + " team's " + args[3] + " location to your current location");
	}
}
