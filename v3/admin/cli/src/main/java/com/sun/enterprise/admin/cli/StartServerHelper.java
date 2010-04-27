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
import com.sun.enterprise.util.net.NetUtils;
import java.io.File;
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
    public StartServerHelper(CLILogger logger0, boolean terse0, File pidFile0, GFLauncher launcher0) {
        logger = logger0;
        terse = terse0;
        launcher = launcher0;
        info = launcher.getInfo();
        ports = info.getAdminPorts();
        pidFile = pidFile0;
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

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
    
    private final boolean terse;
    private final GFLauncher launcher;
    private final CLILogger logger;
    private final File pidFile;
    private final GFLauncherInfo info;
    private final Set<Integer> ports;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartServerHelper.class);
}
