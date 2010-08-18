/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.PostConstruct;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.internal.api.PostStartup;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.common.HTMLActionReporter;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Calendar;
import java.io.File;
import java.io.IOException;
import java.net.URI;

@Service
@Scoped(Singleton.class)
public class ApplicationConfigListener implements TransactionListener, 
    PostStartup, PostConstruct {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ApplicationConfigListener.class);

    private Logger logger;

    @Inject
    Transactions transactions;

    @Inject
    Domain domain;

    @Inject
    Applications applications;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    Deployment deployment;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    public void transactionCommited( final List<PropertyChangeEvent> changes) {
        for (PropertyChangeEvent event : changes) {
            if (event.getSource() instanceof Application || 
                event.getSource() instanceof ApplicationRef) {
                if (event.getPropertyName().equals("enabled")) {
                    String oldValue = (String)event.getOldValue();
                    String newValue = (String)event.getNewValue();
                    if (oldValue != null && newValue != null && 
                        !oldValue.equals(newValue)) {
                        // if this is a change event of the enable 
                        // attribute on application or application-ref element
                        handleAppEnableChange(event.getSource(), 
                            Boolean.valueOf(newValue));
                    }
                }
            }
        }
    }

    public void unprocessedTransactedEvents(
        List<UnprocessedChangeEvents> changes) {
    }

    public void postConstruct() {
        transactions.addTransactionsListener(this);
        logger = Logger.getLogger(ApplicationConfigListener.class.getName());
    }

    private void handleAppEnableChange(Object parent, boolean enabled) {
        String appName = null;
        if (parent instanceof Application) {
            appName = ((Application)parent).getName();
        } else if (parent instanceof ApplicationRef) {
            appName = ((ApplicationRef)parent).getRef();
        }
        if (enabled) {
            if (isCurrentInstanceMatchingTarget(parent)) {
                enableApplication(appName);
            }
        } else {
            if (isCurrentInstanceMatchingTarget(parent)) {
                disableApplication(appName);
            }
        }
    }

    private boolean isCurrentInstanceMatchingTarget(Object parent) {
        // DAS receive all the events, so we need to figure out 
        // whether we should take action on DAS depending on the event
        if (parent instanceof ApplicationRef) {
            Object grandparent = ((ApplicationRef)parent).getParent();
            if (grandparent instanceof Server) {
                Server gpServer = (Server)grandparent;      
                if (server.isDas() && !gpServer.isDas()) {
                    return false; 
                }
            } else if (grandparent instanceof Cluster) {
                if (server.isDas()) {
                    return false; 
                }
            }
        }
        return true;
    }

    private void enableApplication(String appName) {        
        Application app = applications.getApplication(appName);
        ApplicationRef appRef = domain.getApplicationRefInServer(server.getName(), appName);
        // if the application does not exist or application is not referenced
        // by the current server instance, do not load
        if (app == null || appRef == null) {
            return;
        }

        // if the application is not in enable state, do not load 
        if (!deployment.isAppEnabled(app)) {
            return;
        }

        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo == null) {
            return;
        }

        // if the application is already loaded, do not load again
        if (appInfo.isLoaded()) {
            return;
        }

        long operationStartTime = 
            Calendar.getInstance().getTimeInMillis();

        try {
            ActionReport report = new HTMLActionReporter();

            deployment.enable(server.getName(), app, appRef, report, logger);

            if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
                logger.log(Level.INFO, "loading.application.time", new Object[] { appName, (Calendar.getInstance().getTimeInMillis() - operationStartTime)});
            } else if (report.getActionExitCode().equals(ActionReport.ExitCode.WARNING)){
                logger.warning(report.getMessage());
            } else if (report.getActionExitCode().equals(ActionReport.ExitCode.FAILURE)) {
                throw new Exception(report.getMessage());
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error during enabling: ", e);
            RuntimeException re = new RuntimeException(e.getMessage());
            re.initCause(e); 
            throw re;
        }
    }

    private void disableApplication(String appName) {
        Application app = applications.getApplication(appName);
        ApplicationRef appRef = domain.getApplicationRefInServer(server.getName(), appName);
        // if the application does not exist or application is not referenced
        // by the current server instance, do not unload
        if (app == null || appRef == null) {
            return;
        }

        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo == null) {
            return;
        }

        // if the application is not loaded, do not unload
        if (!appInfo.isLoaded()) {
            return;
        }

        try {
            ActionReport report = new HTMLActionReporter();
            UndeployCommandParameters commandParams =
                new UndeployCommandParameters();
            commandParams.name = appName;
            commandParams.target = server.getName();
            commandParams.origin = UndeployCommandParameters.Origin.unload;
            deployment.disable(commandParams, app, appInfo, report, logger);

            if (report.getActionExitCode().equals(ActionReport.ExitCode.FAILURE)) {
                throw new Exception(report.getMessage());
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error during disabling: ", e);
            RuntimeException re = new RuntimeException(e.getMessage());
            re.initCause(e); 
            throw re;
        }
    }
}
