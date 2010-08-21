/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.util.*;
import java.util.logging.*;

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

    public static void main(final String args[]) throws Exception {
        ASMainHelper.checkJdkVersion();
        String platform = ASMainHelper.whichPlatform();
        if (ASMainHelper.isOSGiPlatform(platform)) {
            // For OSGi platforms, we have switched to new way of launching GlassFish.
            GlassFishMain.main(args);
            return;
        }
        File installRoot = ASMainHelper.findInstallRoot();
        File instanceRoot = ASMainHelper.findInstanceRoot(installRoot, args);
        Properties ctx = ASMainHelper.buildStartupContext(platform, installRoot, instanceRoot, args);
        ASMainHelper.setSystemProperties(ctx);

        PlatformMain delegate=ASMainHelper.getMain(platform);
        if (delegate!=null) {
            logger.info("Launching GlassFish on " + platform + " platform");
            logger.fine("Startup Context: " + ctx);
            try {
                delegate.setLogger(logger);
                delegate.start(ctx);
            } catch(Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else {
            logger.severe("Cannot launch GlassFish on the unknown " + platform + " platform");
        }
    }

}
