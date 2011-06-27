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

import org.glassfish.embeddable.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.net.URI;
import java.util.Properties;

/**
 *
 * This activator is used when glassfish.jar is installed and started
 * in an existing OSGi runtime. It expects install root and instance root
 * to be set via framework context properties called com.sun.aas.installRoot and com.sun.aas.instanceRoot
 * respectively. The former one refers to the directory where glassfish is installed.
 * (e.g., /tmp/glassfish3/glassfish)
 * The latter one refers to the domain directory - this is a directory containing
 * configuration information and deployed applications, etc.
 * If instance root is not set, it defaults to $installRoot/domains/domain1/.
 *
 * @see #prepareStartupContext(org.osgi.framework.BundleContext)
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class GlassFishMainActivator implements BundleActivator {
    private GlassFishRuntime gfr;
    private GlassFish gf;

    public void start(BundleContext context) throws Exception {
        Properties properties = prepareStartupContext(context);

        // Should we do the following in a separate thread?
        gfr = GlassFishRuntime.bootstrap(new BootstrapProperties(properties), getClass().getClassLoader());
        gf = gfr.newGlassFish(new GlassFishProperties(properties));
        gf.start();
    }

    private Properties prepareStartupContext(BundleContext context) {
        Properties properties = new Properties();
        String installRoot = context.getProperty(Constants.INSTALL_ROOT_PROP_NAME);

        if (installRoot == null) {
            installRoot = guessInstallRoot(context);
            if (installRoot == null) {
                throw new RuntimeException("Property named " + Constants.INSTALL_ROOT_PROP_NAME + " is not set.");
            } else {
                System.out.println("Deduced install root as : " + installRoot + " from location of bundle. " +
                        "If this is not correct, set correct value in a property called " +
                       Constants.INSTALL_ROOT_PROP_NAME);
            }
        }
        if (!new File(installRoot).exists()) {
            throw new RuntimeException("No such directory: [" + installRoot + "]");
        }
        properties.setProperty(Constants.INSTALL_ROOT_PROP_NAME,
                installRoot);
        String instanceRoot = properties.getProperty(Constants.INSTANCE_ROOT_PROP_NAME);
        if (instanceRoot == null) {
            instanceRoot = new File(installRoot, "domains/domain1/").getAbsolutePath();
        }
        properties.setProperty(Constants.INSTANCE_ROOT_PROP_NAME,
                instanceRoot);

        // This property is understood by our corresponding builder.
        properties.setProperty(EmbeddedOSGiGlassFishRuntimeBuilder.BUILDER_NAME_PROPERTY, EmbeddedOSGiGlassFishRuntimeBuilder.class.getName());
        return properties;
    }

    /**
     * This method tries to guess install root based on location of the bundle. Please note, because location of a
     * bundle is free form string, this method can come to very wrong conclusion if user wants to fool us.
     *
     * @param context
     * @return
     */
    private String guessInstallRoot(BundleContext context) {
        String location = context.getBundle().getLocation();
        try {
            final URI uri = URI.create(location);
            File f = new File(uri);
            if (f.exists() && f.isFile() && f.getParentFile().getCanonicalPath().endsWith("modules") &&
                    f.getParentFile().getParentFile().getCanonicalPath().endsWith("glassfish")) {
                return f.getParentFile().getParentFile().getAbsolutePath();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void stop(BundleContext context) throws Exception {
        gf.stop();
    }
}
