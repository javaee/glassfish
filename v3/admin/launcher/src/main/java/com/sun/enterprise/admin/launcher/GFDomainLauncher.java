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
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.sun.enterprise.glassfish.bootstrap.Main;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.*;

/**
 * GFDomainLauncher
 * This class is a package-private subclass of GFLauncher designed for
 * domain launching
 * @author bnevins
 */
class GFDomainLauncher extends GFLauncher {

    GFDomainLauncher(GFLauncherInfo info) {
        super(info);
    }

    void internalLaunch() throws GFLauncherException {
        try {
            if (getInfo().isEmbedded()) {
                launchEmbedded();
            }
            else {
                launchExternal();
            }
        }
        catch (GFLauncherException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new GFLauncherException(ex);
        }
    }

    private void launchEmbedded() throws GFLauncherException, BootException {
        Main main = new Main();
        main.start(getInfo().getArgsAsStringArray());
        GFLauncherLogger.info("finishedEmbedded", getInfo().getDomainName());
    }

    private void launchExternal() throws GFLauncherException, MiniXmlParserException {
        List<String> cmds = getCommandLine();
        ProcessBuilder pb = new ProcessBuilder(cmds);

        // Temporary
        System.out.println("****************************************************");
        System.out.println("************ TP2 Launcher  *******************");
        System.out.println("** The TP2 Launcher does not support profiling");
        System.out.println("** The commandline below is here for your info.  When logging is setup " +
                "it will go to the log file.");
        System.out.println("**   jvm command line  **");
        System.out.println("---------------------------------------------------");
        for (String s : cmds) {
            System.out.println(s);
        }
        System.out.println("****************************************************");
        
        //run the process and attach Stream Drainers
        Process p;
        try {
            p = pb.start();
            if (getInfo().isVerbose())
                ProcessStreamDrainer.redirect(getInfo().getDomainName(), p);
            else
                ProcessStreamDrainer.drain(getInfo().getDomainName(), p);
        }
        catch (IOException e) {
            throw new GFLauncherException("jvmfailure", e, e);
        }

        long endTime = System.currentTimeMillis();
        GFLauncherLogger.info("launchTime", (endTime - getStartTime()));
        
        //if verbose, hang round until the domain stops
        try {
            if (getInfo().isVerbose())
                p.waitFor();
        }
        catch (InterruptedException ex) {
            throw new GFLauncherException("verboseInterruption", ex, ex);
        }
    }


    List<File> getMainClasspath() throws GFLauncherException {
        List<File> list = new ArrayList<File>();
        File f = new File(getEnvProps().get(INSTALL_ROOT_PROPERTY));
        f = new File(f, BOOTSTRAP_JAR_RELATIVE_PATH);

        if (!f.exists())
            throw new GFLauncherException("nobootjar", f.getPath());

        list.add(GFLauncherUtils.absolutize(f));
        return list;
    }

    String getMainClass() throws GFLauncherException {
        return MAIN_CLASS;
    }
    
    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.Main";
    private static final String BOOTSTRAP_JAR_RELATIVE_PATH = "modules/glassfish-10.0-SNAPSHOT.jar";
}

