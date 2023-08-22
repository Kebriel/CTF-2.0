package kebriel.ctf.internal.sql;

import kebriel.ctf.game.map.GameMaps;
import kebriel.ctf.internal.sql.SQLHelper.ConditionType;
import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.player.Stat;

/**
 * A utility constant representing tables, or rather, each type of table (player stats,
 * maps, etc.) -- aims to give easy structure to database interaction
 */
public enum Table {

    PLAYERS(WrappedData.wrapStats(Stat.getPermStats())),
    MAPS(GameMaps.getLocDefaults());

    private final WrappedData[] columns;

    Table(WrappedData... columns) {
        this.columns = columns;
        verifyTable();
    }

    /**
     * Is run immediately upon plugin's initialization, ensures that the
     * defined table exists and can be interacted with
     */
    private void verifyTable() {
        SQLHelper.doWithCondition(ConditionType.TABLE_EXISTS, null, getName(), exists -> {
            if(!exists)
                SQLHelper.createTable(this);
        });
    }

    public WrappedData[] getColumns() {
        return columns;
    }

    public WrappedData getColumn(String name) {
        for(WrappedData col : columns) {
            if(col.getName().equals(name)) return col;
        }
        return null;
    }

    public String getName() {
        return this.toString();
    }

    /**
     * Datatypes to read from and write to SQL databases, named so
     * that they can be translated directly into queries
     */
    public enum SQLDataType {
        VARCHAR, INT, BOOL, DOUBLE, BIGINT, UUID
    }
}
