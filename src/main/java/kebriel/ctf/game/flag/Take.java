package kebriel.ctf.game.flag;

import kebriel.ctf.event.async.AsyncFlagTakeEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;

public class Take implements EventReactor {

    static {
        EventReaction.register(new Take());
    }

    @EventReact(allowedWhen = GameStage.IN_GAME)
    public void onFlagTake(AsyncFlagTakeEvent event) {

    }
}
