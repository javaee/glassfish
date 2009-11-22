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
 *
 */

package com.sun.appserv.connectors.internal.api;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.api.ConnectorClassFinder;
import org.jvnet.hk2.config.types.Property;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.runtime.connector.SunConnector;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.logging.LogDomains;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class AppSpecificConnectorClassLoaderUtil {

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    Habitat habitat;

    private Logger _logger = LogDomains.getLogger(ConnectorRuntime.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public void detectReferredRARs(String appName) {
        ApplicationInfo appInfo = appRegistry.get(appName);

        //call to detectReferredRAs can be called only when appInfo is available
        if (appInfo == null) {
            throw new IllegalStateException("ApplicationInfo is not available for application [ " + appName + " ]");
        }
        Application app = appInfo.getMetaData(Application.class);

        if(!appInfo.isJavaEEApp()){
            if(_logger.isLoggable(Level.FINEST)){
                _logger.finest("Application ["+appName+"] is not a Java EE application, skipping " +
                        "resource-adapter references detection");
            }
            return;
        }

        // Iterate through all bundle descriptors, ejb-descriptors, managed-bean descriptors
        // for references to resource-adapters
        //
        // References can be via :
        // resource-ref
        // resource-env-ref
        // ra-mid
        //
        // Resource definition can be found in :
        // domain.xml
        // sun-ra.xml
        // default connector resource

        //handle application.xml bundle descriptor
        processDescriptorForRAReferences(app, app);

        Collection<BundleDescriptor> bundleDescriptors = app.getBundleDescriptors();

        //bundle descriptors
        for (BundleDescriptor bundleDesc : bundleDescriptors) {
            if (bundleDesc instanceof JndiNameEnvironment) {
                processDescriptorForRAReferences(app, bundleDesc);
            }
            // ejb descriptors
            if (bundleDesc instanceof EjbBundleDescriptor) {
                EjbBundleDescriptor ejbDesc = (EjbBundleDescriptor) bundleDesc;
                Set<EjbDescriptor> ejbDescriptors = ejbDesc.getEjbs();
                for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                    processDescriptorForRAReferences(app, ejbDescriptor);

                    if (ejbDescriptor instanceof EjbMessageBeanDescriptor) {
                        EjbMessageBeanDescriptor messageBeanDesc = (EjbMessageBeanDescriptor) ejbDescriptor;
                        String raMid = messageBeanDesc.getResourceAdapterMid();
                        //there seem to be applications that do not specify ra-mid
                        if (raMid != null) {
                            app.addResourceAdapter(raMid);
                        }
                    }
                }
                //ejb interceptors
                Set<EjbInterceptor> ejbInterceptors = ejbDesc.getInterceptors();
                for (EjbInterceptor ejbInterceptor : ejbInterceptors) {
                    processDescriptorForRAReferences(app, ejbInterceptor);
                }

            }
            // managed bean descriptors
            Set<ManagedBeanDescriptor> managedBeanDescriptors = bundleDesc.getManagedBeans();
            for (ManagedBeanDescriptor mbd : managedBeanDescriptors) {
                processDescriptorForRAReferences(app, mbd);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getRARsReferredByApplication(String appName) {
        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo != null) {
            Application app = appInfo.getMetaData(Application.class);
            if(appInfo.isJavaEEApp()){
                return app.getResourceAdapters();
            }
        }
        return new HashSet<String>();
    }

    private void processDescriptorForRAReferences(com.sun.enterprise.deployment.Application app,
                                                  Descriptor descriptor) {
        if (descriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment jndiEnv = (JndiNameEnvironment) descriptor;

            // resource-ref
            for (Object resourceRef : jndiEnv.getResourceReferenceDescriptors()) {
                ResourceReferenceDescriptor resRefDesc = (ResourceReferenceDescriptor) resourceRef;
                String jndiName = resRefDesc.getJndiName();
                //ignore refs where jndi-name is not available
                if(jndiName != null){
                    detectResourceInRA(app, jndiName);
                }
            }

            // resource-env-ref
            for (Object jmsDestRef : jndiEnv.getJmsDestinationReferenceDescriptors()) {
                JmsDestinationReferenceDescriptor jmsDestRefDesc = (JmsDestinationReferenceDescriptor) jmsDestRef;
                String jndiName = jmsDestRefDesc.getJndiName();
                //ignore refs where jndi-name is not available
                if(jndiName != null){
                    detectResourceInRA(app, jndiName);
                }
            }
        }
    }

    private void detectResourceInRA(Application app, String jndiName) {
        //domain.xml
        Resource res = getResources().getResourceByName(BindableResource.class, jndiName);
        //embedded ra's resources may not be created yet as they can be created only after .ear deploy
        //  (and .ear may refer to these resources in DD)
        if (res != null) {
            if (ConnectorResource.class.isAssignableFrom(res.getClass())) {
                String poolName = ((ConnectorResource) res).getPoolName();
                Resource pool = getResources().getResourceByName(ResourcePool.class, poolName);
                if (ConnectorConnectionPool.class.isAssignableFrom(pool.getClass())) {
                    String raName = ((ConnectorConnectionPool) pool).getResourceAdapterName();
                    app.addResourceAdapter(raName);
                }
            } else if (AdminObjectResource.class.isAssignableFrom(res.getClass())) {
                String raName = ((AdminObjectResource) res).getResAdapter();
                app.addResourceAdapter(raName);
            }
        } else {
            boolean found = false;
            //detect sun-ra.xml

            // find all the standalone connector modules
            List<com.sun.enterprise.config.serverbeans.Application> applications =
                    getApplications().getApplicationsWithSnifferType("connector", true);
            Iterator itr = applications.iterator();
            while (itr.hasNext()) {
                com.sun.enterprise.config.serverbeans.Application application =
                        (com.sun.enterprise.config.serverbeans.Application) itr.next();
                        String appName = application.getName();
                        ApplicationInfo appInfo = appRegistry.get(appName);
                        Application dolApp = appInfo.getMetaData(Application.class);
                        Collection<ConnectorDescriptor> rarDescriptors = dolApp.getRarDescriptors();
                        for (ConnectorDescriptor desc : rarDescriptors) {
                            SunConnector sunraDesc = desc.getSunDescriptor();
                            if (sunraDesc != null) {
                                String sunRAJndiName = (String) sunraDesc.getResourceAdapter().
                                        getValue(ResourceAdapter.JNDI_NAME);
                                if (jndiName.equals(sunRAJndiName)) {
                                    app.addResourceAdapter(desc.getName());
                                    found = true;
                                    break;
                                }
                            } else {
                                //check whether it is default resource in the connector
                                if (desc.getDefaultResourcesNames().contains(jndiName)) {
                                    app.addResourceAdapter(desc.getName());
                                    found = true;
                                    break;
                                }
                            }
                        }
            }

            if (!found) {
                DOLUtils.getDefaultLogger().log(Level.FINEST, "could not find resource by name : " + jndiName);
            }
        }
    }

    public Collection<ConnectorClassFinder> getSystemRARClassLoaders() {
        try {
            return getConnectorsClassLoaderUtil().getSystemRARClassLoaders();
        } catch (ConnectorRuntimeException cre) {
            throw new RuntimeException(cre.getMessage(), cre);
        }
    }

    public boolean useGlobalConnectorClassLoader() {
        boolean flag = false;
        ConnectorService connectorService = habitat.getComponent(ConnectorService.class);
        //it is possible that connector-service is not yet defined in domain.xml
        if(connectorService != null){
            String classLoadingPolicy = connectorService.getClassLoadingPolicy();
            if (classLoadingPolicy != null &&
                    classLoadingPolicy.equals(ConnectorConstants.CLASSLOADING_POLICY_GLOBAL_ACCESS)) {
                flag = true;
            }
        }
        return flag;
    }

    public Collection<String> getRequiredResourceAdapters(String appName) {
        List<String> requiredRars = new ArrayList<String>();
        if (appName != null) {
            ConnectorService connectorService = habitat.getComponent(ConnectorService.class);
            //it is possible that connector-service is not yet defined in domain.xml

            if (connectorService != null) {
                if (appName != null && appName.trim().length() > 0) {
                    Property property = connectorService.getProperty(appName.trim());
                    if (property != null) {
                        String requiredRarsString = property.getValue();
                        StringTokenizer tokenizer = new StringTokenizer(requiredRarsString, ",");
                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken().trim();
                            requiredRars.add(token);
                        }
                    }
                }
            }
        }
        return requiredRars;
    }

    private ConnectorsClassLoaderUtil getConnectorsClassLoaderUtil() {
        return habitat.getComponent(ConnectorsClassLoaderUtil.class);
    }

    private Resources getResources() {
        return habitat.getComponent(Resources.class);
    }

    private Applications getApplications() {
        return habitat.getComponent(Applications.class);
    }
}
