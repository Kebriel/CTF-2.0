package kebriel.ctf.game.map;

import kebriel.ctf.game.map.GameMaps.GameMap;
import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapLocation {

    public enum LocationType {
        HUB, FLAG, NPC, SPAWN
    }

    private static final List<MapLocation> cache = new ArrayList<>();

    private Location loc;
    private final LocationType type;
    private final TeamColor teamColor;
    private final GameMap map;

    public static MapLocation get(Location loc, LocationType type, GameMap map, TeamColor teamColor) {
        MapLocation result = getByComponents(loc, type, map, teamColor);
        if(result != null)
            return result;

        result = getEmptyByComponent(type, map, teamColor);
        if(result != null)
            return result;

        if(!doesExist(loc, type, map, teamColor))
            result = new MapLocation(loc, type, map, teamColor);

        return result;
    }

    public static MapLocation getEmpty(LocationType type, GameMap map, TeamColor teamColor) {
        return getEmptyByComponent(type, map, teamColor);
    }

    public static MapLocation getHub() {
        return getEmpty(LocationType.HUB, null, null);
    }

    public static void setHub(Location loc) {
        MapLocation hub = getByComponents(null, LocationType.HUB, null, null);
        if(hub != null)
            hub.fillLocation(loc);
    }

    private static MapLocation getByComponents(Location loc, LocationType type, GameMap map, TeamColor teamColor) {
        for(MapLocation ctfloc : cache) {
            // Immediately returns Hub if found
            if(type == LocationType.HUB && ctfloc.getType() == type)
                return ctfloc;
            // Check equality with significant precision
            if (JavaUtil.arePreciseEqual(loc, ctfloc.getBukkitLocation(), 0.05)
                    && ctfloc.getMap().equals(map)
                    && ctfloc.getType() == type
                    && ctfloc.getWhichTeam() == teamColor) {
                return ctfloc;
            }
        }
        return null;
    }

    private static MapLocation getEmptyByComponent(LocationType type, GameMap map, TeamColor teamColor) {
        for(MapLocation ctfloc : cache)
            if(ctfloc.getMap().equals(map) && ctfloc.getType() == type && ctfloc.getWhichTeam() == teamColor)
                return ctfloc;
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MapLocation ctfloc)
            return JavaUtil.arePreciseEqual(loc, ctfloc.getBukkitLocation(), 0.05)
                    && ctfloc.getType() == type
                    && ctfloc.getWhichTeam() == teamColor;
        return false;
    }

    /**
     * Utility method, likely only used in database interaction
     * @return returns a String list of all possible types of location,
     * i.e. 'BLUE_FLAG', 'RED_SPAWN', etc.
     */
    public static List<String> getAllLocNames() {
        List<String> values = new ArrayList<String>();
        for(TeamColor t : TeamColor.values()) {
            for(LocationType l : LocationType.values()) {
                if(l == LocationType.HUB) continue;
                values.add(t.toString() + "_" + l.toString());
            }
        }
        return values;
    }

    private static boolean doesExist(Location loc, LocationType type, GameMap map, TeamColor teamColor) {
        return getByComponents(loc, type, map, teamColor) != null;
    }

    private MapLocation(Location loc, LocationType type, GameMap map, TeamColor teamColor) {
        this.loc = loc;
        this.type = type;
        this.map = map;
        this.teamColor = teamColor;

        cache.add(this);
    }

    public void fillLocation(Location loc) {
        this.loc = loc;
    }

    public void fillCoord(int coord, double value) {
        switch(coord) {
            case 1 -> loc.setX(value);
            case 2 -> loc.setY(value);
            case 3 -> loc.setZ(value);
            default -> throw new IllegalArgumentException("Invalid coordinate, must be an integer 1-3");
        }
    }

    public double[] fillAndGetCoordArray() {
        double[] coords = new double[3];
        if (!isEmpty()) {
            coords[0] = loc.getX();
            coords[1] = loc.getY();
            coords[2] = loc.getZ();
        } else {
            Arrays.fill(coords, 0.0);
        }
        return coords;
    }

    public GameMap getMap() {
        return map;
    }

    public Location getBukkitLocation() {
        if(isEmpty())
            return null;
        return loc;
    }

    public LocationType getType() {
        return type;
    }

    public TeamColor getWhichTeam() {
        return teamColor;
    }

    public String getName() {
        if(type == LocationType.HUB)
            return "hub";
        return teamColor.toString() + "_" + type.toString();
    }

    public String[] formatLocFieldArray() {
        String[] formatted = new String[3];
        for(int i = 0; i < 3; i++)
            formatted[i] = formatSingleField(i);
        return formatted;
    }

    public boolean isEmpty() {
        return loc != null && (loc.getX() != 0 || loc.getY() != 0 || loc.getZ() != 0);
    }

    /**
     * Gets the formatted name of this location's row
     * @param coordType an integer, 1-3, that is used to determine whether
     * this is an x, y or z coordinate
     * @return returns the literal name of this location's team and type, with
     * its coordinate identifier suffixed behind an underscore
     */
    public String formatSingleField(int coordType) {
        String c = "";
        switch(coordType) {
            case 1 -> c+="X";
            case 2 -> c+="Y";
            case 3 -> c+="Z";
            default -> throw new IllegalArgumentException("Invalid coordinate, must be an integer 1-3");
        }
        return getName()+c;
    }

    /**
     * Converts this object into a WrappedData array that is easily
     * readable by SQL interaction code
     *
     * Structured as so:
     * Array index 0 = {TEAMCOLOR_NAME + "_" + LOCATIONTYPE_NAME + "X/Y/Z"} X, {actual_value}, {default_value}
     * [...]
     *
     * Formatting of the first field is done by formatSingleField()
     *
     * For example:
     * Array index 0 = new WrappedData(BLUE_SPAWNX, 341.5, 0.0)
     * [...]
     */
    public WrappedData[] wrapLoc() {
        WrappedData[] data = new WrappedData[3];

        double[] coords = fillAndGetCoordArray();
        double[] defaults = new double[3];
        Arrays.fill(defaults, 0.0);

        for(int i = 0; i < coords.length; i++)
            data[i] = (new WrappedData(formatSingleField(i), coords[i], defaults[i]));

        return data;
    }

}
