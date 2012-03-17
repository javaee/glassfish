/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.maven;

import org.glassfish.embeddable.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */
public class PluginUtil {

    private static final Logger logger = Logger.getLogger("maven-embedded-glassfish-plugin");

    static {
        logger.setLevel(Level.FINE);
    }

    static GlassFishRuntime gfr;
    // Map with Key=serverId Value=GlassFish
    private final static Map<String, GlassFish> gfMap =
            new HashMap<String, GlassFish>();

    public static GlassFish startGlassFish(String serverId,
                                           ClassLoader bootstrapClassLoader,
                                           Properties bootstrapProperties,
                                           Properties glassfishProperties) throws Exception {
        GlassFish gf = getGlassFish(serverId, bootstrapClassLoader,
                bootstrapProperties, glassfishProperties);
        if (gf.getStatus() != GlassFish.Status.STARTED) {
            long startTime = System.currentTimeMillis();
            gf.start();
            logger.logp(Level.INFO, "PluginUtil", "startGlassFish", "Started GlassFish ServerId = {0}, " +
                    "GlassFish = {1}, TimeTaken = {2} ms",
                    new Object[]{serverId, gf, System.currentTimeMillis() - startTime});
        }
        return gf;
    }

    public static void stopGlassFish(String serverId) throws Exception {
        GlassFish gf = gfMap.remove(serverId);
        if (gf != null && gf.getStatus().equals(GlassFish.Status.STARTED)) {
            gf.stop();
            if (gfr != null) {
                gfr.shutdown();
            }
        }
        logger.logp(Level.INFO, "PluginUtil", "stopGlassFish",
                "Stopped GlassFish ServerId = {0}, GlassFish = {1}",
                new Object[]{serverId, gf});
    }

    public static void doDeploy(String serverId, ClassLoader cl,
                                Properties bootstrapProperties,
                                Properties glassfishProperties,
                                File archive, String[] deploymentParameters) throws Exception {
        GlassFish gf = startGlassFish(serverId, cl, bootstrapProperties, glassfishProperties);
        // Lookup the deployer.
        Deployer deployer = gf.getService(Deployer.class);
        logger.logp(Level.FINE, "PluginUtil", "doDeploy", "Deployer = {0}", deployer);
        logger.info("Deploying [" + archive + "] with parameters " +
                (deploymentParameters!= null ? Arrays.asList(deploymentParameters).toString() : "[]"));
        String name = deployer.deploy(archive.toURI(), deploymentParameters);
        logger.logp(Level.INFO, "PluginUtil", "doDeploy", "Deployed {0}", name);
    }

    public static void doUndeploy(String serverId, ClassLoader bootstrapClassLoader,
                                  Properties bootstrapProperties,
                                  Properties glassfishProperties,
                                  String appName, String[] deploymentParameters) {
        try {
            GlassFish gf = startGlassFish(serverId, bootstrapClassLoader,
                    bootstrapProperties, glassfishProperties);
            // Lookup the deployer.
            Deployer deployer = gf.getService(Deployer.class);
            logger.logp(Level.INFO, "PluginUtil", "doUndeploy", "Deployer = {0}", deployer);

            deployer.undeploy(appName, deploymentParameters);
            logger.logp(Level.INFO, "PluginUtil", "doUndeploy", "Undeployed {0}", appName);
        } catch (Exception ex) {
            // Ignore the exception since it is undeployment.
            logger.logp(Level.WARNING, "PluginUtil", "doUndeploy", "Unable to undeploy {0}. Exception = {1}",
                    new Object[]{appName, ex.getMessage()});
        }
    }

    private static GlassFish getGlassFish(String serverId, ClassLoader bootstrapClassLoader,
                                          Properties bootstrapProperties,
                                          Properties glassfishProperties)
            throws Exception {
        GlassFish gf = gfMap.get(serverId);
        if (gf == null) {
            long startTime = System.currentTimeMillis();
            logger.logp(Level.FINE, "PluginUtil", "getGlassFish", "Creating GlassFish ServerId = {0}", serverId);
            BootstrapProperties bootstrapOptions = new BootstrapProperties(bootstrapProperties);
            gfr = gfr != null ? gfr : GlassFishRuntime.bootstrap(bootstrapOptions, bootstrapClassLoader);
/*
            GlassFishRuntime gfr = GlassFishRuntime.bootstrap(bootstrapOptions,
                    PluginUtil.class.getClassLoader());
*/
            logger.logp(Level.FINE, "PluginUtil", "getGlassFish", "Created GlassFishRuntime " +
                    "ServerId = {0}, GlassFishRuntime = {1}, TimeTaken = {2} ms",
                    new Object[]{serverId, gfr, System.currentTimeMillis() - startTime});
            GlassFishProperties gfOptions = new GlassFishProperties(glassfishProperties);
            gf = gfr.newGlassFish(gfOptions);
            logger.logp(Level.INFO, "PluginUtil", "getGlassFish", "Created GlassFish ServerId = {0}, " +
                    "BootstrapProperties = {1}, GlassFishRuntime = {2}, GlassFishProperties = {3}, " +
                    "GlassFish = {4}, GlassFish Status = {5}, TimeTaken = {6} ms",
                    new Object[]{serverId, bootstrapProperties, gfr, glassfishProperties,
                            gf, gf.getStatus(), System.currentTimeMillis() - startTime});
            gfMap.put(serverId, gf);
        }
        return gf;
    }

    public static void runCommand(String serverId, String[] commandLines)
            throws Exception {
        GlassFish gf = gfMap.get(serverId);
        if (gf != null) {
            CommandRunner cr = gf.getService(CommandRunner.class);
            for (String commandLine : commandLines) {
                String[] split = commandLine.split(" ");
                String command = split[0].trim();
                String[] commandParams = null;
                if (split.length > 1) {
                    commandParams = new String[split.length - 1];
                    for (int i = 1; i < split.length; i++) {
                        commandParams[i - 1] = split[i].trim();
                    }
                }
                try {
                    CommandResult result = commandParams == null ?
                            cr.run(command) : cr.run(command, commandParams);
                    logger.logp(Level.INFO, "PluginUtil", "runCommand",
                            "Ran command [{0}]. Exit Code [{1}], Output = [{2}]",
                            new Object[]{commandLine, result.getExitStatus(), result.getOutput()});
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

}
