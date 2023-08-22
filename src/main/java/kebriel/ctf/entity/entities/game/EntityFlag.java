package kebriel.ctf.entity.entities.game;

import kebriel.ctf.entity.components.RenderOnly;
import kebriel.ctf.entity.entities.DynamicHologram;
import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.game.flag.Flag.FlagStatus;
import kebriel.ctf.game.Team;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.map.MapLocation;
import kebriel.ctf.game.map.MapLocation.LocationType;
import kebriel.ctf.game.map.GameMaps;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.concurrent.TimeUnit;

@RenderOnly
public class EntityFlag extends DynamicHologram {

    private final MapLocation spawn;
    private AsyncExecutor renderTask;
    private final Flag flag;
    private final Team team;

    private static final String SUBTEXT_ALLIED =
            Text.get().green("Your Flag").toString();
    private static final String SUBTEXT_ENEMY =
            Text.get().darkRed("Enemy Flag").toString();
    private static final String TIP_SPAWN_ALLIED =
            Text.get().underlineColor(ChatColor.AQUA, "Protect this at all costs").toString();
    private static final String TIP_SPAWN_ENEMY =
            Text.get().underlineColor(ChatColor.YELLOW, "Click to take").toString();
    private static final String TIP_DROPPED_ALLIED =
            Text.get().green("Click to recover").toString();
    private static final String TIP_DROPPED_ENEMY =
            Text.get().yellow("Click to recover").toString();

    public static EntityFlag newFlag(Flag flag) {
        if(GameMaps.getCurrent() == null)
            throw new IllegalStateException("Cannot instantiate a Flag object until the game's map has been set");

        Team t = flag.getTeam();
        return new EntityFlag(flag, t, GameMaps.getCurrent().getLocation(t.getTeamColor(), LocationType.FLAG));
    }

    private EntityFlag(Flag flag, Team team, MapLocation loc) {
        super(loc.getBukkitLocation(), "");
        this.team = team;
        spawn = loc;
        this.flag = flag;

        setDisplay(flag.getName().toUpperCase());
    }

    /**
     * Before rendering, ensure sure that all extra
     */
    @Override
    public void renderEntity() {
        if(super.isRendered())
            return;
        renderFlag();
        super.renderEntity();
    }

    /**
     * Inject cleanup of rotation/render-when-dropped task when
     * derendering
     */
    @Override
    public void derenderEntity() {
        if(!super.isRendered())
            return;
        if(renderTask != null)
            renderTask.terminate(true);
        wipeNodes(); // Reset display nodes/subtext
        super.derenderEntity();
    }

    /**
     * Renders the flag either at its team's base as a stationary upright
     * banner, or while dropped as a slowly-rotating 'interactive' banner
     */
    private void renderFlag() {
        // y -1.75 as proper visual offset in relation to the ground -- after all, this is an armor stand 'wearing' a banner on its head
        newDisplay("", new Vector(0, -1.75, 0)).setBlockWearing(this.flag.getFlagAsItem());
        renderSubtext();
        if(flag.getStatus() == FlagStatus.TAKEN)
            return;
        if(flag.getStatus() == FlagStatus.DROPPED) {
            // Creates async thread that renders the flag as rotating
            renderTask = new AsyncExecutor(t -> {
                if(flag.getStatus() != FlagStatus.DROPPED) {
                    t.terminate(true);
                    return;
                }
                rotateBy(2, 0); // JavaUtil.wrapAngle ensures yaw never becomes unusably large, aka >360
            }).doRepeating(0, JavaUtil.ticksAsMillis(1), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Renders the flag's subtext using DynamicHologram's display node system
     *
     * Has an effect only if the flag is dropped on the ground or at its team's
     * base
     */
    private void renderSubtext() {
        FlagStatus status = flag.getStatus();
        if(status == FlagStatus.TAKEN) // Failsafe -- flags have no visible status if they're on a player's head
            return;
        // Create subtexts each visible to certain players based on the team that they're in
        newDisplay(SUBTEXT_ALLIED, new Vector(0, -0.2, 0), team.getPlayers());
        newDisplay(SUBTEXT_ENEMY, new Vector(0, -0.2, 0), Teams.getAllPlayersInTeamsBesides(team));
        newDisplay(status == FlagStatus.SPAWN ? TIP_SPAWN_ALLIED : TIP_DROPPED_ALLIED, new Vector(0, -0.45, 0), team.getPlayers());
        newDisplay(status == FlagStatus.SPAWN ? TIP_SPAWN_ENEMY : TIP_DROPPED_ENEMY, new Vector(0, -0.45, 0), Teams.getAllPlayersInTeamsBesides(team));
    }

    /**
     * Raw method to force the flag entity to drop, should never be
     * run except by Flag wrapper class
     *
     * Contains somewhat significant degrees of locational logic to prevent
     * bugs associated with the flag floating, getting stuck in walls, etc.
     */
    public void dropAt(Location loc) {
        /*
         * Handle the flag being inside of a solid block
         *
         * Note that, in this case, 'above' is just the second block being taken up by the flag,
         * aka also the eye position of the holograms
         */
        if(MinecraftUtil.isSolidBlock(loc) || MinecraftUtil.isSolidBlockAbove(loc)) {
            /*
             * Find the nearest 2-block space where the flag will be able to fit
             * Realistically this should never be more than one or maybe two blocks
             * in a given direction, but technically, this process is very thorough
             * so as to avoid gamebreaking bugs in the event of an anomalous situation
             */

            // Searches effectively to bedrock
            Location nextOpenDown = MinecraftUtil.findNextOpenSpace(loc, loc.getBlockY(), 2, 'y', '-');
            // Searches to the build limit
            Location nextOpenUp = MinecraftUtil.findNextOpenSpace(loc, 255-loc.getBlockY(), 2, 'y', '+');

            // Somehow, there's not a single open space between bedrock and the build limit -- automatically return the flag
            if(nextOpenDown == null && nextOpenUp == null) {
                flag.returnFlag();
                return;
                // Next -- easily choose between down or up if the other has returned no available space
            }else if(nextOpenDown == null) {
                setLocation(nextOpenUp);
                return;
            }else if(nextOpenUp == null){
                setLocation(nextOpenDown);
                return;
            }

            /*
             * Anomalously, the distance between the nearest 2-block space above or below is
             * precisely equal -- arbitrarily prefer upwards (there could be more logic here
             * to determine an appropriate space is absolutely needed)
             */
            if(nextOpenDown.distance(loc) == nextOpenUp.distance(loc)) {
                setLocation(nextOpenUp);
                return;
            }

            /*
             * Finally, simply compare which of the open spaces is closer and
             * send the flag there
             */
            setLocation(nextOpenDown.distance(loc) > nextOpenUp.distance(loc) ? nextOpenUp : nextOpenDown);
            return;
        }

        /*
         * Handles the flag not being on top of a solid block, e.g. tall grass,
         * vines, a ladder, or something else comparable that would result in weird
         * 'floating' visuals for the flag if a player died with it mid-air, or while
         * descending a ladder, or whatever
         *
         * In the future: replace immediate teleportation of flag to nearest solid block downwards
         * with a gradual but smooth 'falling' animation
         */
        if(!MinecraftUtil.isSolidBlockBelow(loc)) {
            // Go downwards to simulate the flag 'falling' through the non-solid block
            Location down = MinecraftUtil.findNextOpenSpace(loc, loc.getBlockY(), 1, 'y', '-');
            if(down == null) {
                // There is only either air or non-solid blocks downwards from here on the Y axis before reaching the void
                flag.returnFlag();
            }else{
                setLocation(down);
            }
        }

        renderEntity();
    }

    public Team getTeam() {
        return team;
    }

    public Flag getFlag() {
        return flag;
    }
}
