package kebriel.ctf.game.flag;

import kebriel.ctf.entity.components.EquipmentSlots;
import kebriel.ctf.entity.entities.game.EntityFlag;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.async.AsyncFlagCaptureEvent;
import kebriel.ctf.event.async.AsyncFlagDropEvent;
import kebriel.ctf.event.async.AsyncFlagReturnEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.game.Team;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.util.ColorMappings;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

public class Flag implements EventReactor {

    public enum FlagStatus {
        SPAWN, TAKEN, DROPPED
    }

    private final EntityFlag entityFlag;
    private final Team team;
    private FlagStatus status;
    private CTFPlayer holder;
    private PlayerState holderState;
    private GamePacket heldRender;
    private volatile boolean heldRendered;

    public Flag(Team team) {
        this.team = team;
        status = FlagStatus.SPAWN;

        entityFlag = EntityFlag.newFlag(this);

        EventReaction.register(this);
    }

    public FlagStatus getStatus() {
        return status;
    }

    /**
     * Handles when a player picks up this flag, called for both picking up
     * from enemy spawn, and from the ground
     * @param player the player who picked up this flag
     */
    public void pickupFlag(CTFPlayer player) {
        status = FlagStatus.TAKEN;

        holder = player;
        holderState = holder.getState();
        renderHeld();

        entityFlag.derenderEntity();
    }

    /**
     * Called when a player drops a flag by dying
     */
    public void dropFlag() {
        CTFEvent.fireEvent(new AsyncFlagDropEvent(holder, this));
        status = FlagStatus.DROPPED;

        derenderHeld();

        entityFlag.dropAt(holder.getBukkitPlayer().getLocation());

        if(holderState.getHeldFlag() != null)
            holderState.eraseFlag();

        holder = null;
    }

    /**
     * Called when a player successfully takes this flag to
     * their team's base
     */
    public void captureFlag() {
        CTFEvent.fireEvent(new AsyncFlagCaptureEvent(holder, this));
        status = FlagStatus.SPAWN;

        derenderHeld();

        if(holderState.getHeldFlag() != null)
            holderState.eraseFlag();
        holder = null;

        entityFlag.renderEntity();
    }

    public void returnFlag() {
        CTFEvent.fireEvent(new AsyncFlagReturnEvent(holder, this, holder != null));
        status = FlagStatus.SPAWN;

        derenderHeld();

        if(holderState.getHeldFlag() != null)
            holderState.eraseFlag();
        holder = null;

        entityFlag.renderEntity();
    }

    public EntityFlag getFlagEntity() {
        return entityFlag;
    }

    public ItemStack getFlagAsItem() {
        DyeColor color = ColorMappings.chatToDyeColor(entityFlag.getTeam().getChatColor());
        org.bukkit.inventory.ItemStack stack = CTFItem.newItem(Material.BANNER).setBannerColor(color).build();
        return CraftItemStack.asNMSCopy(stack);
    }

    private void renderHeld() {
        if(!heldRendered && holder != null) {
            heldRender = new GamePacket.SetEntityEquipment(MinecraftUtil.convertBukkitPlayer(holder.getBukkitPlayer()),
                    EquipmentSlots.HELMET,
                    getFlagAsItem());
            heldRender.render();
            heldRendered = true;
        }
    }

    private void derenderHeld() {
        if(heldRendered && holder != null) {
            heldRender.derender();
            heldRendered = false;
        }
    }

    public void setup() {
        entityFlag.renderEntity();
    }

    public void gameReset() {
        status = FlagStatus.SPAWN;
        holder = null;

        if(heldRendered)
            derenderHeld();
        if(entityFlag.isRendered())
            entityFlag.derenderEntity();
    }

    public Team getTeam() {
        return team;
    }

    public CTFPlayer getHolder() {
        return holder;
    }

    public String getName() {
        return team.getChatColor() + JavaUtil.capitalizeFirstLetter(team.getNameRaw().toLowerCase()) + " Flag";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Flag flag)
            return flag.getTeam().equals(team);
        return false;
    }

}
