/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.bootstrap.StartupContext;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.module.bootstrap.ArgumentManager;
import com.sun.enterprise.module.bootstrap.PlatformMain;

/**
 * Tag Main to get the manifest file 
 */
public class ASMain {

    /*
     * Most of the code in this file has been moved to ASMainHelper
     *and  ASMainOSGi
     */
    final static Logger logger = Logger.getAnonymousLogger();

    private final static String PLATFORM_PROPERTY_KEY = "GlassFish_Platform";

    // bundle containing module startup
    private final static String GF_KERNEL = "org.glassfish.core.kernel";
    // Supported platform we know about, not limited to.
    public enum Platform {Felix, Knopflerfish, Equinox, Static}

    public static void main(final String args[]) {
        int minor = getMinorJdkVersion();

        if(minor < 6) {
            logger.severe("GlassFish requires JDK 6, you are using JDK version " + minor);
            System.exit(1);
        }
        setStartupContextProperties(args);
        String platform = Platform.Felix.toString(); // default is Felix

        // first check the system props
        String temp = System.getProperty(PLATFORM_PROPERTY_KEY);
        if (temp == null || temp.trim().length() <= 0) {
            // not in sys props -- check environment
            temp = System.getenv(PLATFORM_PROPERTY_KEY);
        }

        if (temp != null && temp.trim().length() != 0) {
            platform = temp.trim();
        }


        PlatformMain delegate=getMain(platform);
        if (delegate!=null) {

            logger.info("Launching GlassFish on " + platform + " platform");            
            // Set the system property if downstream code wants to know about it
            System.setProperty(PLATFORM_PROPERTY_KEY, platform);
            
            try {
                delegate.setLogger(logger);
                delegate.start(args);
            } catch(Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else {
            logger.severe("Cannot launch GlassFish on the unkown " + platform + " platform");
        }
    }

    /**
     * use META-INF/services services definition to look up all possible platform implementations
     * and return the one
     * @param platform the platform name {@see AbstractMain#getName()}
     * @return an platform provider or null if not found
     */
    private static PlatformMain getMain(String platform) {
        ServiceLoader<PlatformMain> loader =  ServiceLoader.load(PlatformMain.class, ASMain.class.getClassLoader());
        for (PlatformMain main : loader) {
            if (main.getName().equalsIgnoreCase(platform))
                return main;
        }
        return null;
    }

    /**
     * Save the args in a system property
     */

    private static void setStartupContextProperties(String... args)
    {
        Properties p = ArgumentManager.argsToMap(args);
        p.put(StartupContext.TIME_ZERO_NAME, (new Long(System.currentTimeMillis())).toString());
        p.put(StartupContext.STARTUP_MODULE_NAME, GF_KERNEL);
        // temporary hack until CLI does that for us.
        for (int i=0;i<args.length;i++) {
            if (args[i].equals("-upgrade")) {
                if (i+1<args.length && !args[i+1].equals("false"))  {
                    p.put(StartupContext.STARTUP_MODULESTARTUP_NAME, "upgrade" );
                }
            }
        }
        addRawStartupInfo(args, p);

        try {
            Writer writer = new StringWriter();
            p.store(writer, null);
            System.setProperty(StartupContext.ARGS_PROP, writer.toString());
        }
        catch (IOException e) {
            logger.info("Could not save startup parameters, will start with none");
            System.setProperty(StartupContext.ARGS_PROP, "");
        }
    }

    /**
     * Need the raw unprocessed args for RestartDomainCommand in case we were NOT started 
     * by CLI
     *
     * @param args raw args to this main()
     * @param p the properties to save as a system property
     */
    private static void addRawStartupInfo(final String[] args, final Properties p) {
        //package the args...
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < args.length; i++) {
            if(i > 0)
                sb.append(StartupContext.ARG_SEP);

            sb.append(args[i]);
        }

        if(!wasStartedByCLI(p)) {
            // no sense doing this if we were started by CLI...
            p.put(StartupContext.ORIGINAL_CP, System.getProperty("java.class.path"));
            p.put(StartupContext.ORIGINAL_CN, ASMain.class.getName());
            p.put(StartupContext.ORIGINAL_ARGS, sb.toString());
        }
    }

    private static boolean wasStartedByCLI(final Properties props) {
        // if we were started by CLI there will be some special args set...

        return
            props.getProperty("-asadmin-classpath") != null &&
            props.getProperty("-asadmin-classname") != null &&
            props.getProperty("-asadmin-args")      != null;
    }

    private static int getMinorJdkVersion() {
        // this is a subset of the code in com.sun.enterprise.util.JDK
        // this module has no dependencies on util code so it was dragged in here.

        try {
            String jv = System.getProperty("java.version");
            String[] ss = jv.split("\\.");

            if(ss == null || ss.length < 3 || !ss[0].equals("1"))
                return 1;

            return Integer.parseInt(ss[1]);
        }
        catch(Exception e) {
            return 1;
        }
    }
}
