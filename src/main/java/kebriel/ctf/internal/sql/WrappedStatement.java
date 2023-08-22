package kebriel.ctf.internal.sql;

import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.util.MinecraftUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WrappedStatement {

    // The raw result of this statement
    private ResultSet result;
    private final SQLStatement statement;
    // The data contained within this statement's result, extracted from its ResultSet
    private final List<List<WrappedData>> unpackedValues;
    // A latch to allow threads to wait for this statement to finish
    private final CountDownLatch finisher = new CountDownLatch(1);

    public static WrappedStatement wrap(SQLStatement statement) {
        return new WrappedStatement(statement);
    }

    private WrappedStatement(SQLStatement statement) {
        this.statement = statement;
        unpackedValues = new ArrayList<>();
    }

    public Table getTable() {
        return statement.getTable();
    }

    public boolean wasSuccessful() {
        return statement.wasSuccessful();
    }

    /**
     * Grabs the value of the given field from the given row
     *
     * Note: unless multiple rows were queried, 'row' will only
     * ever be 1, even if there are many fields
     */
    public <T> T getValueOfField(int row, String field) {
        if (!isFinished() || !statement.wasSuccessful() || result == null) return null;
        for(WrappedData d : unpackedValues.get(row-1)) {
            if(d.getName().equals(field)) return (T) d.getValueNormal();
        }
        return null;
    }

    public <T> T getValueOfField(List<WrappedData> row, String field) {
        if (!isFinished() || !statement.wasSuccessful() || result == null) return null;
        for(WrappedData d : row) {
            if(d.getName().equals(field)) return (T) d.getValueNormal();
        }
        return null;
    }

    /**
     * Should only be used to search unique values, like names or UUIDs. If a non-unique
     * value, such as a numeric, is searched, it will simply return the first row
     * that is found to contain that value. Rows will be in the order that they
     * are stored in the table.
     */
    public List<WrappedData> getRowWithValue(WrappedData value) {
        if (!isFinished() || !statement.wasSuccessful() || result == null) return null;
        for(List<WrappedData> row : unpackedValues) {
            if(containsValue(row, value)) return row;
        }
        return null;
    }

    private boolean containsValue(List<WrappedData> row, WrappedData value) {
        for(WrappedData d : row) {
            if(d.getValueNormal().equals(value.getValueNormal()) && d.getName().equals(value.getName())) return true;
        }
        return false;
    }

    /**
     * Returns all data present in the given row
     */
    public List<WrappedData> getValuesOfRow(int row) {
        if (!isFinished() || !statement.wasSuccessful() || result == null) return null;
        return unpackedValues.get(row-1);
    }

    /**
     * Returns all data in all rows, i.e., all data queried by this
     * statement
     * @return
     */
    public List<List<WrappedData>> getAllRows() {
        if (!isFinished() || !statement.wasSuccessful() || result == null) return null;
        return unpackedValues;
    }

    /**
     * Cleanly and automatically loads each row as its own list of wrapped values,
     * closing the ResultSet after it's finished. This allows for a WrappedStatement
     * to be kept around for a long while, and queried for data, without risking
     * memory leaks
     */
    private void unpackResults() {
        if (!isFinished() || !statement.wasSuccessful() || result == null) return;
        try {
            ResultSetMetaData metadata = result.getMetaData();
            while(result.next()) {
                List<WrappedData> row = new ArrayList<>();
                for(int i = 1; i <= metadata.getColumnCount(); i++) { // Get all fields in current row
                    row.add(new WrappedData(metadata.getColumnLabel(i), result.getObject(i)));
                }
                unpackedValues.add(row);
            }
        } catch (SQLException ex) {
            SQLError.log(ex);
        }
        closeResults();
    }

    /**
     * Cleanly closes a ResultSet, and formats any SQLException
     * as a more readable SQLError
     */
    private void closeResults() {
        try {
            result.close();
        } catch(SQLException ex) {
            SQLError.log(ex);
        }
    }

    public ExecutionType getStatementType() {
        return statement.getType();
    }

    public SQLStatement getBaseStatement() {
        return statement;
    }

    /**
     * Executes the statement, and then unpacks its
     * results. Thread will wait upon statement's completion
     */
    public void execute() {
        if(isFinished()) return; // Cannot be executed more than once

        Connection connection = ConnectionPooling.MAIN_POOL.takeConnection();
        result = statement.execute(connection); // Thread will automatically wait here
        ConnectionPooling.MAIN_POOL.releaseConnection(connection); // Connection is no longer needed

        unpackResults();

        // Notify threads, if any are waiting, that statement execution is fully finished
        finisher.countDown();
    }

    public boolean hasResults() {
        return !unpackedValues.isEmpty();
    }

    /**
     * Halts a thread until this statement has finished
     * and has fully available results
     */
    public void waitForFinish() {
        // Under no circumstances should the main thread ever be paused, the server will freeze
        MinecraftUtil.ensureAsync();
        try {
            finisher.await();
        } catch(InterruptedException ex) {
            throw new RuntimeException("A thread was interrupted while waiting on a SQL statement!");
        }
    }

    public boolean isFinished() {
        return finisher.getCount() == 0;
    }

    public enum ExecutionType {
        EXECUTE, EXECUTE_UPDATE, EXECUTE_QUERY
    }
}
