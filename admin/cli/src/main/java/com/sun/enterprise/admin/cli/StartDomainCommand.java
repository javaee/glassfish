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

import com.sun.enterprise.admin.cli.remote.CLIRemoteCommand;
import com.sun.enterprise.admin.cli.remote.CommandInvoker;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.BufferedReader;
import java.net.*;
import java.util.*;
import java.util.logging.*;

public class StartDomainCommand extends AbstractCommand {
    public void runCommand() throws CommandException, CommandValidationException {
        try {
            GFLauncher launcher = null;

            // bnevins nov 23 2008
            // Embedded is a new type of server
            // For now -- we ONLY start embedded
			
            //boolean gfe = Boolean.parseBoolean(System.getenv("GFE_ENABLED"));
            //if(gfe)
                launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.embedded);
            /*
             else
                launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.domain);
            */

            info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName((String) operands.firstElement());
            }

            String parent = getOption("domaindir");

            if (parent != null) {
                info.setDomainParentDir(parent);
            }

            boolean verbose = getBooleanOption("verbose");
            info.setVerbose(verbose);
            info.setDebug(getBooleanOption("debug"));
            launcher.setup();




            if(isServerAlive(4848)) {
                String msg = getLocalizedString("ServerRunning2");
                throw new CommandException(msg);
            }
            
            launcher.launch();
            
            // if we are in verbose mode, we definitely do NOT want to wait for DAS --
            // since it already ran and is now dead!!
            //if(!verbose) {
                waitForDAS(info.getAdminPorts());
                report(info);
            //}
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
        try {
            // world's simplest test
            Socket socket = new Socket("localhost", port);
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }

    /*
     private boolean isServerAlive(int port) {
        CommandInvoker invoker = new CommandInvoker(CLIRemoteCommand.RELIABLE_COMMAND); // version
        invoker.put(PORT, ""+port);
        invoker.put(USER, getOption(USER));
        invoker.put(PASSWORDFILE, getOption(PASSWORDFILE));
        //what about --secure, that's next!
        return (CLIRemoteCommand.pingDASQuietly(invoker));
    }
     */
    
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
        } catch(Exception e) {
            lg.printMessage("Server started");
        } finally {
            lg.popAndUnlockLevel();
        }
    }
    private static final long WAIT_FOR_DAS_TIME_MS = 90000;
    private GFLauncherInfo info;
}


