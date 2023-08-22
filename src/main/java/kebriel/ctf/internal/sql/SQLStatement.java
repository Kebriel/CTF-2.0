package kebriel.ctf.internal.sql;

import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.internal.sql.WrappedStatement.ExecutionType;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract class the subclasses of which are each meant to represent
 * a common and useful SQL statement. They are outfitted with appropriate
 * fields and methods to make said statement as simple and foolproof
 * to use as possible
 */
public abstract class SQLStatement {

    // Text placeholder intended to be replaced by the Table the statement is interacting with
    private static final String TABLE = "#TABLE#";
    // Placeholder representing an argument in a SQL statement such as "kills BIGINT(250)"
    private static final String COLUMN_PLUS_TYPE = "column+type";
    // E.g. "kills 23" (a player has 23 kills, or the kills are being set to 23)
    private static final String COLUMN_PLUS_VALUE = "column+value";
    // A column's label, e.g. "kills"
    private static final String LABEL = "label";
    // A column's value in a row (field), e.g. "342"
    private static final String VALUE = "value";
    // A column's SQL datatype, e.g. "BOOL"
    private static final String DATA_TYPE = "type";

    // The SQL statements are first built as compounding text
    private final StringBuilder sql;
    // Any and all parameters being interacted with by the statement
    private List<WrappedData> params;
    private final Table table;
    // A condition formatted as WrappedData, if applicable
    // E.g. UPDATE {table_name} SET kills = 23 --> WHERE id = {uuid in byte format} <--
    private WrappedData condition;
    // Whether this statement was successful in whatever it's trying to accomplish
    private boolean success;
    private SQLError error;

    public SQLStatement(Table table) {
        params = new ArrayList<>();
        this.table = table;
        sql = new StringBuilder();
    }

    /**
     * Basic abstract method grabbing actual SQL from
     * subclasses
     */
    abstract void prepareSQL();

    /**
     * The 'type' of execution that this statement requires,
     * e.g. 'executeUpdate', 'execute', etc.
     * @return
     */
    abstract ExecutionType getType();

    /**
     * Final steps in readying the SQL for sending
     *
     * 1. Grab raw SQL from subclass implementation
     * 2. Replace the TABLE placeholder with the actual table in question
     * 3. Add a necessary semicolon if the statement is lacking one
     * @return
     */
    private String getSQL() {
        prepareSQL();
        insertTable();
        return JavaUtil.builderContains(sql, ";") ? sql.toString() : sql.append(";").toString();
    }

    /**
     * Replaces table placeholders with the name of the actual table
     */
    private void insertTable() {
        JavaUtil.builderReplaceAll(sql, TABLE, table.getName());
    }

    public Table getTable() {
        return table;
    }

    /**
     * Executes the SQL in accordance with its execution type, and returns
     * the ResultSet immediately (unfinished)
     */
    protected ResultSet execute(Connection connection) {
        ResultSet result = null;
        try {
            switch(getType()) {
                case EXECUTE -> connection.prepareStatement(getSQL()).execute();
                case EXECUTE_UPDATE -> connection.prepareStatement(getSQL()).executeUpdate();
                case EXECUTE_QUERY -> result = connection.prepareStatement(getSQL()).executeQuery();
            }
            success = true; // If the code reached here without exception, it's safe to presume success
        } catch(SQLException ex) {
            error = SQLError.log(ex);
        }
        return result;
    }

    public boolean wasSuccessful() {
        return success;
    }

    /**
     * Returns the SQL error object if it's desired, and if there was an error.
     * Currently lacks purpose, but could be used to design a more dynamic and
     * in-depth error-handling and error-review system in the future
     */
    public SQLError getError() {
        return error;
    }

    /**
     * Uses a stream function to format all placeholders, and
     * add any extra data to them in the process (if applicable)
     * Currently, only 'addColumnToTable' provides an extra
     * argument; "ADD"
     */
    private void fillSequence(String type, String extra) {
        MinecraftUtil.ensureAsync();

        Function<WrappedData, String> formatFunc;

        switch(type) {
            case COLUMN_PLUS_TYPE -> formatFunc = WrappedData::formatSQLTypeStatement;
            case COLUMN_PLUS_VALUE -> formatFunc = WrappedData::formatSQLFullValue;
            case LABEL -> formatFunc = WrappedData::getName;
            case VALUE -> formatFunc = WrappedData::formatSQLValue;
            case DATA_TYPE -> formatFunc = WrappedData::formatType;
            default -> throw new IllegalArgumentException("Invalid type of SQL parameter entered!");
        }

        List<String> formattedSQLParams = params.stream()
                .map(e -> extra + formatFunc.apply(e))
                .collect(Collectors.toList());
        sql.append(String.join(", ", formattedSQLParams));
    }

    /**
     * Formats and addends a condition to the SQL statement,
     * if applicable
     */
    private void insertCondition() {
        sql.append(" WHERE ").append(condition.formatSQLFullValue());
    }

    /**
     * Gets all rows present in a given table
     */
    static class GetAllRows extends SQLStatement {

        public GetAllRows(Table table) {
            super(table);
        }

        @Override
        void prepareSQL() {
            super.sql.append("SELECT * FROM " + TABLE);
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE_QUERY;
        }
     }

    /**
     * Sets any number of fields in a Table to the
     * provided values, accepting conditions and UUID
     * keys that single out and specify certain rows
     */
    static class SetValue extends SQLStatement {

        public SetValue(Table table, List<WrappedData> params, WrappedData condition) {
            this(table, params);
            super.condition = condition;
        }

        public SetValue(Table table, List<WrappedData> params, UUID key) {
            this(table, params);
            super.condition = new WrappedData("id", key);
        }

        public SetValue(Table table, List<WrappedData> params) {
            super(table);
            super.params = params;
        }

        public SetValue(Table table, WrappedData param) {
            super(table);
            super.params.add(param);
        }

        public SetValue(Table table, WrappedData param, WrappedData condition) {
            this(table, param);
            super.condition = condition;
        }

        public SetValue(Table table, WrappedData param, UUID key) {
            this(table, param);
            super.condition = new WrappedData("id", key);
        }

        @Override
        void prepareSQL() {
            super.sql.append("UPDATE " + TABLE + " SET ");
            super.fillSequence(COLUMN_PLUS_VALUE, "");
            super.insertCondition();
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE_UPDATE;
        }
    }

    /**
     * Adds a new column to a table (a category of data that
     * rows are allowed to have)
     */
    static class AddColumnToTable extends SQLStatement {

        public AddColumnToTable(Table table, List<WrappedData> params) {
            super(table);
            super.params = params;
        }

        public AddColumnToTable(Table table, WrappedData param) {
            super(table);
            super.params.add(param);
        }

        @Override
        void prepareSQL() {
            super.sql.append("ALTER " + TABLE);
            super.fillSequence(COLUMN_PLUS_TYPE, "ADD ");
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE_UPDATE;
        }
    }

    /**
     * Adds a new row with a unique identifier
     */
    static class AddRow extends SQLStatement {

        private UUID id;
        private String name;

        public AddRow(Table table, UUID id) {
            super(table);
            this.id = id;
        }

        public AddRow(Table table, String name) {
            super(table);
            this.name = name;
        }

        @Override
        void prepareSQL() {
            super.sql.append("INSERT INTO " + TABLE);
            if(id != null) {
                super.sql.append(" (id) VALUES (" + JavaUtil.convertUUIDToBytes(id));
            }else{
                super.sql.append(" (name) VALUES ('" + name + "'");
            }
            super.sql.append(")");
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE_UPDATE;
        }
    }

    /**
     * Queries the values of any number of fields
     */
    static class GetValue extends SQLStatement {

        public GetValue(Table table, WrappedData condition, WrappedData... params) {
            super(table);
            super.condition = condition;
            super.params = Arrays.asList(params);
        }

        public GetValue(Table table, UUID key, WrappedData... params) {
            super(table);
            super.condition = new WrappedData("id", key);
            super.params = Arrays.asList(params);
        }

        @Override
        void prepareSQL() {
            super.sql.append("SELECT ");
            super.fillSequence(LABEL, "");
            super.sql.append(" FROM " + TABLE);
            super.insertCondition();
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE_QUERY;
        }
    }

    /**
     * Gets a specific row using a condition or unique
     * identifier -- that row can then have its data
     * reviewed and parsed
     */
    static class GetRow extends SQLStatement {

        private UUID key;

        public GetRow(Table table, WrappedData condition) {
            super(table);
            super.condition = condition;
        }

        public GetRow(Table table, UUID key) {
            super(table);
            super.condition = new WrappedData("id", key);
        }

        @Override
        void prepareSQL() {
            super.sql.append("SELECT * FROM " + TABLE);
            super.insertCondition();
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE_QUERY;
        }
    }

    /**
     * Creates a table in the database
     */
    static class CreateTable extends SQLStatement {

        public CreateTable(Table table) {
            super(table);
            super.params = Arrays.asList(table.getColumns());
        }

        @Override
        void prepareSQL() {
            super.sql.append("CREATE TABLE " + TABLE + " (id BINARY(16), ");
            super.fillSequence(COLUMN_PLUS_TYPE, "");
            super.sql.append(")");
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE;
        }
    }

    /**
     * Deletes a specific row
     */
    static class DeleteRow extends SQLStatement {

        private String value;
        private UUID key;

        public DeleteRow(Table table, WrappedData condition) {
            super(table);
            super.condition = condition;
        }

        public DeleteRow(Table table, UUID key) {
            super(table);
            super.condition = new WrappedData("id", key);
        }

        @Override
        void prepareSQL() {
            super.sql.append("DELETE FROM " + TABLE);
            super.insertCondition();
        }

        @Override
        public ExecutionType getType() {
            return ExecutionType.EXECUTE_UPDATE;
        }
    }
}
