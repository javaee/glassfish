/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.admin.common.exception.DeploymentException;
import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.server.core.AdminNotificationHelper;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.server.core.DeploymentNotificationHelper;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.backend.ClientJarMakerRegistry;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentRequestRegistry;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.interfaces.DeploymentImplConstants;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.resource.Resource;
import com.sun.enterprise.resource.ResourcesXMLParser;
import com.sun.enterprise.resource.SunResourcesXML;
import com.sun.enterprise.resource.ResourceUtilities;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashMap;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;

import org.netbeans.modules.schema2beans.Common;

public class DeploymentServiceUtils {

    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();

    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( DeploymentServiceUtils.class );

    private static final Object[] emptyParams = new Object[]{};
    private static final String[] emptySignature = new String[]{};

    /**
     * @return the path for the client jar file of a deployed application
     */
    public static String getClientJarPath(String moduleID) {
        
        // let's ensure first that our client jar is ready.
        ClientJarMakerRegistry registry = ClientJarMakerRegistry.getInstance();
        
        if (registry.isRegistered(moduleID)) {
            
            // let's wait until it is finished.
            registry.waitForCompletion(moduleID);
        }
        
        return moduleID + DeploymentImplConstants.ClientJarSuffix;
        
    }

    /**
     * This method is used by StartPhase, StopPhases to multicast start/stop
     * events to the listeners. To multicast to a particular server the managedServerInstance
     * representing that particular server is used
     * @param eventType APPLICATION_DEPLOYED/APPLICATION_UNDEPLOYED/APPLICATION_REDEPLOYED
     *                  MODULE_DEPLOYED/MODULE_REDEPLOYED/MODULE_UNDEPLOYED
     * @param entityName app/module name
     * @param targetName server to which event has to be sent
     */
    static boolean multicastEvent(int eventType, String entityName, 
				  String targetName) throws IASDeploymentException {
        return multicastEvent(eventType, entityName, null, targetName);
    }

    static boolean multicastEvent(int eventType, String entityName, 
				  String moduleType, String targetName) 
	throws IASDeploymentException {
        return multicastEvent(eventType,entityName,moduleType, false, targetName);
    }
    
    /**
     * This method is used by StartPhase, StopPhases to multicast start/stop
     * events to the listeners. To multicast to a particular server the managedServerInstance
     * representing that particular server is used
     * @param eventType APPLICATION_DEPLOYED/APPLICATION_UNDEPLOYED/APPLICATION_REDEPLOYED
     *                  MODULE_DEPLOYED/MODULE_REDEPLOYED/MODULE_UNDEPLOYED
     * @param entityName app/module name
     * @param moduleType ejb/web/connector
     * @param targetName server to which event has to be sent
     */
    static boolean multicastEvent(int eventType, String entityName, 
				  String moduleType, boolean cascade, String targetName) throws IASDeploymentException {
        return multicastEvent(eventType, entityName, moduleType, cascade, false, targetName) ;
    }

    static boolean multicastEvent(int eventType, String entityName,
                                  String moduleType, boolean cascade, boolean forceDeploy, String targetName) throws IASDeploymentException {
        return multicastEvent(eventType, entityName, moduleType, cascade, forceDeploy, Constants.LOAD_UNSET, targetName) ;
    }
    
    public static boolean multicastEvent(int eventType, String entityName, 
				  String moduleType, boolean cascade, boolean forceDeploy, int loadUnloadAction, String targetName) 
	throws IASDeploymentException {

        // Flush config changes before sending the event. The events API
        // requires that config be saved prior to sending event.
        try {
            ConfigContext config =  getConfigContext();
            if (config != null && config.isChanged()) {
                config.flush();
            } else {
                // Server is initializing. Can not flush config changes.
            }
        } catch (ConfigException ce) {
            throw new IASDeploymentException(ce);
        }
	try {
	    return DeploymentNotificationHelper.multicastEvent(eventType, entityName,
                                  moduleType, cascade, forceDeploy, loadUnloadAction, targetName);
	} catch (Throwable t) {
        IASDeploymentException iasEx = new IASDeploymentException(t.getMessage());
        iasEx.initCause(t);
        throw iasEx;
        }
        // unreachable
    }

    protected static void flushConfigAndSendEvents() throws Exception {
        AdminContext adminContext =
            AdminService.getAdminService().getAdminContext();
        ConfigContext ctx = adminContext.getAdminConfigContext();
                                                                                
        if (ctx.isChanged()) {
            ctx.flush();
        }
                                                                                
        new AdminNotificationHelper(adminContext).sendNotification();
    }

    /**
     * Get module type string using the DeployableObjectType. This string is used
     * while multicasting events to remote servers
     * @param moduleType deployableObjectType of the module
     * @return String 
     * DeployableObjectType.APP = null
     * DeployableObjectType.EJB = "ejb"
     * DeployableObjectType.WEB = "web"
     * DeployableObjectType.CONNECTOR = "connector"
     */
    public static String getModuleTypeString(DeployableObjectType moduleType)
    {
        String moduleTypeString = null;
        if (moduleType.equals(DeployableObjectType.EJB)) 
        {
            moduleTypeString = ModuleDeployEvent.TYPE_EJBMODULE;
        }
        else if(moduleType.equals(DeployableObjectType.WEB))
        {
            moduleTypeString = ModuleDeployEvent.TYPE_WEBMODULE;
        }
        else if(moduleType.equals(DeployableObjectType.CONN))
        {
            moduleTypeString = ModuleDeployEvent.TYPE_CONNECTOR;
        } 
        else if (moduleType.equals(DeployableObjectType.CAR)) 
        {
            moduleTypeString = ModuleDeployEvent.TYPE_APPCLIENT;
        }
        return moduleTypeString;
    }

    public static BaseManager getInstanceManager(
        DeployableObjectType moduleType) {
        try {
            InstanceEnvironment insEnv = 
                new InstanceEnvironment(getInstanceName());
            if (moduleType.equals(DeployableObjectType.APP)) {
                return new AppsManager(insEnv);
            } else if (moduleType.equals(DeployableObjectType.EJB)) {
                return new EjbModulesManager(insEnv);
            } else if(moduleType.equals(DeployableObjectType.WEB)) {
                return new WebModulesManager(insEnv);
            } else if(moduleType.equals(DeployableObjectType.CONN)) {
                return new ConnectorModulesManager(insEnv);
            } else if (moduleType.equals(DeployableObjectType.CAR)) {
                return new AppclientModulesManager(insEnv);
            } else {
                // invalid module type
                return null;
            }
        } catch (Exception e) {
            sLogger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }


    /** This method is for j2ee application with embedded web modules.
     *  First it tries to get application from AppsManager of DAS,
     *  if it's not already loaded there, then we load from disk.
     *  Since this method might read from disk, it could be time-consuming.
     *  The context roots in sun-application.xml will override the ones
     *  defined in application.xml.
     *  The method returns String[0] when there is no web module in the
     *  application.
     *                            
     *  @param name of the j2ee app registered in the config
     *  @return the context roots of the embedded web modules in j2ee 
     *          application
     */ 
    public static String[] getContextRootsForEmbeddedWebApp(String appName)
        throws IASDeploymentException {
        try {
            AppsManager appsManager =
                new AppsManager(new InstanceEnvironment(getInstanceName()));
            Application application =
                appsManager.getRegisteredDescriptor(appName);
            // if the application already loaded on DAS
            if (application != null) {
                ArrayList contextRoots = new ArrayList();
                for (Iterator itr = application.getWebBundleDescriptors().iterator(); itr.hasNext();) {
                    WebBundleDescriptor wbd = (WebBundleDescriptor) itr.next();
                    contextRoots.add(wbd.getContextRoot());
                }
                return (String[])contextRoots.toArray(new String[contextRoots.size()]);
            // if not, read from disk
            } else {
                // load from generated/xml dir first
                // print a warning if generated/xml dir is not there
                // and load from original dir (upgrade scenario)
                String xmlDir = appsManager.getGeneratedXMLLocation(appName);
                if (!FileUtils.safeIsDirectory(xmlDir)) {
                    String appDir = appsManager.getLocation(appName);
                    // log a warning message in the server log
                    sLogger.log(Level.WARNING, "deployment.no_xmldir",
                        new Object[]{xmlDir, appDir});
                    xmlDir = appDir;
                }
                AppDD appDD = new AppDD(new File(xmlDir));
                return appDD.getContextRoots();
            }
        } catch (Exception  e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            } else {
                throw new IASDeploymentException(e);
            }
        }
    }

    public static List<String> getTargetNamesFromTargetString (
        String targetString) {
        if (targetString == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(targetString);
        List<String> targetNames = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            targetNames.add(st.nextToken()); 
        }
        return targetNames;
    }

    public static void setResourceOptionsInRequest (DeploymentRequest req, 
        DeploymentProperties props) {
        req.setResourceAction(props.getResourceAction());
        req.setResourceTargetList(props.getResourceTargetList());
    }

    public static FilenameFilter getFilenameFilter(final String fileName){
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if ( (new File(dir, name)).isDirectory()) {
                    return true;
                } else if (name !=null && name.equals(fileName)){
                    return true;
                } else {
                    return false;
                }
            }
        };
        return filter;
    }

    private static List<Resource> getResourcesFromResourcesXML(String appId, 
        DeployableObjectType moduleType, DeploymentContext deploymentCtx) 
        throws Exception {
        // retrieve all the sun-resources.xml under the 
        // generated/xml directory
        String directoryToLook = 
            getInstanceManager(moduleType).getGeneratedXMLLocation(appId); 
        Set listOfFiles = FileUtil.getAllFilesUnder(
                new File(directoryToLook), 
            getFilenameFilter(SUN_RESOURCE_XML), true);

        // get the expected paths
        ArrayList<String> expectedXMLPaths = new ArrayList<String>();

        Application app = getInstanceManager(
            moduleType).getRegisteredDescriptor(appId);

        // The only case it could be null is during undeployment
        // and when the application is already unregistered from 
        // instance manager. In that case, get it from the deployment
        // context cache.
        if (app == null && deploymentCtx != null) {
            app = deploymentCtx.getApplication(appId);
        }

        if (app != null) {
            if (app.isVirtual()) {
                // for standalone module, it's under the root META-INF
                expectedXMLPaths.add(SUN_RESOURCE_XML_PATH);
            } else {
                // for application
                // first add the one under app root
                expectedXMLPaths.add(SUN_RESOURCE_XML_PATH);
                // then add the ones under sub module root
                for (Iterator itr = app.getModules();itr.hasNext();) {
                    ModuleDescriptor aModule =
                        (ModuleDescriptor) itr.next();
                    String moduleUri = FileUtils.makeFriendlyFileName(
                        aModule.getArchiveUri());
                    String subModulePath = moduleUri +
                       File.separator + SUN_RESOURCE_XML_PATH;
                    expectedXMLPaths.add(subModulePath);
                }
            }
        } else {
            // the default
            expectedXMLPaths.add(SUN_RESOURCE_XML_PATH);
        }
                                                                            
        // process all the sun-resources.xml and add them
        // to a list for further processing
        List<SunResourcesXML> resourcesXMLList  = 
            new ArrayList<SunResourcesXML>();
           
        for (Iterator fIter = listOfFiles.iterator(); fIter.hasNext(); ) {
            File file = (File)fIter.next();
            String filePath = file.getPath();
            String xmlFilePath =
                (new File(directoryToLook, filePath)).getAbsolutePath();
            if (expectedXMLPaths.contains(filePath)) {
                ResourcesXMLParser allResources =
                    new ResourcesXMLParser(xmlFilePath);
                SunResourcesXML sunResourcesXML = new SunResourcesXML(
                    filePath, allResources.getResourcesList());
                resourcesXMLList.add(sunResourcesXML); 
            } else {
                sLogger.log(Level.WARNING,
                    "enterprise.deployment.ignore.sun.resources.xml",
                     xmlFilePath);
            }
        }

        return processResourcesList(resourcesXMLList);
    }

    // this method calls the relevant connector backend API(s)
    // and resolve duplicates/conflicts within the archive 
    private static List<Resource> processResourcesList(
        List<SunResourcesXML> resourcesXMLList) throws Exception {
        // resolve the duplicates/conflicts within the archive
        Set<Resource> resolvedResourcesWithinArchive = 
            ResourceUtilities.resolveResourceDuplicatesConflictsWithinArchive(
                resourcesXMLList); 

        List<Resource> allResources = new ArrayList<Resource>();
        for (Resource res:  resolvedResourcesWithinArchive) {
            allResources.add(res);
        }
 
        return allResources;
    }

    //We must convert the absolute path back into a path of the
    //form ${com.sun.aas.instanceRoot}/applications/... so that
    //domain.xml is non-installation location specific.
    public static String getLocation(File appDir)
    {
        if (appDir != null) {
            return (RelativePathResolver.unresolvePath(
                        appDir.getAbsolutePath(),
                        new String[] {SystemPropertyConstants.INSTANCE_ROOT_PROPERTY,
                        SystemPropertyConstants.INSTALL_ROOT_PROPERTY}));
        } else {
            return null;
        }
    }

    private static Applications getAppsConfigBean() throws ConfigException {
        ConfigContext configContext = getConfigContext();
        Domain domainConfig = ConfigAPIHelper.getDomainConfigBean(configContext);
        Applications appsConfig = domainConfig.getApplications();
        return appsConfig;
    }
    

    // The following methods are related to get/set attributes of a deployed
    // app/module

    // Retrieving the location attribute
    public static String getLocation(String appId, DeployableObjectType type) 
        throws IASDeploymentException {
        try {
            ConfigBean module = getModule(appId, type);
            String location = module.getAttributeValue(ServerTags.LOCATION);
            return (new RelativePathResolver()).resolve(location);
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    // Retrieving the enabled attribute
    public static boolean isEnabled(String appId, DeployableObjectType type) 
        throws IASDeploymentException {
        try {
            ConfigBean module = getModule(appId, type);
            String enabled = module.getAttributeValue(ServerTags.ENABLED);
            return Boolean.valueOf(enabled).booleanValue();
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    // Retrieving the directory-deployed attribute
    public static boolean isDirectoryDeployed(String appId, 
        DeployableObjectType type) throws IASDeploymentException {
        try {
            ConfigBean module = getModule(appId, type);
            String[] attributeNames = module.getAttributeNames();
            for (String attributeName: attributeNames) {
                if (attributeName.equals(DIRECTORY_DEPLOYED_ATTR)) {
                    String directoryDeployed = 
                        module.getAttributeValue(DIRECTORY_DEPLOYED_ATTR);
                    return Boolean.valueOf(directoryDeployed).booleanValue();
                }
            } 

            return false;
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    // Retrieving the object-type attribute
    public static String getObjectType(String appId, DeployableObjectType type) 
        throws IASDeploymentException {
        try {
            // appclient module does not have object type
            if (type.isCAR()) {
                return null;
            }
            ConfigBean module = getModule(appId, type);
            String objectType = module.getAttributeValue(ServerTags.OBJECT_TYPE);
            return objectType;
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    // Set the context-root attribute
    public static void setContextRoot(String appId, DeployableObjectType type, 
				      String contextRoot) throws IASDeploymentException {
        try {
            if (contextRoot != null && contextRoot.length() > 0) {
                ConfigBean module = getModule(appId, type);
                module.setAttributeValue(ServerTags.CONTEXT_ROOT, contextRoot);
            }
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    // Retrieving the object-type attribute
    public static boolean isSystem(String appId, DeployableObjectType type) 
        throws IASDeploymentException{
        String objectType = getObjectType(appId, type);
        if (objectType != null) {
            return objectType.startsWith(SYSTEM_PREFIX);
        } else {
            return false;
        }
    }

    private static AttributeList populateAttributeList(
        DeploymentRequest request) throws IASDeploymentException {
        AttributeList attrList = new AttributeList();

        String name = request.getName();
        // name attribute
        if (name != null && name.length() > 0 ) {
            Attribute nameAttr = new Attribute(ServerTags.NAME, name);
            attrList.add(nameAttr);
        }

        // location attribute
        String location = getLocation(request.getDeployedDirectory());
        if (location != null && location.length() > 0 ) {
            Attribute locationAttr = new Attribute(ServerTags.LOCATION, location);
            attrList.add(locationAttr);
        }

        // description attribute
        String description = request.getDescription();
        if (description != null && description.length() > 0 ) {
            Attribute descriptionAttr =
                new Attribute(ServerTags.DESCRIPTION, description);
            attrList.add(descriptionAttr);
        }

        // enable attribute does not apply to app client module
        if (! request.getType().isCAR()) {
            String enabled = String.valueOf(request.isStartOnDeploy());
            if (enabled != null && enabled.length() > 0 ) {
                Attribute enableAttr = new Attribute(ServerTags.ENABLED, enabled);
                attrList.add(enableAttr);
            }
        }

        // java-web-start-enabled attribute
        // applies to application and appclient module
        if (request.getType().isAPP() || request.getType().isCAR()) {
            String jwsEnabled = 
                String.valueOf(request.isJavaWebStartEnabled());
            if (jwsEnabled != null && jwsEnabled.length() > 0 ) {
                Attribute jwsEnableAttr = new Attribute(
                    ServerTags.JAVA_WEB_START_ENABLED, jwsEnabled);
                attrList.add(jwsEnableAttr);
            }
        }

        // libraries attribute
        // applies to application, ejb, web module
        if (request.getType().isAPP() || request.getType().isEJB() || 
            request.getType().isWEB()) {
            String libraries = request.getLibraries();
            if (libraries != null && libraries.length() > 0 ) {
                Attribute librariesAttr = new Attribute(
                    ServerTags.LIBRARIES, libraries);
                attrList.add(librariesAttr);
            }
        }

        // context-root attribute only apply to web module
        if (request.getType().isWEB()) {
            String contextRoot = request.getContextRoot();
            if (contextRoot != null && contextRoot.length() > 0 ) {
                Attribute contextRootAttr =
                    new Attribute(ServerTags.CONTEXT_ROOT, contextRoot);
                attrList.add(contextRootAttr);
            }
        }

        // availability-enabled attribute does not apply to app client 
        // or connector module
        if (! request.getType().isCAR() && ! request.getType().isCONN()) {
            String availEnabled = String.valueOf(request.isAvailabilityEnabled());
            if (availEnabled!= null && availEnabled.length() > 0 ) {
                Attribute availEnabledAttr = 
                    new Attribute(ServerTags.AVAILABILITY_ENABLED, availEnabled);
                attrList.add(availEnabledAttr);
            }
        }

        // directory-deployed attribute
        String dirDeployed = String.valueOf(request.isDirectory());
        if (dirDeployed != null &&  dirDeployed.length() > 0 ) {
            Attribute  dirDeployedAttr =
                new Attribute(ServerTags.DIRECTORY_DEPLOYED, dirDeployed);
            attrList.add(dirDeployedAttr);
        }


        // other optional attributes like object-type
        Properties optionalAttributes = request.getOptionalAttributes();
        if (optionalAttributes != null) {
            // remove the virtual-server attribute which belongs
            // to application-ref
            // cloning is done so that the original Properties object
            // is not tampered with. The original Properties object is
            // referred to in a few other places.
            Properties mProps = (Properties) optionalAttributes.clone();
            try {
                mProps.remove(ServerTags.VIRTUAL_SERVERS);
            } catch(Exception re) {}

            Enumeration tags = mProps.keys();
            while(tags.hasMoreElements()) {
                String tag = (String)tags.nextElement();
                String value = mProps.getProperty(tag);
                if (tag != null && tag.length() > 0 &&
                    value != null && value.length() > 0) {
                    Attribute optionalAttr = new Attribute(tag, value);
                    attrList.add(optionalAttr);
                }
            }
        }

        return attrList;
    }

    // used when adding app/module to config
    public static void addToConfig(DeploymentRequest request) 
        throws IASDeploymentException {
        try {
            ConfigBean newApp = createNewModule(request.getType());
            DeployableObjectType type = request.getType();
            
            AttributeList attrList = populateAttributeList(request);
            for (Object obj : attrList) {
                Attribute attr = (Attribute) obj;
                newApp.setAttributeValue(attr.getName(), (String) attr.getValue());
            }
            
            setPropertyOnAppBean(newApp, request);

            addModule(request.getType(), newApp);
            
            getConfigContext().resetConfigChangeList();
        } catch (Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e; 
            } else {
                throw new IASDeploymentException(e);
            }
        }
    }

    // used when updating config
    public static void updateConfig(DeploymentRequest request)
        throws IASDeploymentException { 
        try {
            String name = request.getName();
            ConfigBean appBean =
                    ApplicationHelper.findApplication(getConfigContext(), name);
            if (appBean != null) {
                for (Iterator itr = populateAttributeList(request).iterator();
                     itr.hasNext();) {
                    Attribute attr = (Attribute)itr.next();
                    appBean.setAttributeValue(attr.getName(), 
                                              (String)attr.getValue());
                }  
                setPropertyOnAppBean(appBean, request);

                getConfigContext().flush();
                getConfigContext().resetConfigChangeList();
            }
        } catch (Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e; 
            } else {
                throw new IASDeploymentException(e);
            }
        }
    }

    // used when removing app/module to config
    public static void removeFromConfig(String name, DeployableObjectType type) 
        throws IASDeploymentException {
        try {
            ConfigBean module = getModule(name, type);
            if (module != null) {
                removeModule(type, module);
                getConfigContext().flush();
                getConfigContext().resetConfigChangeList();
            }
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    public static void setPropertyOnAppBean(ConfigBean appBean,
        DeploymentRequest request) throws ConfigException {
        if (request.isExternallyManagedApp()) {
            ElementProperty extManagedProperty = new ElementProperty();
            extManagedProperty.setName(EXTERNALLY_MANAGED);
            extManagedProperty.setValue("true");
            if (appBean instanceof J2eeApplication) {
                J2eeApplication app = (J2eeApplication) appBean;
                app.addElementProperty(extManagedProperty);
            } else if (appBean instanceof EjbModule) {
                EjbModule app = (EjbModule) appBean;
                app.addElementProperty(extManagedProperty);
            } else if (appBean instanceof WebModule) {
                WebModule app = (WebModule) appBean;
                app.addElementProperty(extManagedProperty);
            } else if (appBean instanceof AppclientModule) {
                AppclientModule app = (AppclientModule) appBean;
                app.addElementProperty(extManagedProperty);
            } else if (appBean instanceof ConnectorModule) {
                ConnectorModule app = (ConnectorModule) appBean;
                app.addElementProperty(extManagedProperty);
            }
        }
    }

    public static boolean isExternallyManagedApp(String appName,
        DeployableObjectType type) throws IASDeploymentException {
        try {
            ConfigBean appBean = getModule(appName, type);
            if (appBean != null) {
                ElementProperty extManagedProperty = null;
                if (appBean instanceof J2eeApplication) {
                    J2eeApplication app = (J2eeApplication) appBean;
                    extManagedProperty =
                        app.getElementPropertyByName(EXTERNALLY_MANAGED);
                } else if (appBean instanceof EjbModule) {
                    EjbModule app = (EjbModule) appBean;
                    extManagedProperty =
                        app.getElementPropertyByName(EXTERNALLY_MANAGED);
                } else if (appBean instanceof WebModule) {
                    WebModule app = (WebModule) appBean;
                    extManagedProperty =
                        app.getElementPropertyByName(EXTERNALLY_MANAGED);
                } else if (appBean instanceof AppclientModule) {
                    AppclientModule app = (AppclientModule) appBean;
                    extManagedProperty =
                        app.getElementPropertyByName(EXTERNALLY_MANAGED);
                } else if (appBean instanceof ConnectorModule) {
                    ConnectorModule app = (ConnectorModule) appBean;
                    extManagedProperty =
                        app.getElementPropertyByName(EXTERNALLY_MANAGED);
                }
                                                                                
                if (extManagedProperty != null) {
                    return Boolean.valueOf(
                        extManagedProperty.getValue()).booleanValue();
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    public static boolean isRegistered(String appName, 
				       DeployableObjectType type) throws IASDeploymentException {
        try {
            ConfigBean module = getModule(appName, type);
            return (module != null);
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

    public static ConfigContext getConfigContext() {
        try {
            AdminService adminService = AdminService.getAdminService();
            if (adminService != null) {
                ConfigContext config =
                        adminService.getAdminContext().getAdminConfigContext();
                return config;
            } else {
               return null;
            }
        } catch (Exception e) {
            sLogger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }   
    }       

    public static String getInstanceName() {
         return ApplicationServer.getServerContext().getInstanceName();
    }

    public static String getDomainName()
        throws IASDeploymentException {
        try {
           AdminService adminService = AdminService.getAdminService();
            if (adminService != null) {
                String domainName =
                        adminService.getAdminContext().getDomainName();
                return domainName;
            } else {
               return null;
            }
        } catch (Exception e) {
            throw new IASDeploymentException(e);
        }
    }

            
    /**
     * This method checks if the app specified is of
     * a system application/module or
     * an externally managed application/module or
     * a resource adapter having dependent resources
     * @param appID name of the application
     * @param type object type of the application
     * @param action deploy/redeploy/undeploy/enable/disable
     * @throws IASDeploymentException if application/module is
     * of type system/externally managed/resource adapter with 
     * dependent resources
     */
    public static void validate(String appID, DeployableObjectType type,
        String action, DeploymentRequest request) 
            throws IASDeploymentException {

        boolean allowSystemAppModification =
            (Boolean.valueOf(System.getProperty(
                Constants.ALLOW_SYSAPP_DEPLOYMENT, "false")).booleanValue());

        if(!allowSystemAppModification && isSystem(appID, type)) {
            String msg = localStrings.getString(
                "enterprise.deployment.phasing.component_is_system",
                 new Object[]{ action, appID });
             throw new IASDeploymentException(msg);
        }

        boolean isExternallyManagedPath = request.isExternallyManagedPath();
        if (!isExternallyManagedPath && 
            isExternallyManagedApp(appID, type)) {
                String msg = localStrings.getString("enterprise.deployment.phasing.component_is_externally_managed", new Object[] {action, appID});
                throw new IASDeploymentException( msg );
        }

        // only when this is undeployment request
        String resAction = request.getResourceAction();
        if (action.equals(PEDeploymentService.STOP_ACTION) && 
            resAction != null && 
            resAction.equals(DeploymentProperties.RES_UNDEPLOYMENT)){
            String dependentResource = 
                checkConnectorDependentResourcesForUndeploy(request);
            if (dependentResource != null) {
                String msg = localStrings.getString("enterprise.deployment.phasing.stop.dependentresexist", new Object[] {dependentResource, request.getName()});
                throw new IASDeploymentException( msg );
            }
        }
    }

    public static boolean containsResourceAdapter(DeploymentRequest req) {
        boolean containsRar = false;

        if(!req.isApplication())
        {
            if (req.isConnectorModule()) {
                containsRar = true;
            }
        } else {
            Application app = getInstanceManager(DeployableObjectType.APP
                ).getRegisteredDescriptor(req.getName());

            if ( (app != null) && (app.getRarComponentCount() != 0) ) {
                containsRar = true;
            }
        }
 
        return containsRar;
    }

    // check whether we should abort the undeployment based on whether 
    // the application being undeployed has any connector dependent resources
    // return null if no dependent connector resource are found, otherwise 
    // return the name of the first found dependent connector resource
    public static String checkConnectorDependentResourcesForUndeploy(
        DeploymentRequest req) {
        try {
            if (req.getCascade() || !containsResourceAdapter(req)) {
                return null;
            }
            List<Resource> resourceList = getResourceList(req, false, null);
            if (resourceList == null || resourceList.size() == 0) {
                return checkConnectorDependentResources(req);
            } else {
                return null;
            }
        } catch (Exception e) {
            sLogger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    // If there are dependent resources on this resource adpater, return the
    // name of the first found dependent resource. Return null if no dependent
    // resources are found.
    private static String checkConnectorDependentResources(
        DeploymentRequest req) throws Exception {
        String id = req.getName();
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        ObjectName mbeanName =
            new ObjectName("com.sun.appserv:type=resources,category=config");
                                                                                
        Object[] params = new Object[] {};
        String[] signature = new String[] {};
                                                                                
        // check admin objects
        ObjectName[] adminObjs =
            (ObjectName[]) mbs.invoke(mbeanName, LIST_ADMIN_OBJECTS,
            params, signature);
        for(int i = 0 ; i< adminObjs.length;i++) {
            String raName =
                (String)mbs.getAttribute(adminObjs[i],"res_adapter");
            if(id.equals(raName)) {
                String adminObjName =
                    (String)mbs.getAttribute(adminObjs[i], "jndi_name");
                return ("admin object [" + adminObjName + "]");
            }
        }

        // check pool and resources
        ObjectName[] poolNames = (ObjectName[])mbs.invoke(mbeanName,
            LIST_CONNECTOR_CONNECTION_POOLS, params, signature);
        for (int i = 0 ; i< poolNames.length;i++) {
            String raName = (String)mbs.getAttribute(poolNames[i],
                "resource_adapter_name");
            if(id.equals(raName)) {
                String poolName = (String)mbs.getAttribute(poolNames[i],
                    "name");
                return ("connector connection pool [" + poolName + "]");
            }
        }

       // check resource adapter config
        ObjectName[] resAdapterConfigs = (ObjectName[])mbs.invoke(mbeanName,
            LIST_RESOURCE_ADAPTER_CONFIGS, params, signature);
        for (int i = 0 ; i< resAdapterConfigs.length;i++) {
            String raName = (String)mbs.getAttribute(resAdapterConfigs[i],
                "resource_adapter_name");
            if(id.equals(raName)) {
                return ("resource adapter config");
            }
        }

        return null;
    }

    // get cached resources list if available and applicable, or else get
    // it from parsing sun-resources.xml
    public static List<Resource> getResourceList(DeploymentRequest req, 
        boolean isForceParsing, DeploymentContext deploymentCtx) 
        throws Exception {
        List<Resource> resourceList = null;

        // if resource list is null, it means it needs to
        // read in sun-resource.xml and parse it,
        // if resource list is empty, it means it's been processed
        // but no sun-resource.xml is found
        Application app = getInstanceManager(req.getType()
            ).getRegisteredDescriptor(req.getName());

        if (app == null && deploymentCtx != null) {
            app = deploymentCtx.getApplication(req.getName());
        }
                                                                                
        if (app != null && app.getResourceList() != null && 
            !isForceParsing) {  
            // first try to get the in-memory copy from application
            // always re-set the list from disk when isForceParsing is 
            // set to true
            resourceList = (List<Resource>)app.getResourceList();
        } else {
            // then try to get it from resources.xml
            // and set in the application object
            resourceList = getResourcesFromResourcesXML(
                req.getName(), req.getType(), deploymentCtx);
            if (app != null) {
                app.setResourceList(resourceList);
            }
        }
                                                                                
        return resourceList;
    }

    /**
     * This method checks if any of the virtual servers has the given web
     * module as default-web-module. If yes, it throws exception.
     * @param webModuleName the name of the web module.
     * @throws ConfigException if any of the virtual servers has this web
     * module as default-web-module.
     */
    public static void checkWebModuleReferences(String webModuleName)
    throws IASDeploymentException {
        ArrayList virtualServerIds = new ArrayList();

        try {
            Config config  = (Config) ConfigBeansFactory.getConfigBeanByXPath(
                getConfigContext(), ServerXPathHelper.XPATH_CONFIG);
            HttpService httpService = config.getHttpService();
            VirtualServer[] virtualServers = httpService.getVirtualServer();
            for (int j = 0; j < virtualServers.length; j++) {
                VirtualServer aServer   = virtualServers[j];
                String defWebModule     = aServer.getDefaultWebModule();
                if ((defWebModule != null) &&
                (defWebModule.equals(webModuleName))) {
                    virtualServerIds.add(aServer.getId());
                }
            }
        } catch (Exception e) { 
            throw new IASDeploymentException(e);
        }       
        if (!virtualServerIds.isEmpty()) {
            throw new IASDeploymentException(localStrings.getString(
                "enterprise.deployment.phasing.def_web_module_refs_exist",
                virtualServerIds.toString(), webModuleName));
        }
    }

    public static void checkAbort(String moduleID) 
        throws IASDeploymentException {
        DeploymentRequest request = 
            DeploymentRequestRegistry.getRegistry().getDeploymentRequest(
                moduleID);
        if (request != null && request.isAborted()) {
            DeploymentRequestRegistry.getRegistry().removeDeploymentRequest(
                moduleID);
            String msg = localStrings.getString(
                "enterprise.deployment.phasing.operation.abort",
                new Object[]{ moduleID });
            throw new IASDeploymentException(msg);
        }  
    }

   /**
     * This method returns the deployableObjectType of an archive by checking
     * the deployable descriptors in the archive
     * @param filePath absolute path to the archive
     * @return type DeployableObjectType
     */
    public static DeployableObjectType getTypeFromFile(String name, String filePath)
        throws DeploymentException {

        if(filePath == null)
            throw new DeploymentException("deploy file not specified");

        try {
            //@@@ special handling the deployment of .class POJO webservice
            //class.  
            if (filePath.endsWith(".class")) {
                // get the top directory from the moduleID, in the case of 
                // autodeploy, the moduleID will be the full class name (with package)
                // with . replaced with _
                StringTokenizer tk = new StringTokenizer(name, "_");
                File f = new File(filePath);
                for (int i=0;i<tk.countTokens();i++) {
                    f = f.getParentFile();
                }
                try {
                    // it's important to not cache this because the class can 
                    // change overtime... we need to load the last one.
                    URL[] urls = new URL[1];
                    urls[0] = f.toURL();
                    URLClassLoader cLoader = new URLClassLoader(urls, 
                                DeploymentServiceUtils.class.getClassLoader());
                    String className = name.replaceAll("_", ".");
                    Class clazz = cLoader.loadClass(className);
                    if (clazz!=null && clazz.getAnnotation(javax.ejb.Stateless.class)!=null) {
                        return DeployableObjectType.EJB;
                    } else {
                        return DeployableObjectType.WEB;
                    }                    
                } catch(Exception e) {
                    sLogger.log(Level.WARNING, e.getMessage(), e);
                    return DeployableObjectType.WEB;
                }
            }

            Archivist archivist = 
                ArchivistFactory.getArchivistForArchive(filePath);
            ModuleType moduleType = archivist.getModuleType();
            return getDeployableObjectType(moduleType);
        } catch (IOException ioe) {
            DeploymentException de = new DeploymentException(
                localStrings.getString(
                    "enterprise.deployment.ioexc_getting_archtype", filePath));
            de.initCause(ioe);
            throw de;
        } catch(Exception  ex) {
            DeploymentException de = new DeploymentException(
                localStrings.getString(
                    "enterprise.deployment.unknown.application.type", filePath));
            de.initCause(ex);
            throw de;
        }
    }

    public static DeploymentTarget getAndValidateDeploymentTarget(String 
        targetName, String appName, boolean isDeleting) 
        throws IASDeploymentException {
        try {
            final DeploymentTarget target = getDeploymentTarget(targetName);
            if (targetName == null) {
                // If the targetName passed in was null, we need to set it
                // to its default value.
                targetName = target.getTarget().getName();
            }
            if (target.getTarget().getType() == TargetType.SERVER ||
                target.getTarget().getType() == TargetType.DAS) {
                // make sure we have the reference before we delete it
                if (isDeleting) {
                    if (!ServerHelper.serverReferencesApplication(
                        getConfigContext(), targetName, appName)) {
                        throw new IASDeploymentException(localStrings.getString("serverApplicationRefDoesNotExist", targetName, appName));
                    }
                // make sure we don't have the reference already before 
                // we create it again
                } else {
                   if (ServerHelper.serverReferencesApplication(
                        getConfigContext(), targetName, appName)) {
                        throw new IASDeploymentException(localStrings.getString("serverApplicationRefAlreadyExists", appName, targetName));
                   }
                }
            } else if (target.getTarget().getType() == TargetType.CLUSTER){
               // make sure we have the reference before we delete it
                if (isDeleting) {
                    if (!ClusterHelper.clusterReferencesApplication(
                        getConfigContext(), targetName, appName)) {
                        throw new IASDeploymentException(localStrings.getString("clusterApplicationRefDoesNotExist", targetName, appName));
                    }
                // make sure we don't have the reference already before 
                // we create it again
                } else {
                    if (ClusterHelper.clusterReferencesApplication(
                        getConfigContext(), targetName, appName)) {
                        throw new IASDeploymentException(localStrings.getString("clusterApplicationRefAlreadyExists", appName, targetName));
                    }
                }
            }
         
            return target;
        } catch (IASDeploymentException ex) {
            throw (ex);
        } catch (Exception ex) {
            throw new IASDeploymentException(ex);
        }
    }

    /**
     * Set http listener host and port in deployment request. If the server
     * is not configured properly the defaults used are localhost:8080 for
     * clear and localhost:8181 for SSL.
     */
    public static void setHostAndPort(DeploymentRequest req)
            throws ServerInstanceException, ConfigException {

        String virtualServers = (String) req.getOptionalAttributes().get(ServerTags.VIRTUAL_SERVERS);
        if (virtualServers==null) {
            HostAndPort hap = getHostAndPort(false);
            if(hap != null) {
                req.setHttpHostName(getHostName(hap));
                req.setHttpPort(getPort(hap, false));
            }
            hap = getHostAndPort(true);
            if(hap != null) {
                req.setHttpsHostName(getHostName(hap));
                req.setHttpsPort(getPort(hap, true));
            }
        } else {
            StringTokenizer st = new StringTokenizer(virtualServers,",");
            if (st.hasMoreTokens()) {
                String aVirtualServer = st.nextToken();
                HostAndPort hap = getVirtualServerHostAndPort(aVirtualServer, false);
                if(hap != null) {
                    req.setHttpHostName(getHostName(hap));

                    req.setHttpPort(getPort(hap, false));
                }
                hap = getVirtualServerHostAndPort(aVirtualServer, true);
                if(hap != null) {
                    req.setHttpsHostName(getHostName(hap));
                    req.setHttpsPort(getPort(hap, true));
                }
            }
        }
    }

    private static String getHostName(HostAndPort hap) {
        String hostName = hap.getHost();
        if (hostName == null || hostName.trim().equals("")) {
            hostName = getDefaultHostName();
        }
        return hostName;
    }

    private static String getDefaultHostName() {
        String defaultHostName = "localhost";
        try {
            InetAddress host = InetAddress.getLocalHost();
            defaultHostName = host.getCanonicalHostName();
        } catch(UnknownHostException uhe) {
            sLogger.log(Level.FINEST, "mbean.get_local_host_error", uhe);
            sLogger.log(Level.INFO, "mbean.use_default_host");

       }
        return defaultHostName;
    }

    private static int getPort(HostAndPort hap, boolean securityEnabled) {
        int port = hap.getPort();
        if (port == 0) {
            port = getDefaultPort(securityEnabled);
        }
        return port;
    }

    private static int getDefaultPort(boolean securityEnabled) {
        int port = 0;
        if (securityEnabled) {
            port = 8181;
        } else {
            port = 8080;
        }
        sLogger.log(Level.INFO, "mbean.use_default_port", String.valueOf(port));
        return port;
    }

    /**
     * Get value of named attribute from attributes list. If an attribute with
     * specified name does not exist, the method returns null. If there are
     * than one attributes with same name then the method returns value of
     * first matching attribute.
     *
     * @param attrs list of attributes
     * @param attrName name of the attribute
     * @return value of the specified attrName or null if the attrName is
     *     not present in specified attrs
     */
    private static Object getNamedAttributeValue(AttributeList attrs,
            String attrName) {
        if (attrs == null || attrName == null) {
            return null;
        }
        Object value = null;
        Iterator iter = attrs.iterator();
        while (iter.hasNext()) {
           Attribute attr = (Attribute)iter.next();
           if (attrName.equals(attr.getName())) {
               value = attr.getValue();
               break;
           }
        }
        return value;
    }

    /**
     *Prepare HostAndPort from the current configuration.
     *@param securityEnabled whether the listener should be secure (vs. open)
     *@return HostAndPort for the first non-admin listener matching the security setting
     */
    private static HostAndPort buildHostAndPortFromCurrentConfig(boolean securityEnabled) throws ConfigException {
        ServerContext serverContext = ApplicationServer.getServerContext();
        if (serverContext == null) {
            throw new IllegalStateException("Unable to locate server context");
        }
        ConfigContext configContext = serverContext.getConfigContext();
        if (configContext == null) {
            throw new IllegalStateException("Unable to locate config context from server");
        }
        return buildHostAndPortFromConfig(configContext, securityEnabled);
    }
    
    /**
     *Prepare HostAndPort object from the specified configuration.
     *@param configContext the ConfigContext to use in finding the HTTP listener of interest
     *@param securityEnabled whether the listener should be secure or open
     *@return HostAndPort for the first non-admin listener matching the security setting
     */
    private static HostAndPort buildHostAndPortFromConfig(ConfigContext configContext, boolean securityEnabled) throws ConfigException {
        Config config = ServerBeansFactory.getConfigBean(configContext);
        HttpService httpService = config.getHttpService();
        HttpListener [] listeners = httpService.getHttpListener();

        return findNonadminListener(configContext, listeners, securityEnabled);
    }
    
    /**
     *Find the first non-admin HTTP listener in the specified list with the
     *requested security setting.
     *@param listeners array of HttpListeners
     *@param securityEnabled indicates if the listener of interest is secure or open
     */
    private static HostAndPort findNonadminListener(ConfigContext configContext, HttpListener[] listeners, boolean securityEnabled) throws ConfigException {
        /*
         *Find a listener that is enabled, not the admin listener, with the
         *the requested security setting.
         */
        HostAndPort result = null;
        for (HttpListener listener : listeners) {
            if (listener.isEnabled() 
                && ! listener.getDefaultVirtualServer().equals(
                    com.sun.enterprise.web.VirtualServer.ADMIN_VS)
                && listener.isSecurityEnabled() == securityEnabled) {

                String serverName = listener.getServerName();
                if (serverName == null || serverName.trim().equals("")) {
                    serverName = getDefaultHostName();
                }
                
                String portStr = listener.getPort();
                String redirectPortStr = listener.getRedirectPort();
                if (redirectPortStr != null && ! redirectPortStr.trim().equals("")) {
                    portStr = redirectPortStr;
                }
                /*
                 *Resolve any property expression to an integer.
                 */
                String resolvedPortStr =
                    new PropertyResolver(configContext,
                        getInstanceName()).resolve(portStr);
                int port = Integer.parseInt(resolvedPortStr);
                
                result = new HostAndPort(serverName, port, listener.isSecurityEnabled());
                break;
            }
        }
        return result;
    }

    /**
     * @param securityEnabled
     * @throws ServerInstanceException
     */
    private static HostAndPort getHostAndPort(boolean securityEnabled) throws ServerInstanceException, ConfigException {
        return buildHostAndPortFromCurrentConfig(securityEnabled);
    }

    private static HostAndPort getVirtualServerHostAndPort(String vs, boolean securityEnabled)
        throws ServerInstanceException
    {
        String serverName = null;
        int port = 0;
        try {
            Domain domain = ConfigAPIHelper.getDomainConfigBean(getConfigContext());
            Config config = domain.getConfigs().getConfig(0);
            HttpService httpService = config.getHttpService();
            HttpListener[] httpListener = httpService.getHttpListener();
            VirtualServer[] virtualServer = httpService.getVirtualServer();
            
            // iterate for each of the config virtual server

            for (VirtualServer v : virtualServer) {


                // virtual server id check
                //
                // if the virtual server obtained from application ref
                // does not match with the virtual server from config
                // then continue with next virtual server

                if ( ! v.getId().equals(vs)) {
                    continue;
                }

                // should we check for state, let us assume ON for PE

                // http listener
                //
                // Obtain the http listeners list from the virtual server
                // and iterate to match with the http listeners from config.
                // When a match is found get the host and port data

                String httpListeners = v.getHttpListeners();
                List<String> httpListenerList = (List<String>) StringUtils.parseStringList(httpListeners, " ,");

                for (String vsHttpListenerID : httpListenerList) {
                    for (HttpListener listener : httpListener) {
                        if ( ! listener.getId().equals(vsHttpListenerID)) {
                            continue;
                        }
                        
                        if ( ! listener.isEnabled()) {
                            continue;
                        }
                        
                        if (listener.isSecurityEnabled() == securityEnabled) {
                            serverName = listener.getServerName();
                            if (serverName == null || serverName.trim().equals("")) {
                                serverName = getDefaultHostName();
                            }
                            
                            String portStr = listener.getPort();
                            String redirPort = listener.getRedirectPort();
                            if (redirPort != null && !redirPort.trim().equals("")) {
                                portStr = redirPort;
                            }
                            final String resolvedPort =
                                    new PropertyResolver(getConfigContext(),
                                        getInstanceName()).resolve(portStr);
                            port = Integer.parseInt(resolvedPort);
                            return new HostAndPort(serverName, port);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ServerInstanceException sie = 
                new ServerInstanceException(e.getLocalizedMessage());
            sie.initCause(e);
            throw sie;
        }
        return null;
    }

    public static DeploymentTarget getDeploymentTarget(String targetName) 
        throws IASDeploymentException {
        try {
            final DeploymentTarget target = DeploymentTargetFactory.getDeploymentTargetFactory().getTarget(getConfigContext(), getDomainName(), targetName);
            return target;
        } catch (IASDeploymentException ex) {
            throw (ex);
        } catch (Exception ex) {
            throw new IASDeploymentException(ex);
        }
    }


    public static void checkAppReferencesBeforeUndeployFromDomain(
        String appName) throws IASDeploymentException {
        try {
            // make sure no one reference the app before we undeploy it
            if (ApplicationHelper.isApplicationReferenced(
                getConfigContext(), appName)) {
                throw new IASDeploymentException(localStrings.getString("applicationIsReferenced", appName, ApplicationHelper.getApplicationReferenceesAsString(getConfigContext(), appName)));
            }
        } catch (IASDeploymentException ex) {
            throw (ex);
        } catch (Exception ex) {
            throw new IASDeploymentException(ex);
        }
    }

    /**     
     * Returns the type of the registered component.
     * If the component is not registered throws a DeploymentException
     * @param name name of the component
     * @return type DeployableObjectType of the registered component(app/module)
     */     
    public static DeployableObjectType getRegisteredType(String name) 
        throws DeploymentException {
        try{
            for(int i = 0; i< deployableObjectTypes.length; i++)
            {
                if (isRegistered(name, deployableObjectTypes[i])) {
                    return deployableObjectTypes[i];
                }
            }
        }catch(Exception e){
            throw new DeploymentException(e.getMessage());
        }
        String msg = localStrings.getString(
            "enterprise.deployment.component.not.registered", name);
        throw new DeploymentException(msg);
    }

    /**
     * This method should be removed once we remove the use of 
     * DeployableObjectType.
     */
    public static DeployableObjectType getDeployableObjectType(ModuleType type) {
        if (ModuleType.EAR.equals(type)) {
            return DeployableObjectType.APP;
        } else if (ModuleType.EJB.equals(type)) {
            return DeployableObjectType.EJB;
        } else if (ModuleType.WAR.equals(type)) {
            return DeployableObjectType.WEB;
        } else if (ModuleType.CAR.equals(type)) {
            return DeployableObjectType.CAR;
        } else if (ModuleType.RAR.equals(type)) {
            return DeployableObjectType.CONN;
        }
        return null;
    }

    /**
     *Creates a new instance of the specified module type.
     *@param type the DeployableObjectType to be instantiated
     *@return a ConfigBean subclass instance of the appropriate type
     */
    private static ConfigBean createNewModule(DeployableObjectType type) {
        ConfigBean result = null;
        if (type.equals(DeployableObjectType.APP)) {
            result = new J2eeApplication();
        } else if (type.equals(DeployableObjectType.CAR)) {
            result = new AppclientModule();
        } else if (type.equals(DeployableObjectType.CONN)) {
            result = new ConnectorModule();
        } else if (type.equals(DeployableObjectType.EJB)) {
            result = new EjbModule();
        } else if (type.equals(DeployableObjectType.WEB)) {
            result = new WebModule();
        } else {
            throw new IllegalArgumentException(type.toString());
        }
        return result;
    }
    
    /**
     *Adds the specified module of the specified type.
     *@param type the DeployableObjectType for the new module
     *@param module the ConfigBean subclass instance - the module - to be added
     */
    private static void addModule(DeployableObjectType type, ConfigBean module) throws ConfigException {
        getAppsConfigBean().addValue(toConfigBeanType(type), module);
    }
    
    /**
     *Removes the specified module of the specified type.
     *@param type the DeployableObjectType of the module to be removed
     *@param the module to be removed
     */
    private static void removeModule(DeployableObjectType type, ConfigBean module) throws ConfigException {
        removeReferences(module);
        getAppsConfigBean().removeValue(toConfigBeanType(type), module);
    }
    
    /**
     *Removes refererences to the specified module.
     *@param module the module whose references are to be removed
     */
    private static void removeReferences(ConfigBean module) throws ConfigException {
        ConfigContext configContext = module.getConfigContext();
        String name = module.getAttributeValue(ServerTags.NAME);
        Server [] servers = ServerHelper.getServersReferencingApplication(configContext, name);
        Cluster [] clusters = ClusterHelper.getClustersReferencingApplication(configContext, name);
        for (Server s : servers) {
            ApplicationRef ref = s.getApplicationRefByRef(name);
            s.removeApplicationRef(ref);
        }
        for (Cluster c : clusters) {
            ApplicationRef ref = c.getApplicationRefByRef(name);
            c.removeApplicationRef(ref);
        }
    }
    
    /**
     *Finds the module with the specified ID of the given type.
     *@param appID the module ID
     *@param type the DeployableObjectType indicating the type of module of interest
     *@return the requested module as an instance of the appropriate subclass of ConfigBean 
     */
    private static ConfigBean getModule(String appID, DeployableObjectType type) throws ConfigException {
        ConfigBean module = ApplicationHelper.findApplication(getConfigContext(), appID);
        if (module != null && type.equals(toDeployableObjectType(module))) {
            return module;
        } else {
            return null;
        }
    }

    /**
     *Converts a deployable object type to the String for the ConfigBean type.
     *@param type the DeployableObjectType
     *@return the corresponding ConfigBean type string
     */
    private static String toConfigBeanType(DeployableObjectType type) {
        String result;
        if (type.equals(DeployableObjectType.APP)) {
            result = Applications.J2EE_APPLICATION;
        } else if (type.equals(DeployableObjectType.CAR)) {
            result = Applications.APPCLIENT_MODULE;
        } else if (type.equals(DeployableObjectType.CONN)) {
            result = Applications.CONNECTOR_MODULE;
        } else if (type.equals(DeployableObjectType.EJB)) {
            result = Applications.EJB_MODULE;
        } else if (type.equals(DeployableObjectType.WEB)) {
            result = Applications.WEB_MODULE;
        } else {
            throw new IllegalArgumentException(type.toString());
        }
        return result;
    }
    
    /**
     *Returns the DeployableObjectType for the particular type of ConfigBean.
     *@param module the ConfigBean of interest
     *@return the DeployableObjectType for the module 
     */
    private static DeployableObjectType toDeployableObjectType(ConfigBean module) {
        DeployableObjectType result;
        if (module instanceof J2eeApplication) {
            result = DeployableObjectType.APP;
        } else if (module instanceof EjbModule) {
            result = DeployableObjectType.EJB;
        } else if (module instanceof WebModule) {
            result = DeployableObjectType.WEB;
        } else if (module instanceof LifecycleModule) {
            result = DeployableObjectType.LCM;
        } else if (module instanceof AppclientModule) {
            result = DeployableObjectType.CAR;
        } else if (module instanceof ConnectorModule) {
            result = DeployableObjectType.CONN;
        } else {
            result = null;
        }
        return result;
    }
    
    /** string constants */
    private static final String SYSTEM_PREFIX = "system-";
 
     // Attribue names for http listeners
    private static final String PORT = "port";
    private static final String DEF_VS = "default-virtual-server";
    private static final String SERVER_NAME = "server-name";
    private static final String REDIRECT_PORT = "redirect-port";
    private static final String SEC_ENABLED = "security-enabled";
    private static final String LISTENER_ENABLED = "enabled";
    private static final String OBJECT_TYPE = "object-type";

    private static final String EXTERNALLY_MANAGED = "externally-managed";

    private static final String DIRECTORY_DEPLOYED_ATTR = "DirectoryDeployed";

    // Attribute names for virtual server
    private static final String HOSTS = "hosts";
    private static final String HTTP_LISTENERS = "http_listeners";
    private static final String DEFAULT_WEB_MODULE = "default_web_module";
    private static final String STATE = "state";
    private static final String ID = "id";

    // resources constants
    private static final String SUN_RESOURCE_XML = "sun-resources.xml";
    private static final String SUN_RESOURCE_XML_PATH = 
        "META-INF" + File.separator + "sun-resources.xml";

    private static final String LIST_ADMIN_OBJECTS =
        "getAdminObjectResource";
    private static final String LIST_CONNECTOR_CONNECTION_POOLS =
        "getConnectorConnectionPool";
    private static final String LIST_RESOURCE_ADAPTER_CONFIGS =
        "getResourceAdapterConfig";

    private static final DeployableObjectType[] deployableObjectTypes =
        new DeployableObjectType[] { DeployableObjectType.APP,
        DeployableObjectType.EJB, DeployableObjectType.WEB,
        DeployableObjectType.CONN, DeployableObjectType.CAR, 
        DeployableObjectType.LCM, DeployableObjectType.CMB };

    private static String[] httpListenerAttrNames = {LISTENER_ENABLED,
            DEF_VS, SERVER_NAME, REDIRECT_PORT, PORT, SEC_ENABLED, ID };

    private static String[] vsAttrNames = {HOSTS, HTTP_LISTENERS,
            DEFAULT_WEB_MODULE, STATE, ID};

}
