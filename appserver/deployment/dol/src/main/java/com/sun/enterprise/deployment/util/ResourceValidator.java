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
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.resourcebase.resources.api.ResourceConstants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
            message = "JNDI lookup failed for the resource: Name: {0}, Lookup: {1}, Type: {2}.",
            level = "SEVERE",
            cause = "JNDI lookup for the specified resource failed.",
            action = "Configure the required resources before deploying the application.",
            comment = "For the method validateJNDIRefs of com.sun.enterprise.deployment.util.ResourceValidator."
    )
    private static final String RESOURCE_REF_JNDI_LOOKUP_FAILED = "AS-DEPLOYMENT-00026";

    private String target;

    private DeploymentContext dc;

    private Application application;

    @Inject
    private Events events;

    @Inject
    private Domain domain;

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
            if (application == null || Boolean.getBoolean("deployment.switchoff.resource.validation"))
                return;
            AppResources appResources = new AppResources();
            parseResources(appResources);
            validateResources(appResources);
        }
    }

    /**
     * Store all the resources before starting the validation.
     */
    private void parseResources(AppResources appResources) {
        parseResources(application, appResources);
        for (BundleDescriptor bd : application.getBundleDescriptors()) {
            if (bd instanceof WebBundleDescriptor || bd instanceof ApplicationClientDescriptor)
                parseResources(bd, appResources);
            if (bd instanceof EjbBundleDescriptor) {
                parseResources(bd, appResources);
                EjbBundleDescriptor ebd = (EjbBundleDescriptor) bd;
                for (EjbDescriptor ejb : ebd.getEjbs())
                    parseResources(ejb, appResources);
            }
        }

        parseManagedBeans(appResources);

        // Parse AppScoped resources
        String appName = DOLUtils.getApplicationName(application);
        Map<String, List<String>> resourcesList =
                (Map<String, List<String>>) dc.getTransientAppMetadata().get(ResourceConstants.APP_SCOPED_RESOURCES_JNDI_NAMES);
        appResources.storeAppScopedResources(resourcesList, appName);
    }

    private void parseManagedBeans(AppResources appResources) {
        for (BundleDescriptor bd : application.getBundleDescriptors()) {
            for (ManagedBeanDescriptor managedBean : bd.getManagedBeans()) {
                appResources.storeInNamespace(managedBean.getGlobalJndiName(), (JndiNameEnvironment)bd);
            }
        }
    }

    private void parseResources(BundleDescriptor bd, AppResources appResources) {
        if (!(bd instanceof JndiNameEnvironment))
            return;
        JndiNameEnvironment env = (JndiNameEnvironment) bd;
        for (Object next : env.getResourceReferenceDescriptors()) {
            parseResources((ResourceReferenceDescriptor) next, env, appResources);
        }

        for (Object next : env.getResourceEnvReferenceDescriptors()) {
            parseResources((ResourceEnvReferenceDescriptor) next, env, appResources);
        }

        for (Object next : env.getMessageDestinationReferenceDescriptors()) {
            parseResources((MessageDestinationReferenceDescriptor) next, env, appResources);
        }

        for (Object next : env.getEnvironmentProperties()) {
            parseResources((EnvironmentProperty) next, env, appResources);
        }

        for (Object next : env.getAllResourcesDescriptors()) {
            parseResources((ResourceDescriptor) next, env, appResources);
        }

        for (Object next : env.getEntityManagerReferenceDescriptors()) {
            storeInNamespace(((EntityManagerReferenceDescriptor) next).getName(), env, appResources);
        }

        for (Object next : env.getEntityManagerFactoryReferenceDescriptors()) {
            storeInNamespace(((EntityManagerFactoryReferenceDescriptor) next).getName(), env, appResources);
        }

        for (Object next : env.getEjbReferenceDescriptors()) {
            parseResources((EjbReferenceDescriptor) next, env, appResources);
        }

        for (Object next : env.getServiceReferenceDescriptors()) {
            parseResources((ServiceReferenceDescriptor) next, env, appResources);
        }

        for (PersistenceUnitsDescriptor pus : bd.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
            for (PersistenceUnitDescriptor pu : pus.getPersistenceUnitDescriptors()) {
                parseResources(pu, env, appResources);
            }
        }

        for (ManagedBeanDescriptor mbd: bd.getManagedBeans()) {
            parseResources(mbd, env, appResources);
        }
    }

    /**
     * Store resources in ResourceRefDescriptor.
     */
    private void parseResources(ResourceReferenceDescriptor resRef, JndiNameEnvironment env, AppResources appResources) {
        resRef.checkType();
        String name = getLogicalJNDIName(resRef.getName(), env);
        String type = resRef.getType();
        String jndiName = resRef.getJndiName();
        AppResource resRefResource = new AppResource(name, jndiName, type, env, true);

        if (resRef.isURLResource()) {
            if (jndiName != null && !(jndiName.startsWith(ResourceConstants.JAVA_SCOPE_PREFIX))) {
                try {
                    // for jndi-name like "http://localhost:8080/index.html"
                    new java.net.URL(jndiName);
                    resRefResource.noValidation();
                } catch (MalformedURLException e) {
                    // If jndi-name is not an actual url, we might want to lookup the name
                }
            }
        }
        if (resRef.isWebServiceContext())
            resRefResource.noValidation();

        appResources.store(resRefResource);
    }

    /**
     * Store resources in ResourceEnvRefDescriptor.
     */
    private void parseResources(ResourceEnvReferenceDescriptor resEnvRef, JndiNameEnvironment env, AppResources appResources) {
        resEnvRef.checkType();
        String name = getLogicalJNDIName(resEnvRef.getName(), env);
        String type = resEnvRef.getType();
        String jndiName = resEnvRef.getJndiName();
        AppResource resEnvRefResource = new AppResource(name, jndiName, type, env, true);

        if (resEnvRef.isEJBContext() || resEnvRef.isValidator() || resEnvRef.isValidatorFactory() || resEnvRef.isCDIBeanManager())
            resEnvRefResource.noValidation();

        appResources.store(resEnvRefResource);
    }

    /**
     * If the message destination ref is linked to a message destination, fetch the linked destination and validate it.
     * We might be duplicating our validation efforts since we are already validating message destination separately.
     */
    private void parseResources(MessageDestinationReferenceDescriptor msgDestRef, JndiNameEnvironment env, AppResources appResources) {
        String name = getLogicalJNDIName(msgDestRef.getName(), env);
        String jndiName;
        if (msgDestRef.isLinkedToMessageDestination()) {
            jndiName = msgDestRef.getMessageDestination().getJndiName();
        }
        else {
            jndiName = msgDestRef.getJndiName();
        }
        appResources.store(new AppResource(name, jndiName, msgDestRef.getType(), env, true));
    }

    /**
     * Store references to environment entries.
     * Also validate custom resources of primitive data types.
     */
    private void parseResources(EnvironmentProperty envProp, JndiNameEnvironment env, AppResources appResources) {
        String name = getLogicalJNDIName(envProp.getName(), env);
        String jndiName = "";
        if (envProp.hasLookupName())
            jndiName = envProp.getLookupName();
        // error handling for mapped name null case done in getMappedName
        else if (envProp.getMappedName().length() > 0)
            jndiName = envProp.getMappedName();

        AppResource envPropResource = new AppResource(name, jndiName, envProp.getType(), env, true);
        // If lookup/mapped name is not present, then we do not need to validate.
        if (jndiName.length() == 0)
            envPropResource.noValidation();

        appResources.store(envPropResource);

        // Store EnvProps even if they do not have a valid lookup element
        appResources.storeInNamespace(name, env);
    }

    /**
     * TODO: @EJB
     */
    private void parseResources(EjbReferenceDescriptor ejbRef, JndiNameEnvironment env, AppResources appResources) {

    }

    /**
     * TODO: @WebServiceRef
     */
    private void parseResources(ServiceReferenceDescriptor serviceRef, JndiNameEnvironment env, AppResources appResources) {

    }

    /**
     * Store the resource definitions in our namespace.
     * CFD and AODD are not valid in an AppClient.
     */
    private void parseResources(ResourceDescriptor resourceDescriptor, JndiNameEnvironment env, AppResources appResources) {
        if (env instanceof ApplicationClientDescriptor)
            if (resourceDescriptor.getResourceType().equals(JavaEEResourceType.CFD) || resourceDescriptor.getResourceType().equals(JavaEEResourceType.AODD))
                return;
        storeInNamespace(resourceDescriptor.getName(), env, appResources);
    }

    /**
     * Record the Data Source specified in PUD.
     */
    private void parseResources(PersistenceUnitDescriptor pu, JndiNameEnvironment env, AppResources appResources) {
        String jtaDataSourceName = pu.getJtaDataSource();
        String nonJtaDataSourceName = pu.getNonJtaDataSource();

        if (jtaDataSourceName != null && jtaDataSourceName.length() > 0)
            appResources.store(new AppResource(pu.getName(), jtaDataSourceName, "javax.sql.DataSource", env, true));
        if (nonJtaDataSourceName != null && nonJtaDataSourceName.length() > 0)
            appResources.store(new AppResource(pu.getName(), nonJtaDataSourceName, "javax.sql.DataSource", env, true));
    }

    private void parseResources(ManagedBeanDescriptor managedBean, JndiNameEnvironment env, AppResources appResources) {
        for (Object next : managedBean.getResourceReferenceDescriptors()) {
            parseResources((ResourceReferenceDescriptor) next, env, appResources);
        }

        for (Object next : managedBean.getResourceEnvReferenceDescriptors()) {
            parseResources((ResourceEnvReferenceDescriptor) next, env, appResources);
        }

        for (Object next : managedBean.getMessageDestinationReferenceDescriptors()) {
            parseResources((MessageDestinationReferenceDescriptor) next, env, appResources);
        }

        for (Object next : managedBean.getEjbReferenceDescriptors()) {
            parseResources((EjbReferenceDescriptor) next, env, appResources);
        }

        for (Object next : managedBean.getEnvironmentProperties()) {
            parseResources((EnvironmentProperty) next, env, appResources);
        }

        for (Object next : env.getAllResourcesDescriptors()) {
            parseResources((ResourceDescriptor) next, env, appResources);
        }
    }

    private void parseResources(EjbDescriptor ejb, AppResources appResources) {
        for (Object next : ejb.getResourceReferenceDescriptors()) {
            parseResources((ResourceReferenceDescriptor) next, ejb, appResources);
        }

        for (Object next : ejb.getResourceEnvReferenceDescriptors()) {
            parseResources((ResourceEnvReferenceDescriptor) next, ejb, appResources);
        }

        for (Object next : ejb.getMessageDestinationReferenceDescriptors()) {
            parseResources((MessageDestinationReferenceDescriptor) next, ejb, appResources);
        }

        for (Object next : ejb.getEnvironmentProperties()) {
            parseResources((EnvironmentProperty) next, ejb, appResources);
        }

        for (Object next : ejb.getEjbReferenceDescriptors()) {
            parseResources((EjbReferenceDescriptor) next, ejb, appResources);
        }

        for (Object next : ejb.getAllResourcesDescriptors()) {
            parseResources((ResourceDescriptor) next, ejb, appResources);
        }
    }

    private void storeInNamespace(String name, JndiNameEnvironment env, AppResources appResources) {
        String logicalJNDIName = getLogicalJNDIName(name, env);
        appResources.storeInNamespace(logicalJNDIName, env);
    }

    /**
     * @param rawName to be converted
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

    /**
     * Convert JNDI names beginning with java:module and java:app to their corresponding java:global names.
     *
     * @return the converted name with java:global JNDI prefix.
     */
    private String convertModuleOrAppJNDIName(String jndiName, JndiNameEnvironment env) {
        BundleDescriptor bd = null;
        if( env instanceof EjbDescriptor ) {
            bd = ((EjbDescriptor)env).getEjbBundleDescriptor();
        } else if( env instanceof BundleDescriptor ) {
            bd = (BundleDescriptor) env;
        }

        if (jndiName == null)
            return null;

        if( bd != null ) {
            String appName = null;
            if (!application.isVirtual()) {
                appName = application.getAppName();
            }
            String moduleName = bd.getModuleDescriptor().getModuleName();
            StringBuilder javaGlobalName = new StringBuilder("java:global/");
            if (jndiName.startsWith(ResourceConstants.JAVA_APP_SCOPE_PREFIX)) {
                if (appName != null) {
                    javaGlobalName.append(appName);
                    javaGlobalName.append("/");
                }

                // Replace java:app/ with the fully-qualified global portion
                int javaAppLength = ResourceConstants.JAVA_APP_SCOPE_PREFIX.length();
                javaGlobalName.append(jndiName.substring(javaAppLength));
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_MODULE_SCOPE_PREFIX)) {
                if (appName != null) {
                    javaGlobalName.append(appName);
                    javaGlobalName.append("/");
                }

                javaGlobalName.append(moduleName);
                javaGlobalName.append("/");

                // Replace java:module/ with the fully-qualified global portion
                int javaModuleLength = ResourceConstants.JAVA_MODULE_SCOPE_PREFIX.length();
                javaGlobalName.append(jndiName.substring(javaModuleLength));
            }
            else {
                return "";
            }
            return javaGlobalName.toString();
        }
        return "";
    }

    /**
     * Start of validation logic.
     */
    private void validateResources(AppResources appResources) {
        for (AppResource resource : appResources.myResources) {
            if (resource.validate)
                validateJNDIRefs(resource, appResources.myNamespace);
        }
    }

    /**
     * Strategy for validating a given jndi name
     * 1) Check in domain.xml
     * 2) Check in the resources defined within the app. These have not been binded to the namespace yet.
     * 3) Check for resources defined by an earlier application.
     *
     * In case a null jndi name is passed, we fail the deployment.
     *
     * @param resource to be validated.
     */
    private void validateJNDIRefs(AppResource resource, JNDINamespace namespace) {
        // In case lookup is not present, check if another resource with the same name exists
        if(!resource.hasLookup() && !namespace.find(resource.getName(), resource.getEnv())) {
            deplLogger.log(Level.SEVERE, RESOURCE_REF_JNDI_LOOKUP_FAILED,
                    new Object[] {resource.getName(), null, resource.getType()});
            throw new DeploymentException("Null JNDI resource");
        }

        String jndiName = resource.getJndiName();
        JndiNameEnvironment env = resource.getEnv();

        if (isResourceInDomainXML(jndiName) || isDefaultResource(jndiName))
            return;

        // Managed Bean & EJB portable JNDI names
        if (jndiName.startsWith(ResourceConstants.JAVA_MODULE_SCOPE_PREFIX) || jndiName.startsWith(ResourceConstants.JAVA_APP_SCOPE_PREFIX)) {
            String newName = convertModuleOrAppJNDIName(jndiName, resource.getEnv());
            if (namespace.find(newName, env))
                return;
        }

        // convert comp to module if req
        String convertedJndiName = getLogicalJNDIName(jndiName, env);
        if (namespace.find(convertedJndiName, env))
            return;

        try {
            if(loadOnCurrentInstance()) {
                InitialContext ctx = new InitialContext();
                ctx.lookup(jndiName);
            }
        } catch (NamingException e) {
            deplLogger.log(Level.SEVERE, RESOURCE_REF_JNDI_LOOKUP_FAILED,
                    new Object[] {resource.getName(), jndiName, resource.getType()});
            DeploymentException de = new DeploymentException(String.format("JNDI resource not present: %s, value: %s",
                    resource.getName(), jndiName));
            de.initCause(e);
            throw de;
        }
    }

    /**
     * Validate the given resource in the corresponding target using domain.xml server beans.
     * For resources defined outside the application.
     *
     * @param jndiName to be validated
     * @return True if resource is present in domain.xml in the corresponding target. False otherwise.
     */
    private boolean isResourceInDomainXML(String jndiName) {
        if (jndiName == null)
            return false;

        Server svr = domain.getServerNamed(target);
        if (svr != null) {
            return svr.isResourceRefExists(jndiName);
        }

        Cluster cluster = domain.getClusterNamed(target);
        return cluster != null && cluster.isResourceRefExists(jndiName);
    }

    /**
     * Default resources provided by GF.
     */
    private boolean isDefaultResource(String jndiName) {
        return (jndiName != null &&
                (jndiName.equals("java:comp/DefaultDataSource") ||
                        jndiName.equals("java:comp/DefaultJMSConnectionFactory") ||
                        jndiName.equals("java:comp/ORB") ||
                        jndiName.equals("java:comp/DefaultManagedExecutorService") ||
                        jndiName.equals("java:comp/DefaultManagedScheduledExecutorService") ||
                        jndiName.equals("java:comp/DefaultManagedThreadFactory") ||
                        jndiName.equals("java:comp/DefaultContextService") ||
                        jndiName.equals("java:comp/UserTransaction") ||
                        jndiName.equals("java:comp/TransactionSynchronizationRegistry") ||
                        jndiName.equals("java:comp/BeanManager") ||
                        jndiName.equals("java:comp/ValidatorFactory") ||
                        jndiName.equals("java:comp/Validator") ||
                        jndiName.equals("java:module/ModuleName") ||
                        jndiName.equals("java:app/AppName") ||
                        jndiName.equals("java:comp/InAppClientContainer")));
    }

    private static class AppResource {
        private String name;

        private String lookup;

        private String type;

        private JndiNameEnvironment env;

        boolean validate;

        private AppResource(String name, String lookup, String type, JndiNameEnvironment env, boolean validate) {
            this.name = name;
            this.lookup = lookup;
            this.type = type;
            this.env = env;
            this.validate = validate;
        }

        private String getJndiName() {
            return lookup;
        }

        private JndiNameEnvironment getEnv() {
            return env;
        }

        private String getName() {
            return name;
        }

        private String getType() {
            return type;
        }

        private boolean hasLookup() {
            return lookup != null && lookup.length() > 0;
        }

        private void noValidation() {
            validate = false;
        }
    }

    private static class AppResources {
        List<AppResource> myResources;

        JNDINamespace myNamespace;

        private AppResources() {
            myResources = new ArrayList<>();
            myNamespace = new JNDINamespace();
        }

        /**
         * Store in namespace only if it has a valid lookup value. This is because we do not want to store invalid
         * resources in our namespace.
         */
        private void store(AppResource resource) {
            myResources.add(resource);
            if (resource.hasLookup())
                myNamespace.store(resource.name, resource.env);
        }

        /**
         * If we know that the name points to a valid resource, directly store in namespace.
         */
        private void storeInNamespace(String name, JndiNameEnvironment env) {
            myNamespace.store(name, env);
        }

        private void storeAppScopedResources(Map<String, List<String>> resourcesList, String appName) {
            myNamespace.storeAppScopedResources(resourcesList, appName);
        }
    }

    /**
     * A class to record all the logical JNDI names of resources defined in the application in the appropriate scopes.
     * App scoped resources, Resource Definitions are also stored in this data structure.
     */
    private static class JNDINamespace {
        private Map<String, List<String>> componentNamespaces;

        private Map<String, List<String>> moduleNamespaces;

        private List<String> appNamespace;

        private List<String> globalNameSpace;

        private JNDINamespace() {
            componentNamespaces = new HashMap<>();
            moduleNamespaces = new HashMap<>();
            appNamespace = new ArrayList<>();
            globalNameSpace = new ArrayList<>();
        }

        /**
         * Store app scoped resources in this namespace to facilitate lookup during validation.
         *
         * @param resources - App scoped resources
         * @param appName - Application name
         */
        private void storeAppScopedResources(Map<String, List<String>> resources, String appName) {
            if (resources == null)
                return;
            List<String> appLevelResources = resources.get(appName);
            appNamespace.addAll(appLevelResources);
            for (Map.Entry<String, List<String>> entry: resources.entrySet()) {
                if (!entry.getKey().equals(appName)) {
                    List<String> jndiNames = moduleNamespaces.get(entry.getKey());
                    if (jndiNames == null) {
                        jndiNames = new ArrayList<>();
                        jndiNames.addAll(entry.getValue());
                        moduleNamespaces.put(entry.getKey(), jndiNames);
                    }
                    else {
                        jndiNames.addAll(entry.getValue());
                    }
                }
            }
        }

        /**
         * Store the jndi name in the correct scope. Will be stored only if jndi name is javaURL.
         */
        public void store(String jndiName, JndiNameEnvironment env) {
            if (jndiName.startsWith(ResourceConstants.JAVA_COMP_SCOPE_PREFIX)) {
                String componentId = DOLUtils.getComponentEnvId(env);
                List<String> jndiNames = componentNamespaces.get(componentId);
                if (jndiNames == null) {
                    jndiNames = new ArrayList<>();
                    jndiNames.add(jndiName);
                    componentNamespaces.put(componentId, jndiNames);
                }
                else {
                    jndiNames.add(jndiName);
                }
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_MODULE_SCOPE_PREFIX)) {
                String moduleName = DOLUtils.getModuleName(env);
                List<String> jndiNames = moduleNamespaces.get(moduleName);
                if (jndiNames == null) {
                    jndiNames = new ArrayList<>();
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
         * @return True if the jndi name is found in the namespace. False otherwise.
         */
        public boolean find(String jndiName, JndiNameEnvironment env) {
            if (jndiName == null)
                return false;

            if (jndiName.startsWith(ResourceConstants.JAVA_COMP_SCOPE_PREFIX)) {
                String componentId = DOLUtils.getComponentEnvId(env);
                List jndiNames = componentNamespaces.get(componentId);
                return jndiNames != null && jndiNames.contains(jndiName);
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_MODULE_SCOPE_PREFIX)) {
                String moduleName = DOLUtils.getModuleName(env);
                List jndiNames = moduleNamespaces.get(moduleName);
                return jndiNames != null && jndiNames.contains(jndiName);
            }
            else if (jndiName.startsWith(ResourceConstants.JAVA_APP_SCOPE_PREFIX))
                return appNamespace.contains(jndiName);
            else if (jndiName.startsWith(ResourceConstants.JAVA_GLOBAL_SCOPE_PREFIX))
                return globalNameSpace.contains(jndiName);
            return false;
        }
    }

    /**
     * Copy from ApplicationLifeCycle.java
     * TODO: Cluster case - Implement
     */
    private boolean loadOnCurrentInstance() {
        return true;
    }
}
