package kebriel.ctf.entity.entities.game;

import com.mojang.authlib.properties.PropertyMap;
import kebriel.ctf.display.gui.menu.Overview;
import kebriel.ctf.entity.components.EntityWrapper;
import kebriel.ctf.entity.components.RenderOnly;
import kebriel.ctf.entity.entities.DynamicHologram;
import kebriel.ctf.event.async.AsyncInteractNPCEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.Team;
import kebriel.ctf.game.map.MapLocation;
import kebriel.ctf.internal.APIQuery;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RenderOnly
public class EntityNPC extends EntityWrapper<NPCBase> implements EventReactor {

    private static final UUID SKIN_UUID = UUID.fromString("c7fddb9a-adfd-4be3-b326-d700df252e88");

    private final DynamicHologram display;
    private final Team team;
    private final PropertyMap properties;

    public EntityNPC(MapLocation loc, Team team) {
        super(NPCBase.class, loc.getBukkitLocation());
        this.team = team;

        super.getEntity().setCustomName(Text.get().boldColor(ChatColor.GOLD, "Abilities").toString());
        display = new DynamicHologram(loc.getBukkitLocation());
        display.newDisplay("Manage abilities", new Vector(0, -0.25, 0), team.getPlayers());

        EventReaction.register(this);
        properties = getEntity().getProfile().getProperties();
        querySkin();
    }

    private void querySkin() {
        AsyncExecutor.doTask(() -> {
            try {
                // get() blocks until finished
                // If false, skin fetching was a failure for whatever reason
                if(APIQuery.querySkinFromUUID(SKIN_UUID, properties).get()) {
                    if(isRendered())
                        refreshEntity(); // Load skin
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public void renderEntity() {
        super.renderEntity();
        display.renderEntity();
    }

    @Override
    public void derenderEntity() {
        super.derenderEntity();
        display.derenderEntity();
    }

    @EventReact(allowedWhen = GameStage.IN_MAP)
    public void onClick(AsyncInteractNPCEvent event) {
        CTFPlayer player = event.getPlayer();
        if(!event.getNPC().equals(this))
            return;

        if(getTeam().equals(player.getTeam()))
            player.openMenu(new Overview(player));
    }
}
