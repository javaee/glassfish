/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.*;
import org.glassfish.api.admin.CommandRunner;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyVetoException;

/**
 * Very simple ModuleStartup that basically force an immediate shutdown.
 * When start() is invoked, the upgrade of the domain.xml has already been
 * performed.
 * 
 * @author Jerome Dochez
 */
@Service(name="upgrade")
public class UpgradeStartup implements ModuleStartup {

    @Inject
    CommandRunner runner;

    @Inject
    AppServerStartup appservStartup;

    @Inject
    Applications applications;

    // we need to refine, a better logger should be used.
    @Inject
    Logger logger;


    public void setStartupContext(StartupContext startupContext) {
        appservStartup.setStartupContext(startupContext);
    }

    // do nothing, just return, at the time the upgrade service has
    // run correctly.
    public void start() {

        // disable all applications.
        List<Application> enabledApps = new ArrayList<Application>();
        for (Application app : applications.getApplications()) {
            System.out.println("app " + app.getName() + " is " + app.getEnabled() + " resulting in " + Boolean.parseBoolean(app.getEnabled()));
            if (Boolean.parseBoolean(app.getEnabled())) {
                logger.log(Level.INFO, "Disabling application " + app.getName());
                enabledApps.add(app);
            }
        }
        if (enabledApps.size()>0) {
            try  {
                ConfigSupport.apply(new ConfigCode() {
                    public Object run(ConfigBeanProxy... configBeanProxies) throws PropertyVetoException, TransactionFailure {
                        for (ConfigBeanProxy proxy : configBeanProxies) {
                            Application app = (Application) proxy;
                            app.setEnabled(Boolean.FALSE.toString());
                        }
                        return null;
                    }
                }, enabledApps.toArray(new Application[enabledApps.size()]));
            } catch(TransactionFailure tf) {
                logger.log(Level.SEVERE, "Exception while disabling applications", tf);
                return;
            }
        }

        // start the application server
        appservStartup.start();

        // redeploy all existing applications
        for (Application app : applications.getApplications()) {
            logger.log(Level.INFO, "Redeploy application " + app.getName() + " located at " + app.getLocation());    
        }

        // re-enables all applications.
        if (enabledApps.size()>0) {
            try  {
                ConfigSupport.apply(new ConfigCode() {
                    public Object run(ConfigBeanProxy... configBeanProxies) throws PropertyVetoException, TransactionFailure {
                        for (ConfigBeanProxy proxy : configBeanProxies) {
                            Application app = (Application) proxy;
                            logger.log(Level.INFO, "Enabling application " + app.getName());
                            app.setEnabled(Boolean.TRUE.toString());
                        }
                        return null;
                    }
                }, enabledApps.toArray(new Application[enabledApps.size()]));
            } catch(TransactionFailure tf) {
                logger.log(Level.SEVERE, "Exception while disabling applications", tf);
                return;
            }
        }

        // stop-the server.
        Logger.getAnonymousLogger().info("Exiting after upgrade");
        try {
            Thread.sleep(3000);
            if (runner!=null) {
                runner.doCommand("stop-domain", new Properties(), new PlainTextActionReporter());
                return;
            }

        } catch (InterruptedException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Exception while attempting to shutdown after upgrade", e);
        }

    }

    public void stop() {
        appservStartup.stop();
    }
}
