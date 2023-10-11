package kebriel.ctf.internal.sql;

import kebriel.ctf.Constants;
import kebriel.ctf.game.map.MapLocation;
import kebriel.ctf.game.map.GameMaps;
import kebriel.ctf.game.map.GameMaps.GameMap;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.sql.SQLHelper.ConditionType;
import kebriel.ctf.internal.sql.Table.SQLDataType;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import kebriel.ctf.util.CTFLogger;
import kebriel.ctf.util.JavaUtil;

import java.util.*;

public class SQLManager {

    public static void saveMaps() {
        for(GameMap map : GameMaps.getMapCache()) {
            map.saveMap();
        }
    }

    public static void loadMaps() {
        AsyncExecutor.doTask(() -> {
            WrappedStatement statement = SQLHelper.getAllRows(Table.MAPS);

            // Hold the current thread here until all data is successfully fetched
            statement.waitForFinish();

            // Load Hub's coordinates
            List<WrappedData> hubRow = statement.getRowWithValue(new WrappedData("name", Constants.HUB.getName()));
            for(int coordNum = 0; coordNum < 3; coordNum++)
                Constants.HUB.fillCoord(coordNum, statement.getValueOfField(hubRow, Constants.HUB.formatLocFieldArray()[coordNum]));

            // Load each map
            for(List<WrappedData> row : statement.getAllRows()) {
                GameMap m = GameMaps.makeExisting(statement.getValueOfField(row, "id"), statement.getValueOfField(row, "name"));
                for(MapLocation loc : m.getLocations()) { // For every type of location, fill in the xyz
                    String[] coords = loc.formatLocFieldArray();
                    for(int coordNum = 0; coordNum < 3; coordNum++)
                        loc.fillCoord(coordNum, statement.getValueOfField(row, coords[coordNum]));
                }
            }

            if(GameMaps.getMapCache().isEmpty()) { // Maps were succesfully found and loaded
                CTFLogger.logWarning("No maps were found to be loaded");
                return;
            }

            CTFLogger.logDebug("All maps loaded!");
        });
    }

    /*
     * Checking existence of row prevents unnecessary errors and helps avoid concurrency issues
     *
     * For example, if the Map was deleted in the database from another source but
     * we're not aware of it, the row therefor won't exist and can't be deleted.
     */
    public static void deleteMap(GameMap map) {
        SQLHelper.doWithCondition(ConditionType.ROW_EXISTS, Table.MAPS, map.getID(), exists -> {
            if(exists)
                SQLHelper.deleteRow(Table.MAPS, map.getID());
        });
    }

    public static void setMap(GameMap map, List<WrappedData> locs) {
        SQLHelper.doWithCondition(ConditionType.ROW_EXISTS, Table.MAPS, map.getID(), exists -> {
            if(exists) {
                SQLHelper.setValue(Table.MAPS, map.getID(), JavaUtil.typeArray(locs, WrappedData.class));
            }else{
                WrappedStatement createRow = SQLHelper.createRow(Table.MAPS, map.getID());
                createRow.waitForFinish();
                SQLHelper.setValue(Table.MAPS, map.getID(), JavaUtil.typeArray(locs, WrappedData.class));
            }
        });
    }

    public static void setHub() {
        SQLHelper.doWithCondition(ConditionType.ROW_EXISTS, Table.MAPS, Constants.HUB_NAME, exists -> {
            WrappedData[] locs = Constants.HUB.wrapLoc();
            if(exists) {
                SQLHelper.setValue(Table.MAPS, new WrappedData("name", Constants.HUB_NAME), locs);
            }else{
                WrappedStatement createRow = SQLHelper.createRow(Table.MAPS, Constants.HUB_NAME);
                createRow.waitForFinish();
                SQLHelper.setValue(Table.MAPS, new WrappedData("name", Constants.HUB_NAME), locs);
            }
        });
    }

    public static void setupForPlayer(UUID id) {
        SQLHelper.doWithCondition(ConditionType.ROW_EXISTS, Table.PLAYERS, id, exists -> {
            if(!exists)
                SQLHelper.createRow(Table.PLAYERS, id);
        });
    }

    /**
     * Intended to be used for when a new Stat is implemented, and players
     * already have established data
     * @param stat
     * @param id
     */
    public static void addNewStat(Stat stat, UUID id) {
        if(stat.isTemporary()) return;
        SQLHelper.doWithCondition(ConditionType.ROW_EXISTS, Table.PLAYERS, id, exists -> {
            if(!exists)
                SQLHelper.createValueType(Table.PLAYERS, WrappedData.fromStat(stat));
        });
    }

    private static void setFor(UUID id, WrappedData... stats) {
        SQLHelper.doWithCondition(ConditionType.COLUMN_EXISTS, Table.PLAYERS, id, exists -> {
            if(exists) {
                SQLHelper.setValue(Table.PLAYERS, id, stats);
            }else{
                WrappedStatement createRow = SQLHelper.createRow(Table.PLAYERS, id);
                createRow.waitForFinish();
                SQLHelper.setValue(Table.PLAYERS, id, stats);
            }
        });
    }

    public static void loadStatsForPlayer(UUID id) {
        AsyncExecutor.doTask(() -> {
            CTFPlayer profile = CTFPlayer.get(id);
            WrappedStatement getStat = SQLHelper.getRow(Table.PLAYERS, id);
            getStat.waitForFinish();
            for(Stat type : Stat.getPermStats())
                profile.setStat(type, getStat.getValueOfField(1, type.getID()));
        });
    }

    public static void saveStatsForPlayer(CTFPlayer prof) {
        setFor(prof.getUUID(), JavaUtil.typeArray(prof.getWrappedStats(), WrappedData.class));
    }

    public static class WrappedData {

        private Object value;
        private Object valueSQL;
        private Object defaultValue;
        private String name;
        private SQLDataType type;

        public static WrappedData wrapEmpty(String name, Object defaultValue) {
            return new WrappedData(name, defaultValue, defaultValue);
        }

        public static WrappedData fromStat(Stat type) {
            return new WrappedData(type.getID(), type.getDefaultValue(), type.getDefaultValue());
        }

        public static WrappedData[] wrapStats(Stat... stats) {
            List<WrappedData> wrapped = new ArrayList<WrappedData>();
            for(Stat stat : stats) {
                wrapped.add(fromStat(stat));
            }
            return JavaUtil.typeArray(wrapped, WrappedData.class);
        }

        public static WrappedData[] wrapNames(String... valueNames) {
            List<WrappedData> wrapped = new ArrayList<WrappedData>();
            for(String s : valueNames) {
                wrapped.add(new WrappedData(s));
            }
            return JavaUtil.typeArray(wrapped, WrappedData.class);
        }

        public WrappedData(String name, Object value, Object defaultValue) {
            this(name, value);
            this.defaultValue = defaultValue;
        }

        public WrappedData(String name, Object value) {
            this(value);
            this.name = name;
        }

        public WrappedData(Object value) {
            this.value = value;

            if (value instanceof UUID) type = SQLDataType.UUID;
            if (value instanceof Integer) type = SQLDataType.INT;
            if (value instanceof String) type = SQLDataType.VARCHAR;
            if (value instanceof Boolean) type = SQLDataType.BOOL;
            if (value instanceof Double) type = SQLDataType.DOUBLE;
            if (value instanceof Long) type = SQLDataType.BIGINT;

            valueSQL = value instanceof UUID id ? JavaUtil.convertUUIDToBytes(id) : value;
        }

        public WrappedData(String name) {
            this.name = name;
        }

        public Object getValueNormal() {
            return value;
        }

        public <T> T getValueAbsolute() {
            try {
                return (T) value;
            } catch(NullPointerException ex) {
                return null;
            } catch(ClassCastException ex) {
                throw new ClassCastException("An attempt was made to parse a non-primitive type as primitive");
            }
        }

        public Object getValueForSQL() {
            return valueSQL;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String getName() {
            return name;
        }
        
        public SQLDataType getSQLDataType() {
            return type;
        }

        public Class<?> getRawDataType() {
            return JavaUtil.getRawType(value);
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String formatSQLValue() {
            if(value == null) return null;
            String str = getValueForSQL().toString();
            if(getValueForSQL() instanceof String) {
                str = "'" + str + "'";
            }
            return str;
        }

        public String formatSQLFullValue() {
            if (value == null) return null;
            return name + " = " + formatSQLValue();
        }

        public String formatSQLTypeStatement() {
            return name + " " + formatType();
        }

        public String formatType() {
            String formatted = type.toString();
            if(type == SQLDataType.VARCHAR) formatted += "(20)";
            if(type == SQLDataType.BIGINT) formatted += "(255)";
            return formatted;
        }
    }

}
