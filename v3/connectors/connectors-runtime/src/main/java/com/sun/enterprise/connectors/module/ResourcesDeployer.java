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
 * file and include the License file at packager/legal/LICENSE.txt.
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
import com.sun.appserv.connectors.internal.api.ConnectorConstants.TriState;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.resource.ResourceUtilities;
import com.sun.logging.LogDomains;
import org.glassfish.admin.cli.resources.*;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.*;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DeploymentProperties;
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

/**
 * ResourcesDeployer to handle "glassfish-resources.xml(s)" bundled in the application.
 *
 * @author Jagadish Ramu
 */
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

    private static Map<String, Map<String, Resources>> resourceConfigurations =
            new HashMap<String, Map<String, Resources>>();
    private static Map<String, Application> preservedApps = new HashMap<String, Application>();

    private static Logger _logger = LogDomains.getLogger(ConnectorDeployer.class, LogDomains.RSR_LOGGER);

    private static final String RESOURCES_XML_META_INF = "META-INF/glassfish-resources.xml";
    private static final String RESOURCES_XML_WEB_INF = "WEB-INF/glassfish-resources.xml";


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
        application.setApplicationName(getAppNameFromDeployCmdParams(context));
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

                String appName = getAppNameFromDeployCmdParams(dc);
                //using appName as it is possible that "deploy --name=APPNAME" will
                //be different than the archive name.
                retrieveAllResourcesXMLs(fileNames, archive, appName);

                for (String moduleName: fileNames.keySet()) {
                    String fileName = fileNames.get(moduleName);
                    debug("Sun Resources XML : " + fileName);

                    moduleName = getActualModuleName(moduleName);
                    String scope ;
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

    /**
     * retain old resource configuration for the new archive being deployed.
     * @param dc DeploymentContext
     * @param allResources all resources (app scoped, module scoped) of old application
     * @throws Exception when unable to retain old resource configuration.
     */
    public static void retainResourceConfig(DeploymentContext dc, Map<String, Resources> allResources) throws Exception {
        String appName = getAppNameFromDeployCmdParams(dc);
        Application application = dc.getTransientAppMetaData(Application.APPLICATION, Application.class);
        Resources appScopedResources = allResources.get(appName);

        if(appScopedResources != null){
            application.setResources(appScopedResources);
        }

        if(DeploymentUtils.isEAR(dc.getSource())){
            List<Module> modules = application.getModule();
            if(modules != null){
                for(Module module : modules){
                    Resources moduleScopedResources = allResources.get(module.getName());
                    if(moduleScopedResources != null){
                        module.setResources(moduleScopedResources);
                    }
                }
            }
        }
    }

    /**
     * During "load()" event (eg: app/app-ref enable, server start),
     * populate resource-config in app-info so that it can be used for
     * constructing connector-classloader for the application.
     * @param dc DeploymentContext
     */
    public static void populateResourceConfigInAppInfo(DeploymentContext dc){
        String appName = getAppNameFromDeployCmdParams(dc);
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
        String appName = getAppNameFromDeployCmdParams(dc);
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
    createConfig(Resources resources, Iterator<org.glassfish.resource.common.Resource> resourcesToRegister,
                 boolean embedded)
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
                    if(embedded &&
                            isEmbeddedRarResource(configBeanResource, resources.getResources()) == TriState.TRUE){
                        resources.getResources().add(configBeanResource);
                        resourceConfigs.add(configBeanResource);
                    }else if(!embedded &&
                            isEmbeddedRarResource(configBeanResource, resources.getResources()) == TriState.FALSE){
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
                                boolean embedded) throws Exception {
        for(com.sun.enterprise.config.serverbeans.Resource configBeanResource : configBeanResources){
            if(configBeanResource instanceof ResourcePool){
/*
                ResourcePool resourcePool = (ResourcePool)configBeanResource;

                if(embedded){
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources) == TriState.TRUE){
                        getResourceDeployer(resourcePool).deployResource(resourcePool, applicationName, moduleName);
                    }
                }else{
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources) == TriState.FALSE){
                        getResourceDeployer(resourcePool).deployResource(resourcePool, applicationName, moduleName);
                    }
                }
*/
            }else if(configBeanResource instanceof BindableResource) {
                BindableResource resource = (BindableResource)configBeanResource;
                ResourceInfo resourceInfo = new ResourceInfo(resource.getJndiName(), applicationName, moduleName);
                if(embedded){
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources) == TriState.TRUE){
                        resourcesBinder.deployResource(resourceInfo, resource);
                    }
                }else{
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources)== TriState.FALSE){
                        resourcesBinder.deployResource(resourceInfo, resource);
                    }
                }
            }else{
                if(embedded){
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources)== TriState.TRUE){
                        //work-security-map, resource-adapter-config
                        getResourceDeployer(configBeanResource).deployResource(configBeanResource);
                    }
                }else{
                    if(isEmbeddedRarResource(configBeanResource, configBeanResources)== TriState.FALSE){
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
        TriState result = TriState.FALSE;
        if(configBeanResource instanceof ConnectorResource){
            String poolName = ((ConnectorResource)configBeanResource).getPoolName();
            ConnectorConnectionPool pool = getPool(configBeanResources, poolName);
            if(pool != null){
                if(pool.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                    result = TriState.TRUE;
                }
            }else{
                result = TriState.UNKNOWN;
            }
        }else if(configBeanResource instanceof AdminObjectResource){
            AdminObjectResource aor = (AdminObjectResource)configBeanResource;
            if(aor.getResAdapter().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                result = TriState.TRUE;
            }
        }else if (configBeanResource instanceof ConnectorConnectionPool){
            ConnectorConnectionPool ccp = (ConnectorConnectionPool)configBeanResource;
            if(ccp.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                result = TriState.TRUE;
            }
        }else if (configBeanResource instanceof WorkSecurityMap){
            WorkSecurityMap wsm = (WorkSecurityMap)configBeanResource;
            if(wsm.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)){
                result = TriState.TRUE;
            }
        }/*else if (configBeanResource instanceof ResourceAdapterConfig){
            ResourceAdapterConfig rac = (ResourceAdapterConfig)configBeanResource;
            result = rac.getResourceAdapterName().contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
        }*/
        return result;
    }

    public static ConnectorConnectionPool getPool(
            Collection<com.sun.enterprise.config.serverbeans.Resource> configBeanResources, String poolName) {
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

    private static String getAppNameFromDeployCmdParams(DeploymentContext dc) {
        final DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
        return commandParams.name();
    }

    public void retrieveAllResourcesXMLs(Map<String, String> fileNames, ReadableArchive archive,
                                         String actualArchiveName) throws IOException {

        if(DeploymentUtils.isEAR(archive)){
            //Look for top-level META-INF/glassfish-resources.xml
            if(archive.exists(RESOURCES_XML_META_INF)){
                String archivePath = archive.getURI().getPath();
                String fileName = archivePath + RESOURCES_XML_META_INF;
                if(_logger.isLoggable(Level.FINEST)){
                    _logger.finest("GlassFish-Resources Deployer - fileName : " + fileName +
                            " - parent : " + archive.getName());
                }
                fileNames.put(actualArchiveName, fileName);
            }

            //Lok for sub-module level META-INF/glassfish-resources.xml and WEB-INF/glassfish-resources.xml
            Enumeration<String> entries = archive.entries();
            while(entries.hasMoreElements()){
                String element = entries.nextElement();
                if(element.endsWith(".jar") || element.endsWith(".war") || element.endsWith(".rar") ||
                        element.endsWith("_jar") || element.endsWith("_war") || element.endsWith("_rar")){
                    ReadableArchive subArchive = archive.getSubArchive(element);
                    if(subArchive != null ){
                        retrieveResourcesXMLFromArchive(fileNames, subArchive, subArchive.getName());
                    }
                }
            }
        }else{
            //Look for standalone archive's META-INF/glassfish-resources.xml and WEB-INF/glassfish-resources.xml
            retrieveResourcesXMLFromArchive(fileNames, archive, actualArchiveName);
        }
    }

    private void retrieveResourcesXMLFromArchive(Map<String, String> fileNames, ReadableArchive archive,
                                                 String actualArchiveName) {
        if(DeploymentUtils.hasResourcesXML(archive)){
            String archivePath = archive.getURI().getPath();
            String fileName ;
            if(DeploymentUtils.isWebArchive(archive)){
                fileName = archivePath +  RESOURCES_XML_WEB_INF;
            }else{
                fileName = archivePath + RESOURCES_XML_META_INF;
            }
            if(_logger.isLoggable(Level.FINEST)){
                _logger.finest("GlassFish-Resources Deployer - fileName : " + fileName +
                        " - parent : " + archive.getName());
            }

            fileNames.put(actualArchiveName, fileName);
        }
    }

    /*
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
                    if (!files[i].isDirectory()) {
                        String name = file.getParentFile().getName();
                        fileNames.put(name, files[i].getAbsolutePath());
                    }
                } else if(files[i].isDirectory()){
                    retrieveAllResourcesXMLs(fileNames, files[i].getAbsolutePath());
                }
            }
        }
    }
    */

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
     * Event listener to listen to </code>application undeploy validation</code> and
     * if <i>preserveResources</i> flag is set, cache the &lt;resources&gt;
     * config for persisting it in domain.xml
     */
    public void event(Event event) {
        if (event.is(Deployment.DEPLOYMENT_BEFORE_CLASSLOADER_CREATION)) {
            DeploymentContext dc = (DeploymentContext) event.hook();
            final DeployCommandParameters deployParams = dc.getCommandParameters(DeployCommandParameters.class);
            processResources(dc, deployParams);
        }else if(event.is(Deployment.UNDEPLOYMENT_VALIDATION)){
            DeploymentContext dc = (DeploymentContext) event.hook();
            final UndeployCommandParameters undeployCommandParameters =
                    dc.getCommandParameters(UndeployCommandParameters.class);
            preserveResources(dc, undeployCommandParameters);
        }/*else if(Deployment.UNDEPLOYMENT_FAILURE.equals(event.type())){
            DeploymentContext dc = (DeploymentContext) event.hook();
            cleanupPreservedResources(dc, event);
        }else if(Deployment.DEPLOYMENT_FAILURE.equals(event.type())){
            DeploymentContext dc = (DeploymentContext) event.hook();
            cleanupPreservedResources(dc, event);
        }*/
    }

    private void processResources(DeploymentContext dc, DeployCommandParameters deployParams) {
        try{
            if (deployParams.origin == OpsParams.Origin.deploy) {
                Properties properties = deployParams.properties;
                if(properties != null){
                    //handle if "preserveAppScopedResources" property is set (during deploy --force=true)
                    //TODO ASR need to check whether the call is "force=true" or re-deploy
                    String preserve = properties.getProperty(DeploymentProperties.PRESERVE_APP_SCOPED_RESOURCES);
                    if(preserve != null && Boolean.valueOf(preserve)){
                        String appName = getAppNameFromDeployCmdParams(dc);
                        Map<String, Resources> allResources = resourceConfigurations.remove(appName);
                        Application oldApp = preservedApps.remove(appName);
                        if(allResources != null && oldApp != null){
                            Application application = dc.getTransientAppMetaData(Application.APPLICATION, Application.class);
                            validatePreservedResources(allResources, oldApp, application);
                            retainResourceConfig(dc, allResources);
                        }
                        return ;
                    }
                }

                processArchive(dc);
                createResources(dc, false);
            }else if(deployParams.origin == OpsParams.Origin.load){
                //during load event (ie., app/app-ref enable or server start, resource configuration
                //is present in domain.xml. Use the configuration.
                populateResourceConfigInAppInfo(dc);
            }
        }catch(Exception e){
            // only DeploymentExceptions are propagated and result in deployment failure
            // in the event notification infrastructure
            throw new DeploymentException(e);
        }
    }

    /**
     * Validates the old resource configuration against new archive's modules.
     * @param allResources all resources (app scoped, module scoped)
     * @param oldApp Old Application config
     * @param newApp New Applicatoin config
     * @throws ResourceConflictException when it is not possible to map any of the resource(s) to 
     * new application/its modules
     */
    private void validatePreservedResources(Map<String, Resources> allResources, Application oldApp,
                                               Application newApp) throws ResourceConflictException {
        //check whether old app has any RAR
        List<Module> oldRARModules = new ArrayList<Module>();
        List<Module> oldModules = oldApp.getModule();
        for (Module oldModule : oldModules) {
            if (oldModule.getEngine(ConnectorConstants.CONNECTOR_MODULE) != null) {
                oldRARModules.add(oldModule);
            }
        }

/*
       //check whether new app has any RAR
       //TODO ASR : <sniffer> info is not available during initial phase of deployment. Hence doing "module-name" check.
       List<Module> newRARModules = new ArrayList<Module>();
        List<Module> newModules = newApp.getModule();
        for (Module newModule : newModules) {
            if (newModule.getEngine(ConnectorConstants.CONNECTOR_MODULE) != null) {
                newRARModules.add(newModule);
            }
        }
*/
        List<Module> newRARModules = newApp.getModule();


        //check whether all old RARs are present in new RARs list.
        List<Module> staleRars = new ArrayList<Module>();
        for (Module oldRARModule : oldRARModules) {
            String oldRARModuleName = oldRARModule.getName();
            boolean found = false;
            for (Module newRARModule : newRARModules) {
                String newRARModuleName = newRARModule.getName();
                if (newRARModuleName.equals(oldRARModuleName)) {
                    found = true;
                }
            }
            if(!found){
                staleRars.add(oldRARModule);
            }
        }

        String appName = newApp.getName();
        if (staleRars.size() > 0) {
            Resources appScopedResources = allResources.get(appName);
            if (appScopedResources != null) {
                validateResourcesForStaleReference(appName, staleRars, appScopedResources);
            }

            List<Module> newModules = newApp.getModule();
            for(Module newModule : newModules){
                Module oldModule = oldApp.getModule(newModule.getName());
                if(oldModule != null){
                    Resources oldModuleResources = oldModule.getResources();
                    if(oldModuleResources != null){
                        validateResourcesForStaleReference(appName, staleRars, oldModuleResources);
                    }
                }//else its a new module in the archive being redeployed.
            }
        }
    }

    /**
     * Validates whether the old application has RARs and those are retained in new application.<br>
     * If the new application does not have any of the old application's RAR, validates whether<br>
     * any module is using the RAR's resources. If used, fail with ResourceConflictException<br>
     * as the RAR's resource is not valid anymore.
     * @param appName application-name
     * @param staleRars List of Stale Resource Adapters (ie., were defined in old app, not in new app)
     * @param resources resources that need to be checked for stale RAR references.
     * @throws ResourceConflictException When any of the resource has reference to old RAR
     */
    private void validateResourcesForStaleReference(String appName, List<Module> staleRars, Resources resources)
            throws ResourceConflictException{
        boolean found = false;
        for (Resource resource : resources.getResources()) {
            ResourcesUtil resourcesUtil = ResourcesUtil.createInstance();
            //connector type of resource may be : connector-resource, ccp, aor, wsm, rac
            if (resourcesUtil.isRARResource(resource)) {
                String rarNameOfResource = resourcesUtil.getRarNameOfResource(resource, resources);
                if (rarNameOfResource.contains(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER)) {
                    String embeddedRARName = ConnectorsUtil.getRarNameFromApplication(rarNameOfResource);
                    for (Module module : staleRars) {
                        //check whether these RARs are referenced by app-scoped-resources ?
                        if (getActualModuleName(module.getName()).equals(embeddedRARName)) {
                            throw new ResourceConflictException("Existing resources refer RAR " +
                                    "[ " + embeddedRARName + " ] which is" +
                                    "not present in the re-deployed application ["+appName+"] anymore. " +
                                    "re-deploy the application after resolving the conflicts");
                        }
                    }
                }
            }
        }
    }

/*
    private void cleanupPreservedResources(DeploymentContext dc, Event event) {
        if (Deployment.DEPLOYMENT_FAILURE.equals(event.type())) {
            final DeployCommandParameters deployCommandParameters =
                    dc.getCommandParameters(DeployCommandParameters.class);
            if (deployCommandParameters.origin == OpsParams.Origin.deploy) {
                Properties properties = deployCommandParameters.properties;
                String appName = deployCommandParameters.name();
                cleanupPreservedResources(appName, properties);
            }
        } else if (Deployment.UNDEPLOYMENT_FAILURE.equals(event.type())) {
            final UndeployCommandParameters undeployCommandParameters =
                    dc.getCommandParameters(UndeployCommandParameters.class);
            if (undeployCommandParameters.origin == OpsParams.Origin.undeploy) {
                Properties properties = undeployCommandParameters.properties;
                String appName = undeployCommandParameters.name();
                cleanupPreservedResources(appName, properties);
            }
        }
    }

    private void cleanupPreservedResources(String appName, Properties properties) {
        if(properties != null){
            String preserve = properties.getProperty(DeploymentProperties.PRESERVE_APP_SCOPED_RESOURCES);
            if(preserve != null && Boolean.valueOf(preserve)){
                resourceConfigurations.remove(appName);
                preservedApps.remove(appName);
            }
        }
    }
*/

    /**
     * preserve the old application's resources so that they can be registered during deploy.
     * @param dc DeploymentContext
     * @param undeployCommandParameters undeploy command parameters
     */
    private void preserveResources(DeploymentContext dc, UndeployCommandParameters undeployCommandParameters) {
        try{
            if (undeployCommandParameters.origin == OpsParams.Origin.undeploy) {
                Properties properties = undeployCommandParameters.properties;
                if(properties != null){
                    String preserve = properties.getProperty(DeploymentProperties.PRESERVE_APP_SCOPED_RESOURCES);
                    if(preserve != null && Boolean.valueOf(preserve)){
                        debug("Preserve app scoped resources enabled");
                        Map<String, Resources> allResources = new HashMap<String, Resources>();
                        final UndeployCommandParameters commandParams =
                                dc.getCommandParameters(UndeployCommandParameters.class);
                        String appName = commandParams.name();
                        Application app = applications.getApplication(appName);
                        Resources appScopedResources = app.getResources();
                        if(appScopedResources != null){
                            allResources.put(appName, appScopedResources);
                        }
                        List<Module> modules = app.getModule();
                        if(modules != null){
                            for(Module module : modules){
                                Resources moduleScopedResources = module.getResources();
                                if(moduleScopedResources != null){
                                    allResources.put(module.getName(), moduleScopedResources);
                                }
                            }
                        }
                        //store the resource-configuration and application (for module information ie., sniffer type)
                        resourceConfigurations.put(appName, allResources);
                        preservedApps.put(appName, app);
                    }
                }
            }
        }catch(Exception e){
            // only DeploymentExceptions are propagated and result in deployment failure
            // in the event notification infrastructure
            throw new DeploymentException(e.getMessage(), e);
        }
    }

    private static void debug(String message){
        _logger.finest("[ASR] ResourceDeployer : " + message);
    }
}
