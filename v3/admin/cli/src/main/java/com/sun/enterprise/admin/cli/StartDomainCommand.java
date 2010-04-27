/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import java.io.*;
import java.util.*;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.util.net.NetUtils;
import static com.sun.enterprise.admin.cli.CLIConstants.*;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

/**
 * The start-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "start-domain")
@Scoped(PerLookup.class)
public class StartDomainCommand extends LocalDomainCommand implements StartServerCommand {

    private GFLauncherInfo info;
    private GFLauncher launcher;

    @Param(optional = true, defaultValue = "false")
    private boolean verbose;

    @Param(optional = true, defaultValue = "false")
    private boolean upgrade;

    @Param(optional = true, defaultValue = "false")
    private boolean debug;

    @Param(name = "domain_name", primary = true, optional = true)
    private String domainName0;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartDomainCommand.class);
    // 5 minute timeout should be plenty!
    private static final int DEATH_TIMEOUT_MS = 5 * 60 * 1000;
    // the name of the master password option

    @Override
    public GFLauncherFactory.ServerType getType() {
         return GFLauncherFactory.ServerType.domain;
    }

    @Override
    protected void validate()
                        throws CommandException, CommandValidationException {
        setDomainName(domainName0);
        super.validate();
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            createLauncher();

            if (Boolean.getBoolean(RESTART_FLAG)) {
                new DeathWaiter();
            } else { // plain start-domain
                String err = adminPortInUse();

                if (err != null) {
                    logger.printWarning(err);
                    return ERROR;
                }

                String msg = getServerDirs().deletePidFile();

                if(msg != null)
                    logger.printDebugMessage(msg);
            }

            // this can be slow, 500 msec,
            // with --passwordfile option it is ~~ 18 msec
            String mpv = getMasterPassword();
            info.addSecurityToken(CLIConstants.MASTER_PASSWORD, mpv);

            doUpgrade(mpv);

            // launch returns very quickly if verbose is not set
            // if verbose is set then it returns after the domain dies
            launcher.launch();

            if (verbose || upgrade) { // we can potentially loop forever here...
                while (launcher.getExitValue() == RESTART_EXIT_VALUE) {
                    logger.printMessage(strings.get("restart"));

                    if (CLIConstants.debugMode)
                        System.setProperty(CLIConstants.WALL_CLOCK_START_PROP,
                                            "" + System.currentTimeMillis());

                    launcher.relaunch();
                }
                return launcher.getExitValue();
            } else {
                //todo -- maybe --wrap these args into a class for easier 
                // maintenance and usage...
                StartServerHelper helper = new StartServerHelper(
                        logger,
                        programOpts.isTerse(),
                        getServerDirs().getPidFile(),
                        launcher);
                helper.waitForServer();
                report();
                return SUCCESS;
            }
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    /**
     * Create a launcher for the domain specified by arguments to
     * this command.  The launcher is for a server of the specified type.
     * Sets the launcher and info fields.
     * It has to be public because it is part of an interface
     */
    
    @Override
    public void createLauncher()
                        throws GFLauncherException, MiniXmlParserException {
            launcher = GFLauncherFactory.getInstance(getType());
            info = launcher.getInfo();

            info.setDomainName(getDomainName());
            info.setDomainParentDir(getDomainsDir().getPath());
            info.setVerbose(verbose || upgrade);
            info.setDebug(debug);
            info.setUpgrade(upgrade);

            info.setRespawnInfo(programOpts.getClassName(),
                            programOpts.getClassPath(),
                            programOpts.getProgramArguments());

            launcher.setup();
    }
 
    private void report() {
        String logfile;
        try {
            logfile = launcher.getLogFilename();
        } catch (GFLauncherException ex) {
            logfile = "UNKNOWN";        // should never happen
        }
        logger.printMessage(strings.get("DomainLocation", info.getDomainName(),
                            info.getDomainRootDir().getAbsolutePath(),
                            logfile));
        Integer ap = -1;
        try {
            ap = info.getAdminPorts().iterator().next();
        } catch (Exception e) {
            //ignore
        }
        logger.printMessage(strings.get("DomainAdminPort",
                                        Integer.toString(ap)));

        if (debug) {
            int debugPort = launcher.getDebugPort();
            if (debugPort > 0)
                logger.printMessage(strings.get("DomainDebugPort",
                                                Integer.toString(debugPort)));
            else
                logger.printMessage(strings.get("DomainDebugPort",
                                                "UNKNOWN"));
        }
    }

    private String adminPortInUse() {
        Set<Integer> adminPorts = info.getAdminPorts();
        return adminPortInUse(adminPorts);
    }

    private String adminPortInUse(Set<Integer> adminPorts) {
        // it returns a String for logging --- if desired
        for (Integer port : adminPorts)
            if (!NetUtils.isPortFree(port))
                return strings.get("ServerRunning", port.toString());

        return null;
    }

    /*
     * This is useful for debugging restart-domain problems.
     * In that case the Server process will run this class and it is fairly
     * involved to attach a debugger (though not bad -- see RestartDomain on
     * the server to see how).  Standard output disappears.  This is a
     * generally useful method.  Feel free to copy & paste!
     */
    private void debug(String s) {
        try {
            PrintStream ps = new PrintStream(
                                new FileOutputStream("startdomain.txt", true));
            ps.println(new Date().toString() + ":  " + s);
        } catch (FileNotFoundException ex) {
            //
        }
    }

    /*
     * If this domain needs to be upgraded and --upgrade wasn't
     * specified, first start the domain to do the upgrade and
     * then start the domain again for real.
     */
    private void doUpgrade(String mpv) throws GFLauncherException, MiniXmlParserException, CommandException {
        if(upgrade || !launcher.needsUpgrade())
            return;

        logger.printMessage(strings.get("upgradeNeeded"));
        info.setUpgrade(true);
        launcher.setup();
        launcher.launch();
        Process p = launcher.getProcess();
        int exitCode = -1;
        try {
            exitCode = p.waitFor();
        } catch (InterruptedException ex) {
            // should never happen
        }
        if (exitCode != SUCCESS) {
            ProcessStreamDrainer psd =
                launcher.getProcessStreamDrainer();
            String output = psd.getOutErrString();
            if (ok(output))
                throw new CommandException(
                        strings.get("upgradeFailedOutput",
                            info.getDomainName(), exitCode, output));
            else
                throw new CommandException(strings.get("upgradeFailed",
                            info.getDomainName(), exitCode));
        }
        logger.printMessage(strings.get("upgradeSuccessful"));

        // need a new launcher to start the domain for real
        createLauncher();
        info.addSecurityToken(CLIConstants.MASTER_PASSWORD, mpv);
        // continue with normal start...
    }

    private class DeathWaiter implements Runnable{
        @Override
        public void run() {
            try {
                // When parent process is almost dead, in.read returns -1 (EOF)
                // as the pipe breaks.

                while (System.in.read() >= 0)
                    ;
            } catch (IOException ex) {
                // ignore
            }

            // The port may take some time to become free after the pipe breaks
            Set<Integer> adminPorts = info.getAdminPorts();

            while(adminPortInUse(adminPorts) != null)
                ;

            success = true;
        }

        public DeathWaiter() throws CommandException{
            try {
                Thread t = new Thread(this);
                t.start();
                t.join(DEATH_TIMEOUT_MS);
            }
            catch(Exception e) {
                // ignore!
            }

            if (!success)
                throw new CommandException(
                    strings.get("deathwait_timeout", DEATH_TIMEOUT_MS));
        }
        boolean success = false;
    }
}
