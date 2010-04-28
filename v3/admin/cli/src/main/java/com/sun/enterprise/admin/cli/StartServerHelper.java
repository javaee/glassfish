/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.universal.StringUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.util.io.ServerDirs;
import com.sun.enterprise.util.net.NetUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.glassfish.api.admin.CommandException;
import static com.sun.enterprise.admin.cli.CLIConstants.WAIT_FOR_DAS_TIME_MS;

/**
 * Java does not allow multiple inheritance.  Both StartDomainCommand and
 * StartInstanceCommand have common code but they are already in a different
 * hierarchy of classes.  The first common baseclass is too far away -- e.g.
 * no "launcher" variable, etc.
 *
 * Instead -- put common code in here and call it as common utilities
 * This class is designed to be thread-safe and IMMUTABLE
 * @author bnevins
 */
public class StartServerHelper {
    public StartServerHelper(CLILogger logger0, boolean terse0,
            ServerDirs serverDirs0, GFLauncher launcher0, String masterPassword0) {
        logger = logger0;
        terse = terse0;
        launcher = launcher0;
        info = launcher.getInfo();
        ports = info.getAdminPorts();
        serverDirs = serverDirs0;
        pidFile = serverDirs.getPidFile();
        masterPassword = masterPassword0;
    }
    public void waitForServer() throws CommandException {
        long startWait = System.currentTimeMillis();
        if(!terse) {
            // use stdout because logger always appends a newline
            System.out.print(strings.get("WaitServer") + " ");
        }

        boolean alive = false;
        int count = 0;

        pinged:
        while(!timedOut(startWait)) {
            if(pidFile != null) {
                logger.printDebugMessage("Check for pid file: " + pidFile);
                if(pidFile.exists()) {
                    alive = true;
                    break pinged;
                }
            }
            else {
                // first, see if the admin port is responding
                // if it is, the DAS is up
                for(int port : ports) {
                    if(NetUtils.isRunning(port)) {
                        alive = true;
                        break pinged;
                    }
                }
            }

            // check to make sure the DAS process is still running
            // if it isn't, startup failed
            try {
                Process p = launcher.getProcess();
                int exitCode = p.exitValue();
                // uh oh, DAS died
                ProcessStreamDrainer psd = launcher.getProcessStreamDrainer();
                String output = psd.getOutErrString();
                if(StringUtils.ok(output))
                    throw new CommandException(strings.get("dasDiedOutput",
                            info.getDomainName(), exitCode, output));
                else
                    throw new CommandException(strings.get("dasDied",
                            info.getDomainName(), exitCode));
            }
            catch (GFLauncherException ex) {
                // should never happen
            }
            catch (IllegalThreadStateException ex) {
                // process is still alive
            }

            // wait before checking again
            try {
                Thread.sleep(100);
                if(!terse && count++ % 10 == 0)
                    System.out.print(".");
            }
            catch (InterruptedException ex) {
                // don't care
            }
        }

        if(!terse)
            System.out.println();

        if(!alive) {
            String msg = strings.get("dasNoStart",
                    info.getDomainName(), (WAIT_FOR_DAS_TIME_MS / 1000));
            throw new CommandException(msg);
        }
    }

    /**
     * Run a series of commands to prepare for a launch.
     * @return false if there was a problem.
     */
    public boolean prepareForLaunch() throws CommandException {

        waitForParentToDie();
        setSecurity();

        if(checkPorts() == false)
            return false;

        deletePidFile();

        return true;
    }

    /**
     * If the parent is a GF server -- then wait for it to die.  This is part
     * of the Client-Server Restart Dance!
     * @throws CommandException if we timeout waiting for the parent to die or
     *  if the admin ports never free up
     */
    private void waitForParentToDie() throws CommandException {
        if(Boolean.getBoolean(CLIConstants.RESTART_FLAG))
            new ParentDeathWaiter();
    }


    private boolean checkPorts() {
        String err = adminPortInUse();

        if(err != null) {
            logger.printWarning(err);
            return false;
        }

        return true;
    }

    private void deletePidFile() {
        String msg = serverDirs.deletePidFile();

        if(msg != null)
            logger.printDebugMessage(msg);
    }

    private void setSecurity() {
        info.addSecurityToken(CLIConstants.MASTER_PASSWORD, masterPassword);
    }


    private String adminPortInUse() {
        Set<Integer> adminPorts = info.getAdminPorts();
        return adminPortInUse(adminPorts);
    }

    private String adminPortInUse(Set<Integer> adminPorts) {
        // it returns a String for logging --- if desired
        for(Integer port : adminPorts)
            if(!NetUtils.isPortFree(port))
                return strings.get("ServerRunning", port.toString());

        return null;
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
    private final boolean terse;
    private final GFLauncher launcher;
    private final CLILogger logger;
    private final File pidFile;
    private final GFLauncherInfo info;
    private final Set<Integer> ports;
    private final ServerDirs serverDirs;
    private final String masterPassword;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartServerHelper.class);

    /**
     * bnevins
     * the restart flag is set by the RestartDomain command in the local
     * server.  The dying server has started a new JVM process and is
     * running this code.  Our official parent process is the dying server.
     * The ParentDeathWaiter waits for the parent process to disappear.
     * see RestartDomainCommand in core/kernel for more details
     */
    private class ParentDeathWaiter implements Runnable {
        @Override
        @SuppressWarnings("empty-statement")
        public void run() {
            try {
                // When parent process is almost dead, in.read returns -1 (EOF)
                // as the pipe breaks.

                while(System.in.read() >= 0);
            }
            catch (IOException ex) {
                // ignore
            }

            // The port may take some time to become free after the pipe breaks
            Set<Integer> adminPorts = info.getAdminPorts();

            while(adminPortInUse(adminPorts) != null);

            success = true;
        }

        public ParentDeathWaiter() throws CommandException {
            try {
                Thread t = new Thread(this);
                t.start();
                t.join(CLIConstants.DEATH_TIMEOUT_MS);
            }
            catch (Exception e) {
                // ignore!
            }

            if(!success)
                throw new CommandException(
                        strings.get("deathwait_timeout", CLIConstants.DEATH_TIMEOUT_MS));
        }
        boolean success = false;
    }
}
