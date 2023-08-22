package kebriel.ctf.game.flag;

import kebriel.ctf.entity.entities.game.EntityFlag;
import kebriel.ctf.event.async.AsyncFlagTakeEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.flag.Flag.FlagStatus;
import kebriel.ctf.internal.nms.event.AsyncPacketEventEntityInteract;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.GameMode;

/**
 * This class listens to the packet event that's thrown when
 * a player interacts with a flag, and handles that interaction
 * appropriately. It does by either sending appropriate messages
 * (clicking your own flag, trying to pick up a flag when
 * already holding on), or progressing the interaction by triggering
 * FlagTakeEvent
 */
public class Interact implements EventReactor {

    static {
        EventReaction.register(new Interact());
    }

    /**
     * Handle players clicking flags -- translate action
     * into AsyncFlagTakeEvent if appropriate
     * @param event
     */
    @EventReact(allowedWhen = GameStage.IN_GAME)
    public void onPacketHit(AsyncPacketEventEntityInteract event) {
        if(event.getEntity() instanceof EntityFlag flagEntity) {
            Flag flag = flagEntity.getFlag();
            CTFPlayer hitter = event.getPlayer();
            if(hitter.getBukkitPlayer().getGameMode() == GameMode.SPECTATOR)
                return;

            // It's your flag and it's not dropped
            if(hitter.getTeam().equals(flag.getTeam()) && flag.getStatus() != FlagStatus.DROPPED) {
                hitter.send(GameMessage.GAME_FLAG_CANT_TAKE_YOURS);
                return;
            }

            // You're already holding a flag, any flag
            if(hitter.getState().isHoldingFlag()) {
                hitter.send(GameMessage.GAME_FLAG_ALREADY_HOLDING);
                return;
            }

            CTFEvent.fireEvent(new AsyncFlagTakeEvent(flag, hitter));
        }
    }
}
