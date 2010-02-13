/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.Configuration;

import java.io.StringReader;
import java.io.IOException;
import java.io.File;
import java.util.Properties;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.logging.Logger;

import com.sun.enterprise.module.bootstrap.ModuleStartup;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 *
 * This is the bundle activator responsible for starting GlassFish server process.
 * It also starts any bundles that's necessary for glassfish to function (e.g., file install, config admin)
 */

public class GlassFishActivator implements BundleActivator {

    private ServiceTracker caTracker;

    private volatile Configuration config;

    private BundleContext bundleContext;

    /**
     * PID of the managed service registered by GlassFish activator.
     */
    public static final String gfpid = "com.sun.enterprise.glassfish.bootstrap.GlassFish";

    /**
     * PID of the managed service registered by HK2
     */
    private static final String hk2pid = "org.jvnet.hk2.osgiadapter.StartupContextService";

    public void start(final BundleContext context) throws Exception {
        this.bundleContext = context;
        startBundles();
        // get the startup context from the System properties
        String lineformat = context.getProperty(Constants.ARGS_PROP);
        if (lineformat != null) {
            Properties args = new Properties();
            StringReader reader = new StringReader(lineformat);
            args.load(reader);
            caTracker = new CATracker(args);
            caTracker.open();
        } else {
            Properties p = new Properties();
            p.setProperty(org.osgi.framework.Constants.SERVICE_PID, gfpid);
            context.registerService(ManagedService.class.getName(), new ManagedService(){
                public void updated(Dictionary dictionary) throws ConfigurationException {
                    try {
                        if (dictionary != null) {
                            Properties args = dict2Properties(dictionary);
                            caTracker = new CATracker(args);
                            caTracker.open();
                        } else {
                            deleteConfig();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                    }
                }
            }, p);
        }
    }

    private Properties dict2Properties(Dictionary dictionary) {
        Properties args = new Properties();
        Enumeration e = dictionary.keys();
        while (e.hasMoreElements()) {
            String k = e.nextElement().toString();
            String v = dictionary.get(k).toString();
            args.put(k, v);
        }
        return args;
    }

    public void stop(BundleContext context) throws Exception {
        if (config != null) config.delete();
        if (caTracker != null) caTracker.close();
    }

    private void setEnv(Properties properties) {
        ASMainHelper helper = new ASMainHelper(Logger.getAnonymousLogger());
        File installRoot = new File(properties.getProperty(Constants.INSTALL_ROOT_PROP_NAME));
        File instanceRoot = new File(properties.getProperty(Constants.INSTANCE_ROOT_PROP_NAME));
        System.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
        System.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
        final Properties asenv = helper.parseAsEnv(installRoot);
        for (String s : asenv.stringPropertyNames()) {
            System.setProperty(s, asenv.getProperty(s));
        }
        System.setProperty(Constants.INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());
        System.setProperty(Constants.INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());
    }

    private void startBundles() {
        // 1. Start cofigadmin as we depend on its service.
        startConfigAdmin();

        // 2.  Start osgi-adapter (this is a hk2 bootstrap module)
        startBundle("com.sun.enterprise.osgi-adapter");

        // The rest of the bundles are started only after GlassFish kernel has successfully started.
        // This ensures that all the configuration data set as system properties in domain.xml is set
        // in the system for these bundles to use. e.g., shell port or file install watched directory
        // We track glassfish using service tracker. If you look at HK2Main, it registers
        // ModuleStartup in OSGi service registry after launching it, so we track that service.
        ServiceTracker gfTracker = new ServiceTracker(bundleContext, ModuleStartup.class.getName(), null) {
                @Override
                public Object addingService(ServiceReference reference) {
                    startBundle("org.apache.felix.shell");
                    startBundle("org.apache.felix.org.apache.felix.shell.remote");
                    startBundle("org.apache.felix.fileinstall");
                    close(); // we don't have to track this any more
                    return super.addingService(reference);
                }
        };
        gfTracker.open();
    }

    /**
     * Start config admin if not already started
     */
    private void startConfigAdmin() {
        ServiceReference sr = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (sr == null) {
            startBundle("org.apache.felix.configadmin");
        } else {
            bundleContext.ungetService(sr);
        }
    }

    /**
     * Start Felix shell if not already started
     */
    private void startBundle(String bsn) {
        Bundle b = findBundle(bsn);
        if (b != null) {
            try {
                b.start(Bundle.START_TRANSIENT);
            } catch (BundleException e) {
                System.out.println("Failed to start: " + bsn);
                e.printStackTrace();
            }
        } else {
            System.out.println("Can't locate bundle: " + bsn);
        }
    }

    private Bundle findBundle(String bsn) {
        for (Bundle b : bundleContext.getBundles()) {
            if (bsn.equals(b.getSymbolicName())) return b;
        }
        return null;
    }

    private class CATracker extends ServiceTracker {
        Properties properties;
        public CATracker(Properties properties) {
            super(bundleContext, ConfigurationAdmin.class.getName(), null);
            this.properties = properties;
        }

        @Override
        public Object addingService(ServiceReference reference) {
            try {
                final ConfigurationAdmin ca = (ConfigurationAdmin) context.getService(reference);
                assert(ca != null);
                updateConfig(properties, ca);
            } catch (Exception ioe) {
                throw new RuntimeException(ioe);
            }
            return super.addingService(reference);
        }
    }

    private void updateConfig(Properties properties, ConfigurationAdmin ca) throws Exception {
        config = ca.getConfiguration(hk2pid, null);
        config.update(properties);
        setEnv(properties);
    }

    private void deleteConfig() throws IOException {
        if (config != null) {
            config.delete();
            config = null;
        }
    }

}
