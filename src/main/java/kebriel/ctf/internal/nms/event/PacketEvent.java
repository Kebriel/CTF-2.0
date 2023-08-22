package kebriel.ctf.internal.nms.event;

import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.player.CTFPlayer;

public abstract class PacketEvent extends AsyncEvent {

    public PacketEvent(CTFPlayer player) {
        super(player);
    }
}
