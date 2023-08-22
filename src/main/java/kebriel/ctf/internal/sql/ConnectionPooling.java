package kebriel.ctf.internal.sql;

import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.util.CTFLogger;
import kebriel.ctf.util.MinecraftUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPooling {

    // How long the server should wait before timing out when pulling a new connection
    private static final long WAIT_TIMEOUT = 3; // in seconds
    // A very basic SQL query that verifies the validity of the database
    private static final String VALIDATION_QUERY = "SELECT 1";
    /*
     * An arbitrary number representing the number of connections that this pool should
     * hold and maintain at any given time
     */
    private static final int POOL_SIZE = 10;

    // Database info
    public static final int PORT = 3306;
    private static final String HOST = "localhost";
    private static final String DB_NAME = "kebriel/ctf";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    /*
     * Stored instance of the main (and only, currently) ConnectionPool used
     * by this plugin
     */
    public static final ConnectionPool MAIN_POOL;

    static {
        MAIN_POOL = new ConnectionPool();
    }

    public static class ConnectionPool {

        private final BlockingQueue<Connection> pool;
        private AsyncExecutor VALIDATION_THREAD;

        /**
         * Upon initialization, fill the ConnectionPool
         * to its capacity. In theory, if the pool
         * were to be updated to a more dynamic and advanced
         * system, the capacity would likely itself be
         * dynamic, and the number of pools created
         * initially would be based on some estimation
         * of demand yielded from analytics
         */
        private ConnectionPool() {
            pool = new ArrayBlockingQueue<>(POOL_SIZE);

            AsyncExecutor.doAsyncIfNot(() -> {
                for (int i = 0; i < POOL_SIZE; i++) {
                    pool.offer(createConnection());
                }
                startConnectionValidationThread();
            });
        }

        /**
         * Will attempt to grab a connection from the queue
         *
         * If the timeout delay is exceeded while waiting for a
         * connection to become available, a 'ghost' connection is
         * opened (a functional connection that is not from
         * the queue). Ghost connections can technically be released into
         * the queue, so long as there is capacity. This functionality
         * exists to prevent the plugin from freezing up and
         * failing to access the database due to greater demand than
         * what was expected. This is a lazy failsafe, and if demand
         * is regularly exceeding the limit, it is expected to
         * raise the limit accordingly or to implement a dynamic functionality
         * that adjusts the pool size based on demand
         *
         * Will also validate connections before returning them,
         * returning a new connection if the taken connection
         * proves to be invalid
         *
         * @return returns a usable connection object from the
         * queue, or freshly-created if necessary
         */
        public Connection takeConnection() {
            MinecraftUtil.ensureAsync();

            Connection connection = null;
            try {
                connection = pool.poll(WAIT_TIMEOUT, TimeUnit.SECONDS);
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
            if (connection == null) {
                CTFLogger.logWarning("Timed out waiting for connection (" + WAIT_TIMEOUT + "s), ghost connection provided");
                connection = createConnection();
            }
            try {
                if (connection.isClosed() || !isValid(connection)) connection = createConnection();
            } catch(SQLException ex) {
                SQLError.log(ex);
            }
            return connection;
        }

        /**
         * Called when an operation is finished using a connection,
         * so that the connection can be smoothly returned to the pool.
         *
         * Also verifies the continued viability of the connection, creating
         * a new one to replace it if it's invalid
         * @param connection the connection that's no longer in use
         */
        public void releaseConnection(Connection connection) {
            AsyncExecutor.doAsyncIfNot(() -> {
                if(connection != null) {
                    try {
                        if(!connection.isClosed() && isValid(connection)) {
                            pool.offer(connection);
                        }else{
                            pool.offer(createConnection());
                        }
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        /**
         * Create a connection using provided SQL login constants
         * @return the connection
         */
        private Connection createConnection() {
            MinecraftUtil.ensureAsync();

            try {
                return DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME, USERNAME, PASSWORD);
            } catch(SQLException ex) {
                SQLError.log(ex);
            }
            return null;
        }

        /**
         * Basic connection validation using a very simple SQL query
         * @param connection the connection to validate
         * @return the validity of the connection
         */
        private boolean isValid(Connection connection) {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeQuery(VALIDATION_QUERY);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }

        /**
         * Validates all connections in the pool every minute
         */
        private void startConnectionValidationThread() {
            if(VALIDATION_THREAD != null)
                return;
            VALIDATION_THREAD = new AsyncExecutor(task -> {
                for (Connection connection : pool) {
                    if (!isValid(connection)) {
                        pool.remove(connection);
                        pool.offer(createConnection());
                    }
                }
            }).doRepeating(0, 60, TimeUnit.SECONDS);
        }

        public void shutdown() {
            VALIDATION_THREAD.terminate(true);
        }
    }
}
