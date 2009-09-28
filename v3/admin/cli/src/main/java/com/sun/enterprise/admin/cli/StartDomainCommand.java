/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.util.net.NetUtils;
import static com.sun.enterprise.admin.cli.CLIConstants.*;
import com.sun.enterprise.admin.cli.LocalDomainCommand;
import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

import java.util.*;

/**
 * The start-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "start-domain")
@Scoped(PerLookup.class)
public class StartDomainCommand extends LocalDomainCommand {

    private GFLauncherInfo info;
    private GFLauncher launcher;
    private boolean verbose;
    private boolean upgrade;
    private boolean debug;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartDomainCommand.class);
    private static final int DEATH_TIMEOUT_MS = 5 * 60 * 1000; // 5 minute timeout should be plenty!

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, "debug", '\0', "BOOLEAN", false, "false");
        addOption(opts, "domaindir", '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        addOption(opts, "upgrade", '\0', "BOOLEAN", false, "false");
        addOption(opts, "verbose", 'v', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 0;
        operandMax = 1;

        processProgramOptions();
    }

    @Override
    protected int executeCommand() throws CommandException {
        verbose = getBooleanOption("verbose");
        upgrade = getBooleanOption("upgrade");
        debug = getBooleanOption("debug");

        String gfejar = System.getenv("GFE_JAR");
        if (gfejar != null && gfejar.length() > 0)
            return runCommandEmbedded();
        else
            return runCommandNotEmbedded();
    }

    private int runCommandNotEmbedded() throws CommandException {
        try {
            launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.domain);
            info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName(operands.get(0));
            }
            
            String parent = options.get("domaindir");
            if (parent != null)
                info.setDomainParentDir(parent);

            info.setVerbose(verbose || upgrade);
            info.setDebug(debug);
            info.setUpgrade(upgrade);

            info.setRespawnInfo(programOpts.getClassName(),
                            programOpts.getClassPath(),
                            programOpts.getProgramArguments());

            launcher.setup();

            if(Boolean.getBoolean(RESTART_FLAG)) {
                new DeathWaiter();
            }
            else { // plain start-domain
                String err = adminPortInUse();

                if(err != null) {
                    logger.printWarning(err);
                    return ERROR;
                }
            }

            // this can be slow, 500 msec,
            // with --passwordfile option it is ~~ 18 msec
            setMasterPassword(info);

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
                waitForDAS(info.getAdminPorts());
                report();
                return SUCCESS;
            }
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    private void setMasterPassword1(GFLauncherInfo info)
                                throws CommandException {
        // Sets the password into the launcher info.
        // Yes, setting master password into a string is not right ...
        long t0 = System.currentTimeMillis();
        String mpn  = "AS_ADMIN_MASTERPASSWORD";
        String mpv  = passwords.get(mpn);
        if (mpv == null)
            mpv = readFromMasterPasswordFile();
        if (mpv == null)
            mpv = "changeit"; //the default
        boolean ok = verifyMasterPassword(mpv);
        long t1 = System.currentTimeMillis();
        logger.printDebugMessage("Master Password processing took: " + (t1-t0) + " msec");
        if (!ok) {
            mpv = retry(3);
        }
        info.addSecurityToken(mpn, mpv);
    }
    private void setMasterPassword(GFLauncherInfo info) throws CommandException {
        // Sets the password into the launcher info.
        // Yes, setting master password into a string is not right ...
        final int RETRIES = 3;
        long t0 = System.currentTimeMillis();
        String mpn  = "AS_ADMIN_MASTERPASSWORD";
        String mpv  = passwords.get(mpn);
        if (mpv == null) { //not specified in the password file
            mpv = "changeit";  //optimization for the default case -- see 9592
            if (!verifyMasterPassword(mpv)) {
                mpv = readFromMasterPasswordFile();
                if (!verifyMasterPassword(mpv)) {
                    mpv = retry(RETRIES);
                }
            }
        } else { //the passwordfile contains AS_ADMIN_MASTERPASSWORD, use it at once
            if(!verifyMasterPassword(mpv))
                mpv = retry(RETRIES);
        }
        info.addSecurityToken(mpn, mpv);
        long t1 = System.currentTimeMillis();
        logger.printDebugMessage("Time spent in master password extraction: " + (t1-t0) + " msec");       //TODO
    }

    private String retry(int times) throws CommandException {
        String mpv;
        // prompt times times
        for (int i = 0 ; i < times; i++) {
            // XXX - I18N
            String prompt = strings.get("mp.prompt", (times-i));
            mpv = super.readPassword(prompt);
            if (mpv == null)
                throw new CommandException(strings.get("no.console"));
                // ignore retries :)
            if (verifyMasterPassword(mpv))
                return mpv;
            if (i < (times-1))
                logger.printMessage(strings.get("retry.mp"));
            // make them pay for typos?
            //Thread.currentThread().sleep((i+1)*10000);
        }
        throw new CommandException(strings.get("mp.giveup", times));
    }



    private int runCommandEmbedded() throws CommandException {
        try {
            // bnevins nov 23 2008
            // Embedded is a new type of server
            // For now -- we ONLY start embedded

            launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.embedded);

            info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName(operands.get(0));
            } else {
                info.setDomainName("domain1");
            }

            String parent = options.get("domaindir");

            if (parent != null) {
                info.setDomainParentDir(parent);
            } else
                info.setDomainParentDir(
                            System.getenv("S1AS_HOME") + "/domains"); // TODO

            info.setVerbose(verbose);
            info.setDebug(debug);
            launcher.setup();

            // now admin ports are set.
            Set<Integer> ports = info.getAdminPorts();

            if (isServerAlive(ports)) {
                // todo add the port number to the message
                throw new CommandException("The Admin port is already taken: ");
            }

            launcher.launch();

            // if we are in verbose mode, we definitely do NOT want to wait for
            // DAS, since it already ran and is now dead!!
            //if(!verbose) {
                waitForDAS(ports);
                report();
            //}
            return SUCCESS;
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    private void waitForDAS(Set<Integer> ports) throws CommandException {
        if (ports == null || ports.size() <= 0) {
            String msg = strings.get("noPorts");
            throw new CommandException(
                    strings.get("CommandUnSuccessfulWithArg", name, msg));
        }

        long startWait = System.currentTimeMillis();
        if (!programOpts.isTerse()) {
            // use stderr because logger always appends a newline
            System.err.print(strings.get("WaitDAS") + " ");
        }

        boolean alive = false;

        pinged:
        while (!timedOut(startWait)) {
            // first, see if the admin port is responding
            // if it is, the DAS is up
            for (int port : ports) {
                if (isServerAlive(port)) {
                    alive = true;
                    break pinged;
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
                if (ok(output))
                    throw new CommandException(strings.get("dasDiedOutput",
                                    info.getDomainName(), exitCode, output));
                else
                    throw new CommandException(strings.get("dasDied",
                                    info.getDomainName(), exitCode));
            } catch (GFLauncherException ex) {
                // should never happen
            } catch (IllegalThreadStateException ex) {
                // process is still alive
            }

            // wait before checking again
            try {
                Thread.sleep(100);
                if (!programOpts.isTerse())
                    System.err.print(".");
            } catch (InterruptedException ex) {
                // don't care
            }
        }

        if (!programOpts.isTerse())
            System.err.println();

        if (!alive) {
            String msg = strings.get("dasNoStart", 
                info.getDomainName(), (WAIT_FOR_DAS_TIME_MS / 1000));
            throw new CommandException(msg);
        }
    }
 
    private boolean isServerAlive(int port) {
        logger.printDebugMessage("Check if server is alive on port " + port);
        programOpts.setPort(port);
        programOpts.setInteractive(false);      // don't prompt
        return DASUtils.pingDASQuietly(programOpts, env);
    }
 
    private boolean isServerAlive(Set<Integer> ports) {
        if (ports == null || ports.size() == 0)
            return false;
        return isServerAlive(ports.iterator().next());
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
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

    /* this is useful for debugging restart-domain problems.
     * In that case the Server process will run this class and it is fairly
     * involved to attach a debugger (though not bad -- see RestartDomain on the server to see how).
     * Standard output disappears.  This is a generally useful method.  Feel free to copy & paste!
     */
    private void debug(String s) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("startdomain.txt", true));
            ps.println(new Date().toString() + ":  " + s);
        }
        catch (FileNotFoundException ex) {
            //
        }
    }

    private class DeathWaiter implements Runnable{
        @Override
        public void run() {
            try {
                // When parent process is almost dead, in.read returns -1 (EOF)
                // as the pipe breaks.

                while (System.in.read() >= 0)
                    ;
            }
            catch (IOException ex) {
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

            if(!success)
                throw new CommandException(strings.get("deathwait_timeout", DEATH_TIMEOUT_MS));
        }
        boolean success = false;
    }
}
