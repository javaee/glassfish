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

/*
 * BaseManager.java
 *
 * Created on January 15, 2002, 5:00 PM
 */

package com.sun.enterprise.instance;


import com.sun.enterprise.admin.util.MessageFormatter;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import java.io.FileNotFoundException;
import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.logging.LogDomains;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXParseException;

/**
 *
 * @author  sridatta
 */
public abstract class BaseManager {
    
    protected volatile ConfigContext configContext;
    protected volatile ConfigBean configBean;
    private final DasConfig appConfig;
    protected final InstanceEnvironment instanceEnvironment;
    protected final boolean useBackupServerXml;
    protected static final String SYSTEM_PREFIX = "system-";
    protected static final String SYSTEM_ADMIN_PREFIX = "system-admin";
    protected static final Logger _logger=LogDomains.getLogger(LogDomains.CORE_LOGGER);
    //private static Map registeredDescriptors;

    /** javac debug option */
    private static final String DEBUG_OPTION = "-g";

    /** javac optimize option */
    private static final String OPTIMIZE_OPTION = "-O";
    
    public BaseManager(InstanceEnvironment env, boolean useBackupServerXml)
            throws ConfigException {

        instanceEnvironment = env;
        try {
            this.useBackupServerXml = useBackupServerXml;
				//_logger.log(Level.FINE,"core.serverxml_usage",((useBackupServerXml)?"back":"hot"));

            String fileUrl;
            if(useBackupServerXml) {
                fileUrl = instanceEnvironment.getBackupConfigFilePath();
            } else {
                fileUrl = instanceEnvironment.getConfigFilePath();
            }
            
            if (useBackupServerXml) {
                AdminService as = AdminService.getAdminService();
                if (as != null) {
                    AdminContext ac = as.getAdminContext();
                    if (ac != null) {
                        configContext = ac.getAdminConfigContext();
                    }
                }
            }
            if (configContext == null) {
                configContext = ConfigFactory.createConfigContext(fileUrl);
            }
            
            configBean = ConfigBeansFactory.getConfigBeanByXPath(this.configContext, 
                                        ServerXPathHelper.XPATH_APPLICATIONS);
            //RAMAKANTH
            if (configBean == null) {
                configBean = new Applications();
            }

            //ROB: config changes
            // use 'appConfigTemp' so that 'appConfig' can be 'final'
            DasConfig appConfigTemp =
                ServerBeansFactory.getDasConfigBean(configContext);
            //RAMAKANTH
            //Why?? what does this mean??
            if (appConfigTemp == null) {
                appConfigTemp = new DasConfig();
            }
            
            appConfig   = appConfigTemp;

        }
        catch(Exception e) {
			throw new ConfigException(Localizer.getValue(ExceptionType.MISSING_SERVER_NODE), e);
        }
    }
    
    /**
     * @return the module type this class is managing
     */
    public abstract ModuleType getModuleType();
    
    /**
     * @author bnevins
     * 9-23-03 for S1AS 8.0PE 
     * The namespace has been flattened for 8.0
     * This method is called to see if a name is registered for a different
     * type of J2EE Deployable Object.  Deployment Backend itself will check the 
     * Manager for the same type of object to see if it is a re-deploy.  If that
     * check is negative, it will call here to check all the other types.  If a different
     * type is registered with the same name, Deployment will fail the deployment
     * instead of wiping out the other app/module with the same name.
     * @return A String with the name of the deployable object type -- if the name is registered,
     * otherwise null. 
     */
    
    public static String isRegisteredAnywhere(InstanceEnvironment ienv, String id)
    {
        try {
            BaseManager[] mans = new BaseManager[]{
                new WebModulesManager(ienv), 
                new EjbModulesManager(ienv), 
                new AppclientModulesManager(ienv), 
                new ConnectorModulesManager(ienv),
                new AppsManager(ienv)
            };

            String[] errors = new String[] {"Web Module", "EJB Module", "App Client Module", "Connector Module", "Application"};

            for(int i = 0; i < mans.length; i++){
                if(mans[i].listIds().contains(id)) {
                    return errors[i];
                }
            }
        }
        catch(Exception e){
        }
        return null;
    }
        
    /** 
     * Returns a virtual application for standalone module or a fully
     * initialized application object from the deployment descriptors
     *
     * @param    modId    web module id
     *
     * @return   the deployment descriptor object for this web module
     *
     * @throws   ConfigException if unable to load the deployment descriptor
     */
    public Application getDescriptor(String modId, ClassLoader cl,
    	boolean validateXML) throws ConfigException {
	
	return getDescriptor(modId, cl, getLocation(modId), validateXML);
    }
    
    /** 
     * Returns a virtual application for standalone module or a fully
     * initialized application object from the deployment descriptors
     *
     * @param    modId    web module id
     *
     * @return   the deployment descriptor object for this web module
     *
     * @throws   ConfigException if unable to load the deployment descriptor
     */
    public abstract Application getDescriptor(String modId, ClassLoader cl, String loc, boolean validateXML)
   		throws ConfigException;
	
	    
    /**
     * This method only returns from cache.  Call it only when you are
     * are sure the application should be in the instance managers.
     */
    public Application getDescriptor(String appID)
   		    throws ConfigException {
        return getRegisteredDescriptor(appID);
    }
	
    // START OF IASRI 4709374
     /** 
     * Returns a boolean indicating whether or not bytecode preprocessing is 
     * enabled.  The bytecode preprocessor is deemed to be enabled if if a 
     * value for bytecode-preprocessors is set in the java config.
     * @throws ConfigException - enabled via server.xml
     * @return - true if enabled, otherwise false.  
     */    
      public boolean isByteCodePreprocessingEnabled() 
        throws ConfigException {        
        boolean result = false; 
        //ROB: config changes
        JavaConfig jc = ServerBeansFactory.getJavaConfigBean(configContext); 
        if (jc != null) {
            if (jc.getBytecodePreprocessors() != null) {
                result = true;
            }
        }
        return result;
    }      
    
    /** 
     * Returns an array of bytecode preprocessor class name(s) as set in 
     * server.xml.
     * @throws ConfigException
     * @return - an array of preprocessor class names.
     */    
    public String[] getBytecodeProcessorClassNames() 
        throws ConfigException {
        String result[] = null;        
        //ROB: config changes - use of domain.xml
        JavaConfig jc = ServerBeansFactory.getJavaConfigBean(configContext);
        if (jc != null) {
           String value = jc.getBytecodePreprocessors();
           _logger.log(Level.INFO,
              "core.preprocessor_class_name", value);
           // Split the comma delimited list of bytecode preprocessor
           // class names into the result array
           result = value.split(",");
        }                 

        return result;
    }    
    // END OF IASRI 4709374

    /**
     * Returns true if dynamic reloading is enabled.
     *
     * @return    true if dynamic reloading is enabled
     */
    public boolean isDynamicReloadEnabled() {
        //ROB: config changes
        //return ((Applications)this.configBean).isDynamicReloadEnabled(); 
        return appConfig.isDynamicReloadEnabled();
    }

    /**
     * Returns the reload polling interval in milli-seconds.
     *
     * @return    reload polling interval in milli-seconds
     */
    public long getReloadPollIntervalInMillis() {

        // pool interval from server configuration in seconds
        String intv = appConfig.getDynamicReloadPollIntervalInSeconds(); 


        long pollIntv;

        try {
            pollIntv = Long.parseLong(intv) * 1000;
        } catch (NumberFormatException nme) {

            // use the default interval 
            intv = appConfig.getDefaultDynamicReloadPollIntervalInSeconds();

            try {
                pollIntv = Long.parseLong(intv) * 1000;
            } catch (NumberFormatException ne) { 
                // this should never happen
                pollIntv = 2000l;
            }
        }

        return pollIntv;
    }

    /**
     * Returns the java class path for this server instance. 
     * It includes the "classpath-prefix" and "classpath-suffix". 
     * This method is called during deployment to construct the 
     * class loader. Deployment assumes that server class path will 
     * not differ between admin instance and a regular instance.
     * So, only prefix and suffix are added to the class path.
     *
     * @return    the system class path that includes only prefix & suffix
     *
     * @throws    ConfigException    if an error while reading the server.xml
     */
    public List getSystemCPathPrefixNSuffix() throws ConfigException {

        List classPath = new ArrayList();

        // bean that represents the java configuration
        JavaConfig jconfig = (JavaConfig) ConfigBeansFactory.
                                    getConfigBeanByXPath(this.configContext, 
                                        ServerXPathHelper.XPATH_JAVACONFIG);

        // add the class path prefix first
        String prefix = jconfig.getClasspathPrefix();
        if (prefix != null) {
            StringTokenizer st = new StringTokenizer(prefix,File.pathSeparator);
            while (st.hasMoreTokens()) {
                classPath.add(st.nextToken());
            }
        }

        // adds the class path suffix to the list
        String suffix = jconfig.getClasspathSuffix();
        if (suffix != null) {
            StringTokenizer st = new StringTokenizer(suffix,File.pathSeparator);
            while (st.hasMoreTokens()) {
                classPath.add(st.nextToken());
            }
        }

        return classPath;

    }

    /**
     * Returns the system class path for this server instance. 
     * It includes the "classpath-prefix", "classpath" and "classpath-suffix". 
     *
     * @return    the system class path 
     *
     * @throws    ConfigException    if an error while reading the server.xml
     */

    public List getSystemClasspath() throws ConfigException {
    if(!Boolean.getBoolean(com.sun.enterprise.server.PELaunch.USE_NEW_CLASSLOADER_PROPERTY)){
        List classPath = new ArrayList();

        // bean that represents the java configuration
        JavaConfig jconfig = (JavaConfig) ConfigBeansFactory.
                                    getConfigBeanByXPath(this.configContext, 
                                        ServerXPathHelper.XPATH_JAVACONFIG);

        // add the class path prefix first
        String prefix = jconfig.getClasspathPrefix();
        if (prefix != null) {
            StringTokenizer st = new StringTokenizer(prefix,File.pathSeparator);
            while (st.hasMoreTokens()) {
                classPath.add(st.nextToken());
            }
        }

        // adds the server classpath to the list
        String serverClasspath = jconfig.getServerClasspath();
        if (serverClasspath != null) {
            StringTokenizer st = 
                new StringTokenizer(serverClasspath,File.pathSeparator);
            while (st.hasMoreTokens()) {
                classPath.add(st.nextToken());
            }
        }

        // adds the class path suffix to the list
        String suffix = jconfig.getClasspathSuffix();
        if (suffix != null) {
            StringTokenizer st = new StringTokenizer(suffix,File.pathSeparator);
            while (st.hasMoreTokens()) {
                classPath.add(st.nextToken());
            }
        }

        return classPath;
    } else {
        return com.sun.enterprise.server.PELaunch.getServerClasspath();
    }
    }

    /**
     * Per the platform specification, connector classes are to be availble
     * to all applications, i.e. a connector deployed to target foo should
     * be available to apps deployed on foo (and not target bar). In that
     * case, we will need to figure out all connector module deployed to 
     * the target on which the application is deployed.  Resolving the
     * classpath accordlingly.
     *
     * @param resolveOnDAS Whether to resolve connector classpath or not.
     *                     This should only be true when running on DAS.
     * @param target The target on which the application is to be deployed
     */
    public List getSharedClasspath(boolean resolveOnDAS, String target) 
        throws ConfigException {
        List classpath = new ArrayList();

        ConnectorModule[] mods = null;
        if (resolveOnDAS && (target != null)) {
            mods = ServerHelper.getAssociatedConnectorModules(
                        configContext, target);
        } else {
            mods = ((Applications)this.configBean).getConnectorModule();
        }

        if (mods != null) {
            for (int i=0; i<mods.length; i++) {
                if (resolveOnDAS) {
                    classpath.add(RelativePathResolver.resolvePath(
                        mods[i].getLocation()));
                } else {
                    classpath.add(mods[i].getLocation());
                }
            }
        }
        
        return classpath;
        
    }
    

    /**
     * Returns the rmic options for deployment. 
     *
     * @return    rmic options as of type java.lang.String
     * @throws    ConfigException    if an error while reading server.xml
     */
    public List getRmicOptions() throws ConfigException {
        
        List rmicOptions = new ArrayList();

        // bean that represents the java configuration
        JavaConfig jconfig = (JavaConfig) ConfigBeansFactory.
                                    getConfigBeanByXPath(this.configContext, 
                                        ServerXPathHelper.XPATH_JAVACONFIG);
        String options = jconfig.getRmicOptions();
        if (options == null) {
            options = jconfig.getDefaultRmicOptions(); 
        }
        StringTokenizer st = new StringTokenizer(options, " ");
        while (st.hasMoreTokens()) {
            String op = (String) st.nextToken();
            rmicOptions.add(op);
            _logger.log(Level.FINE, "Detected Rmic option: " + op);
        }
        
        return rmicOptions;
    }

    /**
     * Returns the javac options for deployment. The options can be anything
     * except "-d",  "-classpath" and "-cp".
     * It tokenizes the options by blank space between them. It does 
     * not to detect options like "-g -g -g" since javac handles it.
     *
     * @return    javac options as of type java.lang.String
     * @throws    ConfigException    if an error while reading server.xml
     */
    public List getJavacOptions() throws ConfigException {
        
        List javacOptions = new ArrayList();

        // bean that represents the java configuration
        JavaConfig jconfig = (JavaConfig) ConfigBeansFactory.
                                    getConfigBeanByXPath(this.configContext, 
                                        ServerXPathHelper.XPATH_JAVACONFIG);
        String options = jconfig.getJavacOptions();
        if (options == null) {
            options = jconfig.getDefaultJavacOptions(); 
        }
        StringTokenizer st = new StringTokenizer(options, " ");
        while (st.hasMoreTokens()) {
            String op = (String) st.nextToken();
            if ( !(op.startsWith("-d") 
                || op.startsWith("-cp") || op.startsWith("-classpath")) ) {
                javacOptions.add(op);
            } else {
				_logger.log(Level.WARNING, "core.unsupported_javac_option", op);
            }
        }
        
        return javacOptions;
    }

    /**
     * Returns true if environment class path ignore flag is turned on. 
     * 
     * @return    true if environment class path ignore flag is turned on 
     * @throws    ConfigException    if an error while reading the server.xml
     */
    public boolean isEnvClasspathIgnored() throws ConfigException {

        // bean that represents the java configuration
        JavaConfig jconfig = (JavaConfig) ConfigBeansFactory.
                                    getConfigBeanByXPath(this.configContext, 
                                       ServerXPathHelper.XPATH_JAVACONFIG);

        return jconfig.isEnvClasspathIgnored();
    }

    public final boolean isIASOwned(String id)
	{
		// WBN 2/7/2002 -- I don't think this bit of info is ever going
		// to be persisted.  Here is a M^2 technology way of doing it.

		assert instanceEnvironment != null;

		try
		{
			return instanceEnvironment.isIASOwned(getLocation(id));
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
    /**
     * Refreshes config context using the given context. This method is called
     * during dynamic deploy/undeploy of applications and stand alone modules.
     *
     * @param    ctx    new config context (from the event)
     * @throws   ConfigException    if null config context or an error while 
     *                              retrieving the config bean
     */
    public synchronized void refreshConfigContext(ConfigContext ctx) throws ConfigException {

        this.configContext = ctx;
        this.configBean = ConfigBeansFactory.
                getConfigBeanByXPath(this.configContext,
                                    ServerXPathHelper.XPATH_APPLICATIONS);
    }
    
    public synchronized void refreshConfigContext() throws ConfigException {
        configContext.refresh(true);
        configBean = ConfigBeansFactory.getConfigBeanByXPath(this.configContext,
                                    ServerXPathHelper.XPATH_APPLICATIONS);
    }
    

    public InstanceEnvironment getInstanceEnvironment() {
        return this.instanceEnvironment;
    }
    
    public boolean isShared(String id) throws ConfigException {
		throw new UnsupportedOperationException(
			Localizer.getValue(ExceptionType.UNSUPPORTED, "isShared()"));
    }
    public void setShared(String modId, boolean shared) throws ConfigException {
		throw new UnsupportedOperationException(Localizer.getValue(
				ExceptionType.UNSUPPORTED, "setShared()"));
    }
    
    public String  getStubLocation(String id) {
		throw new UnsupportedOperationException(Localizer.getValue(
				ExceptionType.UNSUPPORTED, "getStubLocation()"));
    }
    
    public boolean isRegistered(String id) {
        return isRegistered(id, this.configBean);
    }
     
    public void saveConfigContext() throws ConfigException {

        if(!this.useBackupServerXml) {
			throw new ConfigException(Localizer.getValue(ExceptionType.CANT_APPLY));	
        }
			
        this.configContext.flush();
    }
    
    public void applyServerXmlChanges() throws ConfigException {
        if(!this.useBackupServerXml) {
			throw new ConfigException(Localizer.getValue(ExceptionType.CANT_APPLY));	
        }
        this.configContext.flush();
        instanceEnvironment.applyServerXmlChanges(false);
    }
        
    public void setVirtualServers(String modId, String value) 
            throws ConfigException {
    }
        
    /** 
     *override for Web Modules
     */
    public String getContextRoot(String id) throws ConfigException {
		throw new UnsupportedOperationException(Localizer.getValue(
				ExceptionType.UNSUPPORTED, "getContextRoot()"));
    }

    /** 
     *override for Web Modules
     */
    public void setContextRoot(String id, String value) throws ConfigException {
		throw new UnsupportedOperationException(Localizer.getValue(
				ExceptionType.UNSUPPORTED, "setContextRoot()"));
    }
    
    /**
     * @return the registered descriptors map
     */
    public abstract Map getRegisteredDescriptors();
    
    /** 
     * register an new application descriptor in the map 
     * 
     * @param registrationName is the registration name for the descriptor
     * @param descriptor the new application to register
     */
    public void registerDescriptor(String registrationName, Application descriptor) {
        
        Map map = getRegisteredDescriptors();
        if (map==null) {
            return;
        } 
        map.put(registrationName, descriptor);
    }
         
    /**
     * @return a registered application descriptor if exists
     * @param the registration name for the application descriptor
     */
    public Application getRegisteredDescriptor(String registrationName) {
        Map map = getRegisteredDescriptors();
        if (map==null) {
            return null;
        }
        return (Application) map.get(registrationName);
    }
    
    /**
     * @return a registered application descriptor if exists
     * @param the registration name for the application descriptor
     */
    public void unregisterDescriptor(String registrationName) {
        Map map = getRegisteredDescriptors();
        if (map==null) {
            return;
        }
        map.remove(registrationName);
    }    

    // Stores the application descriptor object into the application DD.
    public void saveAppDescriptor(String appId, Application appDes,
            String appDir, String generatedAppDir, boolean isVirtual) 
        throws ConfigException {
        try {
            if (isVirtual) {
                appDes.setVirtual(true);
            }
            SerializedDescriptorHelper.store(appId, this, appDes);

            FileArchive archive = new FileArchive();
            archive.open(generatedAppDir);

            FileArchive archive2 = new FileArchive();
            archive2.open(appDir); 

            DescriptorArchivist archivist = new DescriptorArchivist();
            archivist.write(appDes, archive2, archive);

            // copy the additional webservice elements etc
            Archivist.copyExtraElements(archive2, archive);
        } catch (Throwable t) {
                        throw new ConfigException(
                Localizer.getValue(ExceptionType.FAIL_DD_SAVE, appId), t);
        }
    }

    // Begin EE: 4927099 - load only associated applications

    /**
     * Returns true if the given application is referenced by this 
     * server instance. 
     *
     * @param   appId   application id
     * @return  true if the named application is used/referred by this server
     *
     * @throws  ConfigException  if an error while paring domain.xml
     */
    boolean isReferenced(String appId)
    {
        try {
            return ServerHelper.serverReferencesApplication(this.configContext,
                instanceEnvironment.getName(), appId);
        } catch (ConfigException ex) {
            //This represents an unexpected exception. Log and return false
            _logger.log(Level.WARNING, "isReferenced.unexpectedException", ex);
            return false;
        }       
    }

    // End EE: 4927099 - load only associated applications
    
    /**
     * Returns the config context associated with this manager.
     *
     * @return   config context associated with this manager
     */
    public ConfigContext getConfigContext() {
        return this.configContext;
    }

    public abstract String	getLocation(String id) throws ConfigException;
    public abstract void	setLocation(String id, String location) throws ConfigException;
    public abstract String getGeneratedXMLLocation(String id);
    public abstract boolean isEnabled(String id) throws ConfigException;
    public abstract void setEnable(String id, boolean enabled) throws ConfigException;
    public abstract boolean isSystem(String id) throws ConfigException;
    public abstract boolean isSystemAdmin(String id) throws ConfigException;
    public abstract boolean isSystemPredeployed(String id) 
        throws ConfigException;
    public abstract void setOptionalAttributes(String id, Properties optionalAttributes) throws ConfigException;
    public abstract String getDescription(String id) throws ConfigException;
    public abstract void setDescription(String id, String desc)	throws ConfigException;
    protected abstract boolean isRegistered(String id, ConfigBean bean);
    public abstract void remove(String name) throws ConfigException;
    public abstract List listIds() throws ConfigException;

    /**
     * This method is needed because we need to read persistence descriptors
     * from the original app exploded dir as opposed to generated dir
     * because generated XML do not contain any information about PUs.
     * One way to avoid this would be to write out PU details in
     * sun-application.xml, but we have not done that yet.
     * @param appDir directory where application was exploded (this is not same
     * as the directory where generated XML files are written).
     * @param application the application object whose persistence units
     * will be read. This method will read all the persistence units
     * recurssively and register at appropriate level.
     * @throws IOException
     * @throws SAXParseException
     */
    protected void readPersistenceDeploymentDescriptors(
            String appDir, Application application)
            throws IOException, SAXParseException {
        FileArchive archive = new FileArchive();
        archive.open(appDir);
        try {
            ApplicationArchivist.readPersistenceDeploymentDescriptorsRecursively(
                    archive, application);
        } finally {
            archive.close();
        }
    }

    // open archive with appropriate deployment descriptor directory
    // if is system predeployed app, load from original app dir
    // else load from generated/xml dir
    // print a warning if generated/xml dir is not there
    // and load from original dir (upgrade scenario)
    protected FileArchive openDDArchive(String appId, String appDir)
        throws IOException, ConfigException {
        FileArchive in = new FileArchive();
        if (isSystemPredeployed(appId)) {
            in.open(appDir);
        } else {
            String xmlDir = getGeneratedXMLLocation(appId);
            if (FileUtils.safeIsDirectory(xmlDir)) {
                in.open(xmlDir);
            } else {
                // log a warning message in the server log
                _logger.log(Level.WARNING, "core.no_xmldir",
                    new Object[]{xmlDir, appDir});
                in.open(appDir);
            }
        }
        return in;
    }

}
