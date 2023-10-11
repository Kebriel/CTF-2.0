package kebriel.ctf.event.listeners;

import kebriel.ctf.event.async.AsyncPlayerMoveChunkEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.game.Game;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class onTeleport implements Listener {

	@EventHandler
	public void onTp(PlayerTeleportEvent e) {
		CTFPlayer player = CTFPlayer.get(e.getPlayer());

		if(e.getCause() != TeleportCause.ENDER_PEARL)
			return;

		if(!Game.get().isPlaying() || player.getState().isDead() || !player.getIsSelected("item_pearl")) {
			e.setCancelled(true);
			return;
		}

		if(!MinecraftUtil.areChunksEqual(e.getTo().getChunk(), e.getFrom().getChunk()))
			CTFEvent.fireEvent(new AsyncPlayerMoveChunkEvent(player, e.getTo().getChunk(), e.getFrom().getChunk()));
	}

}
 