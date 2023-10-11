package kebriel.ctf.event.async.components;

import kebriel.ctf.player.CTFPlayer;

public abstract class AsyncEvent extends CTFEvent {

    public AsyncEvent(CTFPlayer player) {
        super(player);
    }

    public AsyncEvent() {}

    public abstract void postProcess();
    public abstract void preProcess();
}
