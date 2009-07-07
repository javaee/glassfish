/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.deployment.Application;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.appclient.server.core.jws.AppClientHTTPAdapter;
import org.glassfish.appclient.server.core.jws.JavaWebStartState;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.appclient.server.core.jws.servedcontent.FixedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Represents an app client module, either stand-alone or nested inside
 * an EAR, loaded on the server.
 * <p>
 * The primary purpose of this class is to implement Java Web Start support for
 * launches of this app client.  Other than in that sense, app clients do not
 * run in the server.  To support a client for Java Web Start launches, this
 * class figures out what static content (JAR files) and dynamic content (JNLP
 * documents) are needed by the client.  It then generates the required
 * dynamic content templates and submits them and the static content to a
 * Grizzly adapter which actually serves the data in response to requests.
 *
 * @author tjquinn
 */
public class AppClientServerApplication implements 
        ApplicationContainer<ApplicationClientDescriptor>, ConfigListener {

    private final DeploymentContext dc;
    private final AppClientDeployer deployer;

    private final Logger logger;
    
    private final AppClientDeployerHelper helper;

    /**
     * records if the app client is eligible for Java Web Start support, as
     * defined in the developer-provided sun-application-client.xml descriptor
     */
    private final boolean isJWSEligible;
    
    /**
     * records if the containing app is set to enable Java Web
     * Start access (in the domain.xml config for the application and the
     * module) - could be updated from a separate
     * thread if the administrator changes the java-web-start-enabled setting
     */
    private volatile boolean isJWSEnabledAtApp = true;
    private volatile boolean isJWSEnabledAtModule = true;

    private final JavaWebStartState jwsState = new JavaWebStartState();

    private final static String JAVA_WEB_START_ENABLED_PROPERTY_NAME = "" +
            "java-web-start-enabled";
    
    private final RequestDispatcher requestDispatcher;

    private final ApplicationClientDescriptor acDesc;
    private final Application appDesc;
    private final com.sun.enterprise.config.serverbeans.Applications applications;

    private AppClientHTTPAdapter clientAdapter;

    AppClientServerApplication(final DeploymentContext dc, 
            final AppClientDeployer deployer,
            final AppClientDeployerHelper helper,
            final RequestDispatcher requestDispatcher,
            final Applications applications,
            final Logger logger) {
        this.dc = dc;
        this.deployer = deployer;
        this.helper = helper;
        this.logger = logger;
        this.requestDispatcher = requestDispatcher;

        acDesc = helper.appClientDesc();

        isJWSEligible = acDesc.getJavaWebStartAccessDescriptor().isEligible();
        isJWSEnabledAtApp = isJWSEnabled(dc.getAppProps());
        isJWSEnabledAtModule = isJWSEnabled(dc.getModuleProps());
        appDesc = acDesc.getApplication();

        this.applications = applications;
    }
        

    public ApplicationClientDescriptor getDescriptor() {
        return acDesc;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        return start();
    }


    boolean start() {
        /*
         * The developer might have disabled Java Web Start support in the
         * sun-application-client.xml or in the domain's configuration,
         * so check those before starting JWS services.
         */
        if (isJWSRunnable()) {
            jwsState.transition(JavaWebStartState.Action.START, new Runnable() {
                public void run() {
                    try {
                        startJWSServices();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        return stop();
    }

    boolean stop() {
        jwsState.transition(JavaWebStartState.Action.STOP, new Runnable() {
            public void run() {
                try {
                    stopJWSServices();
                } catch (EndpointRegistrationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return true;
    }

    public boolean suspend() {
        jwsState.transition(JavaWebStartState.Action.SUSPEND, new Runnable() {
            public void run() {
                suspendJWSServices();
            }
        });
        return true;
    }

    public boolean resume() throws Exception {
        if (isJWSRunnable()) {
            jwsState.transition(JavaWebStartState.Action.RESUME, new Runnable() {
                public void run() {
                    resumeJWSServices();
                }
            });
        }
        return true;
    }

    public ClassLoader getClassLoader() {
        /*
         * This cannot be null or it prevents the framework from invoking unload
         * on the deployer for this app.
         */
        return new URLClassLoader(new URL[0]);
    }

    private void startJWSServices() throws EndpointRegistrationException, IOException {
        if (clientAdapter == null) {
            clientAdapter = initAdapter();
        }
        requestDispatcher.registerEndpoint(clientContextRoot(), clientAdapter, this);
        logger.log(Level.INFO, "enterprise.deployment.appclient.jws.started",
                new Object[] {moduleExpression(), clientContextRoot()});
    }

    private void stopJWSServices() throws EndpointRegistrationException {
        requestDispatcher.unregisterEndpoint(clientContextRoot());
        logger.log(Level.INFO, "enterprise.deployment.appclient.jws.stopped",
                moduleExpression());
    }

    private void suspendJWSServices() {
        if (clientAdapter != null) {
            clientAdapter.suspend();
        }
    }

    private void resumeJWSServices() {
        if (clientAdapter != null) {
            clientAdapter.resume();
        }
    }

    private AppClientHTTPAdapter initAdapter() throws IOException {
        final Map<String,StaticContent> staticContent = new HashMap<String,StaticContent>();
        /*
         * The following code is a temporary test hack which provides a
         * single static file and a single dynamic content instance
         */
        staticContent.put(clientContextRoot() + "/VirtualFile", 
                new FixedContent(new File("/Users/Tim/asgroup/sampleOutput.html")));

        final Map<String,AppClientHTTPAdapter.DynamicContent> dynamicContent =
                new HashMap<String,AppClientHTTPAdapter.DynamicContent>();
        dynamicContent.put(clientContextRoot() + "/VirtualDyn.html", 
                new AppClientHTTPAdapter.DynamicContent(
                    "<html><body>This is running from app ${my.app} which was loaded at ${when}!</body></html>",
                    new Date()));

        Properties tokens = new Properties();
        tokens.setProperty("my.app", clientContextRoot());
        tokens.setProperty("when", new Date().toString());

        return new AppClientHTTPAdapter(staticContent,
                dynamicContent, tokens);
    }

    /**
     * Returns if this client is enabled for Java Web Start access.
     * <p>
     * The administrator can set the java-web-start-enabled property at
     * either the application level or the module level or both.  For this
     * client to be enabled, any such specified property must be set to true.
     * The default is true.
     */
    private boolean isJWSEnabled(final Properties props) {
        boolean result = true;
        final String propsSetting = props.getProperty(JAVA_WEB_START_ENABLED_PROPERTY_NAME);
        if (propsSetting != null) {
            result &= Boolean.parseBoolean(propsSetting);
        }
        return result;
    }
    
    private boolean isJWSEnabled() {
        return isJWSEnabledAtApp && isJWSEnabledAtModule;
    }

    private boolean isJWSRunnable() {
        if ( ! isJWSEligible) {
            logger.log(Level.INFO, "enterprise.deployment.appclient.jws.noStart.ineligible",
                    moduleExpression());
        }

        if ( ! isJWSEnabled()) {
            logger.log(Level.INFO, "enterprise.deployment.appclient.jws.noStart.disabled",
                    moduleExpression());
        }
        return isJWSEligible && isJWSEnabled();
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        /* Record any events we tried to process but could not. */
        List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<UnprocessedChangeEvent>();

        for (PropertyChangeEvent event : events) {
            try {
                processChangeEventIfInteresting(event);
            } catch (Exception e) {
                UnprocessedChangeEvent uce =
                        new UnprocessedChangeEvent(event, e.getLocalizedMessage());
                unprocessedEvents.add(uce);
            }
        }

        return (unprocessedEvents.size() > 0) ? new UnprocessedChangeEvents(unprocessedEvents) : null;
    }

    private void processChangeEventIfInteresting(final PropertyChangeEvent event) throws EndpointRegistrationException {
        /*
         * If the source is of type Application or Module and the newValue is of type
         * Property then this could be a change we're interested in.
         */
        final boolean isSourceApp = event.getSource() instanceof
                com.sun.enterprise.config.serverbeans.Application;
        final boolean isSourceModule = event.getSource() instanceof
                com.sun.enterprise.config.serverbeans.Module;

        if (     (! isSourceApp && ! isSourceModule)
              || ! (event.getNewValue() instanceof Property)) {
            return;
        }

        /*
         * Make sure the property name is java-web-start-enabled.
         */
        Property newPropertySetting = (Property) event.getNewValue();
        if ( ! newPropertySetting.getName().equals(JAVA_WEB_START_ENABLED_PROPERTY_NAME)) {
            return;
        }

        String eventSourceName;
        String thisAppOrModuleName;
        if (isSourceApp) {
            eventSourceName = ((com.sun.enterprise.config.serverbeans.Application) event.getSource()).getName();
            thisAppOrModuleName = appDesc.getRegistrationName();
        } else {
            eventSourceName = ((com.sun.enterprise.config.serverbeans.Module) event.getSource()).getName();
            thisAppOrModuleName = acDesc.getModuleName();
        }

        if ( ! thisAppOrModuleName.equals(eventSourceName)) {
            return;
        }

        /*
         * At this point we know that the event applies to this app client,
         * so return a Boolean carrying the newly-assigned value.
         */
        final Boolean newEnabledValue = Boolean.valueOf(newPropertySetting.getValue());
        final Property oldPropertySetting = (Property) event.getOldValue();
        final String oldPropertyValue = (oldPropertySetting != null)
                ? oldPropertySetting.getValue()
                : null;
        final Boolean oldEnabledValue = (oldPropertyValue == null
                ? Boolean.TRUE
                : Boolean.valueOf(oldPropertyValue));

        /*
         * Record the new value of the relevant enabled setting.
         */
        if (isSourceApp) {
            isJWSEnabledAtApp = newEnabledValue;
                } else {
            isJWSEnabledAtModule = newEnabledValue;
        }
        
        /*
         * Now act on the change of state.
         */
        if ( ! newEnabledValue.equals(oldEnabledValue)) {
            if (newEnabledValue) {
                start();
            } else {
                stop();
            }
        }
    }

    /**
     * Returns the client's contextRoot as specified by the developer or,
     * otherwise, in the format appName (for a stand-alone
     * client) and appName/moduleName (for a client in an EAR).  moduleName is
     * the URI to the client JAR.
     * @return
     */
    private String clientContextRoot() {
        String contextRoot;
        final String contextRootInDesc = developerSpecifiedContextRoot();
                
        if (contextRootInDesc != null && ! contextRootInDesc.equals("")) {
            contextRoot = contextRootInDesc;
        } else {
            contextRoot = moduleExpression();
        }
        return "/" + contextRoot;
    }

    private String moduleExpression() {
        String moduleExpression;
        if (appDesc.isVirtual()) {
            moduleExpression = appDesc.getRegistrationName();
        } else {
            moduleExpression = appDesc.getRegistrationName() + "/" + acDesc.getModuleName();
        }
        return moduleExpression;
    }

    private String developerSpecifiedContextRoot() {
        return acDesc.getJavaWebStartAccessDescriptor().getContextRoot();
    }
}
