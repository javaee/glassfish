package com.sun.enterprise.tools.upgrade.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * POC log parser to pick out domain upgrade results.
 */
public class LogParser {

    private static final String SEP = "|";

    // holds the "upgrade failed" cases
    private static final Set<String> FAIL_LEVELS = new HashSet<String>();

    static {
        // example for Level.SEVERE
        FAIL_LEVELS.add(String.format("%s%s%s",
            SEP, Level.SEVERE.getName(), SEP));
    }

    public static void main(String[] args) {
        // usage check
        if (args.length == 0) {
            System.err.println("Pass log file as command line argument.");
            return;
        }

        // grab file to parse
        String fileName = args[0];
        File logFile = new File(fileName);
        if (!logFile.exists()) {
            System.err.println(
                String.format("File %s does not exist.", fileName));
            return;
        } else if (logFile.isDirectory()) {
            System.err.println(
                String.format("File %s is a directory.", fileName));
            return;
        }

        /*
         * This is the logger name for which to check. The usage
         * inside the server would be:
         *
         * Logger upLogger =
         *     Logger.getLogger("com.sun.enterprise.tools.upgrade");
         * upLogger.log(Level.SEVERE, "V3: This is a SEVERE message");
         *
         */
        String loggerName = "com.sun.enterprise.tools.upgrade";
        if (args.length > 1) {
            loggerName = args[1];
        }

        LogParser m = new LogParser();
        m.parseLog(logFile, loggerName);
    }

    /*
     * Iterate through the file and hand off each line to another
     * method. This method will check to see if there were any
     * failures reported.
     */
    void parseLog(File logFile, String loggerName) {

        System.err.println(String.format("Parsing '%s'",
            logFile.getAbsolutePath()));
        System.err.println(String.format("Logger name is '%s'", loggerName));
        final String loggerToken = String.format("%s%s%s",
            SEP, loggerName, SEP);

        BufferedReader reader = null;
        boolean fail = false;

        try {
            reader = new BufferedReader(new FileReader(logFile));
            String line = reader.readLine();
            while (line != null) {
                if (parseForError(loggerToken, line)) {
                    fail = true;
                }
                line = reader.readLine();
            }
        } catch (IOException bad) {
            System.err.println(bad.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                System.err.println(String.format("Whoa: %s", ex.getMessage()));
            }
        }

        if (fail) {
            System.err.println("Upgrade faile. See above errors.");
        }
        System.out.println("\nFinished.");
    }

    /*
     * Checks each line to see if if came from the upgrade logger
     * or not. If so, checks to see if anythin was logged at a level
     * that is in the 'FAIL_LEVELS' set. E.g., Level.SEVERE messages
     * might be considered an upgrade failure.
     */
    private boolean parseForError(String loggerToken, String line) {

        // look for our logger first
        if (line.indexOf(loggerToken) == -1) {
            return false;
        }

        // now check for failures
        for (String levelToken : FAIL_LEVELS) {
            if (line.indexOf(levelToken) != -1) {
                System.err.println(getMessage(line));
                return true;
            }
        }
        return false;
    }

    /*
     * Utility method for getting the message out of a GF log line.
     * Hopefully there's some code already to handle this.
     */
    private String getMessage(String line) {

        // parse off end
        line = line.substring(0, line.lastIndexOf(SEP));

        // now parse up till message
        line = line.substring(1 + line.lastIndexOf(SEP), line.length());
        
        return line;
    }

}
