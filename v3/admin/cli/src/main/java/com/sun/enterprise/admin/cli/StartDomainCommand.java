/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.cli;

import static com.sun.enterprise.admin.cli.CLIConstants.*;
import com.sun.enterprise.admin.cli.remote.CLIRemoteCommand;
import com.sun.enterprise.admin.cli.remote.CommandInvoker;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.util.*;
import java.util.logging.*;

public class StartDomainCommand extends AbstractCommand {

    public void runCommand() throws CommandException, CommandValidationException {
    
        try {
            validateOptions();
            GFLauncher launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.domain);
            info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName((String) operands.firstElement());
            }

            String parent = getOption("domaindir");

            if (parent != null) {
                info.setDomainParentDir(parent);
            }

            boolean verbose = getBooleanOption("verbose");
            boolean watchdog = getBooleanOption(WATCHDOG);
            info.setVerbose(verbose);
            info.setWatchdog(watchdog);
            info.setDebug(getBooleanOption("debug"));
            launcher.setup();
            // CLI calls this method only to ensure that domain.xml is parsed
            // once. This is a performance optimization.
            // km@dev.java.net (Aug 2008)
            if(isServerAlive(info.getAdminPorts())) {
                String port = info.getAdminPorts().toArray(new Integer[0])[0] + "";
                String msg = getLocalizedString("ServerRunning", new String[]{info.getDomainName(), port});
                throw new CommandException(msg);
            }
            
            // if we are in watchdog mode, we may need to restart indefinitely
            if(watchdog) {
                boolean restart;
                do {
                    launcher.launch();
                    int exit = launcher.getExitValue();

                    // temporary!  TODO
                    restart = Boolean.parseBoolean(System.getenv("GLASSFISH_RESTART"));
                    // restart = (exit == RESTART_EXIT_VALUE);
                } while (restart);
            }
            else {
                launcher.launch();
                waitForDAS(info.getAdminPorts());
                report(info);
            }
        }
        catch(GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        }
        catch(MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    // bnevins: note to me -- this String handling is EVIL.  Need to add plenty of utilities...
    
    private void waitForDAS(Set<Integer> ports) throws CommandException {
        try {
            CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            if(ports == null || ports.size() <= 0) {
                String msg = getLocalizedString("noPorts");
                throw new CommandException(
                        getLocalizedString("CommandUnSuccessfulWithArg", new Object[]{name, msg}));
                }
            long startWait = System.currentTimeMillis();
            Log.info("WaitDAS");

            boolean alive = false;

            pinged:
            while(!timedOut(startWait)) {
                for (int port : ports) {
                    if (isServerAlive(port)) {
                        alive = true;
                        break pinged;
                    }
                }
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ex) {
                    // don't care
                }
            }

            if(!alive) {
                Object[] objs = new Object[] {info.getDomainName(), (WAIT_FOR_DAS_TIME_MS / 1000)};            
                String msg = getLocalizedString("dasNoStart", objs);
                throw new CommandException(msg);
            }
        }
        finally{
            CLILogger.getInstance().popAndUnlockLevel();
        }                
    }
    
    private boolean isServerAlive(int port) {
        CommandInvoker invoker = new CommandInvoker(CLIRemoteCommand.RELIABLE_COMMAND); // version
        invoker.put(PORT, ""+port);
        invoker.put(USER, getOption(USER));
        invoker.put(PASSWORDFILE, getOption(PASSWORDFILE));
        //what about --secure, that's next!
        return (CLIRemoteCommand.pingDASQuietly(invoker));
    }
    
    private boolean isServerAlive(Set<Integer> ports) {
        if (ports == null || ports.size() == 0)
            return false;
        return ( isServerAlive(ports.toArray(new Integer[0])[0]) );
    }
    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
    
    private void report(GFLauncherInfo info) {
        CLILogger lg = CLILogger.getInstance();
        try {
            lg.pushAndLockLevel(Level.INFO);
            String msg = getLocalizedString("DomainLocation", new String[]{info.getDomainName(), info.getDomainRootDir().getAbsolutePath()});
            lg.printMessage(msg);
            Integer ap = -1;
            try {
                ap = info.getAdminPorts().toArray(new Integer[0])[0];
            } catch(Exception e) {
                //ignore
            }
            msg = getLocalizedString("DomainAdminPort", new String[]{"" + ap});
            lg.printMessage(msg);
        } finally {
            lg.popAndUnlockLevel();
        }
    }
    private GFLauncherInfo info;
}


