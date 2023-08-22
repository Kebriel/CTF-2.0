package kebriel.ctf.event.async;

import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;

public class AsyncPlayerMoveBlockEvent extends AsyncEvent {

    private final Location from;
    private final Location to;

    public AsyncPlayerMoveBlockEvent(CTFPlayer player, Location from, Location to) {
        super(player);
        this.from = from;
        this.to = to;

        if(to.getX() != from.getX() || to.getZ() != from.getZ()) {
            Chunk cFrom = from.getChunk();
            Chunk cTo = to.getChunk();

            if (!MinecraftUtil.areChunksEqual(cFrom, cTo))
                CTFEvent.fireEvent(new AsyncPlayerMoveChunkEvent(player, cFrom, cTo));
        }
    }

    public Location getLocFrom() {
        return from;
    }

    public Location getLocTo() {
        return to;
    }
}
