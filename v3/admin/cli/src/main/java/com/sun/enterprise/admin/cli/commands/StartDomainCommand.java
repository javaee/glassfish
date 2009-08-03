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

package com.sun.enterprise.admin.cli.commands;

import com.sun.enterprise.admin.cli.CLIConstants;
//import com.sun.enterprise.util.Profiler;
import com.sun.enterprise.util.net.NetUtils;
import static com.sun.enterprise.admin.cli.CLIConstants.*;
import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.LocalDomainCommand;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.ValidOption;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

import java.util.*;

/**
 * The start-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
public class StartDomainCommand extends LocalDomainCommand {


    /** Creates the instance of this command in accordance with what {@link CLICommand} does for it.
     */
    public StartDomainCommand(String name, ProgramOptions po, Environment env) {
        super(name, po, env);
    }

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        processProgramOptions();

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
    }

    @Override
    protected int executeCommand() throws CommandException {
        String gfejar = System.getenv("GFE_JAR");

        if (gfejar != null && gfejar.length() > 0)
            runCommandEmbedded();
        else
            runCommandNotEmbedded();
        return 0;
    }

    private void runCommandNotEmbedded() throws CommandException {
        try {
            GFLauncher launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.domain);
            info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName(operands.get(0));
            }

            String parent = options.get("domaindir");
            if (parent != null)
                info.setDomainParentDir(parent);

            boolean verbose = getBooleanOption("verbose");
            info.setVerbose(verbose);
            info.setDebug(getBooleanOption("debug"));
            info.setUpgrade(getBooleanOption("upgrade"));

            info.setRespawnInfo(programOpts.getClassName(),
                            programOpts.getClassPath(),
                            programOpts.getProgramArguments());

            launcher.setup();

            // only continue if all (normally 1) admin port(s) are free
            Set<Integer> adminPorts = info.getAdminPorts();
            for(Integer port : adminPorts) {
                if(!NetUtils.isPortFree(port)) {
                    String msg = strings.get("ServerRunning", port.toString());
                    logger.printWarning(msg);
                    return;
                }
            }

            // this can be slow, 500 msec, with --passwordfile option it is ~~ 18 msec
            setMasterPassword(info);

            /*  bnevins -- I think this is garbage now.  7/29/2009
             * Delete after 7/31/2009
            boolean isRestart = Boolean.getBoolean(RESTART_FLAG);
            if (isRestart)
                waitForParentToDie();
             */

            // launch returns very quickly if verbose is not set
            // if verbose is set then it returns after the domain dies
            launcher.launch();

            if (verbose) { // we can potentially loop forever here...
                while (launcher.getExitValue() == RESTART_EXIT_VALUE) {
                    logger.printMessage(strings.get("restart"));

                    if (CLIConstants.debugMode)
                        System.setProperty(CLIConstants.WALL_CLOCK_START_PROP,
                                            "" + System.currentTimeMillis());

                    launcher.relaunch();
                }
            } else {
                waitForDAS(info.getAdminPorts());
                report(info);
            }
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    private void setMasterPassword(GFLauncherInfo info) throws CommandException {
        //sets the password into the launcher info. Yes, setting master password into a string is not right ...
        String mpn  = "AS_ADMIN_MASTERPASSWORD";
        String mpv  = passwords.get(mpn);
        if (mpv == null)
            mpv = checkMasterPasswordFile();
        if (mpv == null)
            mpv = "changeit"; //the default
        boolean ok = verifyMasterPassword(mpv);
        if (!ok) {
            mpv = retry(3);
        }
        info.addSecurityToken(mpn, mpv);
    }

    private String retry(int times) throws CommandException {
        String mpv;
        logger.printMessage("No valid master password found");
        //prompt times times
        for (int i = 0 ; i < times; i++) {
            String prompt = "Enter master password (" + (times-i) + " attempt(s) remain)> ";
            mpv = super.readPassword(prompt);
            if (mpv == null)
                throw new CommandException("No console, no prompting possible"); //ignore retries :)
            if(verifyMasterPassword(mpv))
                return mpv;
            else {
                logger.printMessage("Sorry, incorrect master password, retry");
                //Thread.currentThread().sleep((i+1)*10000); //make the pay for typos?
                continue; //next attempt
            }
        }
        throw new CommandException("Number of attempts (" + times + ") exhausted, giving up");
    }



    private void runCommandEmbedded() throws CommandException {
        try {
            GFLauncher launcher;

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

            boolean verbose = getBooleanOption("verbose");
            info.setVerbose(verbose);
            info.setDebug(getBooleanOption("debug"));
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
                report(info);
            //}
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    // bnevins: note to me -- this String handling is EVIL.
    // Need to add plenty of utilities...
    private void waitForDAS(Set<Integer> ports) throws CommandException {
        if (ports == null || ports.size() <= 0) {
            String msg = strings.get("noPorts");
            throw new CommandException(
                    strings.get("CommandUnSuccessfulWithArg", name, msg));
        }
        long startWait = System.currentTimeMillis();
        logger.printMessage(strings.get("WaitDAS"));

        boolean alive = false;

        pinged:
        while (!timedOut(startWait)) {
            for (int port : ports) {
                if (isServerAlive(port)) {
                    alive = true;
                    break pinged;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // don't care
            }
        }

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
 
    private void report(GFLauncherInfo info) {
        String msg = strings.get("DomainLocation", info.getDomainName(),
                            info.getDomainRootDir().getAbsolutePath());
        logger.printMessage(msg);
        Integer ap = -1;
        try {
            ap = info.getAdminPorts().iterator().next();
        } catch (Exception e) {
            //ignore
        }
        msg = strings.get("DomainAdminPort", ""+ap);
        logger.printMessage(msg);
    }

    /***
    private void waitForParentToDie() {
        try {
            // TODO timeout
            // When parent process is dead in.read returns -1 (EOF)
            // as the pipe breaks.
            while (System.in.read() >= 0)
                ;
 
        } catch (IOException ex) {
            Logger lg = Logger.getLogger(StartDomainCommand.class.getName());
            lg.log(Level.SEVERE, null, ex);
        }
    }
    */
    private GFLauncherInfo info;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartDomainCommand.class);
}
