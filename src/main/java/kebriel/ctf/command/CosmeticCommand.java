package kebriel.ctf.command;

import kebriel.ctf.display.gui.menu.Cosmetics;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmeticCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(sender instanceof Player p && cmd.getName().equalsIgnoreCase("cosmetic")) {
			CTFPlayer pl = CTFPlayer.get(p);
			pl.openMenu(new Cosmetics(pl));
		}
		return false;
	}

}
