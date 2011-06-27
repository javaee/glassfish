/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.admin.cli;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.ServerDirs;
import com.sun.enterprise.util.net.NetUtils;
import org.glassfish.api.admin.CommandException;
import static com.sun.enterprise.util.StringUtils.ok;
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

    public StartServerHelper(Logger logger0, boolean terse0,
            ServerDirs serverDirs0, GFLauncher launcher0,
            String masterPassword0, boolean debug0) {
        logger = logger0;
        terse = terse0;
        launcher = launcher0;
        info = launcher.getInfo();

        if (info.isDomain())
            serverOrDomainName = info.getDomainName();
        else
            serverOrDomainName = info.getInstanceName();

        addresses = info.getAdminAddresses();
        serverDirs = serverDirs0;
        pidFile = serverDirs.getPidFile();
        masterPassword = masterPassword0;
        debug = debug0;
    }

    // TODO check the i18n messages
    public void waitForServer() throws CommandException {
        long startWait = System.currentTimeMillis();
        if (!terse) {
            // use stdout because logger always appends a newline
            System.out.print(strings.get("WaitServer", serverOrDomainName) + " ");
        }

        boolean alive = false;
        int count = 0;

        pinged:
        while (!timedOut(startWait)) {
            if (pidFile != null) {
                logger.finer("Check for pid file: " + pidFile);
                if (pidFile.exists()) {
                    alive = true;
                    break pinged;
                }
            }
            else {
                // first, see if the admin port is responding
                // if it is, the DAS is up
                for (HostAndPort addr : addresses) {
                    if (NetUtils.isRunning(addr.getHost(), addr.getPort())) {
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
                String sname;

                if (info.isDomain())
                    sname = "domain " + info.getDomainName();
                else
                    sname = "instance " + info.getInstanceName();

                ProcessStreamDrainer psd = launcher.getProcessStreamDrainer();
                String output = psd.getOutErrString();
                if (ok(output))
                    throw new CommandException(strings.get("serverDiedOutput",
                            sname, exitCode, output));
                else
                    throw new CommandException(strings.get("serverDied",
                            sname, exitCode));
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
                if (!terse && count++ % 10 == 0)
                    System.out.print(".");
            }
            catch (InterruptedException ex) {
                // don't care
            }
        }

        if (!terse)
            System.out.println();

        if (!alive) {
            String msg;
            String time = "" + (WAIT_FOR_DAS_TIME_MS / 1000);
            if (info.isDomain())
                msg = strings.get("serverNoStart", strings.get("DAS"),
                        info.getDomainName(), time);
            else
                msg = strings.get("serverNoStart", strings.get("INSTANCE"),
                        info.getInstanceName(), time);

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

        if (checkPorts() == false)
            return false;

        deletePidFile();

        return true;
    }

    public void report() {
        String logfile;

        try {
            logfile = launcher.getLogFilename();
        }
        catch (GFLauncherException ex) {
            logfile = "UNKNOWN";        // should never happen
        }


        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo
        // add logfile path to ServerDirs
        // TODO IMPORTANT have the enum itself return the strings "domain"  "instance" etc.
        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo
        // TODO  TODO  TODO  TODO  Todo  todo

        int adminPort = -1;
        String adminPortString = "-1";

        try {
            if (addresses != null && addresses.size() > 0)
                adminPort = addresses.get(0).getPort();
            // To avoid having the logger do this: port = 4,848
            // so we do the conversion to a string ourselves
            adminPortString = "" + adminPort;
        }
        catch (Exception e) {
            //ignore
        }

        int debugPort = -1;
        String debugPortString = "-1";

        if (debug) {
            debugPort = launcher.getDebugPort();
            debugPortString = "" + debugPort;
        }

        logger.info(strings.get(
                "ServerStart.SuccessMessage",
                info.isDomain() ? "domain " : "instance",
                serverDirs.getServerName(),
                serverDirs.getServerDir(),
                logfile,
                adminPortString));

        if (debugPort >= 0)
            logger.info(strings.get("ServerStart.DebuggerMessage", debugPortString));
    }

    /**
     * If the parent is a GF server -- then wait for it to die.  This is part
     * of the Client-Server Restart Dance!
     * @throws CommandException if we timeout waiting for the parent to die or
     *  if the admin ports never free up
     */
    private void waitForParentToDie() throws CommandException {
        if (Boolean.getBoolean(CLIConstants.RESTART_FLAG))
            new ParentDeathWaiter();
    }

    private boolean checkPorts() {
        String err = adminPortInUse();

        if (err != null) {
            logger.warning(err);
            return false;
        }

        return true;
    }

    private void deletePidFile() {
        String msg = serverDirs.deletePidFile();

        if (msg != null)
            logger.finer(msg);
    }

    private void setSecurity() {
        info.addSecurityToken(CLIConstants.MASTER_PASSWORD, masterPassword);
    }

    private String adminPortInUse() {
        return adminPortInUse(info.getAdminAddresses());
    }

    private String adminPortInUse(List<HostAndPort> adminAddresses) {
        // it returns a String for logging --- if desired
        for (HostAndPort addr : adminAddresses) {
            if (!NetUtils.isPortFree(addr.getHost(), addr.getPort()))
                return strings.get("ServerRunning",
                        Integer.toString(addr.getPort()));
        }

        return null;
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
    private final boolean terse;
    private final GFLauncher launcher;
    private final Logger logger;
    private final File pidFile;
    private final GFLauncherInfo info;
    private final List<HostAndPort> addresses;
    private final ServerDirs serverDirs;
    private final String masterPassword;
    private final String serverOrDomainName;
    private final boolean debug;
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

                while (System.in.read() >= 0);
            }
            catch (IOException ex) {
                // ignore
            }

            // The port may take some time to become free after the pipe breaks
            while (adminPortInUse(addresses) != null);

            success = true;
        }

        private ParentDeathWaiter() throws CommandException {
            try {
                Thread t = new Thread(this);
                t.start();
                t.join(CLIConstants.DEATH_TIMEOUT_MS);
            }
            catch (Exception e) {
                // ignore!
            }

            if (!success)
                throw new CommandException(
                        strings.get("deathwait_timeout", CLIConstants.DEATH_TIMEOUT_MS));
        }
        boolean success = false;
    }
}
