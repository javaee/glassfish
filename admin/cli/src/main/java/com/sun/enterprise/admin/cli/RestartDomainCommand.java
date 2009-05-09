/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.cli.remote.CLIRemoteCommand;
import java.util.*;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;

/**
 * The local portion of this command is only used to block until:
 * <ul><li>the old server dies
 * <li>the new server starts
 * </ul>
 * Tactics:
 * <ul>
 * <li>Get the uptime for the current server
 * <li>start the remote Restart command
 * <li>Call uptime in a loop until the uptime number is less than the original uptime
 * @author bnevins
 */
public class RestartDomainCommand extends LocalRemoteCommand{

    @Override
    public void runCommand() throws CommandException, CommandValidationException {
        validateOptions();
        long uptimeOldServer = getUptime();  // may throw CommandException
        runRemoteCommand("restart-domain");
        waitForRestart(uptimeOldServer);
        logger.printMessage(strings.get("restartDomain.success"));
    }

    private long getUptime() throws CommandException {
        CLIRemoteCommand rc = runRemoteCommand("uptime");
        Map<String,String> map = rc.getMainAtts();
        String up = map.get("message");
        long up_ms = parseUptime(up);

        if(up_ms <= 0) {
            throw new CommandException(strings.get("StopDomain.dasNotRunning"));
        }

        logger.printDebugMessage("server uptime: " + up_ms);
        return up_ms;
    }

    private long parseUptime(String up) {
        if(up == null || up.length() < 4)
            return 0;

        int index = up.lastIndexOf(':');

        if(index < 0)
            return 0;

        if(up.length() - index < 3)
            return 0;

        try {
            return Long.parseLong(up.substring(index + 2));
        }
        catch(Exception e) {
            return 0;
        }
    }

    private void waitForRestart(long uptimeOldServer) throws CommandException {
        long end = CLIConstants.WAIT_FOR_DAS_TIME_MS + System.currentTimeMillis();

        while(System.currentTimeMillis() < end) {
            try {
                Thread.sleep(300);
                long up = getUptime();

                if(up > 0 && up < uptimeOldServer)
                    return;
            }
            catch (Exception e) {
                // continue
            }
        }
        // if we get here -- we timed out
        throw new CommandException(strings.get("restartDomain.noGFStart"));
    }
}
