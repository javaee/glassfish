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
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.cli.framework.*;
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
            info.setVerbose(verbose);
            info.setDebug(getBooleanOption("debug"));
            launcher.launch();
            
            // if we are in verbose mode, we definitely do NOT want to wait for DAS --
            // since it already ran and is now dead!!
            if(!verbose)
                waitForDAS(info.getAdminPorts());
        }
        catch(GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
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
                    if (CLIRemoteCommand.pingDAS(port)) {
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
    
    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
    private static final long WAIT_FOR_DAS_TIME_MS = 90000;
    private GFLauncherInfo info;
}


