package kebriel.ctf.internal.sql;

import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.sql.SQLStatement.CreateTable;
import kebriel.ctf.internal.sql.SQLStatement.GetRow;
import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.internal.sql.SQLStatement.GetAllRows;

import java.sql.*;
import java.util.*;

public class SQLHelper {

    /**
     * Gets a list of rows in a given table. If the table doesn't
     * exist, built in functionality in SQLTransaction will create
     * the table, and then proceed with the query
     * @return returns a SQLTransaction from which wrapped statements can be
     * gotten
     */
    public static WrappedStatement getAllRows(Table table) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new GetAllRows(table)));
    }

    /**
     * Deletes a row with the given name
     * @param key the column that all rows being deleted should contain
     */
    public static void deleteRow(Table table, UUID key) {
        AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.DeleteRow(table, key)));
    }

    /**
     * Deletes a row that fulfills the given condition, formatted
     * as a WrappedData object
     */
    public static void deleteRowWith(Table table, WrappedData condition) {
        AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.DeleteRow(table, condition)));
    }

    /**
     * @param valueName the name of the field that you wish to fetch data from
     * @param key a UUID key acting as a unique identifier of the row
     * you're accessing
     * @return returns a statement object from which results can be gotten
     */
    public static WrappedStatement queryResult(Table table, String valueName, UUID key) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.GetValue(table, key, new WrappedData(valueName))));
    }

    public static WrappedStatement queryResult(Table table, String valueName, WrappedData condition) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.GetValue(table, condition, new WrappedData(valueName))));
    }

    /**
     * Allows for multiple fields to be queried in a single statement
     * @param table the table from which data should be gotten
     * @param key the unique key identifying the row
     * @param valueNames the names of all the fields you'd like to fetch the
     *                   corresponding data of
     */
    public static WrappedStatement queryMultiResult(Table table, UUID key, String... valueNames) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.GetValue(table, key, WrappedData.wrapNames(valueNames))));
    }

    public static WrappedStatement queryMultiResult(Table table, WrappedData condition, String... valueNames) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.GetValue(table, condition, WrappedData.wrapNames(valueNames))));
    }

    /**
     * Sets any number of values in a table in accordance with the WrappedData
     * params
     * @param table
     * @param params
     * @return
     */
    public static WrappedStatement setValue(Table table, WrappedData... params) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.SetValue(table, Arrays.asList(params))));
    }

    /**
     * Allows for the specification of a key as a row identifier
     */
    public static WrappedStatement setValue(Table table, UUID key, WrappedData... params) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.SetValue(table, Arrays.asList(params), key)));
    }

    public static WrappedStatement setValue(Table table, WrappedData condition, WrappedData... params) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.SetValue(table, Arrays.asList(params), condition)));
    }

    /**
     * Creates a new field of the given type, in accordance with
     * WrappedData params
     * @param table
     * @param params
     */
    public static void createValueType(Table table, WrappedData... params) {
        AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.AddColumnToTable(table, Arrays.asList(params))));
    }

    /**
     * Creates a new unique row that uses a UUID as its unique
     * identifier
     * @param table
     * @param id
     * @return
     */
    public static WrappedStatement createRow(Table table, UUID id) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.AddRow(table, id)));
    }

    /**
     * Allows for a row to be uniquely identified instead by a String name,
     * purposed for rows that don't correspond to player data in any way
     * @param table
     * @param name
     * @return
     */
    public static WrappedStatement createRow(Table table, String name) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new SQLStatement.AddRow(table, name)));
    }

    /**
     * Returns all data that corresponds to the given row
     */
    public static WrappedStatement getRow(Table table, UUID id) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new GetRow(table, id)));
    }

    public static WrappedStatement getRow(Table table, String name) {
        return AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new GetRow(table, new WrappedData("name", name))));
    }

    public static void createTable(Table table) {
        AsyncExecutor.doSQLSingle(WrappedStatement.wrap(new CreateTable(table)));
    }

    /**
     * Uses a basic callback implementation to smoothly 'assign' certain conditions to SQL statements.
     * The code provided via lambda in the ConditionalCallback will execute only if the condition, for
     * example a certain Row already existing, turns out to be true. The thread will perform as a
     * basic transaction, safely waiting on an affirmative response via ResultSet data in order to proceed.
     * @param type the condition that you're testing
     * @param table the table in question
     * @param param the parameter necessary to test the condition (for example UUID)
     * @param execute lambda containing what you'd like to conditionally execute
     */
    public static void doWithCondition(ConditionType type, Table table, Object param, ConditionalCallback execute) {
        if(!(param instanceof String || param instanceof UUID))
            throw new IllegalArgumentException("Parameter for SQL condition must either be string or UUID");

        AsyncExecutor.doTask(() -> {
            Connection conn = ConnectionPooling.MAIN_POOL.takeConnection();
            try {
                switch(type) {
                    case TABLE_EXISTS, COLUMN_EXISTS -> {
                        ResultSet results = type == ConditionType.TABLE_EXISTS ? conn.getMetaData().getTables(null, null, (String) param, new String[] { "TABLE" }) :
                                conn.getMetaData().getColumns(null, null, table.getName(), (String) param);

                        boolean result = results.next();
                        results.close();
                        execute.onCondition(result);
                    }
                    case ROW_EXISTS -> {
                        WrappedStatement getRow = getRow(table, (UUID) param);
                        getRow.waitForFinish();
                        execute.onCondition(getRow.hasResults());
                    }
                }
            } catch(SQLException ex) {
                SQLError.log(ex);
            }
            ConnectionPooling.MAIN_POOL.releaseConnection(conn);
        });
    }

    public enum ConditionType {
        TABLE_EXISTS, COLUMN_EXISTS, ROW_EXISTS
    }

    interface ConditionalCallback {
        void onCondition(boolean condition);
    }

}