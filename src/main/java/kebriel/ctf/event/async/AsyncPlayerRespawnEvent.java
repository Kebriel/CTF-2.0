package kebriel.ctf.event.async;

import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.player.CTFPlayer;

public class AsyncPlayerRespawnEvent extends AsyncEvent {

    public AsyncPlayerRespawnEvent(CTFPlayer player) {
        super(player);
    }
}
