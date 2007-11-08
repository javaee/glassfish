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


package com.sun.enterprise.instance;

//JDK imports
import java.io.File;
import java.io.IOException;
//iAS imports
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.diagnostics.*;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.HttpService;
//import com.sun.enterprise.config.serverbeans.Mime;
//import com.sun.enterprise.config.serverbeans.VirtualServerClass;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import com.sun.enterprise.admin.common.exception.AFRuntimeStoreException;

import com.sun.enterprise.admin.server.core.channel.AdminChannel;
import com.sun.enterprise.admin.server.core.channel.RMIClient;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import com.sun.enterprise.admin.server.core.ManualChangeManager;
/*installConfig is removed and we need better alternative */
//import com.iplanet.ias.installer.core.installConfig;

//ROB: config changes
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.PropertyResolver;

//Tomcat
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.server.ServerContext;

/**
	The class that stores <code> all environment </code> of a iAS SE Server Instance
	with a given name. The name of the instance is the same as given by the
	(administrative) user. The class hides all the details of mapping between
	such a name and its location on disk. Note that this class is responsible for
	environment of a single Server Instance only.
        Upgraded it to the new file layout. Please refer to 
	<a href="http://iaseng.red.iplanet.com/packages/iasse7_filelayout.html">
	new file layout for the instance configuration. </a> This class should
	be in unison with this document.

	Please note that there is a dependency on installer code. If you make
	any changes to this file, make sure that you visit installer code
	at com/iplanet/ias/installer/windows/actions

	@author  Kedar
	@version 1.1
*/

public final class InstanceEnvironment {
    
        private static Object lock = new Object();
    
        /** enable/disable TimeStamp mechanism TOTALLY */
        private static final boolean TIMESTAMP_MECHANISM_ENABLED = true;
        
        /** enable/disable TimeStamp mechinism for Mime.types.
         * makes sense only if TIME_STAMP_MECHANISM_ENABLED is true;
         */
        private static final boolean MIMETYPE_TIMESTAMP_MECHANISM_ENABLED = true;

        private static final boolean VIRTUAL_SERVER_TIMESTAMP_MECHANISM_ENABLED 
                = true;
        
        private static final long INVALID_TIME_STAMP = 0;
        
        private static final long UNINITIALIZED_TIME_STAMP = 0;

	private static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

	/** folder where the configuration of this instance is stored */

	public static final String kConfigDirName		= "config";
	
	/** folder where the configuration of this instance is backed up */
	
	public static final String kBackupDirName		= ".";
	
	// bnevins, Sept. 2003 -- kBackupDirName was changed to "."
	// This String is used for creating
	// repository backup dirs as well as the now defunct config backup dir.
	// I created a new variable for repository.  I see no good reason to make
	// it public like the rest of these variables ?!?
	
	private static final String kRepositoryBackupDirName		= "backup";
	
	/** folder where all generated code like compiled jsps, stubs is stored */
	
	public static final String kGeneratedDirName		= "generated";
	
	/** default folder where deployed j2ee-apps are stored */
	public static final String kRepositoryDirName		= "applications";
		
	public static final String kApplicationDirName		= "j2ee-apps";
	
	/** folder where deployed modules are stored */
	
	public static final String kModuleDirName		= "j2ee-modules";
	
	/** folder where all the deployed life cycle modules are stored */
	
	public static final String kLifecycleModuleDirName      = "lifecycle-modules";
	
	/** folder where ejb related stuff is stored */
	
	public static final String kEJBStubDirName              = "ejb";
	
	/** name of the configuration file */
    /** New for 8.0 - domain.xml is config file name */	

	public static final String kConfigXMLFileName		= "domain.xml";

    /** New for 8.0 - domain.xml is config file name */	

	public static final String kServerXMLFileName		= "server.xml";
	
	/** folder where the other required classes are stored */
	
	public static final String kLibDirName                  = "lib";

    /** folder where the auto deploy monitor looks for new archives to deploy */
	public static final String kAutoDeployDirName           = "autodeploy";
	
	/** folder where the customer overridden classes are stored */
	
	public static final String kLibClassesDirName           = "classes";
	
	/** folder where the compiled JSP pages reside */
	
	public static final String kCompileJspDirName		= "jsp";

	/** folder where the modified xml files reside */
	
	public static final String kGeneratedXMLDirName		= "xml";
	
	/** folder where the session info or passivated ejbs are to be stored
            can be overridden in the server.xml */
	
	public static final String kSessionStoreDirName		= "session-store";
	
	/** folder to persist the HTTP session data */
	
	public static final String kHttpSessionDirName		= "http";
	
	/** folder for docroot */
    public static final String kDocRootDirName              = "docroot";
    
    /** object file name */
    public static final String kObjectFileName              = "obj.conf";

    /** init file name */
    public static final String kInitFileName                = "init.conf";
    
	/** name of the iWS NSS passwords file */
	
	public static final String kSecurityPasswordsFileName		= "password.conf";
	
	/** name of the realms key file */
	
	public static final String kRealmsKeyFileName		= "keyfile";

    /** name of the install dir */

	public static final String kInstallDirName          = "install";

    /** name of the templates dir */

	public static final String kTemplatesDirName        = "templates";

    /**  ACL template name */
	public static final String kAclTemplate             = "template.acl";

    /** default orb-listener port */
    public static final int kOrbListenerPort                = 3700;

    /** default jms-service (imq) port */
    public static final int kJmsProviderPort                = 7676;

    /** directory where java-web-start related files are located */
    public static final String kJavaWebStartDirName         = "java-web-start";
    
    /* installRoot is the server installation root */
	private String instanceRoot				= null;

	private String mLogicalName				= null;
	private String mConfigFilePath				= null;
	private String mBackupConfigFilePath			= null;
	/** mApplicationRootPath points to the application root of the server. If
		this is unspecified, it will default to <instance_dir>/applications.
		This is where J2EE applications, modules, life-cycle modules will be
		created.
	*/
	private String mApplicationRootPath				= null;
	
	/** mApplicationRepositoryPath is where the j2ee-apps are stored. It is
		possible that the applications running/deployed to an instance
		are located at different locations. This attribute denotes the "current"
		location where the j2ee-apps are to be stored at.
		Same is true for other application/module related paths. This does
		not refer to stubs or jsp pages.
	*/
	private String mApplicationRepositoryPath		= null;
	private String mApplicationBackupRepositoryPath		= null;
	private String mModuleRepositoryPath			= null;
	private String mModuleBackupRepositoryPath		= null;
	
	private String mApplicationStubRepositoryPath		= null;
	private String mModuleStubRepositoryPath		= null;
	private String mApplicationGeneratedXMLPath		= null;
	private String mModuleGeneratedXMLPath		= null;
	private String mLibPath					= null;
	private String mAutoDeployDirPath		= null;
	private String mLibClassesPath				= null;
    private String mApplicationPassivatedEjbPath            = null;
    private String mModulePassivatedEjbPath					= null;
    private String mApplicationCompiledJspPath              = null;
    private String mWebModuleCompiledJspPath                = null;
    private String mApplicationHttpSessionStorePath         = null;
    private String mModuleHttpSessionStorePath				= null;
    private String mDocRootPath                             = null;
    private String mStopCommandPath                         = null;
    private String mDefaultAccessLogPath                    = null;
    private String mObjectFilePath                          = null;
    private String mInitFilePath                            = null;
    private String mBackupObjectFilePath                    = null;
    private String mBackupInitFilePath                      = null;
    private String mSecurityPasswordsFilePath               = null;
    private String mRealmsKeyFilePath                       = null;
    private String mBackupRealmsKeyFilePath                 = null;

    // Hamid: Added the following files for Acl: Bug 4700937
    private String mAclFilePath                            = null;
    private String mBackupAclFilePath                      = null;
    private String kAclFilePrefix                          = "generated";
    private String kBackupAclFilePrefix                    = "genwork";
    private String kAclFileSuffix                          = "acl";
    
    private String mJavaWebStartDirPath                    = null;
    
    // Lazy initialization flag
    private boolean mInited                                = false;
	
	private String mInstancesRoot							= null;
	
	/** 
            Creates new InstanceEnvironment for Server Instance with given name.
            Name may not be null or empty String. Name may not begin with a digit.
            <p>
            It is essential to note that an instance with this name 
            is <code> created /<code>indeed before creating object of this class.
            <p>
            @param instanceName String denoting the name of Server Instance.
            @throws IllegalArgumentException if the name is null or empty
            String or does not begin with a letter (in both the variants) or
            the instance directory does not exist. Thus the examples of invalid 
            values are "6ias"

            @see com.sun.enterprise.config.ServerManager#createServerInstance
	*/
	
    public InstanceEnvironment(String instanceName) {
		this(System.getProperty(Constants.IAS_ROOT), 
		     instanceName);
    }

    public InstanceEnvironment(String instancesRoot, String instanceName) {
        if (! StringUtils.ok(instanceName)) {
		  throw new IllegalArgumentException(Localizer.getValue(ExceptionType.NULL_ARG));
        }
        mLogicalName = instanceName;
		mInstancesRoot = instancesRoot;
        createConfigFilePath();
        //createBackupConfigFilePath();
    }

    private PropertyResolver mPathResolver;

    private void init() {
        if (mInited) {
            return;
        }
        createPathResolver();
		createApplicationRootPath();
        createApplicationRepositoryPath();
        createApplicationBackupRepositoryPath();
        createModuleRepositoryPath();
        createModuleBackupRepositoryPath();
        createApplicationStubPath();
        createModuleStubPath();
        createApplicationGeneratedXMLPath();
        createModuleGeneratedXMLPath();
        createLibPath();
        createLibClassesPath();
        createAutoDeployDirPath();
        createPassivatedEjbPaths();
        createApplicationCompiledJspPath();
        createWebModuleCompiledJspPath();
        createHttpSessionStorePaths();
        createDocRootPath();
        createStopCommandPath();
        createDefaultAccessLogPath();
        createObjectFilePath();
        createInitFilePath();
        createSecurityPasswordsFilePath();
        createRealmsKeyFilePath();
        createAclFilePath();
        createJavaWebStartPath();
        mInited = true;
    }
    /**
     * Apply the changes from all the files i.e. the other config files
     * to the real one. <code>This method simply copies the files over and 
     * there is no transaction model built into it. </code>
     * If the method throws any exception, there is a chance that the changes
     * are not correctly applied.
     * Note that the only UI from which the object file (obj.conf) and 
     * initialization file (init.conf) is changed is the admin cgis and some
     * servlets. Most of the clients change the server.xml.
     * This method will typically be called when the apply/reconfigure of
     * clients is called.
    */
    public void applyChangesFromBackup() throws ConfigException {
    }
    
    public void applyChangesFromBackup(boolean overwrite) throws ConfigException {
    }

    public boolean hasHotChanged() {
        return false;
    }

    /**
     * Has init.conf or obj.conf file changed.
     * @return true if live init.conf or obj.conf has changed manually,
     *     or backup init.conf or obj.conf file timestamp is later than live
     */
    public boolean hasInitOrObjConfFileChanged() {
    	return false;
    }

    /**
     * Has Realms keyfile changed.
     * @return true if live keyfile has changed manually,
     *     or backup keyfile file timestamp is later than live
     */
    public boolean hasRealmsKeyConfFileChanged() {
    	return false;
    }


    /**
     * Has configured mime file(s) changed.
     * @return true if any of the live configured mime type file has been
     *     changed manually, or if any of the backup mime file timestamp is 
     *     later than the timestamp for live files.
     */
    public boolean hasMimeConfFileChanged() {
        return false;
    }

    public boolean hasVirtualServerConfFileChanged() {
    	return false;
    }

    /**
     * Has any of config files managed by cgi scripts changed. The files
     * managed by cgi scripts are init.conf, obj.conf and mime types files.
     */
    public boolean hasCGIConfigFilesChanged() {
    	return false;
    }

    public boolean hasHotInitChanged() {
    	return false;
    }
    
    public boolean hasHotRealmsKeyChanged() {
    	return false;
    }

    public boolean hasHotObjectChanged() {
    	return false;
    }

    public boolean hasHotXmlChanged() {
    	return false;
    }
    
    /**
     * is used to findout if we can reload transparently
     * i.e., reload configcontext
     * To do this, we need to findout if xml has changed. If it has,
     * then is backup in memory changed, i.e., has ConfigContext Changed ?
     * return true if you can reload
     * return false if hot has not changed or if you cannot reload
     */
    public boolean canReloadManualXmlChanges() {
        return false;
    }
    
    private boolean hasOnlyHotXmlChanged() {
    	return false;
    }
    
    public boolean hasHotMimeChanged() {
    	return false;
    }

    public boolean hasHotVirtualServerConfChanged() {
    	return false;
    }

    public boolean hasBakupVirtualServerConfChanged() {
    	return false;
    }

    private boolean hasVirtualServerConfChanged(ConfigContext   context, 
                                                boolean         isHot) 
        throws ConfigException {
        return false;
    }

    private long getConfigFileTimeStamp(String configFile) {
        //need to read xml and get the timestamp
        long ts = UNINITIALIZED_TIME_STAMP;
        try {
            File f = new File(configFile);
            ts = f.lastModified();
        } catch(Throwable t) {
            //ignore
        }
        return ts;
    }
    
    private long getLastModifiedFromTSFile(String tsFileName) {
            long ts = INVALID_TIME_STAMP; //different from getXmlFileTimeStamp for a purpose
            
            FileReader fr = null;
            try {
                File f = new File(tsFileName);
		//_logger.log(Level.INFO,"ts file: " + ((f.exists())?"exists":"does not exist")); 
		
                
                fr = new FileReader(f);
                
                char[] cbuf = new char[13]; // should be sufficint
                fr.read(cbuf);
		String s = new String(cbuf);
		//_logger.log(Level.FINE,"String is:" + s +":");
                ts = (new Long(s)).longValue();
            } catch(Throwable t) {
				// _logger.log(Level.WARNING,"Exception caught in getting LastModified",t);
                //ignore ?
            } finally {
                try {
                    fr.close();
		    fr = null;
                } catch(Exception ex){}
            }
            return ts;
        }
    
    private String getTimeStampFileName(String fullName) {
        return fullName + ".timestamp";
    }
    
    public void useManualConfigChanges() throws ConfigException {
    }

    public void useManualServerXmlChanges() throws ConfigException {
    }

    private void useManualInitFileChanges() throws ConfigException {
    }

    private void useManualRealmsKeyFileChanges() throws ConfigException {
    }

    private void useManualObjectFileChanges() throws ConfigException {
    }

    private void useManualAllMimeFileChanges() throws ConfigException {
    }

    private void useManualVirtualServerConfFileChanges(ConfigContext bakXmlCtx) 
        throws ConfigException {
/*
		ConfigContext context = ConfigFactory.
			createConfigContext(getConfigFilePath(), true, false, false);
		if (context == null) {
			// this is serious. ConfigContext for
			// this server instance should be created by now.
			
			throw new ConfigException(Localizer.getValue(ExceptionType.NO_XML));
		}
          //ROB: config changes
		//Server 		rootElement = ServerBeansFactory.getServerBean(context);
          Config rootElement = ServerBeansFactory.getConfigBean(context);

		HttpService httpService = rootElement.getHttpService();
		VirtualServerClass[] vsClasses= httpService.getVirtualServerClass();
		for(int i = 0 ; i < vsClasses.length ; i++) {
			VirtualServerClass aClass = vsClasses[i];
            VirtualServer[] virtualServers = aClass.getVirtualServer();
            for(int j = 0 ; j < virtualServers.length ; j++) {
                VirtualServer aServer = virtualServers[j];
                String file = aServer.getConfigFile();
                if ((file == null) || (file.length() == 0)) {
                    continue;
                }
                String destPath = getBackupConfigDirPath() + File.separator
                                    + file;
                String srcPath  = getConfigDirPath() + File.separator
                                    + file;
                copyFile(new File(srcPath), new File(destPath));
                //Chown file
                if (isChownNeeded(null)) {
                    chownFile(destPath);
                }
                if(VIRTUAL_SERVER_TIMESTAMP_MECHANISM_ENABLED) {
                    String tsFilePath = getTimeStampFileName(destPath); 
                    saveTimeStamp(tsFilePath, srcPath);
                }
            }
		}
*/
        /* The following code deletes the config files from the backup
         * directory. These config files correspond to the virtual servers
         * that would have been deleted from the hot xml.
         */
/*
        deleteVirtualServerConfigFiles(bakXmlCtx, context, false);
*/
    }

    /**
     *  This method iterates through the virtual-server elements
     *  foreach virtual-server-class element of the 'ctx' and
     *  checks if a corresponding virtual-server element exists
     *  in 'ctx2'. If not, it assumes that the virtual-server
     *  element has been deleted from ctx2 and deletes the 
     *  corresponding config file from the config directory of
     *  'ctx'.
     *  <warning>This method assumes that virtual-server ids are
     *  unique across virtual-server-class elements. </warning>
     */
    private void deleteVirtualServerConfigFiles(ConfigContext ctx, 
                                                ConfigContext ctx2, 
                                                boolean       isHot)
        throws ConfigException
    {
/*
          //ROB: config changes
 		//Server 		rootElement     = ServerBeansFactory.getServerBean(ctx);
          Config rootElement = ServerBeansFactory.getConfigBean(ctx);

		HttpService httpService     = rootElement.getHttpService();

          //ROB: config changes
          //Server      rootElement2    = ServerBeansFactory.getServerBean(ctx2);
          Config rootElement2 = ServerBeansFactory.getConfigBean(ctx2);

		HttpService httpService2    = rootElement2.getHttpService();

        VirtualServerClass[] vsClasses = 
                                httpService.getVirtualServerClass();
        for(int i = 0 ; i < vsClasses.length ; i++) {
            VirtualServerClass vsClass = vsClasses[i];
            String vsClassId = vsClass.getId();
            VirtualServerClass vsClass2 = 
                    httpService2.getVirtualServerClassById(vsClassId);
            boolean deleteAll = false;
            if (vsClass2 == null) {
                // This virtual-server-class was probably deleted 
                 // from ctx2. So delete the config files for all the
                 // virtual servers.
                
                deleteAll = true;
            }
            VirtualServer[] virtualServers = vsClass.getVirtualServer();
            for(int j = 0 ; j < virtualServers.length ; j++) {
               VirtualServer vs = virtualServers[j]; 
               String vsId = vs.getId();
               if ((deleteAll) || 
                   (vsClass2.getVirtualServerById(vsId) == null)) {
                   String configFileName = vs.getConfigFile();
                   if ((configFileName == null) || 
                       (configFileName.length() == 0)) {
                       continue;
                   }
                   String parent = (isHot) ? getConfigDirPath() : 
                                             getBackupConfigDirPath();
                   File confFile = new File(parent, configFileName);
                   if (confFile.exists()) {
                       confFile.delete();
                   }
                   if (!isHot) {
                       //Remove timestamp file
                       File tsFile = new File(parent, 
                                              configFileName + ".timestamp");
                       if (tsFile.exists()) {
                           tsFile.delete();
                       }
                   }
               }
            }
        }
*/

    }

    /**
     * init to be called from constructor
     */
     public void createTimeStampFiles() {
        init();
        if(!TIMESTAMP_MECHANISM_ENABLED) return;
         synchronized(lock) {
         /* TOMCAT_BEGIN Ramakanth */
         createTSFile(getTimeStampFileName(mConfigFilePath), 
             mConfigFilePath, mConfigFilePath);
         createTSFile(getTimeStampFileName(mBackupRealmsKeyFilePath), 
             mRealmsKeyFilePath, mBackupRealmsKeyFilePath);
         /* TOMCAT_END Ramakanth */
         }
     }
     
     private void createTSFile(String tsFileName, String actualFile, String backupFile) {
         // if the file does not exist, 
         // create ts file
         File f = new File(tsFileName);
            if(!f.exists()) {
                saveTimeStamp(tsFileName, actualFile, backupFile);
            }
     }
     
     private void createMimeTSFiles() {
     }

    private void createVirtualServersConfTSFiles() {
/*
        if(!VIRTUAL_SERVER_TIMESTAMP_MECHANISM_ENABLED) return;
         
        try {
            ConfigContext context = ConfigFactory.
                createConfigContext(mConfigFilePath, true, false, false);
            if (context == null) {
                // this is serious. ConfigContext for
                // this server instance should be created by now.
                
                return;
            }
            //ROB: config changes
            //Server 		rootElement = ServerBeansFactory.getServerBean(context);
            Config rootElement = ServerBeansFactory.getConfigBean(context);

            HttpService httpService = rootElement.getHttpService();
            VirtualServerClass[] vsClasses = httpService.getVirtualServerClass();
            for(int i = 0 ; i < vsClasses.length ; i ++) {
                VirtualServerClass aClass = vsClasses[i];
                VirtualServer[] virtualServers = aClass.getVirtualServer();
                for(int j = 0 ; j < virtualServers.length ; j++) {
                    VirtualServer aServer = virtualServers[j];
                    String file = aServer.getConfigFile();
                    if ((file == null) || (file.length() == 0)) {
                        continue;
                    }
                    String backPath = getBackupConfigDirPath() + File.separator
                                        + file;
                    String hotPath = getConfigDirPath() + File.separator
                                        + file;
                    String tsFileName = getTimeStampFileName(backPath); 
                    File f = new File(tsFileName);
                    if(!f.exists()) {
                        saveTimeStamp(tsFileName, hotPath, backPath);
                    }
                }
            }
        } catch(Throwable t) {
            _logger.log(Level.WARNING,"core.create_mime_ts_file_exception",t);
            //ignore. go ahead.
        }
*/
    }

	/** 
	* saves timestamp and resets backup dir to the value of ts
 	*/         
        private void saveTimeStamp(String tsFileName, String actualFile, String backupFile) {
            if(!TIMESTAMP_MECHANISM_ENABLED) return;
	    long ts = saveTimeStamp(tsFileName, actualFile);
	    new File(backupFile).setLastModified(ts);
	}

        /**
         * set the value of timestamp in a new file
         * the name of timestamp file is name of this xml + .timestamp
         * always create this file even if readonly
         */
        private long saveTimeStamp(String tsFileName, String actualFile) {
            if(!TIMESTAMP_MECHANISM_ENABLED) return INVALID_TIME_STAMP;
            
            long timestamp = getConfigFileTimeStamp(actualFile);
		//_logger.log(Level.FINE,"writing TS file");
            File f = null;
            FileWriter fw = null;
            try {
                f = new File(tsFileName);
                fw = new FileWriter(f);
                fw.write("" + timestamp);
            } catch(Throwable t) {
			 _logger.log(Level.WARNING,"core.file_io_exception",t);
                //ignore
            } finally {
                try {
                    fw.close();
                } catch(Throwable ex){}
            }
            return timestamp;  
        }

	/** overwrite the "live" server.xml with the contents of the 
	 * backup server.xml file
	 */
        /*
    public void applyServerXmlChanges() throws ConfigException {
        applyServerXmlChanges(false);
    }
         */
    
    public void applyServerXmlChanges(boolean overwrite) throws ConfigException {
    }

	/** overwrite the "live" init.conf with the contents of the 
	 * backup init.conf file
	 */
    
    public void applyInitFileChanges() throws ConfigException {
    }

	/** overwrite the "live" keyfile with the contents of the 
	 * backup init.conf file
	 */
    
    public void applyRealmsKeyFileChanges() throws ConfigException {
    }

	/** overwrite the "live" obj.conf with the contents of the 
	 * backup obj.conf file
	 */
    
    public void applyObjectFileChanges() throws ConfigException {
    }

	/** Overwrite all mime.types files for the instance.
		These are the files that are <code> mime </code>
		elements on the <code> http-service </code> element.
		Note, there is no transactional model.
		@throws ConfigException if copy fails.
	*/
	public void applyAllMimeFileChanges() throws ConfigException {
	}

// Hamid: BugId: 4700937: Start
    /** overwrite the generated.<serverId>.acl file with the contents of the
     * genwork.<serverId>.acl file
     */

    public void applyAclFileChanges() throws ConfigException {
    }
// Hamid: BugId: 4700937: End

	public void applyVirtualServersConfFileChanges(ConfigContext hotXmlCtx) 
            throws ConfigException {
/*
        init();
            synchronized(lock) {
		ConfigContext context = ConfigFactory.createConfigContext(
                                    mBackupConfigFilePath);
		if (context == null) {
			// this is serious. ConfigContext for
			//this server instance should be created by now.
			
			throw new ConfigException(
                    Localizer.getValue(ExceptionType.NO_XML_BU));
		}
          //ROB: config changes
		//Server 		rootElement = ServerBeansFactory.getServerBean(context);
          Config rootElement = ServerBeansFactory.getConfigBean(context);
 
		HttpService httpService = rootElement.getHttpService();
		VirtualServerClass[] vsClasses = httpService.getVirtualServerClass();
		for(int i = 0 ; i < vsClasses.length ; i++) {
			VirtualServerClass vsClass = vsClasses[i];
            VirtualServer[] virtualServers = vsClass.getVirtualServer();
            for(int j = 0 ; j < virtualServers.length ; j++) {
                VirtualServer virtualServer = virtualServers[j];
                String  configFileName  = virtualServer.getConfigFile();
                File    srcFile         = null;
                if (configFileName != null) {
                    srcFile = new File(getBackupConfigDirPath(), 
                                       configFileName);
                }
                if ((srcFile == null) || (!srcFile.exists())) {
                    continue;
                }
                File destFile   = new File(getConfigDirPath(), configFileName);
                copyFile(srcFile, destFile);
                //Chown file
                if (isChownNeeded(null)) {
                    chownFile(destFile.getAbsolutePath());
                }
                if(VIRTUAL_SERVER_TIMESTAMP_MECHANISM_ENABLED) {
                    String tsFilePath = 
                            getTimeStampFileName(srcFile.getAbsolutePath());
                    saveTimeStamp(tsFilePath, destFile.getAbsolutePath());
                }
            }
               
            }
*/
        /* The following code deletes the config files from the config
         * directory. These config files correspond to the virtual servers
         * that would have been deleted from the backup xml.
         */
/*
            deleteVirtualServerConfigFiles(hotXmlCtx, context, true);
         }
*/
        }

    /**
     * Copies the <code> offline </code> file on <code> real </code> one.
     * @throws ConfigException in case copy fails for various reasons.
     * @param hot java.io.File that is real one (target).
     * @param cold java.io.File that is backup/offline (source).
    */
    private void copyFile(File fromFile, File toFile) throws ConfigException
    {
        if(!fromFile.exists())
                throw new ConfigException(Localizer.getValue(ExceptionType.FROM_NOT_EXIST, fromFile.getPath()));

        if(fromFile.isDirectory())
                throw new ConfigException(Localizer.getValue(ExceptionType.FROM_IS_DIR, fromFile.getPath()));

        /** WBN March 14, 2002
         * There is a very strange problem -- Windows will sometimes return 0 for
         * the length of a file that is NOT zero-length.  I tested and proved beyond 
         * any doubt that length() is *wrong*.
         * This is probably a JVM bug.  So I've commented-out the following code.
        if(cold.length() <= 0)
        {
                throw new ConfigException(err + coldErr + "is zero length");
        }
        */

        if(toFile.isDirectory())
                throw new ConfigException(Localizer.getValue(ExceptionType.TO_IS_DIR, toFile.getPath()));

        if(toFile.exists() && !toFile.canWrite())
                throw new ConfigException(Localizer.getValue(ExceptionType.TO_READ_ONLY, toFile.getPath()));

        try
        {
                FileUtils.copy(fromFile, toFile);
        }
        catch(Exception e)
        {
			Object[] objs = new Object[] { fromFile.getPath(), toFile.getPath(), e.toString() };
            throw new ConfigException(Localizer.getValue(ExceptionType.FAILED_COPY, objs), e);
        }
    }
	
    /**
            Creates the path for server.xml pertaining to this Instance.
    */
	
    private void createConfigFilePath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, 
                                         kConfigDirName, 
                                         kConfigXMLFileName};
        mConfigFilePath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
            Creates the path for backup server.xml pertaining to this Instance.
    */

    private void createBackupConfigFilePath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, 
                                         kConfigDirName, 
                                         kBackupDirName, 
                                         kConfigXMLFileName};
        mBackupConfigFilePath = StringUtils.makeFilePath(onlyFolderNames, false);
    }
	
	/**
		Creates the ApplicationRootPath that is guaranteed to take into account
		the changes made to server.xml. Initializes the application root to
		a valid non-null String. Does not check whether this is a directory
		that can be written to.
	*/
	private void createApplicationRootPath() {
		try {
			ConfigContext context = ConfigFactory.
				createConfigContext(mConfigFilePath);
			Domain domain = ServerBeansFactory.getDomainBean(context);
			mApplicationRootPath = domain.getApplicationRoot();
			if (mApplicationRootPath == null || mApplicationRootPath.length() <=0){
				createDefaultApplicationRootPath();
			}
            mApplicationRootPath = resolvePath(mApplicationRootPath);
		}
		catch (Exception e) {
			_logger.log(Level.WARNING, "InstanceEnv.createApplicationRootPath()", e);
		}
	}
	
	/** Initializes mApplicationRootPath = <instancedir>/applications.
	*/
	private void createDefaultApplicationRootPath() {
        String[] onlyFolderNames = new String[] {
										mInstancesRoot,
										kRepositoryDirName};
        mApplicationRootPath = StringUtils.makeFilePath(onlyFolderNames, false);
	}
    /**
            Creates the path for repository of applications 
            pertaining to this Instance.
    */

    private void createApplicationRepositoryPath() {
        String[] onlyFolderNames = new String[] {
										mApplicationRootPath,
										kApplicationDirName};
        mApplicationRepositoryPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
            Creates the path for repository of standalone modules
            pertaining to this Instance.
    */

    private void createModuleRepositoryPath() {
        String[] onlyFolderNames = new String[] {
										mApplicationRootPath,
										kModuleDirName};
        mModuleRepositoryPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
            Creates the path for backup repository of applications 
            pertaining to this Instance -- directory-deployed apps
    */

    private void createApplicationBackupRepositoryPath() {
        String[] onlyFolderNames = new String[] {
										mApplicationRootPath,
										kRepositoryBackupDirName, 
										kApplicationDirName};
        mApplicationBackupRepositoryPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
            Creates the path for backup repository of modules 
            pertaining to this Instance -- directory-deployed modules
    */

    private void createModuleBackupRepositoryPath() {
        String[] onlyFolderNames = new String[] {
										mApplicationRootPath,
						                kRepositoryBackupDirName, 
										kModuleDirName};
        mModuleBackupRepositoryPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
     * Formulates the path to the instance auto deploy dir. 
     */
    private void createAutoDeployDirPath() {
        String[] onlyFolderNames = 
            new String[] {mInstancesRoot, kAutoDeployDirName};
        mAutoDeployDirPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }


    /**
     * Formulates the path to the instance lib dir. Any jar files under 
     * this dir will be included by the shared class loader.
     */
    private void createLibPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, kLibDirName};
        mLibPath = StringUtils.makeFilePath(onlyFolderNames, false);

    }

    /**
     * Formulates the path to the instance lib classes directory. Any class
     * files under this dir will be be included by the shared class loader.
     */
    private void createLibClassesPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, kLibDirName, 
        kLibClassesDirName};
        mLibClassesPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }
	
    /**
            Creates the path for repository of EJB stubs for all applications
            deployed to this Instance.
    */

    private void createApplicationStubPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, kGeneratedDirName,
                kEJBStubDirName, kApplicationDirName};
        mApplicationStubRepositoryPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }
	
    /**
            Creates the path for repository of EJB stubs for all standalone modules
            deployed to this Instance.
    */

    private void createModuleStubPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, kGeneratedDirName,
                kEJBStubDirName, kModuleDirName};
        mModuleStubRepositoryPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
            Creates the path for generated xml for all applications
            deployed to this Instance.
    */

    private void createApplicationGeneratedXMLPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, kGeneratedDirName,
                kGeneratedXMLDirName, kApplicationDirName};
        mApplicationGeneratedXMLPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
            Creates the path for generated xml for all standalone modules
            deployed to this Instance.
    */

    private void createModuleGeneratedXMLPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot, kGeneratedDirName,
                kGeneratedXMLDirName, kModuleDirName};
        mModuleGeneratedXMLPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

	
	/** Path upto <instance-dir>/session-store is returned */
	
	private String getDefaultSessionStorePath() {
        String[] onlyFolderNames = new String[] {
				mInstancesRoot, 
				kSessionStoreDirName
		};
        return ( StringUtils.makeFilePath(onlyFolderNames, false) );
	}

	/** Creates the paths for ejbs from both application and module. The paths are
		like this:
		passivated-beans-for-modules=<session-store>/ejb/j2ee-modules
		passivated-beans-for-apps=<session-store>/ejb/j2ee-apps
	*/
	private void createPassivatedEjbPaths() {
		try {
			ConfigContext context = ConfigFactory.createConfigContext(
				mConfigFilePath);
			EjbContainer ejbContainer = ServerBeansFactory.getConfigBean(context).getEjbContainer();
			String sessionStore = ejbContainer.getSessionStore();
			if (sessionStore == null || sessionStore.length() <= 0) {
				sessionStore = getDefaultSessionStorePath();
			}
            sessionStore = resolvePath(sessionStore);
			String[] onlyFolderNames = new String[] {
				sessionStore,
				kEJBStubDirName,
				kApplicationDirName
			};
			mApplicationPassivatedEjbPath = StringUtils.makeFilePath(onlyFolderNames, false);
			onlyFolderNames[2] = kModuleDirName;
			mModulePassivatedEjbPath	= StringUtils.makeFilePath(onlyFolderNames, false);
		}
		catch (Exception e) {
			_logger.log(Level.WARNING, "InstanceEnv.createApplicationRootPath()", e);
		}
	}

	/** Creates the paths for ejbs from both application and module. The paths are
		like this:
		httpsession-store-for-modules=<session-store>/http/j2ee-modules
		httpsession-store-for-apps=<session-store>/http/j2ee-apps
	*/

	private void createHttpSessionStorePaths() {
		try {
			ConfigContext context = ConfigFactory.createConfigContext(
				mConfigFilePath);
			EjbContainer ejbContainer = ServerBeansFactory.getConfigBean(context).getEjbContainer();
			String sessionStore = ejbContainer.getSessionStore();
			if (sessionStore == null || sessionStore.length() <= 0) {
				sessionStore = getDefaultSessionStorePath();
			}
            sessionStore = resolvePath(sessionStore);
			String[] onlyFolderNames = new String[] {
				sessionStore,
				kHttpSessionDirName,
				kApplicationDirName
			};
			mApplicationHttpSessionStorePath = StringUtils.makeFilePath(onlyFolderNames, false);
			onlyFolderNames[2] = kModuleDirName;
			mModuleHttpSessionStorePath	= StringUtils.makeFilePath(onlyFolderNames, false);
		}
		catch (Exception e) {
			_logger.log(Level.WARNING, "InstanceEnv.createApplicationRootPath()", e);
		}
	}
	
    /** Creates the path for compiled JSP pages from a J2EE application */

    private void createApplicationCompiledJspPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot,
            kGeneratedDirName, kCompileJspDirName, kApplicationDirName};

        mApplicationCompiledJspPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /** Creates the path for compiled JSP Pages from a standalone web
        app module.
    */

    private void createWebModuleCompiledJspPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot,
            kGeneratedDirName, kCompileJspDirName, kModuleDirName};

        mWebModuleCompiledJspPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }

    /**
        Creates the Path for the docroot of this instance. With the
        new layout, the docroot is supposed to be in the folder for 
        instance configuration by default.
    */
    private void createDocRootPath() {
        String[] onlyFolderNames = new String[] {mInstancesRoot,
            kDocRootDirName};

        mDocRootPath = StringUtils.makeFilePath(onlyFolderNames, false);
    }
    /**
        Creates the stop command path for this instance.
    */
    private void createStopCommandPath() {
        String[]    onlyFolderNames    = new String[] {mInstancesRoot};
        String      execName           = null; 
        if(OS.isWindows()) {
            execName = InstanceDefinition.WIN_STOP_COMMAND_NAME;
        }
        else {
            execName = InstanceDefinition.UNIX_STOP_COMMAND_NAME;
        }    
        mStopCommandPath = StringUtils.makeFilePath(onlyFolderNames, true)
                + execName;
    }
    
    /** Creates the path of default access log file */
    private void createDefaultAccessLogPath() {
        final String logDirName         = "logs";
        final String accessLogFileName  = "access";
        String[] fileNames              = new String[] {mInstancesRoot,
           logDirName, accessLogFileName}; 
        mDefaultAccessLogPath = StringUtils.makeFilePath(fileNames, false);
    }
    
    /** Creates the path of object file */
    private void createObjectFilePath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, kObjectFileName};
        mObjectFilePath = StringUtils.makeFilePath(fileNames, false);
    }

    /** Creates the path of initialization file */
    private void createInitFilePath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, kInitFileName};
        mInitFilePath = StringUtils.makeFilePath(fileNames, false);
    }

    /** Creates the path of iWS NSS passwords file */
    private void createSecurityPasswordsFilePath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, kSecurityPasswordsFileName};
        mSecurityPasswordsFilePath = StringUtils.makeFilePath(fileNames, false);
    }

    /** Creates the path of Realms key file */
    private void createRealmsKeyFilePath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, kRealmsKeyFileName};
        mRealmsKeyFilePath = StringUtils.makeFilePath(fileNames, false);
    }

    /** Creates the path of backup object file */
    private void createBackupObjectFilePath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, kBackupDirName, kObjectFileName};
        mBackupObjectFilePath = StringUtils.makeFilePath(fileNames, false);
    }

    /** Creates the path of backup initialization file */
    private void createBackupInitFilePath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, kBackupDirName, kInitFileName};
        mBackupInitFilePath = StringUtils.makeFilePath(fileNames, false);
    }
// Hamid: BugId: 4700937: Start
    /** Creates the path of acl file */
    private void createAclFilePath() {
        // aclFile is generated.<serverId>.acl
        String aclFileName = kAclFilePrefix + "." + getName() + "." + kAclFileSuffix;
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, aclFileName};
        mAclFilePath = StringUtils.makeFilePath(fileNames, false);
    }

    /** Creates the path of backup of acl file */
    private void createBackupAclFilePath() {
        // backupAclFile is genwork.<serverId>.acl
        String backupAclFileName = kBackupAclFilePrefix + "." + getName() + "." + kAclFileSuffix;
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, backupAclFileName};
        mBackupAclFilePath = StringUtils.makeFilePath(fileNames, false);
    }
// Hamid: BugId: 4700937: End

    /** Creates the path of backup Realms key file */
    private void createBackupRealmsKeyFilePath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kConfigDirName, kBackupDirName, kRealmsKeyFileName};
        mBackupRealmsKeyFilePath = StringUtils.makeFilePath(fileNames, false);
    }

    /** Creates the path to the java-web-start directory */
    private void createJavaWebStartPath() {
        String[] fileNames = new String[] {mInstancesRoot,
            kJavaWebStartDirName};
        mJavaWebStartDirPath = StringUtils.makeFilePath(fileNames, false);
    }
    /**
            A method that returns the logical name of this Server Instance.
    */
    public String getName() {
            return ( mLogicalName );
    }
	
    /**
            Returns the path where entire data/config etc. of this instance
            is stored on the disk.

            @return absolute path of the instance's home.
    */

    public String getInstancesRoot() {
        return (mInstancesRoot);
    }
    
    public String getConfigDirPath() {
        String[] folderNames = new String[] {mInstancesRoot,
            kConfigDirName};
        String configDirPath =
                StringUtils.makeFilePath(folderNames, false);
        return ( configDirPath );
    }

    public String getBackupConfigDirPath() {
	/*
        String[] folderNames = new String[] {mInstancesRoot,
            kConfigDirName, kBackupDirName};
        String backupConfigDirPath =
                StringUtils.makeFilePath(folderNames, false);
        return ( backupConfigDirPath );
	*/
	return getConfigDirPath();
    }

    /**
            Returns the absolute path for the config file pertaining
            to this Server Instance.
    */

    public String getConfigFilePath() {
            return ( mConfigFilePath );
    }

    /**
            Returns the absolute path for the backup config file pertaining
            to this Server Instance.
    */

    public String getBackupConfigFilePath() {
            return ( mConfigFilePath );
    }

    /**
            Returns the absolute path for location where all the deployed
            applications are stored for this Server Instance.
    */

    public String getApplicationRepositoryPath() {
        init();
            return ( mApplicationRepositoryPath );
    }

    /**
            Returns the absolute path for location where all the backups of
            directory-deployed applications are stored for this Server Instance.
    */

    public String getApplicationBackupRepositoryPath() {
        init();
            return ( mApplicationBackupRepositoryPath );
    }

    /**
            Returns the absolute path for location where all the deployed
            standalone modules are stored for this Server Instance.
    */

    public String getModuleRepositoryPath() {
        init();
            return ( mModuleRepositoryPath );

    }
    /**
            Returns the absolute path for location where all the deployed
            standalone module backups are stored for this Server Instance.
    */

    public String getModuleBackupRepositoryPath() {
        init();
            return ( mModuleBackupRepositoryPath );

    }

    /**
            Returns the absolute path for location where all ejb stubs for
            all applications are stored for this Server Instance.
    */

    public String getApplicationStubPath() {
        init();
            return ( mApplicationStubRepositoryPath );
    }

    /**
            Returns the absolute path for location where all ejb stubs for
            all modules are stored for this Server Instance.
    */

    public String getModuleStubPath() {
        init();
            return ( mModuleStubRepositoryPath );
    }

    /**
            Returns the absolute path for location where all generated xml for
            all applications are stored for this Server Instance.
    */

    public String getApplicationGeneratedXMLPath() {
        init();
            return ( mApplicationGeneratedXMLPath );
    }

    /**
            Returns the absolute path for location where all generated xml for
            all modules are stored for this Server Instance.
    */

    public String getModuleGeneratedXMLPath() {
        init();
            return ( mModuleGeneratedXMLPath );
    }



    /**
    * Returns the path to the instance lib directory.
    * @return    the path to the instance lib directory
    */
    public String getLibPath() {
        init();
        return ( mLibPath );
    }

    /**
     * Returns the path to the templates directory.
     */
    public static String getTemplatesDirPath() {
        final String installRoot = System.getProperty(Constants.INSTALL_ROOT);
        String[] dirs = new String[] {installRoot, kLibDirName, 
                                      kInstallDirName, kTemplatesDirName};
        return StringUtils.makeFilePath(dirs, false);
    }

   /**
    * Returns the path to the instance's auto deploy directory.
    *
    * @return    the path to the instance's auto deploy directory
    */
    public String getAutoDeployDirPath() {
        init();
        return ( mAutoDeployDirPath );
    }
    
    /**
     * Returns the path to the instance lib/classes directory.
    * @return    the path to the instance lib/classes directory
    */
    public String getLibClassesPath() {
        init();
        return ( mLibClassesPath );
    }

    /**
        Returns the path for compiled JSP Pages from an J2EE application
        that is deployed on this instance. By default all such compiled JSPs
        should lie in the same folder.
    */
    public String getApplicationCompileJspPath() {
        init();
        return ( mApplicationCompiledJspPath );
    }

    /**
        Returns the path for compiled JSP Pages from an Web application
        that is deployed standalone on this instance. By default all such compiled JSPs
        should lie in the same folder.
    */
    public String getWebModuleCompileJspPath() {
        init();
        return ( mWebModuleCompiledJspPath );
    }

    /**
        Returns the path for the Session Store of where HTTP
        session data of the instance can be stored.
    */
    public String getApplicationPassivatedEjbPath() {
        init();
        return ( mApplicationPassivatedEjbPath );
    }
    public String getModulePassivatedEjbPath() {
        init();
        return ( mModulePassivatedEjbPath );
    }
    public String getApplicationHttpSessionStorePath() {
        init();
        return ( mApplicationHttpSessionStorePath );
    }
    public String getModuleHttpSessionStorePath() {
        init();
        return ( mModuleHttpSessionStorePath );
    }
	
    /**
        Returns the docroot path for this instance.
    */
    public String getDocRootPath() {
        init();
        return ( mDocRootPath );
    }
    
    public String getStopCommandPath() {
        init();
        return ( mStopCommandPath );
    }
    
    /**
     * Returns the default value of accesslog for the instance.
     * Returns null in case of error.
     * Note that this is only the default value which may be
     * different than the actual value of any virtual server in this instance.
     * 
     * @return String representing default log file path for any virtual server
     * in this instance.
    */
    public String getDefaultAccessLogPath() {
        init();
        return ( mDefaultAccessLogPath );
    }
    
    /* Gets the complete path of the initialization file
     * for this instance */
    public String getInitFilePath() {
        init();
        return ( mInitFilePath );
    }
    /* Gets the complete path of the backup of initialization file
     * for this instance */
    public String getBackupInitFilePath() {
        init();
        return ( mBackupInitFilePath );
    }

    /* Gets the complete path of the Security passwords file
     * for this instance */
    public String getSecurityPasswordsFilePath() {
        init();
        return ( mSecurityPasswordsFilePath );
    }
    /* Gets the complete path of the Realms key file
     * for this instance */
    public String getRealmsKeyFilePath() {
        init();
        return ( mRealmsKeyFilePath );
    }

    /* Gets the complete path of the backup of the Realms key file
     * for this instance */
    public String getBackupRealmsKeyFilePath() {
        init();
        return ( mRealmsKeyFilePath );
    }

    /* Gets the complete path of the object file
     * for this instance */
    public String getObjectFilePath() {
        init();
        return ( mObjectFilePath );
    }
    /* Gets the complete path of the backup of object file
     * for this instance */
    public String getBackupObjectFilePath() {
        init();
        return ( mBackupObjectFilePath );
    }

    public ApplicationEnvironment getApplicationEnvironment(String appName) {

        init();
        if (appName == null) {
            throw new IllegalArgumentException();
        }
        return ( new ApplicationEnvironment(this, appName) );
    }

    public ModuleEnvironment getModuleEnvironment(String moduleName, DeployableObjectType type) {
        init();
        if (moduleName == null) {
                throw new IllegalArgumentException();
        }

        return ( new ModuleEnvironment(this, moduleName, type) );
    }

    public String getJavaWebStartPath() {
        init();
        return mJavaWebStartDirPath;
    }
    
    public String verify()
    {
            // return a String with the error, in English, if the required 
            // directories aren't there or have a problem.
            // return null if all is OK
            return null;
    }

    public String toString()
    {
        init();
            return ObjectAnalyzer.toString(this);
    }


    /** return true if the given file is located somewhere in the instance's file tree
     **/

    public final boolean isIASOwned(String filename)
    {
            return isIASOwned(new File(filename));
    }

    /** return true if the given file is located somewhere in the instance's file tree
     **/

    public final boolean isIASOwned(File file)
    {
        init();
        try
        {
            // get the names and standardize them for comparison
            String iasPath			= getInstancesRoot();
            assert StringUtils.ok(iasPath);

            iasPath			= FileUtils.safeGetCanonicalPath(new File(iasPath));
            String filename	= FileUtils.safeGetCanonicalPath(file);

            if(!StringUtils.ok(iasPath))
                    return false;

            if(!StringUtils.ok(filename))
                    return false;

            if(filename.startsWith(iasPath))
                    return true;

            return false;
        }
        catch(Exception e)
        {
                return false;
        }
    }

    public boolean restartRequired() {
        init();
        //is instance stopped, return false;
        //is instance restarted after changes, return false;
        // return true;
        synchronized(lock) {
        try {
            RMIClient rc = AdminChannel.getRMIClient(this.mLogicalName);
            if(!rc.isAlive()) return false;
            if(rc.hasRestartedSince(getManualChangeTime())) return false;
            
            return true;
        } catch (Exception e) {
            //ignore
        }
        }
        return false;
    }
   

    private long getManualChangeTime() {
        long ts = UNINITIALIZED_TIME_STAMP;
        long tsTmp = UNINITIALIZED_TIME_STAMP;
        
        try {
            ts = getConfigFileTimeStamp(mInitFilePath);
            tsTmp =  getConfigFileTimeStamp(mObjectFilePath);
            
            if(ts < tsTmp) ts = tsTmp;
            
            tsTmp = getConfigFileTimeStamp(mConfigFilePath);
            
            if(ts < tsTmp) ts = tsTmp;
            
/*
            ConfigContext context = ConfigFactory.
            createConfigContext(mConfigFilePath, true, false, false);
            if (context == null) {
                        // this is serious. ConfigContext for
                        //this server instance should be created by now.
                throw new ConfigException(Localizer.getValue(ExceptionType.NO_XML));
            }
            //ROB: config changes
            //Server 		rootElement = ServerBeansFactory.getServerBean(context);
            Config rootElement = ServerBeansFactory.getConfigBean(context);

            HttpService httpService = rootElement.getHttpService();
            Mime[] 		mimes		= httpService.getMime();
            for(int i = 0 ; i < mimes.length ; i ++) {
                Mime aMime = mimes[i];
                String file = aMime.getFile();
                String hotPath = getConfigDirPath() + File.separator
                + file;
                
                
                tsTmp = getConfigFileTimeStamp(hotPath);
                if(ts < tsTmp) ts = tsTmp;
                
            }
 
*/           
        } catch (Throwable t) {
            //ignore return ture;
        }
        return ts;    
    }
   
 
    private boolean hasBakMimeChanged() {
	return false;
    }

    /*
    public static void main(String[] args)
    {
	        //Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
			System.out.println("Use the appserv-ext.jar, appserv-rt.jar from lib area for other classes");
            System.setProperty("com.sun.aas.instanceRoot", "E:\\ias-build\\s1\\appserv\\domains\\domain1");
            InstanceEnvironment env = new InstanceEnvironment("server1");
			//logger.log(Level.INFO, env.toString());
			System.out.println(env.toString());
    }
    */

    public String getInstanceUser() throws IOException {
        init();
        return ServerManager.instance().getInstanceUser(this);
    }

    /**Ignoring the file parameter for now.
     */
    public boolean isChownNeeded(File f) {
        init();
/**
        if (OS.isUnix() || OS.isLinux()) {
            try {
                final String installUser = 
                    ServerManager.instance().getInstanceUser(
                            new InstanceEnvironment("admin-server"));
                final String instanceUser = 
                    ServerManager.instance().getInstanceUser(this);
                if (installUser.equals("root") && 
                    !instanceUser.equals(installUser)) {
                    return true;
                }
            } catch (IOException ioe) {
                _logger.warning(ioe.getMessage());
            }
        }
*/
        return false;
    }

    private void chownFile(String filePath) throws ConfigException {
        /*installConfig is removed and we need better alternative */
        /*
        try {
            String error = new installConfig().chownFile(
                filePath, getInstanceUser());
            if (error != null) {
                throw new ConfigException(error);
            }
        } catch (IOException ioe) {
            throw new ConfigException(ioe.getMessage());
        }
        */
    }

    private void createPathResolver() {
        try {
            mPathResolver = new PropertyResolver(
                ConfigFactory.createConfigContext(mConfigFilePath), 
                mLogicalName);
        } catch (Exception e) {
            //log it.
        }
    }

    private String resolvePath(String unresolved) {
        if (mPathResolver != null) {
            return mPathResolver.resolve(unresolved);
        }
        return unresolved;
    }
}
