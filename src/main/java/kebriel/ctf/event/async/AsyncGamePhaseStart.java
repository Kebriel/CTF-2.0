package kebriel.ctf.event.async;

import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.game.component.phase.GamePhase;

public class AsyncGamePhaseStart extends AsyncEvent {

    private final GamePhase phase;

    public AsyncGamePhaseStart(GamePhase phase) {
        this.phase = phase;
    }

    public GamePhase getPhase() {
        return phase;
    }
}
