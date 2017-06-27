/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.types.MessageDestinationReferencer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceConstants;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Krishna Deepak on 6/9/17.
 */
@Service
public class ResourceValidator implements EventListener, ResourceValidatorVisitor {

    public static final Logger deplLogger = com.sun.enterprise.deployment.util.DOLUtils.deplLogger;

    @LogMessageInfo(
            message = "JNDI lookup failed for the resource with jndi name: {0}",
            level = "SEVERE",
            cause = "JNDI lookup for the specified resource failed.",
            action = "Create the necessary object before deploying the application.",
            comment = "For the method validateJNDIRefs of com.sun.enterprise.deployment.util.ResourceValidator."
    )
    private static final String RESOURCE_REF_JNDI_LOOKUP_FAILED = "AS-DEPLOYMENT-00026";

    String target;

    DeploymentContext dc;

    Application application;

    @Inject
    private Events events;

    @Inject
    Domain domain;

    JNDINamespace myNamespace;
    
    public void postConstruct() {
        events.register(this);
    }

    @Override
    public void event(Event event) {
        if (event.is(Deployment.AFTER_APPLICATION_CLASSLOADER_CREATION)) {
            dc = (DeploymentContext) event.hook();
            application = dc.getModuleMetaData(Application.class);
            DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
            target = commandParams.target;
            if (application == null)
                return;
            parseResources();
            processResources();
        }
    }

    /**
     * Parse all the resources and store them in a namespace.
     */
    private void parseResources() {
        myNamespace = new JNDINamespace();
        parseResources(application);
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.warType())) {
            parseResources((JndiNameEnvironment) bd);
        }
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.carType())) {
            parseResources((JndiNameEnvironment) bd);
        }
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.ejbType())) {
            parseResources((JndiNameEnvironment) bd);
            EjbBundleDescriptor ebd = (EjbBundleDescriptor) bd;
            for (EjbDescriptor ejb : ebd.getEjbs())
                parseResources(ejb);
        }

        String appName = DOLUtils.getApplicationName(application);
        Map<String, List> resourcesList =
                (Map<String, List>) dc.getTransientAppMetadata().get(ResourceConstants.APP_SCOPED_RESOURCES_JNDI_NAMES);
        myNamespace.storeAppScopedResources(resourcesList, appName);
    }

    private void parseResources(JndiNameEnvironment env) {
        for (Iterator it = env.getResourceReferenceDescriptors().iterator(); it.hasNext(); ) {
            parseResources((ResourceReferenceDescriptor) it.next(), env);
        }

        for (Iterator it = env.getResourceEnvReferenceDescriptors().iterator(); it.hasNext(); ) {
            parseResources((ResourceEnvReferenceDescriptor) it.next(), env);
        }

        for (Iterator it = env.getMessageDestinationReferenceDescriptors().iterator(); it.hasNext(); ) {
            parseResources((MessageDestinationReferenceDescriptor) it.next(), env);
        }

        for (Iterator it = env.getEnvironmentProperties().iterator(); it.hasNext(); ) {
            parseResources((EnvironmentProperty) it.next(), env);
        }

        for (Iterator it = env.getAllResourcesDescriptors().iterator(); it.hasNext(); ) {
            parseResources((ResourceDescriptor) it.next(), env);
        }

        for (Iterator it = env.getEntityManagerReferenceDescriptors().iterator(); it.hasNext(); ) {
            parseResources((EntityManagerReferenceDescriptor) it.next(), env);
        }

        for (Iterator it = env.getEntityManagerFactoryReferenceDescriptors().iterator(); it.hasNext(); ) {
            parseResources((EntityManagerFactoryReferenceDescriptor) it.next(), env);
        }

        for (Iterator it = env.getEjbReferenceDescriptors().iterator(); it.hasNext(); ) {
            parseResources((EjbReferenceDescriptor) it.next(), env);
        }

        for (Iterator it = env.getServiceReferenceDescriptors().iterator(); it.hasNext(); ) {
            parseResources((ServiceReferenceDescriptor) it.next(), env);
        }
    }

    private void parseResources(ResourceReferenceDescriptor resRef, JndiNameEnvironment env) {
        resRef.checkType();
        String name = getLogicalJNDIName(resRef.getName(), env);
        myNamespace.store(name, env);
    }

    private void parseResources(ResourceEnvReferenceDescriptor resEnvRef, JndiNameEnvironment env) {
        resEnvRef.checkType();
        String name = getLogicalJNDIName(resEnvRef.getName(), env);
        myNamespace.store(name, env);
    }

    private void parseResources(MessageDestinationReferenceDescriptor msgDestRef, JndiNameEnvironment env) {
        String name = getLogicalJNDIName(msgDestRef.getName(), env);
        myNamespace.store(name, env);
    }

    private void parseResources(EnvironmentProperty envProp, JndiNameEnvironment env) {
        String name = getLogicalJNDIName(envProp.getName(), env);
        myNamespace.store(name, env);
    }

    /**
     * TODO: App client doesn't support some resource descriptors. Might need to fail deployment in such cases.
     */
    private void parseResources(ResourceDescriptor resourceDescriptor, JndiNameEnvironment env) {
        String name = getLogicalJNDIName(resourceDescriptor.getName(), env);
        myNamespace.store(name, env);
    }

    private void parseResources(EntityManagerReferenceDescriptor emRef, JndiNameEnvironment env) {
        String name = getLogicalJNDIName(emRef.getName(), env);
        myNamespace.store(name, env);
    }

    private void parseResources(EntityManagerFactoryReferenceDescriptor entMgrFacRef, JndiNameEnvironment env) {
        String name = getLogicalJNDIName(entMgrFacRef.getName(), env);
        myNamespace.store(name, env);
    }

    private void parseResources(EjbReferenceDescriptor ejbRef, JndiNameEnvironment env) {
        String name = getLogicalJNDIName(ejbRef.getName(), env);
        myNamespace.store(name, env);
    }

    private void parseResources(ServiceReferenceDescriptor serviceRef, JndiNameEnvironment env) {
        String name = getLogicalJNDIName(serviceRef.getName(), env);
        myNamespace.store(name, env);
    }

    /**
     * @param rawName to be converted
     * @param env
     * @return The logical JNDI name which has a java: prefix
     */
    private String getLogicalJNDIName(String rawName, JndiNameEnvironment env) {
        String logicalJndiName = rawNameToLogicalJndiName(rawName);
        boolean treatComponentAsModule = DOLUtils.getTreatComponentAsModule(env);
        if (treatComponentAsModule && logicalJndiName.startsWith(ResourceConstants.JAVA_COMP_SCOPE_PREFIX)) {
            logicalJndiName = logicalCompJndiNameToModule(logicalJndiName);
        }
        return logicalJndiName;
    }

    /**
     * Convert name from java:comp/xxx to java:module/xxx.
     */
    private String logicalCompJndiNameToModule(String logicalCompName) {
        String tail = logicalCompName.substring(ResourceConstants.JAVA_COMP_SCOPE_PREFIX.length());
        return ResourceConstants.JAVA_MODULE_SCOPE_PREFIX + tail;
    }

    /**
     * Attach default prefix - java:comp/env/.
     */
    private String rawNameToLogicalJndiName(String rawName) {
        return (rawName.startsWith(ResourceConstants.JAVA_SCOPE_PREFIX)) ?
                rawName : ResourceConstants.JAVA_COMP_ENV_SCOPE_PREFIX + rawName;
    }

    private void processResources() {
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.warType())) {
            accept(bd);
        }
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.carType())) {
            accept(bd);
        }
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.ejbType())) {
            accept(bd);
            EjbBundleDescriptor ebd = (EjbBundleDescriptor) bd;
            for (EjbDescriptor ejb : ebd.getEjbs())
                accept(ejb);
        }
        accept(application);
    }

    private void accept(EjbDescriptor ejb) {
        // TODO: MDB: priority order for destination?
        if (ejb.getType().equals(EjbMessageBeanDescriptor.TYPE)) {
            EjbMessageBeanDescriptor descriptor = (EjbMessageBeanDescriptor) ejb;
            String jndiName = descriptor.getJndiName();
            if (jndiName == null || "".equals(jndiName)) {
                MessageDestinationDescriptor destDescriptor = descriptor.getMessageDestination();
                if (destDescriptor != null)
                    jndiName = destDescriptor.getJndiName();
            }
            if (jndiName == null || "".equals(jndiName)) {
                jndiName = descriptor.getActivationConfigValue("destinationLookup");
            }
            validateJNDIRefs(jndiName, ejb);
        }
        for (Iterator it = ejb.getResourceReferenceDescriptors().iterator(); it.hasNext(); ) {
            ResourceReferenceDescriptor next = (ResourceReferenceDescriptor) it.next();
            accept(next, ejb);
        }

        for (Iterator it = ejb.getResourceEnvReferenceDescriptors().iterator(); it.hasNext();) {
            ResourceEnvReferenceDescriptor next = (ResourceEnvReferenceDescriptor) it.next();
            accept(next, ejb);
        }

        for (Iterator it = ejb.getMessageDestinationReferenceDescriptors().iterator(); it.hasNext();) {
            MessageDestinationReferenceDescriptor next = (MessageDestinationReferenceDescriptor) it.next();
            accept(next, ejb);
        }

        for (Iterator it = ejb.getEnvironmentProperties().iterator(); it.hasNext();) {
            EnvironmentProperty next = (EnvironmentProperty) it.next();
            accept(next, ejb);
        }
    }

    private void accept(BundleDescriptor bd) {
        if (bd instanceof JndiNameEnvironment) {
            JndiNameEnvironment nameEnvironment = (JndiNameEnvironment) bd;
            for (Iterator<ResourceReferenceDescriptor> itr = nameEnvironment.getResourceReferenceDescriptors().iterator(); itr.hasNext();) {
                accept(itr.next(), nameEnvironment);
            }

            for (Iterator<ResourceEnvReferenceDescriptor> itr = nameEnvironment.getResourceEnvReferenceDescriptors().iterator(); itr.hasNext();) {
                accept(itr.next(), nameEnvironment);
            }

            for (Iterator<MessageDestinationReferenceDescriptor> itr = nameEnvironment.getMessageDestinationReferenceDescriptors().iterator(); itr.hasNext();) {
                accept(itr.next(), nameEnvironment);
            }

            for (Iterator<MessageDestinationDescriptor> itr = bd.getMessageDestinations().iterator(); itr.hasNext();) {
                accept(itr.next(), nameEnvironment);
            }

            for (Iterator<EnvironmentProperty> itr = nameEnvironment.getEnvironmentProperties().iterator(); itr.hasNext();) {
                accept(itr.next(), nameEnvironment);
            }

            for (PersistenceUnitsDescriptor pus : bd.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
                for (PersistenceUnitDescriptor pu : pus.getPersistenceUnitDescriptors()) {
                    accept(pu, nameEnvironment);
                }
            }
        }
    }

    // TODO: Decide what to do in case of ORB
    private void accept(ResourceReferenceDescriptor resRef, JndiNameEnvironment env) {
        String jndiName = resRef.getJndiName();
        // The default values
        if (jndiName.equals("java:comp/DefaultDataSource") || jndiName.equals("java:comp/DefaultJMSConnectionFactory"))
            return;

        if (resRef.isORB() || resRef.isWebServiceContext()) {
            return;
        }

        if (resRef.isURLResource()) {
            if (!(jndiName.startsWith(ResourceConstants.JAVA_SCOPE_PREFIX))) {
                try {
                    // for jndi-name like "http://localhost:8080/index.html"
                    new java.net.URL(jndiName);
                    return;
                } catch (MalformedURLException e) {
                    // If jndi-name is not an actual url, we might want to lookup the name
                }
            }
        }
        validateJNDIRefs(resRef.getJndiName(), env);
    }

    /**
     * TODO: Implement
     * 1) Since a custom JNDI resource is stored in this, we need to exclude all normal references and validate only the custom resources.
     * Custom resources might also get stored in the general resources section depending on their type.
     * 2) All resources specified under resource-env-ref tag go in here.
     *
     * @param resRef
     * @param env
     */
    private void accept(ResourceEnvReferenceDescriptor resRef, JndiNameEnvironment env) {
        return;
    }

    /**
     * If the message destination ref is linked to a message destination, fetch the linked destination and validate it.
     * We might be duplicating our validation efforts since we are already validating message destination separately.
     *
     * @param msgDestRef
     * @param env
     */
    private void accept(MessageDestinationReferenceDescriptor msgDestRef, JndiNameEnvironment env) {
        if (msgDestRef.isLinkedToMessageDestination()) {
            validateJNDIRefs(msgDestRef.getMessageDestination().getJndiName(), env);
        }
        else {
            validateJNDIRefs(msgDestRef.getJndiName(), env);
        }
    }

    /**
     * Validate references to environment entries.
     * Also validate custom resources of primitive data types.
     *
     * @param envProp
     * @param env
     */
    private void accept(EnvironmentProperty envProp, JndiNameEnvironment env) {
        String jndiName = "";
        if (envProp.hasLookupName())
            jndiName = envProp.getLookupName();
        else if (envProp.getMappedName().length() > 0)
            jndiName = envProp.getMappedName();

        // If lookup/mapped name is not present, then we do not need to validate.
        if (jndiName.length() == 0)
            return;

        // Annotations provided by Java EE
        if (jndiName.equals("java:module/ModuleName") || jndiName.equals("java:app/AppName") ||
                jndiName.equals("java:comp/InAppClientContainer"))
            return;

        validateJNDIRefs(jndiName, env);
    }

    /**
     * @param pu - Persistence Unit Descriptor
     * @param env
     */
    private void accept(PersistenceUnitDescriptor pu, JndiNameEnvironment env) {
        String jtaDataSourceName = pu.getJtaDataSource();
        String nonJtaDataSourceName = pu.getNonJtaDataSource();

        if (jtaDataSourceName != null && jtaDataSourceName.length() > 0)
            validateJNDIRefs(jtaDataSourceName, env);
        if (nonJtaDataSourceName != null && nonJtaDataSourceName.length() > 0)
            validateJNDIRefs(nonJtaDataSourceName, env);
    }

    private void accept(NamedDescriptor resRef, JndiNameEnvironment env) {
        validateJNDIRefs(resRef.getJndiName(), env);
    }

    /**
     * Validate the given JNDI name by checking in domain.xml and in resources defined within the app.
     * In case a null jndi name is passed, we fail the deployment.
     *
     * @param jndiName
     * @param env
     */
    private void validateJNDIRefs(String jndiName, JndiNameEnvironment env) {
        if(!isResourceInDomainXML(jndiName)) {
            // convert comp to module if req
            String convertedJndiName = null;
            if (jndiName != null)
                convertedJndiName = getLogicalJNDIName(jndiName, env);
            if (!myNamespace.find(convertedJndiName, env)) {
                deplLogger.log(Level.SEVERE, RESOURCE_REF_JNDI_LOOKUP_FAILED,
                        new Object[] {jndiName});
                throw new DeploymentException(String.format("JNDI resource not present: %s", jndiName));
            }
        }
    }

    /**
     * Validate the given resource in the corresponding target using domain.xml serverbeans.
     * For resources defined outside the application.
     *
     * @param jndiName
     * @return
     */
    private boolean isResourceInDomainXML(String jndiName) {
        if (jndiName == null)
            return false;

        Server svr = domain.getServerNamed(target);
        if (svr != null) {
            return svr.isResourceRefExists(jndiName);
        }

        Cluster cluster = domain.getClusterNamed(target);
        if (cluster != null) {
            return cluster.isResourceRefExists(jndiName);
        }

        return false;
    }

    /**
     * A class to record all the logical JNDI names of resources defined in the application in the appropriate scopes.
     * App scoped resources, Resource Definitions are also stored in this data structure.
     */
    private static class JNDINamespace {
        private Map<String, List> componentNamespaces;

        private Map<String, List> moduleNamespaces;

        private List appNamespace;

        private List globalNameSpace;

        private JNDINamespace() {
            componentNamespaces = new HashMap<String,List>();
            moduleNamespaces = new HashMap<String,List>();
            appNamespace = new ArrayList();
            globalNameSpace = new ArrayList();
        }

        /**
         * Store app scoped resources in this namespace to facilitate lookup during validation.
         *
         * @param resources - App scoped resources
         * @param appName
         */
        private void storeAppScopedResources(Map<String, List> resources, String appName) {
            if (resources == null)
                return;
            List appLevelResources = resources.get(appName);
            appNamespace.addAll(appLevelResources);
            for (String key: resources.keySet()) {
                if (key.equals(appName))
                    continue;
                else {
                    String moduleName = key;
                    List jndiNames = moduleNamespaces.get(moduleName);
                    if (jndiNames == null) {
                        jndiNames = new ArrayList<String>();
                        jndiNames.addAll(resources.get(moduleName));
                        moduleNamespaces.put(moduleName, jndiNames);
                    }
                    else {
                        jndiNames.addAll(resources.get(moduleName));
                    }
                }
            }
        }

        /**
         * Store the jndi name in the correct scope. Will be stored only if jndi name is javaURL.
         *
         * @param jndiName
         * @param env
         */
        public void store(String jndiName, JndiNameEnvironment env) {
            if (jndiName.startsWith(ResourceConstants.JAVA_COMP_SCOPE_PREFIX)) {
                String componentId = DOLUtils.getComponentEnvId(env);
                List jndiNames = componentNamespaces.get(componentId);
                if (jndiNames == null) {
                    jndiNames = new ArrayList<String>();
                    jndiNames.add(jndiName);
                    componentNamespaces.put(componentId, jndiNames);
                }
                else {
                    jndiNames.add(jndiName);
                }
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_MODULE_SCOPE_PREFIX)) {
                String moduleName = DOLUtils.getModuleName(env);
                List jndiNames = moduleNamespaces.get(moduleName);
                if (jndiNames == null) {
                    jndiNames = new ArrayList<String>();
                    jndiNames.add(jndiName);
                    moduleNamespaces.put(moduleName, jndiNames);
                }
                else {
                    jndiNames.add(jndiName);
                }
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_APP_SCOPE_PREFIX)) {
                appNamespace.add(jndiName);
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_GLOBAL_SCOPE_PREFIX)) {
                globalNameSpace.add(jndiName);
            }
        }

        /**
         * Find the jndi name in our namespace.
         *
         * @param jndiName
         * @param env
         * @return
         */
        public boolean find(String jndiName, JndiNameEnvironment env) {
            if (jndiName == null)
                return false;

            if (jndiName.startsWith(ResourceConstants.JAVA_COMP_SCOPE_PREFIX)) {
                String componentId = DOLUtils.getComponentEnvId(env);
                List jndiNames = componentNamespaces.get(componentId);
                if (jndiNames == null)
                    return false;
                else
                    return jndiNames.contains(jndiName);
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_MODULE_SCOPE_PREFIX)) {
                String moduleName = DOLUtils.getModuleName(env);
                List jndiNames = moduleNamespaces.get(moduleName);
                if (jndiNames == null)
                    return false;
                else
                    return jndiNames.contains(jndiName);
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_APP_SCOPE_PREFIX))
                return appNamespace.contains(jndiName);
            else if (jndiName.startsWith(ResourceConstants.JAVA_GLOBAL_SCOPE_PREFIX))
                return globalNameSpace.contains(jndiName);
            return false;
        }
    }
}
