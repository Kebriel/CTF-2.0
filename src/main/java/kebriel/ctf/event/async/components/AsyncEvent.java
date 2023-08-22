package kebriel.ctf.event.async.components;

import kebriel.ctf.player.CTFPlayer;

public abstract class AsyncEvent extends CTFEvent {

    public AsyncEvent(CTFPlayer player) {
        super(player);
    }

    public AsyncEvent() {}

    public void postProcess() {}
    public void preProcess() {}
}
