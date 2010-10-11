/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import org.glassfish.embeddable.BootstrapConstants;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

import org.glassfish.embeddable.BootstrapOptions;
import org.glassfish.embeddable.GlassFishOptions;
import static com.sun.enterprise.module.bootstrap.ArgumentManager.argsToMap;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class GlassFishMain {

    // TODO(Sahoo): Move the code to ASMain once we are ready to phase out ASMain

    public static void main(final String args[]) throws Exception {
        ASMainHelper.checkJdkVersion();

        final Properties argsAsProps = argsToMap(args);

        String platform = ASMainHelper.whichPlatform();

        System.out.println("Launching GlassFish on " + platform + " platform");

        // Set the system property if downstream code wants to know about it
        System.setProperty(BootstrapConstants.PLATFORM_PROPERTY_KEY, platform); // TODO(Sahoo): Why is this a system property?

        File installRoot = ASMainHelper.findInstallRoot();

        // domainDir can be passed as argument, so pass the agrgs as well.
        File instanceRoot = ASMainHelper.findInstanceRoot(installRoot, argsAsProps);

        Properties ctx = ASMainHelper.buildStartupContext(platform, installRoot, instanceRoot, args);
        /*
         * We have a tricky class loading issue to solve. GlassFishRuntime looks for an implementation of RuntimeBuilder.
         * In case of OSGi, the implementation class is OSGiGlassFishRuntimeBuilder. OSGiGlassFishRuntimeBuilder has
         * compile time dependency on OSGi APIs, which are unavoidable. More over, OSGiGlassFishRuntimeBuilder also
         * needs to locate OSGi framework factory using some class loader and that class loader must share same OSGi APIs
         * with the class loader of OSGiGlassFishRuntimeBuilder. Since we don't have the classpath for OSGi framework
         * until main method is called (note, we allow user to select what OSGi framework to use at runtime without
         * requiring them to add any extra jar in system classpath), we can't assume that everything is correctly set up in
         * system classpath. So, we create a single class loader which has GlassFishRuntime, OSGiGlassFishRuntimebuilder
         * and OSGi framework jar in claspath. We use that class loader to load Launcher class which is used to load
         * glassfishapi as well as implementation of glassfishapi and OSGi framework. This launcher class loader is also
         * used as the parent class loader for underlying module system (be it HK2 or OSGi). In case of OSGi, using
         * bootdelegation property or system package list to make sure only version of glassfishapi is loaded.
         */
        final ClassLoader launcherCL = ASMainHelper.createLauncherCL(ctx,
                ClassLoader.getSystemClassLoader().getParent());
        Class launcherClass = launcherCL.loadClass(GlassFishMain.Launcher.class.getName());
        Object launcher = launcherClass.newInstance();
        Method method = launcherClass.getMethod("launch", Properties.class);
        method.invoke(launcher, ctx);
    }

    public static class Launcher {
        /*
         * Only this class has compile time dependency on glassfishapi.
         */
        private static volatile GlassFish gf;
        private static volatile GlassFishRuntime gfr;

        public Launcher() {
        }

        public void launch(Properties ctx) throws Exception {
            addShutdownHook();
            gfr = GlassFishRuntime.bootstrap(new BootstrapOptions(ctx), getClass().getClassLoader());
            gf = gfr.newGlassFish(new GlassFishOptions(ctx));
            if (Boolean.valueOf(Util.getPropertyOrSystemProperty(ctx, "GlassFish_Interactive", "false"))) {
                startConsole();
            } else {
                gf.start();
            }
        }

        private void startConsole() throws IOException {
            String command;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while ((command = readCommand(reader)) != null) {
                try {
                    System.out.println("command = " + command);
                    if ("start".equalsIgnoreCase(command)) {
                        if (gf.getStatus() != GlassFish.Status.STARTED || gf.getStatus() == GlassFish.Status.STOPPING || gf.getStatus() == GlassFish.Status.STARTING)
                            gf.start();
                        else System.out.println("Already started or stopping or starting");
                    } else if ("stop".equalsIgnoreCase(command)) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            System.out.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        gf.stop();
                    } else if (command.startsWith("deploy")) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            System.out.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        Deployer deployer = gf.lookupService(Deployer.class, null);
                        final File file = new File(command.substring(command.indexOf(" ")).trim());
                        String name = deployer.deploy(file, new HashMap<String, String>());
                        System.out.println("Deployed = " + name);
                    } else if (command.startsWith("undeploy")) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            System.out.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        Deployer deployer = gf.lookupService(Deployer.class, null);
                        String name = command.substring(command.indexOf(" ")).trim();
                        deployer.undeploy(name, new HashMap<String, String>());
                        System.out.println("Undeployed = " + name);
                    } else if ("quit".equalsIgnoreCase(command)) {
                        System.exit(0);
                    } else {
                        System.out.println("Unrecognized command:" + command);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        private String readCommand(BufferedReader reader) throws IOException {
            prompt();
            String command = null;
            while((command = reader.readLine()) != null && command.isEmpty()) {
                // loop until a non empty command or Ctrl-D is inputted.  
            }
            return command;
        }

        private void prompt() {
            System.out.print("Enter any of the following commands: start, stop, quit, deploy <path to file>, undeploy <name of app>\n" +
                    "glassfish$ ");
            System.out.flush();
        }

        private static void addShutdownHook() {
            Runtime.getRuntime().addShutdownHook(new Thread("GlassFish Shutdown Hook") {
                public void run() {
                    try {
                        if (gf != null) {
                            gf.stop();
                        }
                        gfr.shutdown();
                    }
                    catch (Exception ex) {
                        System.err.println("Error stopping framework: " + ex);
                        ex.printStackTrace();
                    }
                }
            });

        }

    }

}
