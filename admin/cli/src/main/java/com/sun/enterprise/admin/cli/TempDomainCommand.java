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

import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.net.NetUtils;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public class TempDomainCommand extends Command {

    public boolean validateOptions() throws CommandValidationException {
        return true;
    }

    public void runCommand() throws CommandException, CommandValidationException {
        Level originalLevel = logger.getOutputLevel();
        verbose = getBooleanOption("verbose");
        
        try {
            // get rid of chatty kathy
            logger.setOutputLevel(Level.SEVERE);
            //logger.allowLevelChanges(false);
            createDomain();
            startDomainThread();
            deploy();
            launcherThread.join();
        }
        catch (Throwable t) {
            throw new CommandException(getLocalizedString("CommandUnSuccessfulWithArg",
                    new Object[]{name, t.getMessage()}), t);
        }
        finally {
            //logger.allowLevelChanges(true);
            logger.setOutputLevel(originalLevel);
        }
        
    }

    private void createDomain() throws CommandException, CommandValidationException, InvalidCommandException {
        //create-domain --instanceport 70 --adminport 4848 --domaindir c:/foo --user ""  temp01
        setDomainDir();
        setPorts();
        System.out.println("**** Instance PORT: " + instancePort);
        System.out.println("**** Admin PORT: " + adminPort);
        System.out.println("**** DomainDir: " + domainDir);
        CLIMain cli = new com.sun.enterprise.cli.framework.CLIMain();

        cli.invokeCommand(getCreateDomainArgs());
    }

    private String[] getCreateDomainArgs() {
        String[] args = new String[]{
            "create-domain",
            "--instanceport", "" + instancePort,
            "--adminport", "" + adminPort,
            "--domaindir", domainsDir.getPath(),
            "--user", "",
            domainDir.getName()
        };
        return args;

    }

    private void setPorts() throws CommandException {
        // do NOT try a different port for instance port -- it is

        String portString = getOption("port");
        instancePort = 0;

        try {
            instancePort = Integer.parseInt(portString);
        }
        catch (NumberFormatException e) {
            throw new CommandException("xxxx", e);
        }

        if (instancePort <= 0 || instancePort > 65535) {
            throw new CommandException("xxxx");
        }

        if (!NetUtils.isPortFree(instancePort)) {
            throw new CommandException("xxx port not free");
        }
        for (adminPort = DEFAULT_ADMIN_PORT; adminPort < 65535; adminPort += 5) {
            if (NetUtils.isPortFree(adminPort))
                break;
        }
        if (adminPort >= 65535)
            throw new CommandException("xxxx");
    }

    private void setDomainDir() throws CommandException {
        boolean valid = false;
        setDomainsDir();

        for (int i = 0; i < 100000; i++) {
            domainDir = new File(domainsDir, DOMAIN_DIR_BASE + i);
            if (!domainDir.exists()) {
                valid = true;
                break;
            }
        }
        if (!valid)
            throw new CommandException("xxx");
    }

    private void setDomainsDir() throws CommandException {
        String tmpRootName = System.getProperty("java.io.tmpdir");

        if (!ok(tmpRootName))
            throw new CommandException("xxxxxx");

        File tmpRoot = new File(tmpRootName);

        if (!tmpRoot.isDirectory())
            throw new CommandException("xxxxxx");

        domainsDir = tmpRoot;
    }

    private void startDomainThread() throws CommandException, CommandValidationException, InvalidCommandException {
        // start the domain in another thread in verbose mode
        // the other thread will live until AS is stopped externally or
        // a ^C is entered at the console.
        
        launcherThread = new Thread(new Runnable() {
            public void run(){
                    startDomain();
            }
        }, LAUNCHER_THREAD_NAME);
        launcherThread.start();
        waitForDAS(true);
    }

    private void startDomain() {
        try {
            GFLauncher launcher = GFLauncherFactory.getInstance(GFLauncherFactory.ServerType.domain);
            GFLauncherInfo info = launcher.getInfo();
            info.setDomainRootDir(domainDir);
            info.setVerbose(true);
            info.setDebug(true);
            info.setDeleteDomainOnExit(true);
            launcher.launch();
        }
        catch (Exception ex) {
            System.out.println("xxx Unable to start domain: " + ex);
            System.exit(1);
        }
    }
    
    private boolean timedOut(long start) {
        return (System.currentTimeMillis() - start) > TIMEOUT;
    }
    
    private void waitForDAS(boolean toStart) throws CommandException {
        long startWait = System.currentTimeMillis();
        boolean alive = false;
        
        while(!timedOut(startWait)) {
            if(RemoteCommand.pingDAS(adminPort)) {
                alive = true;
                
                if(toStart)
                    break;
            }
            else {
                if(!toStart)
                    break;
            }
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException ex) {
                // don't care
            }
        }
        if(!alive) {
            //Object[] objs = new Object[] {info.getDomainName(), (WAIT_FOR_DAS_TIME_MS / 1000)};            
            //String msg = getLocalizedString("dasNoStart", objs);
            throw new CommandException("xxx DAS timeout");
        }
    }
         
    private String[] getStartDomainArgs() {
        String[] args = new String[] {
            "start-domain",
            "--domaindir", domainsDir.getPath(),
            "--verbose", 
            "--debug",
            domainDir.getName()
        };
        return args;
    }
    
    private void deploy() throws CommandException, CommandValidationException, InvalidCommandException {
        System.out.println("Deploying....");
        if (operands.isEmpty()) {
            throw new CommandException("xxxx Operand Required");
        }

        new RemoteCommand(getDeployArgs());
    }

    private String[] getDeployArgs() {
        String[] args = new String[] {
            "deploy",
            "-p", "" + adminPort,
            operands.firstElement().toString()
        };
        return args;
        
    }
    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }
    
    
    
    private Thread launcherThread;
    private int instancePort;
    private int adminPort;
    private File domainsDir;
    private File domainDir;
    private boolean verbose = false;
    private static final String DOMAIN_DIR_BASE = "tmpdomain_";
    private static final int DEFAULT_ADMIN_PORT = 4848;
    private static final CLILogger logger = CLILogger.getInstance();
    private static final String LAUNCHER_THREAD_NAME = "DomainLauncher";
    private static final long   TIMEOUT = 90000;
}

