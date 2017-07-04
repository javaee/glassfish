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
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.*;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DeploymentProperties;
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

    @Inject @Named( ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

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
     * Parse all the resources and store them in a namespace before starting the validation.
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
                parseEJB(ejb);
        }

        // Parse the Managed Beans
        parseManagedBeans();

        // Parse AppScoped resources
        String appName = DOLUtils.getApplicationName(application);
        Map<String, List<String>> resourcesList =
                (Map<String, List<String>>) dc.getTransientAppMetadata().get(ResourceConstants.APP_SCOPED_RESOURCES_JNDI_NAMES);
        myNamespace.storeAppScopedResources(resourcesList, appName);
    }

    private void parseEJB(EjbDescriptor ejb) {
        String javaGlobalName = getJavaGlobalJndiNamePrefix(ejb);
        myNamespace.store(javaGlobalName, ejb);

        // interfaces now
        if (ejb.isRemoteInterfacesSupported()) {
            String intf = ejb.getHomeClassName();
            String fullyQualifiedJavaGlobalName = javaGlobalName + "!" + intf;
            myNamespace.store(fullyQualifiedJavaGlobalName, ejb);
        }

        if (ejb.isRemoteBusinessInterfacesSupported()) {
            for (String intf : ejb.getRemoteBusinessClassNames()) {
                String fullyQualifiedJavaGlobalName = javaGlobalName + "!" + intf;
                myNamespace.store(fullyQualifiedJavaGlobalName, ejb);
            }
        }

        if (ejb.isLocalInterfacesSupported()) {
            String intf = ejb.getLocalHomeClassName();
            String fullyQualifiedJavaGlobalName = javaGlobalName + "!" + intf;
            myNamespace.store(fullyQualifiedJavaGlobalName, ejb);
        }

        if (ejb.isLocalBusinessInterfacesSupported()) {
            for (String intf : ejb.getLocalBusinessClassNames()) {
                String fullyQualifiedJavaGlobalName = javaGlobalName + "!" + intf;
                myNamespace.store(fullyQualifiedJavaGlobalName, ejb);
            }
        }

        if (ejb.isLocalBean()) {
            String intf = ejb.getEjbClassName();
            String fullyQualifiedJavaGlobalName = javaGlobalName + "!" + intf;
            myNamespace.store(fullyQualifiedJavaGlobalName, ejb);
        }

        parseResources(ejb);
    }

    protected String getJavaGlobalJndiNamePrefix(EjbDescriptor ejbDescriptor) {

        String appName = null;

        Application app = ejbDescriptor.getApplication();
        if ( ! app.isVirtual() ) {
            appName = ejbDescriptor.getApplication().getAppName();
        }

        EjbBundleDescriptor ejbBundle = ejbDescriptor.getEjbBundleDescriptor();
        String modName = ejbBundle.getModuleDescriptor().getModuleName();

        String ejbName = ejbDescriptor.getName();

        StringBuffer javaGlobalPrefix = new StringBuffer("java:global/");

        if (appName != null) {
            javaGlobalPrefix.append(appName);
            javaGlobalPrefix.append("/");
        }

        javaGlobalPrefix.append(modName);
        javaGlobalPrefix.append("/");
        javaGlobalPrefix.append(ejbName);


        return javaGlobalPrefix.toString();
    }

    private void parseManagedBeans() {
        for (BundleDescriptor bd : application.getBundleDescriptors()) {
            for (ManagedBeanDescriptor managedBean : bd.getManagedBeans()) {
                myNamespace.store(managedBean.getGlobalJndiName(), (JndiNameEnvironment)bd);
            }
        }
    }

    private void parseResources(JndiNameEnvironment env) {
        for (Object next : env.getResourceReferenceDescriptors()) {
            parseResources((ResourceReferenceDescriptor) next, env);
        }

        for (Object next : env.getResourceEnvReferenceDescriptors()) {
            parseResources((ResourceEnvReferenceDescriptor) next, env);
        }

        for (Object next : env.getMessageDestinationReferenceDescriptors()) {
            parseResources((MessageDestinationReferenceDescriptor) next, env);
        }

        for (Object next : env.getEnvironmentProperties()) {
            parseResources((EnvironmentProperty) next, env);
        }

        for (Object next : env.getAllResourcesDescriptors()) {
            parseResources((ResourceDescriptor) next, env);
        }

        for (Object next : env.getEntityManagerReferenceDescriptors()) {
            parseResources((EntityManagerReferenceDescriptor) next, env);
        }

        for (Object next : env.getEntityManagerFactoryReferenceDescriptors()) {
            parseResources((EntityManagerFactoryReferenceDescriptor) next, env);
        }

        for (Object next : env.getEjbReferenceDescriptors()) {
            parseResources((EjbReferenceDescriptor) next, env);
        }

        for (Object next : env.getServiceReferenceDescriptors()) {
            parseResources((ServiceReferenceDescriptor) next, env);
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
     * Start of validation logic.
     */
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
            // validateJNDIRefs(jndiName, ejb);
        }
        for (Object next : ejb.getResourceReferenceDescriptors()) {
            accept((ResourceReferenceDescriptor) next, ejb);
        }

        for (Object next : ejb.getResourceEnvReferenceDescriptors()) {
            accept((ResourceEnvReferenceDescriptor) next, ejb);
        }

        for (Object next : ejb.getMessageDestinationReferenceDescriptors()) {
            accept((MessageDestinationReferenceDescriptor) next, ejb);
        }

        for (Object next : ejb.getEnvironmentProperties()) {
            accept((EnvironmentProperty) next, ejb);
        }

        for (Object next : ejb.getEjbReferenceDescriptors()) {
            accept((EjbReferenceDescriptor) next, ejb);
        }
    }

    private void accept(BundleDescriptor bd) {
        if (bd instanceof JndiNameEnvironment) {
            JndiNameEnvironment nameEnvironment = (JndiNameEnvironment) bd;
            for (Object next : nameEnvironment.getResourceReferenceDescriptors()) {
                accept((ResourceReferenceDescriptor) next, nameEnvironment);
            }

            for (Object next : nameEnvironment.getResourceEnvReferenceDescriptors()) {
                accept((ResourceEnvReferenceDescriptor) next, nameEnvironment);
            }

            for (Object next : nameEnvironment.getMessageDestinationReferenceDescriptors()) {
                accept((MessageDestinationReferenceDescriptor) next, nameEnvironment);
            }

            for (Object next : bd.getMessageDestinations()) {
                accept((MessageDestinationDescriptor) next, nameEnvironment);
            }

            for (Object next : nameEnvironment.getEnvironmentProperties()) {
                accept((EnvironmentProperty) next, nameEnvironment);
            }

            for (Object next : nameEnvironment.getEjbReferenceDescriptors()) {
                accept((EjbReferenceDescriptor) next, nameEnvironment);
            }

            for (PersistenceUnitsDescriptor pus : bd.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
                for (PersistenceUnitDescriptor pu : pus.getPersistenceUnitDescriptors()) {
                    accept(pu, nameEnvironment);
                }
            }

            for (ManagedBeanDescriptor mbd: bd.getManagedBeans()) {
                accept(mbd, nameEnvironment);
            }
        }
    }

    /**
     * Validate resources stored in ResourceRefDescriptor.
     */
    private void accept(ResourceReferenceDescriptor resRef, JndiNameEnvironment env) {
        String jndiName = resRef.getJndiName();

        if (resRef.isWebServiceContext())
            return;

        if (resRef.isURLResource()) {
            if (jndiName != null && !(jndiName.startsWith(ResourceConstants.JAVA_SCOPE_PREFIX))) {
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
     * Validate resources stored in ResourceEnvRefDescriptor.
     * Managed Bean references are validated here.
     * TODO: Need to fail deployment in case a JNDI lookup is used to get a validator?
     */
    private void accept(ResourceEnvReferenceDescriptor resourceEnvRef, JndiNameEnvironment env) {
        String jndiName = resourceEnvRef.getJndiName();
        if (resourceEnvRef.isEJBContext() || resourceEnvRef.isValidator() || resourceEnvRef.isValidatorFactory() || resourceEnvRef.isCDIBeanManager())
            return;

        // Validate Managed Bean references now
        String newName = convertModuleOrAppJNDIName(jndiName, env);
        if (!myNamespace.find(newName, env)) {
            // Every type of resource taken care of. Validate any custom resources now.
            validateJNDIRefs(jndiName, env);
        }
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
     * If the message destination ref is linked to a message destination, fetch the linked destination and validate it.
     * We might be duplicating our validation efforts since we are already validating message destination separately.
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

        validateJNDIRefs(jndiName, env);
    }

    /**
     * TODO: In Progress
     */
    private void accept(EjbReferenceDescriptor ejbRef, JndiNameEnvironment env) {
        // we only need to worry about those references which are not linked yet
        if(ejbRef.getEjbDescriptor() != null)
            return;

        String jndiName = ejbRef.getLookupName();
        if (!ejbRef.hasLookupName()) {
            jndiName = ejbRef.getMappedName();
        }

        String newName = convertModuleOrAppJNDIName(jndiName, env);
        // JNDI names starting with java:app and java:module are taken care of
        if (!myNamespace.find(newName, env)) {
            // fall through
            validateJNDIRefs(jndiName, env);
        }
    }

    /**
     * Validate Data Source in a PUD.
     */
    private void accept(PersistenceUnitDescriptor pu, JndiNameEnvironment env) {
        String jtaDataSourceName = pu.getJtaDataSource();
        String nonJtaDataSourceName = pu.getNonJtaDataSource();

        if (jtaDataSourceName != null && jtaDataSourceName.length() > 0 && !jtaDataSourceName.equals("java:comp/DefaultDataSource"))
            validateJNDIRefs(jtaDataSourceName, env);
        if (nonJtaDataSourceName != null && nonJtaDataSourceName.length() > 0 && !nonJtaDataSourceName.equals("java:comp/DefaultDataSource"))
            validateJNDIRefs(nonJtaDataSourceName, env);
    }

    /**
     * Validate resources defined in a managed bean.
     */
    private void accept(ManagedBeanDescriptor managedBean, JndiNameEnvironment env) {
        for (Object next : managedBean.getEjbReferenceDescriptors()) {
            accept((EjbReferenceDescriptor) next, env);
        }

        for (Object next : managedBean.getResourceReferenceDescriptors()) {
            accept((ResourceReferenceDescriptor) next, env);
        }

        for (Object next : managedBean.getResourceEnvReferenceDescriptors()) {
            accept((ResourceEnvReferenceDescriptor) next, env);
        }

        for (Object next : managedBean.getMessageDestinationReferenceDescriptors()) {
            accept((MessageDestinationReferenceDescriptor) next, env);
        }
    }

    private void accept(MessageDestinationDescriptor msgDest, JndiNameEnvironment env) {
        validateJNDIRefs(msgDest.getJndiName(), env);
    }

    /**
     * Strategy for validating a given jndi name
     * 1) Check in domain.xml
     * 2) Check in the resources defined within the app. These have not been binded to the namespace yet.
     * 3) Check for resources defined by an earlier application.
     *
     * In case a null jndi name is passed, we fail the deployment.
     *
     * @param jndiName to be validated.
     */
    private void validateJNDIRefs(String jndiName, JndiNameEnvironment env) {
        if (jndiName == null || jndiName == "") {
            deplLogger.log(Level.SEVERE, RESOURCE_REF_JNDI_LOOKUP_FAILED,
                    new Object[] {null});
            throw new DeploymentException("Null JNDI resource");
        }
        if(!isResourceInDomainXML(jndiName) && !isDefaultResource(jndiName)) {
            // convert comp to module if req
            String convertedJndiName = getLogicalJNDIName(jndiName, env);
            if (!myNamespace.find(convertedJndiName, env)) {
                // Do a context lookup only if we are on the correct instance
                try {
                    if(loadOnCurrentInstance()) {
                        InitialContext ctx = new InitialContext();
                        ctx.lookup(jndiName);
                    }
                } catch (NamingException e) {
                    deplLogger.log(Level.SEVERE, RESOURCE_REF_JNDI_LOOKUP_FAILED,
                            new Object[] {jndiName});
                    throw new DeploymentException(String.format("JNDI resource not present: %s", jndiName));
                }
            }
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
            componentNamespaces = new HashMap<String,List<String>>();
            moduleNamespaces = new HashMap<String,List<String>>();
            appNamespace = new ArrayList<>();
            globalNameSpace = new ArrayList<>();
        }

        /**
         * Store app scoped resources in this namespace to facilitate lookup during validation.
         *
         * @param resources - App scoped resources
         * @param appName
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
                        jndiNames = new ArrayList<String>();
                        jndiNames.addAll(entry.getValue());
                        moduleNamespaces.put(entry.getKey(), jndiNames);
                    }
                    else {
                        jndiNames.addAll(resources.get(entry.getValue()));
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
     * Copied from ApplicationLifeCycle.java
     */
    private boolean loadOnCurrentInstance() {
        final DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
        final Properties appProps = dc.getAppProps();
        if (commandParams.enabled) {
            // if the current instance match with the target
            if (domain.isCurrentInstanceMatchingTarget(commandParams.target, commandParams.name(), server.getName(), dc.getTransientAppMetaData(DeploymentProperties.PREVIOUS_TARGETS, List.class))) {
                return true;
            }
            if (server.isDas()) {
                String objectType =
                        appProps.getProperty(ServerTags.OBJECT_TYPE);
                if (objectType != null) {
                    // if it's a system application needs to be loaded on DAS
                    if (objectType.equals(DeploymentProperties.SYSTEM_ADMIN) ||
                            objectType.equals(DeploymentProperties.SYSTEM_ALL)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
