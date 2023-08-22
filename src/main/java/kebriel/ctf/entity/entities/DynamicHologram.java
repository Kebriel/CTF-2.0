package kebriel.ctf.entity.entities;

import io.netty.util.internal.ConcurrentSet;
import kebriel.ctf.entity.components.RenderOnly;
import kebriel.ctf.entity.components.Rotatable;
import kebriel.ctf.internal.nms.GamePacket.SpawnEntity;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Set;

/**
 * Advanced Hologram type that allows for the illusion of
 * different players seeing different text on this Hologram,
 * without needing to create and keep track of different Hologram
 * instances for different sets of players
 */
@RenderOnly
public class DynamicHologram extends Hologram implements Rotatable<DynamicHologram> {

    private final Set<DisplayNode> displays;
    private Location loc;

    private boolean rendered;

    {
        displays = new ConcurrentSet<>();
    }

    public DynamicHologram(Location loc, String mainDisplay) {
        super(loc, mainDisplay);
    }

    public DynamicHologram(Location loc) {
        super(loc, "");
        this.loc = loc;
    }

    /**
     *
     * @param display the text that should be displayed
     * @param offset the xyz offset of this display in relation to the
     *               original location
     * @param shouldSee the specific players who should see this display.
     *                  If no arguments are given, defaults to showing it
     *                  ubiquitously to the lobby, as per GamePacket's rendering rules
     * @return returns the new DisplayNode for reference
     */
    public DisplayNode newDisplay(String display, Vector offset, Collection<CTFPlayer> shouldSee) {
        Location easyLoc = loc.clone().add(0, 2, 0); // Eye level of the hologram to minimize silly mistakes
        DisplayNode node = new DisplayNode(this, easyLoc.add(offset), display, shouldSee);
        displays.add(node);
        return node;
    }

    public DisplayNode newDisplay(String display, Vector offset) {
        Location easyLoc = loc.clone().add(0, 2, 0);
        DisplayNode node = new DisplayNode(this, easyLoc.add(offset), display);
        displays.add(node);
        return node;
    }

    public DisplayNode newLine(String display, Collection<CTFPlayer> shouldSee) {
        int lines = 0;
        for(DisplayNode node : displays)
            if(node.isLine())
                lines++;

        return newDisplay(display, new Vector(0, -(lines*0.2), 0), shouldSee).asLine();
    }

    public Set<DisplayNode> getDisplays() {
        return displays;
    }

    public DisplayNode getDisplayByID(int id) {
        for(DisplayNode node : displays)
            if(node.getNodeID() == id) return node;
        return null;
    }

    public boolean hasDisplay(DisplayNode node) {
        return displays.contains(node);
    }

    public boolean hasDisplay(int id) {
        return getDisplayByID(id) != null;
    }

    /**
     * Changes for all display nodes, in bulk, which players should be allowed to see
     * them rendered
     */
    public void addVisibleTo(CTFPlayer... players) {
        for(DisplayNode node : displays)
            node.getRender().addReceivers(players);
        refreshEntity();
    }

    /**
     * Changes for all display nodes, in bulk, which players should be allowed to see
     * them rendered
     */
    public void removeVisibleTo(CTFPlayer... players) {
        for(DisplayNode node : displays)
            node.getRender().removeReceivers(players);
        refreshEntity();
    }

    public void removeDisplay(DisplayNode node) {
        displays.remove(node);
        node.derenderEntity();
    }

    public void wipeNodes() {
        displays.forEach(node -> {
            if(node.isRendered())
                node.derenderEntity();
        });
        displays.clear();
    }

    @Override
    public void renderEntity() {
        if(rendered) return;
        super.renderEntity();
        for(DisplayNode node : displays) {
            node.renderEntity();
        }
        rendered = true;
    }

    @Override
    public void derenderEntity() {
        if(!rendered) return;
        super.derenderEntity();
        for(DisplayNode node : displays) {
            node.derenderEntity();
        }
        rendered = false;
    }

    @Override
    public void refreshEntity() {
        if(!rendered) return;
        super.refreshEntity();
        for(DisplayNode node : displays) {
            node.refreshEntity();
        }
    }

    public static class DisplayNode extends Hologram implements Rotatable<DisplayNode> {

        private final DynamicHologram owner;
        private final int id;
        private final SpawnEntity spawnRender;
        private boolean line;

        private DisplayNode(DynamicHologram owner, Location loc, String display, Collection<CTFPlayer> players) {
            this(owner, loc, display);
            setPlayers(players);
        }

        private DisplayNode(DynamicHologram owner, Location loc, String display) {
            super(loc, display);
            this.owner = owner;
            id = super.getEntityID();
            spawnRender = super.getRender();
        }

        public int getNodeID() {
            return id;
        }

        /**
         * Allows for clicks on these entities to be registered
         * instead as clicks on the DynamicHologram to which they
         * belong, alongside other behavior
         * @return the id of their owner
         */
        @Override
        public int getEntityID() {
            return owner.getEntityID();
        }

        public DisplayNode asLine() {
            line = true;
            return this;
        }

        public void setPlayers(Collection<CTFPlayer> players) {
            spawnRender.setReceivers(players);
            refreshEntity();
        }

        public void addPlayer(CTFPlayer player) {
            spawnRender.addReceiver(player);
            refreshEntity();
        }

        public void removePlayer(CTFPlayer player) {
            spawnRender.removeReceiver(player);
            refreshEntity();
        }

        public void clearPlayers() {
            spawnRender.clearReceivers();
            refreshEntity();
        }

        public boolean isLine() {
            return line;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof DisplayNode node)
                return node.getNodeID() == id;
            return false;
        }
    }
}
