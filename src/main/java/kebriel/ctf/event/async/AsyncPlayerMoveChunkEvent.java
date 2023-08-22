package kebriel.ctf.event.async;

import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.Chunk;

public class AsyncPlayerMoveChunkEvent extends AsyncEvent {

    private final Chunk from;
    private final Chunk to;

    public AsyncPlayerMoveChunkEvent(CTFPlayer player, Chunk from, Chunk to) {
        super(player);
        this.from = from;
        this.to = to;
    }

    public Chunk fromChunkFrom() {
        return from;
    }

    public Chunk fromChunkTo() {
        return to;
    }


}
