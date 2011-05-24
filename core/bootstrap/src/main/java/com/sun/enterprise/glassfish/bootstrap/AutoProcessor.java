/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.startlevel.StartLevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Copied from Felix: AutoProcessor.java
 */
public class AutoProcessor {
    /**
     * The property name prefix for the launcher's auto-install property.
     */
    public static final String AUTO_INSTALL_PROP = "glassfish.auto.install";
    /**
     * The property name prefix for the launcher's auto-start property.
     */
    public static final String AUTO_START_PROP = "glassfish.auto.start";

    private static Logger logger = Logger.getLogger(AutoProcessor.class.getPackage().getName());

    /**
     * Used to instigate auto-deploy directory process and auto-install/auto-start
     * configuration property processing during.
     *
     * @param configMap Map of configuration properties.
     * @param context   The system bundle context.
     */
    public static void process(Map configMap, BundleContext context) {
        configMap = (configMap == null) ? new HashMap() : configMap;
        processAutoProperties(configMap, context);
    }

    /**
     * <p>
     * Processes the auto-install and auto-start properties from the
     * specified configuration properties.
     * </p>
     */
    private static void processAutoProperties(Map configMap, BundleContext context) {
        // Retrieve the Start Level service, since it will be needed
        // to set the start level of the installed bundles.
        StartLevel sl = (StartLevel) context.getService(
                context.getServiceReference(org.osgi.service.startlevel.StartLevel.class.getName()));

        // Retrieve all auto-install and auto-start properties and install
        // their associated bundles. The auto-install property specifies a
        // space-delimited list of bundle URLs to be automatically installed
        // into each new profile, while the auto-start property specifies
        // bundles to be installed and started. The start level to which the
        // bundles are assigned is specified by appending a ".n" to the
        // property name, where "n" is the desired start level for the list
        // of bundles. If no start level is specified, the default start
        // level is assumed.
        for (Iterator i = configMap.keySet().iterator(); i.hasNext();) {
            String key = ((String) i.next()).toLowerCase();

            // Ignore all keys that are not an auto property.
            if (!key.startsWith(AUTO_INSTALL_PROP) && !key.startsWith(AUTO_START_PROP)) {
                continue;
            }

            // If the auto property does not have a start level,
            // then assume it is the default bundle start level, otherwise
            // parse the specified start level.
            int startLevel = sl.getInitialBundleStartLevel();
            if (!key.equals(AUTO_INSTALL_PROP) && !key.equals(AUTO_START_PROP)) {
                try {
                    startLevel = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
                }
                catch (NumberFormatException ex) {
                    System.err.println("Invalid property: " + key);
                }
            }

            // Parse and install the bundles associated with the key.
            StringTokenizer st = new StringTokenizer((String) configMap.get(key), "\" ", true);
            for (String location = nextLocation(st); location != null; location = nextLocation(st)) {
                try {
                    Bundle b = context.installBundle(location, null);
                    sl.setBundleStartLevel(b, startLevel);
                }
                catch (Exception ex) {
                    // We log in FINE level because of issue # 15486
                    logger.logp(Level.FINE, "AutoProcessor", "processAutoProperties",
                            "Auto-properties install: " + location, ex);
                }
            }
        }

        // Now loop through the auto-start bundles and start them.
        for (Iterator i = configMap.keySet().iterator(); i.hasNext();) {
            String key = ((String) i.next()).toLowerCase();
            if (key.startsWith(AUTO_START_PROP)) {
                StringTokenizer st = new StringTokenizer((String) configMap.get(key), "\" ", true);
                for (String location = nextLocation(st); location != null; location = nextLocation(st)) {
                    // Installing twice just returns the same bundle.
                    try {
                        Bundle b = context.installBundle(location, null);
                        if (b != null) {
                            b.start();
                        }
                    }
                    catch (Exception ex) {
                        // We log in FINE level because of issue # 15486
                        logger.logp(Level.FINE, "AutoProcessor", "processAutoProperties",
                                "Auto-properties start: " + location, ex);
                    }
                }
            }
        }
    }

    private static String nextLocation(StringTokenizer st) {
        String retVal = null;

        if (st.countTokens() > 0) {
            String tokenList = "\" ";
            StringBuffer tokBuf = new StringBuffer(10);
            String tok = null;
            boolean inQuote = false;
            boolean tokStarted = false;
            boolean exit = false;
            while ((st.hasMoreTokens()) && (!exit)) {
                tok = st.nextToken(tokenList);
                if (tok.equals("\"")) {
                    inQuote = !inQuote;
                    if (inQuote) {
                        tokenList = "\"";
                    } else {
                        tokenList = "\" ";
                    }

                } else if (tok.equals(" ")) {
                    if (tokStarted) {
                        retVal = tokBuf.toString();
                        tokStarted = false;
                        tokBuf = new StringBuffer(10);
                        exit = true;
                    }
                } else {
                    tokStarted = true;
                    tokBuf.append(tok.trim());
                }
            }

            // Handle case where end of token stream and
            // still got data
            if ((!exit) && (tokStarted)) {
                retVal = tokBuf.toString();
            }
        }

        return retVal;
    }
}
