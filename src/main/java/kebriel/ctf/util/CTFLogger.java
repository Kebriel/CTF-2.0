package kebriel.ctf.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CTFLogger extends Logger {

    private static final String black = "\\u001B[30m";
    private static final String red = "\\u001B[31m";
    private static final String green = "\\u001B[32m";
    private static final String yellow = "\\u001B[33m";
    private static final String blue = "\\u001B[34m";
    private static final String magenta = "\\u001B[35m";
    private static final String cyan = "\\u001B[36m";
    private static final String white = "\\u001B[37m";
    private static final String bold = "\\u001B[1m";
    private static final String italic = "\\u001B[3m";
    private static final String underline = "\\u001B[4m";
    private static final String strikethrough = "\\u001B[9m";
    private static final String reset = "\\u001B[0m";
    public static final String ansi_orange = "\u001B[38;2;255;165;0m";

    public CTFLogger() {
        super("", "");
    }

    public static void logInfo(String msg) {
        new CTFLogger().log(Level.INFO, yellow + msg + reset);
    }

    public static void logWarning(String msg) {
        new CTFLogger().log(Level.WARNING, ansi_orange + msg + reset);
    }

    public static void logError(String msg) {
        new CTFLogger().log(Level.SEVERE, red + msg + reset);
    }

    public static void logDebug(String msg) {
        new CTFLogger().log(Level.FINE, italic + msg + reset);
    }
}
