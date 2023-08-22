package kebriel.ctf.event.async;

import kebriel.ctf.entity.entities.game.EntityNPC;
import kebriel.ctf.event.async.components.AsyncEvent;
import kebriel.ctf.player.CTFPlayer;

public class AsyncInteractNPCEvent extends AsyncEvent {

    private final EntityNPC npc;

    public AsyncInteractNPCEvent(EntityNPC npc, CTFPlayer interact) {
        super(interact);
        this.npc = npc;
    }

    public EntityNPC getNPC() {
        return npc;
    }
}
