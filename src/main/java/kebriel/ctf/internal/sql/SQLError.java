package kebriel.ctf.internal.sql;

import kebriel.ctf.util.CTFLogger;

import java.sql.SQLException;

public class SQLError {

    private final SQLException ex;

    public static SQLError log(SQLException ex) {
        SQLError error = new SQLError(ex);
        CTFLogger.logError(error.getErrorMessage());
        return error;
    }

    private SQLError(SQLException ex) {
        this.ex = ex;
    }

    public String getErrorMessage() {
        return "[SQL Error Code: '" + ex.getErrorCode() + "'] " + ex.getMessage();
    }
}
