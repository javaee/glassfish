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

package org.glassfish.maven;

import org.glassfish.simpleglassfishapi.CommandRunner;
import org.glassfish.simpleglassfishapi.Deployer;
import org.glassfish.simpleglassfishapi.GlassFish;
import org.glassfish.simpleglassfishapi.GlassFishRuntime;

import java.io.File;
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

    // Map with Key=serverId Value=GlassFish
    private final static Map<String, GlassFish> gfMap =
            new HashMap<String, GlassFish>();

    public static GlassFish startGlassFish(String serverId, Properties bootstrapProperties) throws Exception {
        GlassFish gf = getGlassFish(serverId, bootstrapProperties);
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
        if (gf != null) {
            gf.stop();
            GlassFishRuntime.shutdown();
        }
        logger.logp(Level.INFO, "PluginUtil", "stopGlassFish", "Stopped GlassFish ServerId = {0}, GlassFish = {1}",
                new Object[]{serverId, gf});
    }

    public static void doDeploy(String serverId, Properties bootstrapProperties,
                                File archive, Map<String, String> deploymentParameters) throws Exception {
        GlassFish gf = startGlassFish(serverId, bootstrapProperties);
        // Lookup the deployer.
        Deployer deployer = gf.lookupService(Deployer.class, null);
        logger.logp(Level.INFO, "PluginUtil", "doDeploy", "Deployer = {0}", deployer);

        String name = deployer.deploy(archive, deploymentParameters);
        logger.logp(Level.INFO, "PluginUtil", "doDeploy", "Deployed {0}", name);
    }

    public static void doUndeploy(String serverId, Properties bootstrapProperties,
                                  String appName, Map<String, String> deploymentParameters) {
        try {
            GlassFish gf = startGlassFish(serverId, bootstrapProperties);
            // Lookup the deployer.
            Deployer deployer = gf.lookupService(Deployer.class, null);
            logger.logp(Level.INFO, "PluginUtil", "doUndeploy", "Deployer = {0}", deployer);

            deployer.undeploy(appName, deploymentParameters);
            logger.logp(Level.INFO, "PluginUtil", "doUndeploy", "Undeployed {0}", appName);
        } catch (Exception ex) {
            // Ignore the exception since it is undeployment.
            logger.logp(Level.WARNING, "PluginUtil", "doUndeploy", "Unable to undeploy {0}. Exception = {1}",
                    new Object[]{appName, ex.getMessage()});
        }
    }

    private static GlassFish getGlassFish(String serverId, Properties bootstrapProperties)
            throws Exception {
        logger.info("serverId = " + serverId + ", GfMap = " + gfMap);
        GlassFish gf = gfMap.get(serverId);
        if (gf == null) {
            long startTime = System.currentTimeMillis();
            logger.logp(Level.INFO, "PluginUtil", "getGlassFish", "Creating GlassFish ServerId = {0}", serverId);
            GlassFishRuntime gfr = GlassFishRuntime.bootstrap(bootstrapProperties,
                    PluginUtil.class.getClassLoader());
            logger.logp(Level.INFO, "PluginUtil", "getGlassFish", "Created GlassFishRuntime ServerId = {0}, " +
                    "GlassFishRuntime = {1}, TimeTaken = {2} ms",
                    new Object[]{serverId, gfr, System.currentTimeMillis() - startTime});
            startTime = System.currentTimeMillis();
            gf = gfr.newGlassFish(bootstrapProperties);
            logger.logp(Level.INFO, "PluginUtil", "getGlassFish", "Created GlassFish ServerId = {0}, " +
                    "GlassFish = {1}, GlassFish Status = {2}, TimeTaken = {3} ms",
                    new Object[]{serverId, gf, gf.getStatus(), System.currentTimeMillis() - startTime});
            gfMap.put(serverId, gf);
        }
        return gf;
    }

    public static void runCommand(String serverId, String command, Map<String,String> args)
            throws Exception {
        GlassFish gf = gfMap.remove(serverId);
        boolean result = false;
        if (gf != null) {
            CommandRunner commandRunner = gf.lookupService(CommandRunner.class, null);
            result = commandRunner.run(command, args);
        }
        logger.logp(Level.INFO, "PluginUtil", "runCommand", "Ran command {0}, Status {1} ",
                new Object[]{command, result});
    }
    
}
