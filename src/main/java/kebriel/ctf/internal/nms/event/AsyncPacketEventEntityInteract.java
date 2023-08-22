package kebriel.ctf.internal.nms.event;

import kebriel.ctf.entity.components.EntityWrapper;
import kebriel.ctf.entity.entities.game.EntityNPC;
import kebriel.ctf.event.async.AsyncInteractNPCEvent;
import kebriel.ctf.player.CTFPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity.EnumEntityUseAction;

public class AsyncPacketEventEntityInteract extends PacketEvent {

    public enum InteractType {
        LEFT_CLICK, RIGHT_CLICK
    }

    private final EntityWrapper<?> entity;
    private final EnumEntityUseAction action;
    private boolean cancelled;

    public AsyncPacketEventEntityInteract(CTFPlayer player, EntityWrapper<?> entity, EnumEntityUseAction action) {
        super(player);
        this.entity = entity;
        this.action = action;

        if(entity instanceof EntityNPC npc)
            fireEvent(new AsyncInteractNPCEvent(npc, player));
    }

    public EntityWrapper<?> getEntity() {
        return entity;
    }

    public InteractType getInteractType() {
        return switch (action) {
            case INTERACT, INTERACT_AT -> InteractType.RIGHT_CLICK;
            case ATTACK -> InteractType.LEFT_CLICK;
        };
    }

}
