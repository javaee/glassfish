/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.simpleglassfishapi.spi.RuntimeBuilder;
import org.glassfish.simpleglassfishapi.*;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import java.util.Properties;

/**
 * This RuntimeBuilder can only handle GlassFish_Platform of following types:
 * {@link org.glassfish.simpleglassfishapi.Constants.Platform#Felix},
 * {@link org.glassfish.simpleglassfishapi.Constants.Platform#Equinox},
 * and {@link org.glassfish.simpleglassfishapi.Constants.Platform#Knopflerfish}.
 *
 * It can't handle {@link org.glassfish.simpleglassfishapi.Constants.Platform#GenericOSGi} platform,
 * because it reads framework configuration from a framework specific file when it calls
 * {@link ASMainHelper#buildStartupContext(java.util.Properties)}.
 *
 * This class is responsible for
 * a) setting up OSGi framework,
 * b) installing glassfish bundles,
 * c) starting the primordial GlassFish bundle (i.e., GlassFish Kernel bundle),
 * d) obtaining a reference to GlassFishRuntime OSGi service.
 * <p/>
 * Steps #b & #c are handled via {@link AutoProcessor}.
 * We specify our provisioning bundle details in the properties object that's used to boostrap
 * the system. AutoProcessor installs and starts such bundles, The provisioning bundle is also configured
 * via the same properties object.
 * <p/>
 * If caller does not pass in a properly populated properties object, we assume that we are
 * running against an existing installation of glassfish and set appropriate default values.
 * <p/>
 * <p/>
 * This class is registered as a provider of RuntimeBuilder using META-INF/services file.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class OSGiGlassFishRuntimeBuilder implements RuntimeBuilder {
    private Framework framework;

    /**
     * Default constructor needed for meta-inf/service lookup to work
     */
    public OSGiGlassFishRuntimeBuilder() {}

    public GlassFishRuntime build(BootstrapOptions bsOptions) throws GlassFishException {
        ASMainHelper.buildStartupContext(bsOptions.getAllOptions());
        final OSGiFrameworkLauncher fwLauncher = new OSGiFrameworkLauncher(bsOptions.getAllOptions());
        try {
            this.framework = fwLauncher.launchOSGiFrameWork();
            debug("Initialized " + framework);
            return fwLauncher.getService(GlassFishRuntime.class);
        } catch (Exception e) {
            throw new GlassFishException(e);
        }
    }

    public boolean handles(BootstrapOptions bsOptions) {
        /*
         * This builder can't handle GOSGi platform, because we read framework configuration from a framework
         * specific file in ASMainHelper.buildStartupContext(properties);
         */
        final String platformStr = bsOptions.getPlatformProperty();
        if (platformStr == null || platformStr.trim().isEmpty()) {
            return false;
        }
        BootstrapConstants.Platform platform =
                BootstrapConstants.Platform.valueOf(platformStr);
        switch (platform) {
            case Felix:
            case Equinox:
            case Knopflerfish:
                return true;
        }
        return false;
    }

    public void destroy() throws GlassFishException {
        try {
            framework.stop();
            framework.waitForStop(0);
        } catch (InterruptedException ex) {
            throw new GlassFishException(ex);
        } catch (BundleException ex) {
            throw new GlassFishException(ex);
        }
    }

    private static void debug(String s) {
        System.out.println("OSGiGlassFishRuntime: " + s);
    }

}
