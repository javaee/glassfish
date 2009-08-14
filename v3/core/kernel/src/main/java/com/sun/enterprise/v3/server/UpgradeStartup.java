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
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Server;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.*;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.ActionReport;

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

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    CommandRunner commandRunner;

    // we need to refine, a better logger should be used.
    @Inject
    Logger logger;


    public void setStartupContext(StartupContext startupContext) {
        appservStartup.setStartupContext(startupContext);
    }

    // do nothing, just return, at the time the upgrade service has
    // run correctly.
    public void start() {

        // all the applications will be directory redeployed
        // store all the archive deployed applications so we can reset
        // directory-deployed attribute properly after the redeployment 
        List<Application> archiveDeployedApps = new ArrayList<Application>();
        List<String> archiveDeployedAppNames = new ArrayList<String>();
        for (Application app : applications.getApplications()) {
            if (!Boolean.parseBoolean(app.getDirectoryDeployed())) {
                logger.log(Level.INFO, "Application " + app.getName() + " is archive deployed");
                archiveDeployedAppNames.add(app.getName());
                archiveDeployedApps.add(app);
            }
        }

        if (archiveDeployedApps.size()>0) {
            try  {
                ConfigSupport.apply(new ConfigCode() {
                    public Object run(ConfigBeanProxy... configBeanProxies) throws PropertyVetoException, TransactionFailure {
                        for (ConfigBeanProxy proxy : configBeanProxies) {
                            Application app = (Application) proxy;
                            app.setDirectoryDeployed(Boolean.TRUE.toString());
                        }
                        return null;
                    }
                }, archiveDeployedApps.toArray(new Application[archiveDeployedApps.size()]));
            } catch(TransactionFailure tf) {
                logger.log(Level.SEVERE, "Exception while disabling applications", tf);
                return;
            }
        }

        // start the application server
        appservStartup.start();

        // we need to disable all the applications before starting server 
        // so the applications will not get loaded before redeployment
        // store the list of previous enabled applications
        // so we can reset these applications back to enabled after 
        // redeployment
        List<ApplicationRef> enabledApps = new ArrayList<ApplicationRef>();
        List<String> enabledAppNames = new ArrayList<String>();
        for (ApplicationRef appRef : server.getApplicationRef()) {
            logger.log(Level.INFO, "app " + appRef.getRef() + " is " + appRef.getEnabled() + " resulting in " + Boolean.parseBoolean(appRef.getEnabled()));
            if (Boolean.parseBoolean(appRef.getEnabled())) {
                logger.log(Level.INFO, "Disabling application " + appRef.getRef());
                enabledApps.add(appRef);
                enabledAppNames.add(appRef.getRef());
            }
        }

        if (enabledApps.size()>0) {
            try  {
                ConfigSupport.apply(new ConfigCode() {
                    public Object run(ConfigBeanProxy... configBeanProxies) throws PropertyVetoException, TransactionFailure {
                        for (ConfigBeanProxy proxy : configBeanProxies) {
                            ApplicationRef appRef = (ApplicationRef) proxy;
                            appRef.setEnabled(Boolean.FALSE.toString());
                        }
                        return null;
                    }
                }, enabledApps.toArray(new ApplicationRef[enabledApps.size()]));
            } catch(TransactionFailure tf) {
                logger.log(Level.SEVERE, "Exception while disabling applications", tf);
                return;
            }
        }

        // start the application server
        appservStartup.start();

        // redeploy all existing applications in disable state
        for (Application app : applications.getApplications()) {
            // we don't need to redeploy lifecycle modules
            if (Boolean.valueOf(app.getDeployProperties().getProperty
                ("isLifecycle"))) {
                continue;
            }
            logger.log(Level.INFO, "Redeploy application " + app.getName() + " located at " + app.getLocation());    
            if (!redeployApp(app)) {
                return;
            };     
        }

        // re-enables all applications. 
        // we need to use the names in the enabledAppNames to find all 
        // the application refs that need to be re-enabled
        // as the previous application refs collected not longer exist
        // after redeployment
        if (enabledAppNames.size()>0) {
            for (ApplicationRef appRef : server.getApplicationRef()) {
                if (enabledAppNames.contains(appRef.getRef())) {
                    logger.log(Level.INFO, "Enabling application " + appRef.getRef());
                    try {
                        ConfigSupport.apply(new SingleConfigCode<ApplicationRef>() {
                            public Object run(ApplicationRef param) throws PropertyVetoException, TransactionFailure {
                                param.setEnabled(Boolean.TRUE.toString());
                                return null;
                            }
                        }, appRef);
                    } catch(TransactionFailure tf) {
                        logger.log(Level.SEVERE, "Exception while re-enabling application " + appRef.getRef(), tf);
                        return;
                    }
                }
            }
        }

        // reset the directory-deployed attribute for all archive 
        // deployed applications.
        if (archiveDeployedAppNames.size()>0) {
            for (Application app: applications.getApplications()) {
                if (archiveDeployedAppNames.contains(app.getName())) {
                    logger.log(Level.INFO, "Resetting the directory-deployed attribute for " + app.getName());
                    try  {
                        ConfigSupport.apply(new SingleConfigCode<Application>() {
                            public Object run(Application param) throws PropertyVetoException, TransactionFailure {                        
                                param.setDirectoryDeployed(Boolean.FALSE.toString());
                                return null;
                            }
                        }, app);
                    } catch(TransactionFailure tf) {
                        logger.log(Level.SEVERE, "Exception while resetting directory-deployed attribute for archive deployed application " + app.getName(), tf);
                        return;
                    }
                }
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

    private boolean redeployApp(Application app) {
        ApplicationRef ref = null;
        for (ApplicationRef appRef : server.getApplicationRef()) {
            if (appRef.getRef().equals(app.getName())) {
                ref = appRef;
                break;
            }
        }

        // populate the params and properties from application element first
        DeployCommandParameters deployParams = app.getDeployParameters(ref);
        deployParams.properties = app.getDeployProperties();

        // now override the ones needed for the upgrade
        deployParams.force = true;
        deployParams.dropandcreatetables = false;
        deployParams.enabled = false;

        ActionReport report = new PlainTextActionReporter();

        commandRunner.doCommand("deploy", deployParams, report, null, null);

        if (report.getActionExitCode().equals(ActionReport.ExitCode.FAILURE)) {
            logger.log(Level.SEVERE, "Redeployment of application " + app.getName() + " failed: " + report.getMessage() + " Please reploy " + app.getName() + " manually.", report.getFailureCause());
            return false;
        }
        return true;
    }
}
