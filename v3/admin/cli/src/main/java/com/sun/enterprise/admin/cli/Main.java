package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.*;

/**
 * my v3 main, basically some throw away code
 */
public class Main {
    public static void main(String[] args) {
        try {
            new CLIMain().invokeCommand(args);
        } catch (InvalidCommandException ice) {
            if (TRACE) {
                System.out.println("REMOTE COMMAND!!!");
            }
            RemoteCommand rc = RemoteCommand.getInstance();
            rc.handleRemoteCommand(args);
        } catch (Throwable ex) {
            CLILogger.getInstance().printExceptionStackTrace(ex);
            CLILogger.getInstance().printError(ex.getLocalizedMessage());
            System.exit(1);
        }
    }
    public static final boolean TRACE = Boolean.getBoolean("trace");
}


