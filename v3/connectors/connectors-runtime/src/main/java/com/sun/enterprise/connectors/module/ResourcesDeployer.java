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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package com.sun.enterprise.connectors.module;

import com.sun.appserv.connectors.internal.api.*;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.resource.ResourceUtilities;
import com.sun.logging.LogDomains;
import org.glassfish.admin.cli.resources.*;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.*;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.javaee.services.ResourcesBinder;
import org.glassfish.resource.common.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;

import org.glassfish.api.event.EventListener;

import javax.resource.ResourceException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ResourcesDeployer extends JavaEEDeployer<ResourcesContainer, ResourcesApplication>
        implements PostConstruct, PreDestroy, EventListener {

    @Inject
    private static ResourceFactory resourceFactory;

    @Inject
    private Domain domain;

    @Inject
    private ServerContext context;

    @Inject
    private static ApplicationRegistry appRegistry;

    @Inject
    private static Habitat habitat;

    @Inject
    private static ResourcesBinder resourcesBinder;

    @Inject
    private ConfigSupport configSupport;

    @Inject
    private Events events;

    @Inject
    private static Applications applications;

    private static Logger _logger = LogDomains.getLogger(ConnectorDeployer.class, LogDomains.RSR_LOGGER);

    public ResourcesDeployer(){
    }

    public void postConstruct() {
        events.register(this);
    }

    public void preDestroy(){
        events.unregister(this);
    }

    @Override
    protected String getModuleType() {
        return ConnectorConstants.GF_RESOURCES_MODULE_EAR;
    }

    /**
     * Loads a previously prepared application in its execution environment and
     * return a ContractProvider instance that will identify this environment in
     * future communications with the application's container runtime.
     *
     * @param container in which the application will reside
     * @param context   of the deployment
     * @return an ApplicationContainer instance identifying the running application
     */
    @Override
    public ResourcesApplication load(ResourcesContainer container, DeploymentContext context) {
        super.load(container, context);
        debug("App-Scoped-Resources ResourcesDeployer.load()");
        ResourcesApplication application = habitat.getComponent(ResourcesApplication.class);
        application.setApplicationName(getApplicationName(context));
        return application;
    }

    public void unload(ResourcesApplication appContainer, DeploymentContext context) {
        //TODO unregistering resources, removing resources configuration.
        debug("Resources-Deployer :unload() called");
    }


    private void processArchive(DeploymentContext dc) {

        try {
            ReadableArchive archive = dc.getSource();

            if (DeploymentUtils.hasResourcesXML(archive)) {

                Map<String,Map<String, List>> appScopedResources = new HashMap<String,Map<String,List>>();
                Map<String, String> fileNames = new HashMap<String, String>();

                retrieveAllResourcesXMLs(fileNames, archive.getURI().getPath());

                for (String moduleName: fileNames.keySet()) {
                    String fileName = fileNames.get(moduleName);
                    debug("Sun Resources XML : " + fileName);

                    String appName = getApplicationName(dc);
                    moduleName = getActualModuleName(moduleName);
                    String scope = "java:app";
                    if(appName.equals(moduleName)){
                        scope = ConnectorConstants.JAVA_APP_SCOPE_PREFIX;
                    }else{
                        scope = ConnectorConstants.JAVA_MODULE_SCOPE_PREFIX;
                    }

                    File file = new File(fileName);
                    ResourcesXMLParser parser = new ResourcesXMLParser(file, scope);

                    validateResourcesXML(file, parser);

                    List list = parser.getResourcesList();

                    Map<String, List> resourcesList = new HashMap<String, List>();
                    List<org.glassfish.resource.common.Resource> nonConnectorResources =
                            ResourcesXMLParser.getNonConnectorResourcesList(list, false, true);
                    resourcesList.put(ConnectorConstants.NON_CONNECTOR_RESOURCES, nonConnectorResources);
                    
                    List<org.glassfish.resource.common.Resource> connectorResources =
                            ResourcesXMLParser.getConnectorResourcesList(list, false, true);
                    resourcesList.put(ConnectorConstants.CONNECTOR_RESOURCES, connectorResources);

                    appScopedResources.put(moduleName, resourcesList);
                }
                dc.addTransientAppMetaData(ConnectorConstants.APP_SCOPED_RESOURCES_MAP, appScopedResources);
                final String appName = getApplicationName(dc);
                ApplicationInfo appInfo = appRegistry.get(appName);
                if(appInfo != null){
                    Application app = dc.getTransientAppMetaData(Application.APPLICATION, Application.class);
                    appInfo.addTransientAppMetaData(Application.APPLICATION, app);
                }
            }
        } catch (Exception e) {
            // only DeploymentExceptions are propagated and result in deployment failure
            // in the event notification infrastructure
            throw new DeploymentException("Failue while processing glassfish-resources.xml(s) in the archive ", e);
        }
    }

    private void validateResourcesXML(File file, ResourcesXMLParser parser) throws ResourceConflictException {
        String filePath = file.getPath();
        SunResourcesXML sunResourcesXML = new SunResourcesXML(filePath, parser.getResourcesList());
        List<SunResourcesXML> resourcesXMLList = new ArrayList<SunResourcesXML>();
        resourcesXMLList.add(sunResourcesXML);
        ResourceUtilities.resolveResourceDuplicatesConflictsWithinArchive(resourcesXMLList);
    }

    public static void populateResourceConfigInAppInfo(DeploymentContext dc){
        String appName = getApplicationName(dc);
        Application application = applications.getApplication(appName);
        ApplicationInfo appInfo = appRegistry.get(appName);
        if(application != null && appInfo != null){
            Resources appScopedResources = application.getResources();
            if(appScopedResources != null){
                appInfo.addTransientAppMetaData(Application.APPLICATION, application);
            }

            List<Module> modules = application.getModule();
            if(modules != null){
                for(Module module : modules){
                    Resources moduleScopedResources = module.getResources();
                    if(moduleScopedResources != null){
                        appInfo.addTransientAppMetaData(module.getName()+"-resources", moduleScopedResources);
                    }
                }
            }
        }
    }

    public static void createResources(DeploymentContext dc, boolean embedded) throws ResourceException {
        String appName = getApplicationName(dc);
        Application app = dc.getTransientAppMetaData(Application.APPLICATION, Application.class);
        Map<String, Map<String, List>> resourcesList =
                (Map<String, Map<String, List>>)dc.getTransientAppMetadata().get(ConnectorConstants.APP_SCOPED_RESOURCES_MAP);

        if (resourcesList != null) {
            Map<String, List> appLevelResources = resourcesList.get(appName);
            if (appLevelResources != null) {
                List<org.glassfish.resource.common.Resource> connectorResources =
                        appLevelResources.get(ConnectorConstants.CONNECTOR_RESOURCES);

                createAppScopedResources(app, connectorResources, dc, embedded);

                List<org.glassfish.resource.common.Resource> nonConnectorResources =
                        appLevelResources.get(ConnectorConstants.NON_CONNECTOR_RESOURCES);

                createAppScopedResources(app, nonConnectorResources, dc, embedded);

            }
            List<Module> modules = app.getModule();
            if (modules != null) {
                for (Module module : modules) {
                    String actualModuleName = getActualModuleName(module.getName());
                    //create resources for modules, ignore standalone applications where
                    //module name will be the same as app name
                    if(!appName.equals(actualModuleName)){
                        Map<String, List> moduleResources = resourcesList.get(actualModuleName);
                        if (moduleResources != null) {
                            List<org.glassfish.resource.common.Resource> connectorResources =
                                    moduleResources.get(ConnectorConstants.CONNECTOR_RESOURCES);
                            createModuleScopedResources(app, module, connectorResources, dc, embedded);

                            List<org.glassfish.resource.common.Resource> nonConnectorResources =
                                    moduleResources.get(ConnectorConstants.NON_CONNECTOR_RESOURCES);
                            createModuleScopedResources(app, module, nonConnectorResources, dc, embedded);
                        }
                    }
                }
            }
        }
    }

    private static String getActualModuleName(String moduleName) {
        String actualModuleName = moduleName;
        if(moduleName.endsWith("_war")){
            int index = moduleName.lastIndexOf("_war");
            actualModuleName = moduleName.substring(0, index) + ".war";  
        }else if(moduleName.endsWith("_rar")){
            int index = moduleName.lastIndexOf("_rar");
            actualModuleName = moduleName.substring(0, index) + ".rar";
        }else if(moduleName.endsWith("_jar")){
            int index = moduleName.lastIndexOf("_jar");
            actualModuleName = moduleName.substring(0, index) + ".jar";
        }
        return actualModuleName;
    }

    private static Collection<com.sun.enterprise.config.serverbeans.Resource>
    createConfig(Resources resources, Iterator<org.glassfish.resource.common.Resource> resourcesToRegister, boolean embedded)
    throws ResourceException {
        List<com.sun.enterprise.config.serverbeans.Resource> resourceConfigs =
                new ArrayList<com.sun.enterprise.config.serverbeans.Resource>();
        while (resourcesToRegister.hasNext()) {
            org.glassfish.resource.common.Resource resource = resourcesToRegister.next();
            final HashMap attrList = resource.getAttributes();
            final Properties props = resource.getProperties();
            String desc = resource.getDescription();
            if (desc != null) {
                attrList.put("description", desc);
            }

            try {
                final ResourceManager rm = resourceFactory.getResourceManager(resource);
                com.sun.enterprise.config.serverbeans.Resource configBeanResource =
                        rm.createConfigBean(resources, attrList, props);
                if (configBeanResource != null) {
                    if(embedded && isEmbeddedRarResource(configBeanResource, resources.getResources())== ConnectorConstants.TriState.yes){
                        resources.getResources().add(configBeanResource);
                        resourceConfigs.add(configBeanResource);
                    }else if(!embedded && isEmbeddedRarResource(configBeanResource, resources.getResources())== ConnectorConstants.TriState.no){
                        resources.getResources().add(configBeanResource);
                        resourceConfigs.add(configBeanResource);
                    }
                }
            } catch (Exception e) {
                throw new ResourceException(e);
            }
        }
        return resourceConfigs;
    }


    private static void createAppScopedResources(Application app, List<org.glassfish.resource.common.Resource> resources,
                                                 DeploymentContext dc, boolean embedded) throws ResourceException {
        try {
            if (resources != null) {
                Application application = dc.getTransientAppMetaData(Application.APPLICATION, Application.class);
                Resources asc = dc.getTransientAppMetaData(ConnectorConstants.APP_META_DATA_RESOURCES, Resources.class);
                if (asc == null) {
                    asc = application.createChild(Resources.class);
                    application.setResources(asc);
                    dc.addTransientAppMetaData(ConnectorConstants.APP_META_DATA_RESOURCES, asc);
                }

                Collection<Resource> resourceConfigurations =
                        createConfig(asc, resources.iterator(), embedded);
                deployResources(app.getName(), null, resourceConfigurations, embedded);
            }
        } catch (Exception e) {
            Object params[] = new Object[]{app.getName(), e};
            _logger.log(Level.SEVERE, "gf.resources.app.scope.deployment.failure", params);
            throw new ResourceException(e);
        }
    }

    private static void createModuleScopedResources(Application app, Module module,
                                                    List<org.glassfish.resource.common.Resource> resources,
                                                    DeploymentContext dc, boolean embedded) throws ResourceException {
        try {
            if (resources != null) {
                Resources msc = dc.getTransientAppMetaData(module.getName()+"-resources", Resources.class);
                if (msc == null) {
                    msc = module.createChild(Resources.class);
                    module.setResources(msc);
                    dc.addTransientAppMetaData(module.getName()+"-resources", msc);
                    ApplicationInfo appInfo = appRegistry.get(app.getName());
                    if(appInfo != null){
                        appInfo.addTransientAppMetaData(module.getName()+"-resources", msc);
                    }
                }

                Collection<Resource> resourceConfigurations =
                        createConfig(msc, resources.iterator(), embedded);
                deployResources(app.getName(), module.getName(), resourceConfigurations, embedded);
            }
        } catch (Exception e) {
            Object params[] = new Object[]{module.getName(),app.getName(), e};
            _logger.log(Level.SEVERE, "gf.resources.module.scope.deployment.failure", params);
            throw new ResourceException(e);
        }
    }

    public static void deployResources(String applicationName, String moduleName,
                                Collection<com.sun.enterprise.config.serverbeans.Resource> configBeanResources,
                                boolean embedded)
            throws Exception {
        for(com.sun.enterprise.config.serverbeans.Resource configBeanResource : configBeanResources){
            if(configBeanResource instanceof ResourcePool){
                ResourcePool resourcePool = (ResourcePool)configBeanResource;

                if(embedded){
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources) == ConnectorConstants.TriState.yes){
                        getResourceDeployer(resourcePool).deployResource(resourcePool, applicationName, moduleName);
                    }
                }else{
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources) == ConnectorConstants.TriState.no){
                        getResourceDeployer(resourcePool).deployResource(resourcePool, applicationName, moduleName);
                    }
                }
            }else if(configBeanResource instanceof BindableResource) {
                BindableResource resource = (BindableResource)configBeanResource;
                ResourceInfo resourceInfo = new ResourceInfo(resource.getJndiName(), applicationName, moduleName);
                if(embedded){
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources) == ConnectorConstants.TriState.yes){
                        resourcesBinder.deployResource(resourceInfo, resource);
                    }
                }else{
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources)== ConnectorConstants.TriState.no){
                        resourcesBinder.deployResource(resourceInfo, resource);
                    }
                }
            }else{
                if(embedded){
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources)== ConnectorConstants.TriState.yes){
                        //work-security-map, resource-adapter-config
                        getResourceDeployer(configBeanResource).deployResource(configBeanResource);
                    }
                }else{
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources)== ConnectorConstants.TriState.no){
                        //work-security-map, resource-adapter-config
                        getResourceDeployer(configBeanResource).deployResource(configBeanResource);
                    }
                }
            }
        }
    }

    //TODO what if the module being deployed is a RAR and has gf-resources.xml ?
    //TODO can the RAR define its own resources ? eg: connector-resource, pool, a-o-r ?
    public static ConnectorConstants.TriState
    isEmbeddedRarResource(com.sun.enterprise.config.serverbeans.Resource configBeanResource,
                                          Collection<com.sun.enterprise.config.serverbeans.Resource> configBeanResources) {
        //boolean result = false;
        ConnectorConstants.TriState result = ConnectorConstants.TriState.no;
        if(configBeanResource instanceof ConnectorResource){
            String poolName = ((ConnectorResource)configBeanResource).getPoolName();
            ConnectorConnectionPool pool = getPool(configBeanResources, poolName);
            if(pool != null){
                if(pool.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                    result = ConnectorConstants.TriState.yes;
                }
            }else{
                result = ConnectorConstants.TriState.unknown;
            }
        }else if(configBeanResource instanceof AdminObjectResource){
            AdminObjectResource aor = (AdminObjectResource)configBeanResource;
            if(aor.getResAdapter().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                result = ConnectorConstants.TriState.yes;
            }
        }else if (configBeanResource instanceof ConnectorConnectionPool){
            ConnectorConnectionPool ccp = (ConnectorConnectionPool)configBeanResource;
            if(ccp.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                result = ConnectorConstants.TriState.yes;
            }
        }else if (configBeanResource instanceof WorkSecurityMap){
            WorkSecurityMap wsm = (WorkSecurityMap)configBeanResource;
            if(wsm.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                result = ConnectorConstants.TriState.yes;
            }
        }/*else if (configBeanResource instanceof ResourceAdapterConfig){
            ResourceAdapterConfig rac = (ResourceAdapterConfig)configBeanResource;
            result = rac.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
        }*/
        return result;
    }

    public static ConnectorConnectionPool getPool(Collection<com.sun.enterprise.config.serverbeans.Resource> configBeanResources,
                                            String poolName) {
        ConnectorConnectionPool result = null;
        for(com.sun.enterprise.config.serverbeans.Resource res : configBeanResources){
            if(res instanceof ConnectorConnectionPool){
                if(((ConnectorConnectionPool)res).getName().equals(poolName)){
                    result = (ConnectorConnectionPool)res;
                    break;
                }
            }
        }
        return result;
    }

    private static String getApplicationName(DeploymentContext dc) {
        final DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
        return commandParams.name();
    }

    public void retrieveAllResourcesXMLs(Map<String, String> fileNames, String path) throws IOException {
        File file = new File(path);
        File[] files = file.listFiles();
        if(files != null){
            for(int i=0; i < files.length ; i++){
                String entry = files[i].getAbsolutePath();
                if (entry.endsWith("META-INF/glassfish-resources.xml")) {
                    if (!files[i].isDirectory()) {
                        String name = file.getParentFile().getName();
                        fileNames.put(name, files[i].getAbsolutePath());
                    }
                } else if(entry.endsWith("WEB-INF/glassfish-resources.xml")){
                    //TODO ASR : need to make sure that this is indeed *_war directory
                    if (!files[i].isDirectory()) {
                        String name = file.getParentFile().getName();
                        fileNames.put(name, files[i].getAbsolutePath());
                    }
                } else if(files[i].isDirectory()){
                    //TODO ASR : no need to do multi-level recursion ? (first level is sufficient ?)
                    retrieveAllResourcesXMLs(fileNames, files[i].getAbsolutePath());
                }
            }
        }
    }

    /**
     * Given a <i>resource</i> instance, appropriate deployer will be provided
     *
     * @param resource resource instance
     * @return ResourceDeployer
     */
    private static ResourceDeployer getResourceDeployer(Object resource){
        Collection<ResourceDeployer> deployers = habitat.getAllByContract(ResourceDeployer.class);

        for(ResourceDeployer deployer : deployers){
            if(deployer.handles(resource)){
                return deployer;
            }
        }
        return null;
    }

    /**
     * event listener to listen to </code>resource-adapter undeploy validation</code> and
     * to validate the undeployment. Undeployment will fail, if resources are found
     * and --cascade is not set.
     * @param event Event
     */
    public void event(org.glassfish.api.event.EventListener.Event event) {
        if (Deployment.DEPLOYMENT_BEFORE_CLASSLOADER_CREATION.equals(event.type())) {
            DeploymentContext dc = (DeploymentContext) event.hook();
            final DeployCommandParameters deployParams = dc.getCommandParameters(DeployCommandParameters.class);
            try{
                if (deployParams.origin == OpsParams.Origin.deploy) {
                    processArchive(dc);
                    createResources(dc, false);
                }else if(deployParams.origin == OpsParams.Origin.load){
                    populateResourceConfigInAppInfo(dc);
                }
            }catch(Exception e){
                // only DeploymentExceptions are propagated and result in deployment failure
                // in the event notification infrastructure
                throw new DeploymentException(e);
            }
        }
    }

    private static void debug(String message){
        _logger.finest("[ASR] ResourceDeployer : " + message);
    }
}
