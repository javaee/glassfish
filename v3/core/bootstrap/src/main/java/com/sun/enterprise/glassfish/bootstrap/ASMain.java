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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.glassfish.bootstrap;

import java.util.logging.Logger;

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

    // We add both KnopflerFish and Knopflerfish for backward compatibility
    // between tp2 and v3 trunk.
    private enum Platform {HK2, Felix, Knopflerfish, KnopflerFish, Equinox, Static}

    public static void main(final String args[]) {
        Platform platform = Platform.Felix; // default is Felix

        // first check the system props
        String temp = System.getProperty(PLATFORM_PROPERTY_KEY);
        if (temp == null || temp.trim().length() <= 0) {
            // not in sys props -- check environment
            temp = System.getenv(PLATFORM_PROPERTY_KEY);
        }

        if (temp != null && temp.trim().length() != 0) {
            platform = Platform.valueOf(temp.trim());
        }

        // Set the system property if downstream code wants to know about it
        System.setProperty(PLATFORM_PROPERTY_KEY, platform.toString());

        switch (platform) {
            case Felix:
                logger.info("Launching GlassFish on Apache Felix OSGi platform");
                new ASMainFelix(logger, args).run();
                break;
            case Equinox:
                logger.info("Launching GlassFish on Equinox OSGi platform");
                new ASMainEquinox(logger, args).run();
                break;
            case Knopflerfish:
            case KnopflerFish:
                logger.info("Launching GlassFish on Knopflerfish OSGi platform");
                new ASMainKnopflerFish(logger, args).run();
                break;
            case HK2:
                throw new RuntimeException("GlassFish does not run on the HK2 platform anymore");
            case Static:
                new ASMainStatic(logger, args).run();
                break;
            default:
                throw new RuntimeException("Platform not yet supported");
        }
    }

}
