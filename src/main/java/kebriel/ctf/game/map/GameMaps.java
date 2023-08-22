package kebriel.ctf.game.map;

import kebriel.ctf.game.Team;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.game.map.MapLocation.LocationType;
import kebriel.ctf.internal.sql.SQLManager;
import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.util.CTFLogger;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;

import java.util.*;

public class GameMaps {

	private static GameMap current;
	private static final List<GameMap> mapCache = new ArrayList<>();

	/**
	 * Method intended for creating Map objects when loading from database
	 * @param id the map's saved UUID
	 * @param name the map's regular name
	 * @return returns either a new Map object, or an existing one if it's already
	 * cached (should never occur)
	 */
	public static GameMap makeExisting(UUID id, String name) {
		return getMapByID(id) == null ? new GameMap(id, name) : getMapByID(id);
	}

	public static GameMap getNew(String name) {
		return getMapByName(name) == null ? new GameMap(name) : getMapByName(name);
	}

	public static boolean doesExist(String name) {
		return getMapByName(name) == null;
	}

	public static boolean doesExist(UUID id) {
		return getMapByID(id) == null;
	}

	public static WrappedData[] getLocDefaults() {
		return JavaUtil.typeArray(MinecraftUtil.wrapLocations(GameMap.getDefaults(null)), WrappedData.class);
	}

	public static GameMap getMapByName(String name) {
		for(GameMap map : mapCache) {
			if(map.getName().equalsIgnoreCase(name)) {
				return map;
			}
		}
		return null;
	}

	public static GameMap getMapByID(UUID id) {
		for(GameMap map : mapCache) {
			if(map.getID().equals(id)) {
				return map;
			}
		}
		return null;
	}

	public static List<GameMap> getMapCache() {
		return mapCache;
	}

	public static GameMap getCurrent() {
		return current;
	}

	public static class GameMap {

		private final String name;
		private final UUID id;
		private final Set<MapLocation> locCache;

		{
			locCache = new HashSet<>(getDefaults(this));
		}

		public static List<MapLocation> getDefaults(GameMap map) {
			List<MapLocation> result = new ArrayList<>();
			for(Team t : Teams.getTeams())
				for(LocationType type : LocationType.values())
					result.add(MapLocation.getEmpty(type, map, t.getTeamColor()));

			return result;
		}

		/**
		 * Creates a new Map with a new UUID
		 * @param name
		 */
		private GameMap(String name) {
			this(UUID.randomUUID(), name);
		}

		private GameMap(UUID id, String name) {
			this.id = id;
			this.name = name;

			mapCache.add(this);
		}

		public void saveMap() {
			SQLManager.setMap(this, MinecraftUtil.wrapLocations(locCache));
		}

		public void makeCurrent() {
			current = this;
		}

		public void deleteMap() {
			SQLManager.deleteMap(this);
		}

		public String getName() {
			return name;
		}

		public UUID getID() {
			return  id;
		}

		public MapLocation getLocation(TeamColor teamColor, LocationType type) {
			for(MapLocation loc : locCache) {
				if(loc.getType() == type && loc.getWhichTeam() == teamColor) {
					return loc;
				}
			}
			return null;
		}

		public MapLocation getLocationByName(String name) {
			for(MapLocation loc : locCache) {
				if(loc.getName().equalsIgnoreCase(name)) {
					return loc;
				}
			}
			CTFLogger.logError("Failed location search for '" + name + "', is it syntactically correct?");
			return null;
		}

		public Set<MapLocation> getLocations() {
			return locCache;
		}

		public List<MapLocation> getLocationsByType(LocationType type) {
			List<MapLocation> locs = new ArrayList<>();
			for(MapLocation loc : locCache) {
				if(loc.getType() == type)locs.add(loc);
			}
			return locs;
		}

	}

}
