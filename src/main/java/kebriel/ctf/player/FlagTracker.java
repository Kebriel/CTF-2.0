package kebriel.ctf.player;

import kebriel.ctf.ability.PerkUntrackable;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.async.AsyncPlayerMoveBlockEvent;
import kebriel.ctf.event.async.components.FlagEvent;
import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.game.flag.Flag.FlagStatus;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Location;

public class FlagTracker implements EventReactor {

    private static final Location SPINNING_COMPASS = new Location(MinecraftUtil.getBukkitWorld(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final CTFPlayer player;
    private Flag flag;

    public FlagTracker(CTFPlayer player) {
        this.player = player;

        EventReaction.register(this);
    }

    public boolean setTarget(Flag flag) {
        if(this.flag != null && this.flag.equals(flag)) return false;
        this.flag = flag;
        updateTracking();
        updateItem();
        return true;
    }

    private void updateTracking() {
        if(flag == null) return;
        if(flag.getHolder() != null && flag.getHolder().getIsAbilitySelected(PerkUntrackable.class)
                && !flag.getHolder().getTeam().equals(player.getTeam())) return; // Players are able to track flags held by their untrackable teammates
        player.getBukkitPlayer().setCompassTarget(flag.getStatus() == FlagStatus.TAKEN ? flag.getHolder().getLocation() : flag.getFlagEntity().getLocation());
    }

    private void updateItem() {
        String tracking = flag == null ? Text.get().gray("Nothing").toString() : flag.getName();
        PlayerState.getFor(player).TRACKER.getUnbuiltItem().setName("Tracking: " + tracking);
    }

    public void reset() {
        flag = null;
        player.getBukkitPlayer().setCompassTarget(SPINNING_COMPASS);
        updateItem();
    }

    public boolean isTracking() {
        return flag != null;
    }

    public Flag getTrackedFlag() {
        return flag;
    }

    @Override
    public void onFlagEvent(FlagEvent event) {
        if(flag == null) return;
        if(event.getFlag().equals(flag))
            updateTracking();
    }

    /*
     * Alleviates need for an ugly and performance inefficient repeating
     * task on the main thread
     */
    @Override
    public void onMoveBlock(AsyncPlayerMoveBlockEvent event) {
        if(flag == null || flag.getStatus() != FlagStatus.TAKEN || !event.getPlayer().equals(flag.getHolder())) return;
        player.getBukkitPlayer().setCompassTarget(event.getPlayer().getLocation());
    }

}
