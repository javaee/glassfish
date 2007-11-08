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

package com.sun.enterprise.appclient.jws;

import com.sun.enterprise.appclient.AppClientInfo;
import com.sun.enterprise.appclient.Main;
import com.sun.enterprise.appclient.MainWithModuleSupport;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.interfaces.DeploymentImplConstants;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.backend.DeploymentUtils;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.instance.ApplicationEnvironment;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.ExecException;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.web.WebContainer;
import com.sun.logging.LogDomains;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.deploy.shared.ModuleType;
import javax.servlet.http.HttpServletRequest;
import org.xml.sax.SAXParseException;

import org.omg.CORBA.ORB;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.SocketInfo;
import com.sun.corba.ee.impl.orbutil.ORBConstants;
import org.omg.CORBA.ORBPackage.InvalidName;

/**
 *Records and provides access to information about app clients that need
 *Java Web Start support.
 *<p>
 *The basic purpose of this class is to map between the "virtual" location of
 *files as will be specified by incoming HTTP requests and the content to be returned
 *as the responses to those requests.  The 
 *content itself can be dynamic (created based on information about the
 *particular app client or the particular HTTP request) or static (a file
 *that resides somewhere in the file system).  
 *<p>
 *This information is updated as modules are loaded and unloaded in the
 *server instance and as the administrator enables or disables modules
 *for Java Web Start access to their app clients.  The information is used
 *by the system web application that responds to Java Web Start's requests
 *for content related to the app clients.
 *<p>
 *This class is a singleton.
 *
 * @author tjquinn
 */
public class AppclientJWSSupportInfo {

    /** MIME types for dynamic documents */
    private static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";
    private static final String HTML_MIME_TYPE = "text/html";
    private static final String XML_MIME_TYPE = "application/xml";

    /** the singleton instance for this class */
    private static AppclientJWSSupportInfo instance = null;

    /**
     * pattern string describing the format of the pathInfo of incoming requests;
     * includes capture of useful portions as separate groups 
     *
     *The format is:  <category>/<subcategory>/<rest-of-path>
     *
     *where category must be appclient, application, or appserver.  The
     *exact meaning of the subcategory can vary, depending on the category.  
     *The /<rest-of-path> part can be empty, and will be for requests for 
     *the main JNLP document.
     *
     *If the pattern is changed, then the int definitions just below may also
     *need to be changed to reflect the correct positions in the pattern.
     */
    private static final String REQUEST_PATH_INFO_PATTERN = "/(" + 
            NamingConventions.APPCLIENT_CATEGORY + "|" + 
            NamingConventions.APPLICATION_CATEGORY + "|" + 
            NamingConventions.APPSERVER_CATEGORY + "|" + 
            ")/(.*?)(?:/(.*(?:$|\\z)))?";
    
    /*
     *These group numbers are used in retrieving "captured" parts of matched
     *strings.
     */
    private static final int PATTERN_CATEGORY_GROUP_NUMBER = 1;
    private static final int PATTERN_REGNAME_GROUP_NUMBER = 2;
    private static final int PATTERN_RELATIVEPATH_GROUP_NUMBER = 3;
    
    /** pattern used for processing incoming request pathInfo strings */
    private static final Pattern requestPathInfoPattern = Pattern.compile(REQUEST_PATH_INFO_PATTERN);
    
    /*
     *Property settable on the server side to retain temp files on the client.
     *Use the same property name on both sides for clarity.
     */
    private static final String SERVER_RETAIN_TEMP_FILES_PROPERTYNAME = MainWithModuleSupport.APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME;
    
    /*
     * Defines the type prefix that indicates a IIOP socket info object that should
     * NOT be included in the list of IIOP endpoints sent to the client for
     * failover.  Eventually the IIOP classes may provide this constant in
     * one place.
     */
    private static final String IIOP_ENDPOINT_TYPE_PREFIX_IGNORE = "SSL";
    
    /** Property name used to create request for elevated permissions in the JNLP document */
    private static final String APPCLIENT_JAR_SECURITY_ELEMENT_PROPERTYNAME = "appclient.jar.security.setting";
    
    /** local strings manager */
    private StringManager localStrings = StringManager.getManager(getClass());
    
    /*
     *Two key concepts in the implementation are "content" - information that
     *must be served back to Java Web Start on the requesting client - and 
     *"origin" of content - such as an app client, an application, or one of
     *several groupings of content from the app server itself.
     *
     *The following Maps relate each app client registration name, J2EE app reg. name,
     *and app server content grouping (lib, imq, etc.) to the corresponding "origin" of
     *the content.  In particular, by keeping separate collections for the signed
     *vs. unsigned appserver origins, it is easier to create the two JNLP documents
     *that list the files.  Each list of files must appear in a separate JNLP
     *document so each file can have different security settings.
     */
    
    private Map<String,ContentOrigin> appclients;
    private Map<String,ContentOrigin> applications;
    private Map<String,ContentOrigin> appserverOrigins;
    private Map<String,ContentOrigin> signedAppserverOrigins;
    private Map<String,ContentOrigin> extJarAppserverOrigins;
    
    /*
     *The following map relates strings representing pathInfo values to the
     *corresponding Content.  All content available from the Java Web Start
     *system servlet is entered in this map.
     */
    private Map<String,Content> contentMap;
    

    /** maps the category name to the particular map of key to content */
    private Map<String,Map<String,ContentOrigin>> originTypes;
    
    /*
     *Several app server-provided objects are used for looking up apps, 
     *app clients, and getting information about the server.
     */
    /** the app server instance server context */
    protected ServerContext appServerContext;
    
    /** the app server instance environment */
    protected InstanceEnvironment instEnv;
    
    /** the instance's J2EE applications manager */
    protected AppsManager appsManager;
    
    /** the instance's app client modules manager */
    protected AppclientModulesManager appclientModulesManager;
    
    private static final String lineSep = System.getProperty("line.separator");

    private Logger _logger=LogDomains.getLogger(NamingConventions.JWS_LOGGER);
    
    /** Cache for the templates, bundled and retrieved from the jar file that contains this class. */
    private TemplateCache templateCache;
    
    /** URI to the app server's installation root directory */
    private URI installRootURI;
    
    /** File pointing to the app server's root directory */
    private File installRootDir;
    
    /** URI to the derby root directory */
    private URI derbyRootURI;
    
    /** File pointing to the derby root directory */
    private File derbyRootDir;
    
    /** File pointing to the app server's installed lib directory */
    private File libRoot;
    
    /** File pointing to the temporary app client jar directory */
    private File tempJarDirectory;
    
    /** File pointing to the instance's j2ee-modules directory */
    private File j2eeModulesDir;
    
    /** File pointing to the instance's j2ee-apps directory */
    private File j2eeApplicationsDir;

    /** manages extension jar files */
    private ExtensionFileManager extensionFileManager;

    private String UNSIGNED_JWSACC_JARFILE_NAME = "appserv-jwsacc.jar";

    private String SIGNED_JWSACC_JARFILE_NAME = "appserv-jwsacc-signed.jar";
    
    /**
     *Returns the single instance of the class.
     *@return the singleton AppclientJWSSupportInfo
     */
    public static synchronized AppclientJWSSupportInfo getInstance() throws IOException, Exception {
        if (instance == null) {
            instance = new AppclientJWSSupportInfo();
            
            /*
             *Make sure the manager is instantiated so it can register for
             *events.  No reference to the object needs to be kept here; 
             *just make sure the manager has been set up.
             */
             AppclientJWSSupportManager.getInstance();
        }
        return instance;
    }    

    /**
     *Creates a new instance of AppclientJWSSupportInfo.
     *The constructor is private to prevent other objects from creating a new
     *instance except through getInstance().
     */
    private AppclientJWSSupportInfo() throws IOException, Exception {
        
        extensionFileManager = new ExtensionFileManager();
        
        /*
         *Create the collections of content origins (such as the app server itself,
         *app clients, and applications that contain embedded app clients.
         */
        appclients = Collections.synchronizedMap(new HashMap<String,ContentOrigin>());
        applications = Collections.synchronizedMap(new HashMap<String,ContentOrigin>());
        appserverOrigins = Collections.synchronizedMap(new HashMap<String,ContentOrigin>());
        signedAppserverOrigins = Collections.synchronizedMap(new HashMap<String,ContentOrigin>());
        extJarAppserverOrigins = Collections.synchronizedMap(new HashMap<String,ContentOrigin>());
        
        contentMap = Collections.synchronizedMap(new HashMap<String,Content>());
        
        /*
         *Add these maps to the map of maps (!).
         */
        originTypes = new HashMap<String,Map<String,ContentOrigin>>();
        originTypes.put(NamingConventions.APPCLIENT_CATEGORY, appclients);
        originTypes.put(NamingConventions.APPLICATION_CATEGORY, applications);
        originTypes.put(NamingConventions.APPSERVER_CATEGORY, appserverOrigins);
        originTypes.put(NamingConventions.APPSERVER_CATEGORY, signedAppserverOrigins);
        originTypes.put(NamingConventions.APPSERVER_EXTJAR_FILES, extJarAppserverOrigins);
        
        /*
         *Get references to useful app server objects.
         */
        findAppServerObjects();

        /*
         *On Windows, the user may have entered the installation path with
         *different case compared to how the directories are actually spelled.
         *(For example, the directory might be on the disk as MyDir but the
         *user could have entered mydir.)  Windows is okay with this, but
         *a File object using mydir will yield a different URI than one
         *with MyDir.  Using the canonical path for the installation directory
         *makes sure the installRootURI is formatted consistently with the
         *ones for which we construct relative paths later on.
         */
        String installRootDirSpec = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        installRootDir = new File(installRootDirSpec).getCanonicalFile();
        installRootURI = installRootDir.toURI();
        
        String derbyRootDirSpec = System.getProperty(SystemPropertyConstants.DERBY_ROOT_PROPERTY);
        derbyRootDir = new File(derbyRootDirSpec).getCanonicalFile();
        derbyRootURI = derbyRootDir.toURI();
        
        /*
         *Create the template cache.
         */
        templateCache = new TemplateCache();
        
        /*
         *Set up the File objects for the app server's lib directory.  It is
         *used in setting up the content objects for the app server jars served
         *back to the client.
         */
        String instanceRootDirSpec = instEnv.getInstancesRoot();
        libRoot = new File(installRootDir, "lib");

        /*
         *Load the map with app server-provided files.  Do this now, because these
         *are the same for all applications or app clients.
         */
        preloadAppserverContent();
    }
    
    /**
     *Starts Java Web Start services for a stand-alone app client module.
     *
     *@param the Application object for this app client
     *@param the ModuleDescriptor for the app client
     *@param the config context from the deploy event (null if the instance is restarting)
     */
    public void startJWSServicesForAppclient(Application application, ModuleDescriptor moduleDescr, ConfigContext configContext) 
        throws IOException, URISyntaxException, ConfigException, SAXParseException, ConfigException, Exception {
        String regName = application.getRegistrationName();
        AppClientConfig appClientConfig = new AppClientConfig(configContext, regName);
        if (appClientConfig == null) {
            throw new ConfigException("Cannot locate config information for app client " + regName);
        }

        /*
         *Make sure we don't already have services going for this app client.
         */
        String appclientMapKey = NamingConventions.TopLevelAppclient.actualContextRoot(application);
        AppclientContentOrigin origin = findAppclient(appclientMapKey);
        
        if (origin != null) {
            _logger.warning("Attempted to start Java Web Start services for stand-alone app client " + regName + " when they were already started; ignoring the duplicate request");
            return;
        }

        _logger.fine("Starting Java Web Start services for stand-alone app client " + application.getRegistrationName());
        
        /*
         *There is no pre-existing origin for this app client, so create one and
         *start Java Web Start services for it.
         */
        origin = prepareTopLevelAppclient(application, moduleDescr, 
                appClientConfig.getLocation(), appClientConfig.getGeneratedXMLLocation());
        if (origin != null) {
            startJWSServices(origin);

            appclients.put(appclientMapKey, origin);
            refreshConfigContextForManagers(configContext);

            String logOutput = (_logger.isLoggable(Level.FINE) ? origin.toLongString() : origin.toString());
            _logger.info("Java Web Start services started for stand-alone app client " + logOutput);
        }
        
    }
    
    /**
     *Ends Java Web Start support for a stand-alone app client that is being
     *unloaded from the server.
     *
     *@param the application for the app client
     *@param the module descriptor for the app client
     */
    public void endJWSServicesForAppclient(Application application, ModuleDescriptor moduleDescr, ConfigContext configContext) throws ConfigException {
        _logger.fine("Ending Java Web Start services for stand-alone app client " + application.getRegistrationName());
        
        String appclientMapKey = NamingConventions.TopLevelAppclient.actualContextRoot(application);
        AppclientContentOrigin origin = findAppclient(appclientMapKey);
        if (origin != null) {
            endJWSServices(origin);
            appclients.remove(appclientMapKey);

            _logger.info("Java Web Start services ended for stand-alone app client " + origin);
        }
        refreshConfigContextForManagers(configContext);
    }
    
    /**
     *Starts Java Web Start services for an application that contains nested 
     *app clients which need Java Web Start support.
     *@param the application's registration ID
     *@param the module descriptors for child app clients eligible for Java Web Start access
     *@param the config context from the deploy event (null if during an instance restart)
     *@exception IOException during any retrieval of template for dynamic content
     */
    public void startJWSServicesForApplication(Application application, ModuleDescriptor [] moduleDescriptors, ConfigContext configContext) 
            throws IOException, URISyntaxException, SAXParseException, ConfigException, Exception {
        String applicationMapKey = NamingConventions.TopLevelApplication.contextRoot(application);
        String regName = application.getRegistrationName();
        
        ApplicationConfig appConfig = new ApplicationConfig(configContext, regName);
        if (appConfig == null) {
            throw new ConfigException("Cannot locate config information for application " + regName);
        }
        _logger.fine("Starting Java Web Start services for application " + regName);

        /*
         *Make sure we don't have services running already for this application.
         */
        ApplicationContentOrigin origin = findApplication(applicationMapKey);
        if (origin != null) {
            _logger.warning("Attempt to start Java Web Start services for application " + regName + " when they were already started; ignoring the duplicate request");
            return;
        }
        
        /*
         *Preparing the application also includes preparing its child 
         *app clients, which is the real goal.
         */
        origin = prepareApplication(application, moduleDescriptors, 
                appConfig.getLocation(), appConfig.getGeneratedXMLLocation());

        startJWSServices(origin);

        /*
         *Add the origin to the collection.
         */
        applications.put(applicationMapKey, origin);

        refreshConfigContextForManagers(configContext);
        
        String logOutput = (_logger.isLoggable(Level.FINE) ? origin.toLongString() : origin.toString());
        _logger.info("Java Web Start services started for application " + logOutput);
    }
    
    /**
     *Ends Java Web Start services for an application that may contain embedded app clients.
     *
     *@param the application object for the application
     *@param the module descriptor for the application
     */
     public void endJWSServicesForApplication(Application application, ModuleDescriptor[] moduleDescrs, ConfigContext configContext) throws ConfigException {
         String applicationMapKey = NamingConventions.TopLevelApplication.contextRoot(application);
         ApplicationContentOrigin origin = findApplication(applicationMapKey);
         if (origin != null) {
             _logger.fine("Ending Java Web Start services for application " + origin.getTopLevelRegistrationName());
             endJWSServices(origin);
             _logger.info("Java Web Start services ended for application: " + origin);

             /*
              *Finally, remove the application's origin from the collection.
              */
             applications.remove(applicationMapKey);
         }
         refreshConfigContextForManagers(configContext);
     }

      /**
      *Returns whether the specified origin is currently enabled for Java Web
      *Start access or not.
      *@param origin the UserContentOrigin to check
      *@return boolean indicating whether the app client origin is enabled for JWS access
      */
      public boolean isEnabled(UserContentOrigin origin){
          boolean result = false;
          String regName = origin.getApplication().getRegistrationName();
          try {
              if (origin instanceof ApplicationContentOrigin) {
                  result = appsManager.isJavaWebStartEnabled(regName);
              } else {
                  result = appclientModulesManager.isJavaWebStartEnabled(regName);
              }
              return result;
          } catch (ConfigException ce) {
              _logger.log(Level.SEVERE, "Error checking if Java Web Start access enabled for app client " + origin.getApplication().getRegistrationName(), ce);
              return false;
          }
      }
       
      /**
      *Returns whether the specified origin is currently enabled for Java Web
      *Start access or not.
      *@param origin the ApplicationContentOrigin to check
      *@return boolean indicating whether the app client origin is enabled for JWS access
      */
      public boolean isEnabled(ApplicationContentOrigin origin) {
          try {
             return appsManager.isJavaWebStartEnabled(origin.getApplication().getRegistrationName());
          } catch (ConfigException ce) {
              _logger.log(Level.SEVERE, "Error checking if Java Web Start access enabled for application " + origin.getApplication().getRegistrationName(), ce);
              return false;
          }
      }
    /**
     *Returns the Content object corresponding to the path info in the HTTP
     *request.
     *@param the HTTP request asking for the content
     *@return Content object representing the desired content; returns null if no 
     *content matches the HTTP request's information
     */
    public Content getContent(HttpServletRequest request) {
        
        String pathInfo = request.getPathInfo();
        
        /*
         *Adjust the request's pathInfo - for instance, this removes the 
         *context-root of the web app if the web app is nested inside an ear.
         */
        String contentKey = NamingConventions.pathToContentKey(pathInfo);

        /*
         *Even though each origin tracks its own content as well, the single
         *contentMap allows a single look-up to find any content, regardless
         *of its origin.
         */
        Content result = contentMap.get(contentKey);

        /*
         *Make sure the origin for this content is currently enabled for Java 
         *Web Start access.
         */
         if (result != null) {
             ContentOrigin origin = result.getOrigin();
             if ( ! origin.isEnabled()) {
                 if (_logger.isLoggable(Level.FINE)) {
                     _logger.fine("Located requested document with content key " + contentKey + " but reporting 'not found' because the administrator has disabled Java Web Start access for the application or app client");
                 }
                 result = null;
             }
         } else if (_logger.isLoggable(Level.WARNING) && (result == null) ) {
             /*
              *Almost every request that reaches our system servlet results from
              *information we generated into the JNLP documents.  So we should not
              *receive requests for content we do not have.  If the look-up failed 
              *to find the content, report it.
              */
              _logger.warning("Attempt failed to find Java Web Start content at path " + pathInfo + "; content is available at these paths:" + contentMap.keySet());
          }
        
        return result;
    }

     /**
      *Register all deployed appclients with JWS service. This will be called by
      *OnDemand initialization framework, when webcontainer starts.
      */
     public void startJWSServicesForDeployedAppclients() {
         for (ContentOrigin origin : appclients.values()) {
             AppclientContentOrigin acOrigin = (AppclientContentOrigin) origin;
             if (acOrigin.isAdhocPathRegistered() == false) {
                 startJWSServices(acOrigin);
             }
         }

         for (ContentOrigin origin : applications.values()) {
             ApplicationContentOrigin aOrigin = (ApplicationContentOrigin) origin;
             if (aOrigin.isAdhocPathRegistered() == false) {
                 startJWSServices(aOrigin);
             }
         }
     }
    
     /**
      *Returns the AppclientContentOrigin corresponding to the app client with the
      *specified registration name.
      *@param the reg name of the app client 
      *@return the AppclientContentOrigin object for that app client; null if there is none
      */
     private AppclientContentOrigin findAppclient(String regName) {
         AppclientContentOrigin result = (AppclientContentOrigin) appclients.get(regName);
        return result;
     }
     
     /**
     *Returns the AppclientContentOrigin that corresponds to the specified Application and ModuleDescriptor.
     *@param the Application object for the app client
     *@param the ModuleDescriptor object for the app client
     *@return the corresponding AppclientOrigin; null if no matching AppclientOrigin is found
     */
    private AppclientContentOrigin findAppclient(Application application, ModuleDescriptor moduleDescr) {
        return findAppclient(application.getRegistrationName());
    }

    /**
     *Returns the ApplicationContentOrigin corresponding to the specified Application and ModuleDescriptor
     *@param the registration name for the application
     *@return the corresponding AppclientOrigin; null if no matching AppclientOrigin is found
     */
    private ApplicationContentOrigin findApplication(String regName) {
        ApplicationContentOrigin result = (ApplicationContentOrigin) applications.get(regName);
        return result;
    }
    /**
     *Returns the ApplicationContentOrigin corresponding to the specified Application and ModuleDescriptor
     *@param the Application object for the app client
     *@return the corresponding AppclientOrigin; null if no matching AppclientOrigin is found
     */
    private ApplicationContentOrigin findApplication(Application application) {
        return findApplication(application.getRegistrationName());
    }

    /**
     *Does the work to start Java Web Start services for an app client.
     *@param the appclientOrigin for which to begin JWS services
     */
    private void startJWSServices(AppclientContentOrigin origin) {
        
        WebContainer container = WebContainer.getInstance();
        if (container != null) { // Make sure that webcontainer is up.

            /*
             *Ask the web container to route requests for the app client's context-root to 
             *our ad hoc servlet using the appropriate target path for this origin.
             */
            String targetPathString = origin.getTargetPath();
            WebPath targetPath = new WebPath(targetPathString);
            JWSAdHocServletInfo info = new JWSAdHocServletInfo(targetPath.path(), targetPath.contextRoot());

            String virtualContextRoot = origin.getVirtualPath();
            WebPath virtualPath = new WebPath(virtualContextRoot);
            _logger.info("Registering ad hoc servlet: " + virtualPath);
            container.registerAdHocPath(virtualPath.path(),
                                        virtualPath.contextRoot(),
                                        origin.getTopLevelRegistrationName(),
                                        info);
            origin.adhocPathRegistered();
        }
    }
    
    /**
     *Does the work to end Java Web Start support for the specified app client.
     *@param the app client for which Java Web Start is no longer needed
     */
    private void endJWSServices(AppclientContentOrigin origin) {
        
        for (Content c : origin.getContents()) {
            contentMap.remove(c.getContentKey());
        }
        
        WebPath path = new WebPath(origin.getVirtualPath());
        _logger.fine("Unregistering ad hoc servlet: " + path);

        WebContainer container = WebContainer.getInstance();
        if (container != null) {
            container.unregisterAdHocPath(path.path(), path.contextRoot());
        }
    }

    /**
     *Does the work to start Java Web Start services for app clients
     *nested within an application.
     *@param the applicationOrigin for which to begin JWS services
     */
    private void startJWSServices(ApplicationContentOrigin origin) {
        
        WebContainer container = WebContainer.getInstance();
        if (container != null) { // Make sure that webcontainer is up.
            /*
             *Start services for each of the child app clients.
             */
            for (AppclientContentOrigin appclient : origin.getAppclientOrigins()) {
                startJWSServices(appclient); 
            }
            origin.adhocPathRegistered();
        }
    }
    
    /**
     *Does the work to end Java Web Start support for the app clients
     *within the specified parent application content origin.
     *@param the app client for which Java Web Start is no longer needed
     */
    private void endJWSServices(ApplicationContentOrigin origin) {

        for (AppclientContentOrigin appclient : origin.getAppclientOrigins()) {
            endJWSServices(appclient); 
        }
        
        for (Content c : origin.getContents()) {
            contentMap.remove(c.getContentKey());
        }
    }
    
    /**
     *Loads several app server objects for retrieving useful information
     *about the app server instance and applications deployed on it.
     *@throws IllegalStateException reporting any error retrieving the references
     */
    private void findAppServerObjects() throws IllegalStateException {
        /*
         *Build a string listing each failed attempt to locate a useful object.
         */
        StringBuilder failedObjects = new StringBuilder();
        
        Throwable relatedException = null;
        if ((appServerContext = ApplicationServer.getServerContext()) == null) {
            failedObjects.append(lineSep).append(" ApplicationServer.getServerContext()");
        } else if ((instEnv = appServerContext.getInstanceEnvironment()) == null) {
            failedObjects.append(lineSep).append(" appServerContext.getInstanceEnvironment()");
        }
        try {
            appsManager = new AppsManager(instEnv, false);
            
        } catch (ConfigException ce) {
            relatedException = ce;
            failedObjects.append(lineSep).append(" AppsManager(instEnv)");
        }
    
        try {
            appclientModulesManager = new AppclientModulesManager(instEnv, false);
        } catch (ConfigException ce) {
            relatedException = ce;  // Might override earlier assignment; ok, since it'd be rare and both are config exc.
            failedObjects.append(lineSep).append(" AppclientModulesManager(instEnv)");
        } finally {
            /*
             *If there were any failures to find the objects, report them.
             */
            if (failedObjects.length() > 0) {
                if (relatedException == null) {
                     relatedException = new IllegalStateException("Null returned");
                }
                throw new IllegalStateException("The following utility objects could not be initialized: " + failedObjects.toString(), relatedException);
            }
        }
    }

    /**
     *Loads the appserver files map.
     *<p>
     *This method loads the app server file entries into the map.  This map is
     *also used to populate part of the JNLP documents that list app
     *server files for Java Web Start.  This method is then the only place that needs to change
     *if the list of app server files needs to change.
     *@exception IOException indicates problems loading a dynamic template
     */
    private void preloadAppserverContent() throws IOException, Exception {
        
        /*
         *Build the collection of signed jar files as one content origin.
         *
         *Note that this may change so that the files listed below become part
         *of the previous group instead with only a small bootstrap jar needing
         *to be signed.
         */
        AppserverContentOrigin signedLibFilesOrigin = new AppserverContentOrigin(
                NamingConventions.APPSERVER_CATEGORY,
                NamingConventions.APPSERVER_LIB_FILES);

        addJWSACCStaticContent(signedLibFilesOrigin);
        
        /*
         *Add the app server lib files entry to the collection of app server entries.
         */
        signedAppserverOrigins.put(NamingConventions.APPSERVER_LIB_FILES, signedLibFilesOrigin);

        AppserverContentOrigin libFilesOrigin = new AppserverContentOrigin(
                NamingConventions.APPSERVER_CATEGORY,
                NamingConventions.APPSERVER_LIB_FILES);

        /*
         *Add a new static content object for each unsigned app server lib file to the
         *app server lib entry.
         */
        addAppserverStaticContent(libRoot, libFilesOrigin, false /* isMain */, 
                "appserv-rt.jar",
                "appserv-cmp.jar",
                "appserv-admin.jar",
                "appserv-deployment-client.jar",
                "javaee.jar",
                "jmac-api.jar",
                "appserv-ext.jar",
                "mail.jar",
                "activation.jar",
                "webservices-rt.jar",
                "webservices-tools.jar",
                "toplink-essentials.jar",
                "dbschema.jar"
                );

        /*
         *Add the app server lib files entry to the collection of app server origins.
         */
        appserverOrigins.put(NamingConventions.APPSERVER_LIB_FILES, libFilesOrigin);

        /*
         *Add the derby jar.
         */
        AppserverContentOrigin derbyLibFilesOrigin = new AppserverContentOrigin(
                NamingConventions.APPSERVER_CATEGORY,
                NamingConventions.APPSERVER_LIB_FILES);
        File derbyLib = new File(derbyRootDir, "lib");
        
    addAppserverStaticContent(derbyLib, derbyLibFilesOrigin, false /* isMain */, "derbyclient.jar");
        appserverOrigins.put(NamingConventions.APPSERVER_DERBY_FILES, derbyLibFilesOrigin);
        
        /*
         *Add static content objects for the mq files needed on the client and add
         *the origin to the appserver origins.
         */
        AppserverContentOrigin mqlibFilesOrigin = new AppserverContentOrigin(
            NamingConventions.APPSERVER_CATEGORY,
            NamingConventions.APPSERVER_LIB_FILES);

        File mqRoot = new File(installRootDir,  "imq");
        File mqlibRoot = new File(mqRoot, "lib");
        
        addAppserverStaticContent(mqlibRoot, mqlibFilesOrigin, false /* isMain */, "fscontext.jar");
        appserverOrigins.put(NamingConventions.APPSERVER_MQLIB_FILES, mqlibFilesOrigin);

        /*
         *Again, for the jms ra jar.
         */
        AppserverContentOrigin jmsraFilesOrigin = new AppserverContentOrigin(
            NamingConventions.APPSERVER_CATEGORY,
            NamingConventions.APPSERVER_LIB_FILES);
        
        File installRoot = new File(libRoot, "install");
        File appsRoot = new File(installRoot, "applications");
        File jmsraRoot = new File(appsRoot, "jmsra");
        
        addAppserverStaticContent(jmsraRoot, jmsraFilesOrigin, false /* isMain */, "imqjmsra.jar");
        
        appserverOrigins.put(NamingConventions.APPSERVER_JMSRALIB_FILES, jmsraFilesOrigin);
        
        /*
         *Add static content for the extension jars.
         */
        AppserverContentOrigin extJarsOrigin = new AppserverContentOrigin(
                NamingConventions.APPSERVER_CATEGORY,
                NamingConventions.APPSERVER_LIB_FILES);
        
        Map<ExtensionFileManager.ExtensionKey,ExtensionFileManager.Extension> extFileInfo = extensionFileManager.getExtensionFileEntries();
        
        for (ExtensionFileManager.Extension e : extFileInfo.values()) {
            File f = e.getFile();
            String path = NamingConventions.extJarFilePath(e.getExtDirectoryNumber(), f);
            String contentKey = extJarsOrigin.getContentKeyPrefix() + path;
            AppserverStaticContent extContent = new AppserverStaticContent(
                extJarsOrigin,
                contentKey,
                path,
                f,
                installRootURI,
                false);
                
            extJarsOrigin.pathToContent.put(extContent.getContentKey(), extContent);
            contentMap.put(extContent.getContentKey(), extContent);
        }
        
        extJarAppserverOrigins.put(NamingConventions.APPSERVER_EXTJAR_FILES, extJarsOrigin);
    }

    private void addJWSACCStaticContent(AppserverContentOrigin origin) throws Exception {

        String signedJarFileName = NamingConventions.SignedJar.signedJarPath(UNSIGNED_JWSACC_JARFILE_NAME);
        String path = "/" + signedJarFileName;
        String contentKey = origin.getContentKeyPrefix() + path;
        File unsignedJar = new File(installRootDir, "lib" + File.separator + UNSIGNED_JWSACC_JARFILE_NAME);
        File signedJar = new File(instEnv.getJavaWebStartPath(), signedJarFileName);
            
        SignedStaticContent content = new SignedStaticContent(
            origin,
            contentKey, 
            path,
            signedJar,
            unsignedJar,
            installRootURI,
            localStrings,
            true /* isMain */
            );
            
        addAppserverStaticContent(origin, content);
    }

    /**
     *Adds static content objects to an origin.
     *
     *@param prefix to use at the beginning of the URL path for these files
     *@param the directory in which the files actually reside
     *@param the appserver origin whose pathToContent map should contain mappings for these paths to the files
     *@param list of file names
     */
    private void addAppserverStaticContent(File dir, AppserverContentOrigin origin, boolean isMain, String... fileName) {
        for (String fn : fileName) {
            String path = "/" + fn;
            String contentKey = origin.getContentKeyPrefix() + path;
            
            AppserverStaticContent content = new AppserverStaticContent(
                origin,
                contentKey, 
                path, 
                new File(dir, fn), 
                installRootURI, 
                isMain);

            /*
             *Add the new content object both to the origin's map and also to 
             *the global content map.
             */
            addAppserverStaticContent(origin, content);
        }
    }

    /**
     *Adds static content objects to an origin.
     *
     *@param prefix to use at the beginning of the URL path for these files
     *@param the directory in which the files actually reside
     *@param the appserver origin whose pathToContent map should contain mappings for these paths to the files
     *@param list of file names
     */
    private void addAppserverStaticContent(AppserverContentOrigin origin, StaticContent content) {
        /*
         *Add the new content object both to the origin's map and also to 
         *the global content map.
         */
        origin.pathToContent.put(content.getContentKey(), content);
        contentMap.put(content.getContentKey(), content);
    }

    /**
     *Creates Properties object containing names and values of placeholders 
     *known when an appclient or application is loaded.  This excludes information
     *available only when a request arrives.  This Properties object is used
     *to substitute the values for the names in the generated documents.
     *<p>
     *All properties are defined in a single Properties object, and that Properties
     *instance is used in processing all dynamic documents before those dynamic 
     *documents are stored in the maps.  At request-time, further substitution
     *occurs with information available only then.
     *@param the origin for which values are to be assigned
     *@param the app client's specified main class name
     *@param the name of a class present in the generated app client jar file
     *@return Properties object containing the token-to-value correspondence
     *@throws IOException for errors getting information 
     */
    protected Properties prepareInitPlaceholders(
            AppclientContentOrigin origin, 
            String mainClassName,
            String location
                ) throws ConfigException, URISyntaxException {
        String appclientRegName = origin.getTopLevelRegistrationName();

        Properties answer = new Properties();

        answer.setProperty("appserver.codebase.path", NamingConventions.appServerCodebasePath());
        
        answer.setProperty("appclient.codebase.path", NamingConventions.appclientCodebasePath(origin));
        answer.setProperty("appclient.context-root", origin.getContextRoot());
        String vendor = origin.getVendor();
        answer.setProperty("appclient.vendor", (vendor == null || vendor.length() == 0) ? localStrings.getString("jws.defaultVendorName") : vendor);
        answer.setProperty("appclient.client.jnlp.filename", NamingConventions.Client.JNLPFilename(appclientRegName));
        answer.setProperty("appclient.client.html.filepath", NamingConventions.Client.HTMLPath(appclientRegName));
        answer.setProperty("appclient.mainext.jnlp.filename", NamingConventions.Main.JNLPExtFilename(appclientRegName));
        
        /*
         *Stop-gap support for specifying image and splash image.
         */
        String imageElements = prepareImageInfo(origin, location);
        answer.setProperty("appclient.information.images", imageElements);
        
//        answer.setProperty("default.wss.client.config.path", NamingConventions.defaultWSSClientConfigPathInJNLP());
//        
//        answer.setProperty("default.sun.acc.xml.url.property.name", com.sun.enterprise.appclient.MainWithModuleSupport.DEFAULT_SUN_ACC_URL_PROPERTY);
//        answer.setProperty("default.wss.client.config.xml.url.property.name", com.sun.enterprise.appclient.MainWithModuleSupport.DEFAULT_WSS_CONFIG_URL_PROPERTY);
//
//        answer.setProperty("default.sun.acc.xml.path", NamingConventions.defaultSunACCPathInJNLP());
//        answer.setProperty("security.config.file.property.name", com.sun.enterprise.appclient.MainWithModuleSupport.SUN_ACC_SECURITY_CONFIG_PROPERTY);

        prepareIIOPProperties(answer);
        
        /*
         *The Java Web Start ACC creates temporary files that are marked for deletion on exit. 
         *For diagnostic purposes, administrators can set a property on the server to
         *ask that these files be retained on the client.  The property value is sent to
         *each client when the user requests the main JNLP document.
         */
        boolean retainTempFiles = Boolean.getBoolean(SERVER_RETAIN_TEMP_FILES_PROPERTYNAME);
        answer.setProperty("appclient.retainTempFiles.propertyName", MainWithModuleSupport.APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME);
        answer.setProperty("appclient.retainTempFiles", String.valueOf(retainTempFiles));
        
        /*
         *Set the JNLP information title to the app client module's display name,
         *if one is present.
         */
        String displayName = origin.getDisplayName();
        String jnlpInformationTitle = (displayName != null && displayName.length() > 0) ? displayName : "Application Client " + appclientRegName;
        answer.setProperty("appclient.information.title", jnlpInformationTitle);
        
        /*
         *Set the file part of the homepage URL.
         */
        String jnlpInformationHomepageFilepath = NamingConventions.Main.HTMLPath(appclientRegName);
        answer.setProperty("appclient.information.homepage.filepath", jnlpInformationHomepageFilepath);
        
        /*
         *Set the one-line description the same as the title for now.
         */
        answer.setProperty("appclient.information.description.one-line", jnlpInformationTitle);
        
        /*
         *Set the short description to the description from the descriptor, if any.
         */
        String description = origin.getDescription();
        String jnlpInformationShortDescription = (description != null && description.length() > 0) ? description : jnlpInformationTitle;
        answer.setProperty("appclient.information.description.short", jnlpInformationShortDescription);
        
        /*
         *Set up variables used for populating descriptive information in the
         *JNLP document that lists app server jars.  Although this document is
         *generated for each separate app client that is loaded, it's content
         *is basically the same for all since all app clients rely on the same
         *app server jars.
         */
        prepareAppserverPlaceholders(answer);
        
        /*
         *If a main class is specified in the manifest, use it to specify the
         *main class argument to ACC.  Otherwise, do not emit the mainclass
         *argument into the JNLP document and let the app client container
         *process any user-supplied mainclass or name argument.
         */
        String mainClassArgsValue = "";
        if ((mainClassName != null) && ( ! mainClassName.equals("")) ) {
            mainClassArgsValue = "        <argument>-mainclass</argument>" + lineSep + 
                    "        <argument>" + mainClassName + "</argument>";
        }
        answer.setProperty(
            "appclient.main.class.arguments", mainClassArgsValue);

        /*
         *Set a property that indicates to the app client container that it is
         *running under Java Web Start and what the download host name is.
         */
        answer.setProperty("appclient.isJWS.propertyName", MainWithModuleSupport.APPCLIENT_ISJWS_PROPERTYNAME);
        answer.setProperty("appclient.download.host.propertyName", MainWithModuleSupport.APPCLIENT_DOWNLOAD_HOST_PROPERTYNAME);
        answer.setProperty("appclient.user.code.is.signed.propertyName", AppClientInfo.USER_CODE_IS_SIGNED_PROPERTYNAME);
        return answer;
    }
    
    /**
     *Returns XML to specify the icon image, the splash screen image, neither, or 
     *both, depending on the contents of the <vendor> text in the descriptor.
     *@param origin the AppclientContentOrigin
     *@return XML specifying one or both images; empty if the developer encoded 
     *no image information in the <vendor> element.
     */
    private String prepareImageInfo(AppclientContentOrigin origin, String location) throws ConfigException, URISyntaxException {
        StringBuilder result = new StringBuilder();
        String imageURI = origin.getImageURI();
        if (imageURI.length() > 0) {
            result.append("<icon href=\"" + imageURI + "\"/>");
            addImageContent(origin, location, imageURI);
        }
        String splashImageURI = origin.getSplashImageURI();
        if (splashImageURI.length() > 0) {
            result.append("<icon kind=\"splash\" href=\"" + splashImageURI + "\"/>");
            addImageContent(origin, location, splashImageURI);
        }
        return result.toString();
    }
    
    private void addImageContent(AppclientContentOrigin origin, String location, String imageURI) throws ConfigException, URISyntaxException {
        addStaticContent(origin, ensureLeadingSlash(imageURI), new File(location, imageURI)); 
    }
    
    private String ensureLeadingSlash(String s) {
        if ( ! s.startsWith("/")) {
            return "/" + s;
        } else {
            return s;
        }
    }

    /**
     *Prepares property value assignments for IIOP connectitivy.
     */
    private void prepareIIOPProperties(Properties props) {
        /*
         *Set the default IIOP information for the app client based on this server.  The template
         *uses the host retrieved from the incoming request as the default host value. Note that 
         *because this code and the app client container Main both must use the same property names
         *for this to work, those names are defined as constants in Main and the constants are used
         *both there and here.
         */
        props.setProperty("appclient.iiop.defaultHost.propertyName", MainWithModuleSupport.APPCLIENT_IIOP_DEFAULTHOST_PROPERTYNAME);
        props.setProperty("appclient.iiop.defaultPort.propertyName", MainWithModuleSupport.APPCLIENT_IIOP_DEFAULTPORT_PROPERTYNAME);
        props.setProperty("appclient.iiop.defaultPort", String.valueOf(ORBManager.getORBInitialPort()));
        
        /*
         *If this instance participates in a failover group, prepare the property 
         *that the JWS-aware ACC will recognize.
         */
        String failoverEndpoints = getIIOPEndpoints();
        String failoverEndpointsSetting = "";
        if (failoverEndpoints != null) {
            failoverEndpointsSetting = "<property name=\"" + MainWithModuleSupport.APPCLIENT_IIOP_FAILOVER_ENDPOINTS_PROPERTYNAME + "\" value=\"" + failoverEndpoints + "\"/>";
        }
        props.setProperty("appclient.iiop.failover.endpoints", failoverEndpointsSetting);
    }
    
    /**
     *Returns the IIOP endpoints in the cluster in which the current instance
     *participates.
     *@return String suitable for use in defining the IIOP property for endpoints; null if this instance is not in a cluster
     */
    private String getIIOPEndpoints() {
        String result = com.sun.enterprise.iiop.IIOPEndpointsInfo.getIIOPEndpoints();
        return result;
    }

    /**
     *Adds values for placeholders used in the generated JNLP document that
     *lists app server jars.  These values do not depend on the particular app
     *client but are retrieved from the local strings bundle.
     *@param p the Properties holding placeholders and their values
     */
    private void prepareAppserverPlaceholders(Properties p) {
        p.setProperty(
                "appserver.information.title", 
                localStrings.getString("jws.appserver.information.title"));
        p.setProperty(
                "appserver.information.vendor", 
                localStrings.getString("jws.appserver.information.vendor"));
        p.setProperty(
                "appserver.information.description.one-line", 
                localStrings.getString("jws.appserver.information.description.one-line"));
        p.setProperty(
                "appserver.information.description.short", 
                localStrings.getString("jws.appserver.information.description.short"));
    }
    
    /**
     *Prepares the document entries for an application's nested app clients.
     *@param application the Application descriptor
     *@param moduleDescrs array of ModuleDescriptor for the app's submodules
     *@param earDirectory the directory where the app is deployed
     *@param generatedXMLLocation the directory where the app's generated XML files reside
     */
    private ApplicationContentOrigin prepareApplication(Application application, 
            ModuleDescriptor [] moduleDescrs, String earDirectory, String generatedXMLLocation) 
        throws IOException, URISyntaxException, SAXParseException, ConfigException, Exception {
        
        ApplicationContentOrigin result = new ApplicationContentOrigin(application);
        
        /*
         *Add a static content instance for the single combined client jar file
         *for all app clients in this module.
         */
        SignedStaticContent jarFileContent = addAppclientJarContent(result, generatedXMLLocation);

        String jarHrefs = jarFileContent.asJNLPJarElement();
        
        /**
         *Each module descriptor in the array represents an app client that is eligible
         *for Java Web Start services.  For each, create a nested app client content
         *origin and add it to the parent application content origin.
         */
        for (ModuleDescriptor md : moduleDescrs) {
            /*
             *Get the descriptor for the app client of interest.
             *The prepareAppClient method uses it to extract the main class for use as a command line argument.
             */
            Manifest mf = getManifest(earDirectory, md);
            Attributes mainAttrs = mf.getMainAttributes();
            String mainClassName = mainAttrs.getValue(Attributes.Name.MAIN_CLASS);
            String appclientJarURI = md.getArchiveUri();

            /*
             *Extension libraries are not bundled into the generated jar file.
             *So any relevant extension library jar needs to be added as static
             *content for this appclient origin.  An extension library could
             *be referenced from a jar that appears in the app client's 
             *Class-Path expression.  URIs there are relative to the directory
             *that contains the jar with the Class-Path.  So we need the 
             *directory that contains the developer's app client jar.
             */
            File appclientJar = new File(appclientJarURI);
            File appclientJarDir = appclientJar.getParentFile();
            
            if ( ! appclientJar.isAbsolute()) {
                appclientJar = new File(earDirectory, appclientJarURI);
            }
            
            /*
             *Create and add the nested origin.
             */
            NestedAppclientContentOrigin nestedAppclient = prepareNestedAppclient(
                    result, 
                    md,
                    earDirectory,
                    jarHrefs, 
                    mainClassName, 
                    mainAttrs, 
                    appclientJar.getParent(), 
                    jarFileContent);
            result.addNestedOrigin(nestedAppclient);
        }
        
        return result;
    }
    
    /**
     *Returns the manifest for the nested app client specified by the
     *module descriptor.
     *@param earDirectoryPath the directory into which the ear has been expanded
     *@param md the ModuleDescriptor for the embedded app client of interest
     */
    private Manifest getManifest(String earDirectoryPath, ModuleDescriptor md) {
        /*
         *Use the same logic that the J2EEModuleExploder uses to choose the
         *name for the embedded app client's subdirectory.
         */
        String submoduleDirSpec = FileUtils.makeFriendlyFilename(md.getArchiveUri());
        File submoduleDir = new File(earDirectoryPath, submoduleDirSpec);
        Manifest mf = EJBClassPathUtils.getManifest(submoduleDir.getAbsolutePath());
        return mf;
    }
    
    /**
     *Prepares the content origin for a top-level app client.
     *<p>
     *The object returned describes a single, identifiable origin for content pertinent
     *to a single app client.
     *@param application the Application for the app client
     *@param moduleDescr the ModuleDescriptor describing the app client
     *@param location the directory where the app client resides
     *@param generatedXMLLocation the directory where the app client's generated XML files reside
     *@return the populated content origin object for the app client; null if there is a problem creating it
     *@throws IOException for errors reading the app client's manifest
     *@throws URISyntaxException for errors preparing URIs for static content
     *@throws ConfigException for errors finding the app client's generated JAR directory
     */
    private AppclientContentOrigin prepareTopLevelAppclient(Application application, 
            ModuleDescriptor moduleDescr, String location, String generatedXMLLocation) 
        throws IOException, URISyntaxException, ConfigException, FileNotFoundException, SAXParseException, Exception {
        
        String contextRoot = NamingConventions.TopLevelAppclient.defaultVirtualContextRoot(application);

        AppclientContentOrigin result = new AppclientContentOrigin(application, moduleDescr, contextRoot);
        String regName = result.getTopLevelRegistrationName();
        
        /*
         *Potentially, there could be multiple jar files needed by this app client.  
         *Currently, only the generated app client jar is needed.  Build the
         *string containing <jar href...> elements for insertion into the
         *client JNLP document.
         */
        
        String appclientJarPath = result.getAppclientJarPath();
        
        /*
         *Add a static content instance for the single combined client jar file
         *for all app clients in this module.
         */
        StaticContent jarFileContent = addAppclientJarContent(result, generatedXMLLocation);

        String jarHrefs = jarFileContent.asJNLPJarElement();

        String dirPath = location;
        Manifest mf = EJBClassPathUtils.getManifest(dirPath);
        Attributes mainAttrs = mf.getMainAttributes();
        String mainClassName = mainAttrs.getValue(Attributes.Name.MAIN_CLASS);
        
        /*
         *Perform the rest of the initialization which is shared with nested app client origins.
         */
        prepareAppclient(result, application, moduleDescr, dirPath, jarHrefs, regName, mainClassName, mainAttrs, dirPath, jarFileContent);
        return result;
    }
    
    /**
     *Performs initialization that is common between top-level and nested app clients.
     *@param origin the app client origin object to be further set up
     *@param application the Application for this app client
     *@param moduleDescr the ModuleDescriptor for this app client
     *@param location the directory holding the app client files
     *@param jarHRefs a string representing the module name to be used in constructing virtual file names
     *@param modName the name of the module
     *@param mainClassName the name of the main class for this app client
     *@param mainAttrs the main attributes for the client that may contain extension jar requirements
     *@param dirPathForRelativeClassPathEntries location of the jar file to be used in resolving relative Class-Path manifest entries
     *@param appclientJarContent the content object describing the app client jar to publish
     */
    private void prepareAppclient(
            AppclientContentOrigin origin, 
            Application application, 
            ModuleDescriptor moduleDescr, 
            String location, 
            String jarHRefs, 
            String modName, 
            String mainClassName, 
            Attributes mainAttrs,
            String dirPathForRelativeClassPathEntries,
            StaticContent appclientJarContent) 
                throws FileNotFoundException, IOException, URISyntaxException, ConfigException {

        /*
         *Using information now available, assemble as much of the JNLP and HTML
         *documents as possible and store them with the content origin object
         *for this app client.  As it handles specific incoming
         *requests, the JWS system servlet will do further substitutions.
         */
        Properties tokenValues = prepareInitPlaceholders(
                origin, 
                mainClassName,
                location
                );
        
        /*
         *Prepare the main JNLP document.
         *
         *Build a string containing the <jar> elements for the app server files 
         *that need to be listed in the main JNLP document.  Add the placeholder
         *name and the resulting string to the token values.
         *
         *Get the template, merge with user content (future), and substitute for placeholders.
         *Then add the now partially-substituted main JNLP document template to
         *the content map.
         */
        
        StringBuilder appserverJarElements = buildJarElements(appserverOrigins);
        StringBuilder signedAppserverJarElements = buildJarElements(signedAppserverOrigins);
        
        /*
         *Build hrefs for the extension jars in this app client.  The hrefs will be
         *inserted into the JNLP document for the app client.
         */
        Set<ExtensionFileManager.Extension> earExtJars = findExtensions(mainAttrs, dirPathForRelativeClassPathEntries);
        
        String earJarHrefs = prepareHrefsForFiles(earExtJars);
        
        
        tokenValues.setProperty("appserver.jar.elements", appserverJarElements.toString());
        tokenValues.setProperty("appserver.jar.elements.signed", signedAppserverJarElements.toString());
        tokenValues.put("appclient.jar.elements", jarHRefs);

        /*
         *Prepare the main JNLP document.
         */
        String mainJNLPPath = NamingConventions.Main.JNLPPath(modName);
        addDynamicContent(
                origin,
                mainJNLPPath,
                NamingConventions.APPCLIENT_MAIN_JNLP_TEMPLATE_NAME,
                tokenValues,
                JNLP_MIME_TYPE,
                true /* requiresElevatedPrivs */
                );

        /*
         *Prepare the main extension JNLP document.
         */
        String mainExtJNLPPath = NamingConventions.Main.JNLPExtPath(modName);
        addDynamicContent(
                origin,
                mainExtJNLPPath, 
                NamingConventions.APPCLIENT_MAIN_JNLP_EXT_TEMPLATE_NAME, 
                tokenValues, 
                JNLP_MIME_TYPE);
        
        /*
         *Prepare the main HTML document.
         */
        String mainHTMLPath = NamingConventions.Main.HTMLPath(modName);
        addDynamicContent(
                origin,
                mainHTMLPath, 
                NamingConventions.APPCLIENT_MAIN_HTML_TEMPLATE_NAME, 
                tokenValues, 
                HTML_MIME_TYPE);
        
        /*
         *Prepare the client HTML document.
         */
        String clientHTMLPath = NamingConventions.Client.HTMLPath(modName);
        addDynamicContent(
                origin,
                clientHTMLPath, 
                NamingConventions.APPCLIENT_CLIENT_HTML_TEMPLATE_NAME, 
                tokenValues, 
                HTML_MIME_TYPE);
        
        /*
         *Prepare the client JNLP document.  Get template, merge (future), substitute, add to map.
         *Note that this document's template contains a placeholder for the list of <jar>
         *elements to describe all user-provided jars it needs.  
         */
        String clientJNLPPath = NamingConventions.Client.JNLPPath(modName);
        
        String contentKey = origin.getContentKeyPrefix() + clientJNLPPath;
        String docText = Util.replaceTokens(
                templateCache.getTemplate(NamingConventions.APPCLIENT_CLIENT_JNLP_TEMPLATE_NAME),
                tokenValues);

        DynamicContent clientJNLPContent = new DynamicContent(
                origin,
                contentKey,
                clientJNLPPath,
                docText,
                JNLP_MIME_TYPE,
                true /* requiresElevatedPrivs */
                );
        
        /*
         *The security settings for the app client jar dynamic content is set
         *when the request arrives by checking the current availability of a
         *signed jar at that time. So it is not set here.
         */
        addDynamicContent(origin, clientJNLPContent);
    }

    /**
     *Returns an initialized nested app client content origin.
     *@param parent the parent application content origin
     *@param moduleDescr the module descriptor for the app client
     *@param location the directory containing the deployed parent EAR's files
     *@param jarHRefs a string representing the module name to be used in constructing virtual file names
     *@param mainClassName the name of the main class for this app client
     *@param mainAttrs the main attributes for the client that may contain extension jar requirements
     *@param dirPathForRelativeClassPathEntries location of the jar file to be used in resolving relative Class-Path manifest entries
     *@param appclientJarContent the content object describing the app client jar to publish
     *@return the new NestedAppclientContentOrigin
     */
    private NestedAppclientContentOrigin prepareNestedAppclient(
            ApplicationContentOrigin parent, 
            ModuleDescriptor moduleDescr,
            String location,
            String jarHRefs, 
            String mainClassName, 
            Attributes mainAttrs,
            String dirPathForRelativeClassPathEntries,
            StaticContent appclienJarContent)
                throws FileNotFoundException, IOException, URISyntaxException, ConfigException {
        
        String contextRoot = NamingConventions.NestedAppclient.defaultVirtualContextRoot(parent.getApplication(), moduleDescr);
        
        NestedAppclientContentOrigin result = new NestedAppclientContentOrigin(parent, moduleDescr, contextRoot);

        prepareAppclient(
                result, 
                parent.getApplication(), 
                moduleDescr, 
                location, 
                jarHRefs, 
                result.getName(), 
                mainClassName, 
                mainAttrs, 
                dirPathForRelativeClassPathEntries,
                appclienJarContent
                );
        
        return result;
    }
    
    /**
     *Refreshes the config context in use by the apps manager and the app 
     *client modules manager.  
     *@throws ConfigException in case of error refreshing either context
     */
    private void refreshConfigContextForManagers(ConfigContext configContext) throws ConfigException {
        if (configContext == null) {
            if (! ServerHelper.isDAS(appsManager.getConfigContext(), instEnv.getName())) {
                appsManager.refreshConfigContext();
                appclientModulesManager.refreshConfigContext();
            }
        } else {
            appsManager.refreshConfigContext(configContext);
            appclientModulesManager.refreshConfigContext(configContext);
        }
    }
    
    /**
     * Returns Extension objects for the extension jars cited by the app client.
     * The jars represented by the Extension objects need to be available to app clients in the ear
     * (when dealing with an ear) or to the single stand-alone app client (when
     * dealing with a stand-alone app client).
     * @param attrs the main attributes of the jar of interest
     * @param appDirPath the app's directory to be used in resolving relative Class-Path entries
     * @return Set containing File objects for the required jars
     * @throws IOException for any error while processing the archive
     * @throws ConfigException for any error retrieving information about the application
     */
    private Set<ExtensionFileManager.Extension> findExtensions(Attributes attrs, String appDirPath) throws IOException, ConfigException {
        Set<ExtensionFileManager.Extension> result = null;
        
        File appDir = new File(appDirPath);
        result = extensionFileManager.findExtensionTransitiveClosure(appDir, attrs);
        return result;
    }
    
    private Manifest loadManifestFromFile(String dirPath) throws IOException {
        Manifest result = null;
        InputStream is = null;
        try {
            File manifestFile = new File(dirPath, JarFile.MANIFEST_NAME);
            if (manifestFile.exists() && manifestFile.canRead()) {
                is = new FileInputStream(manifestFile);
                result = new Manifest(is);
            }
            return result;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    /**
     * Converts a set of ExtensionFileManager.Extension objects into a String containing JNLP-friendly
     * HREFs for those files.
     * @param extJarInfo List of ExtensionFileManager.Extension objects representing the jars
     * @return String containing an HREF for each jar in the set
     */
    private String prepareHrefsForFiles(Set<ExtensionFileManager.Extension> extJarInfo) {
        ContentOrigin extJarsOrigin = extJarAppserverOrigins.get(NamingConventions.APPSERVER_EXTJAR_FILES);
        StringBuilder result = new StringBuilder();
        for (ExtensionFileManager.Extension e : extJarInfo) {
            String path = NamingConventions.extJarFilePath(e.getExtDirectoryNumber(), e.getFile());
            String contentKey = extJarsOrigin.getContentKeyPrefix() + path;
            StaticContent content = (StaticContent) extJarsOrigin.getContent(contentKey);
            result.append(content.asJNLPJarElement()).append(lineSep);
        }
        return result.toString();
    }

    /**
     *Convenience method to both add content to the origin and also to the 
     *overall content map.
     *@param origin content origin to which to add the content
     *@param path URI path by which the content can be addressed within its origin
     *@param templateName name of the template to use for this dynamic content
     *@param tokenValues placeholder->value Properties object
     *@param mimeType MIME type of this content
     *@param requiresElevatedPrivs indicates if the JNLP should request privs
     *@return the newly-created Content
     *@throws IOException in case of errors retrieving the template
     */
    private DynamicContent addDynamicContent(
            ContentOrigin origin, 
            String path, 
            String templateName, 
            Properties tokenValues, 
            String mimeType,
            boolean requiresElevatedPrivs) throws IOException {
         
        DynamicContent content = origin.addDynamicContent(
                path, 
                templateCache.getTemplate(templateName), 
                tokenValues, 
                mimeType, 
                requiresElevatedPrivs);
        contentMap.put(content.getContentKey(), content);
        return content;
    }
     
    /**
     *Convenience method to both add content to the origin and also to the 
     *overall content map.
     *@param origin content origin to which to add the content
     *@param path URI path by which the content can be addressed within its origin
     *@param templateName name of the template to use for this dynamic content
     *@param tokenValues placeholder->value Properties object
     *@param mimeType MIME type of this content
     *@return the newly-created Content
     *@throws IOException in case of errors retrieving the template
     */
    private DynamicContent addDynamicContent(
            ContentOrigin origin,
            String path, 
            String templateName, 
            Properties tokenValues, 
            String mimeType) throws IOException {
        return addDynamicContent(origin, path, templateName, tokenValues, mimeType, false /* requiredElevatedPrivs */);
    }
    
    private DynamicContent addDynamicContent(
            ContentOrigin origin, 
            DynamicContent content) {
         
        origin.addDynamicContent(content);
        contentMap.put(content.getContentKey(), content);
        return content;
    }

    /**
     *Convenience method to both add content to the origin and also to the 
     *overall content map.
     *@param the content origin to which to add the content
     *@param the path by which the content can be addressed within its origin
     *@param the file where the static content resides
     *@return the newly-created Content
     *@throws URISyntaxException in case of errors deriving a relative URI for the file
     */
     private StaticContent addStaticContent(
             ContentOrigin origin, 
             String path, 
             File file) throws URISyntaxException {
         
         StaticContent content = origin.addStaticContent(path, installRootURI, file);
         contentMap.put(content.getContentKey(), content);
         return content;
     }
     
     /**
      *Convenience method to both add content to the origin and also to the
      *overall content map.
      *@param origin the origin to add the content to
      *@param content the StaticContent instance to add
      *@returns the content added to the origin
      */
     private StaticContent addStaticContent(
             ContentOrigin origin,
             StaticContent content) throws URISyntaxException {
         
         origin.addStaticContent(content);
         contentMap.put(content.getContentKey(), content);
         return content;
     }
     
     /**
      *Adds the special app client static content object to the application 
      *origin and to the global content map.
      *@param origin the ApplicationContentOrigin to add the content to
      *@param generatedXMLLocation directory containing generated files for this app client
      *@return the newly-added static content
      */
     private SignedStaticContent addAppclientJarContent(
             ApplicationContentOrigin origin,
             String generatedXMLLocation) throws FileNotFoundException, URISyntaxException, Exception {

         return addAppclientJarContent(origin, origin.getAppclientJarPath(), generatedXMLLocation);
     }
     
     /**
      *Adds the special app client static content object to the app client
      *origin and to the global content map.
      *@param origin the AppclientContentOrigin to add the content to
      *@param generatedXMLLocation directory containing generated files for this app client
      *@return the newly-added static content
      */
     private SignedStaticContent addAppclientJarContent(
             AppclientContentOrigin origin,
             String generatedXMLLocation) throws FileNotFoundException, URISyntaxException, Exception {
         
         return addAppclientJarContent(origin, origin.getAppclientJarPath(), generatedXMLLocation);
     }
     
     /**
      *Adds the app client content to the specified origin, regardless of which
      *type of user content origin is involved.
      *@param origin the application or app client origin to add the content to
      *@param appclientJarPath path to the generated app client jar
      *@param generatedXMLLocation directory containing the app client's generated files
      *@return the created StaticContent for the app client jar file
      */
     private SignedStaticContent addAppclientJarContent(
             UserContentOrigin origin,
             String appclientJarPath,
             String generatedXMLLocation) throws FileNotFoundException, URISyntaxException, Exception {
         
         File generatedJar = origin.locateGeneratedAppclientJarFile(generatedXMLLocation);
         File signedGeneratedJar = 
                 NamingConventions.SignedJar.signedGeneratedAppclientJarFile(
                        origin,
                        instEnv,
                        generatedJar);
         String contentKey = origin.getContentKeyPrefix() + "/" + signedGeneratedJar.getName();
        
         SignedStaticContent content = new SignedStaticContent(
                 origin,
                 contentKey,
                 appclientJarPath,
                 signedGeneratedJar,
                 generatedJar,
                 installRootURI,
                 localStrings,
                 false /* isMain */);
        
         addAppclientJarContent(origin, content);
         return content;
     }
     
     /**
      *Adds the app client jar content to the origin - as with any content - but 
      *also identifies it as the app client jar.  This information is used
      *during the generation of the main JNLP document and the client JNLP
      *document when deciding if the app client jar is signed or not.
      *@param origin the origin to which to add the app client jar content
      *@param content the app client jar static content object
      *@return the content object itself
      */
     private SignedStaticContent addAppclientJarContent(UserContentOrigin origin, SignedStaticContent content) throws URISyntaxException {
         addStaticContent(origin, content);
//         origin.setPublishedAppclientJarStaticContent(content);
         return content;
         
     }

     /**
     *Returns a StringBuilder containing XML syntax suitable for inclusion in
     *a JNLP document's resources section listing the jar files required
     *by this module.
     *<p>
     *The return type is StringBuilder (rather than String) so the caller
     *can, if needed, add additional text to the result conveniently.
     *
     *@param map containing content origins of a particular category
     *@return StringBuilder containing JAR element text for the required jar files
     */
    private StringBuilder buildJarElements(Map<String,ContentOrigin> origins) {

        StringBuilder appserverJarElements = new StringBuilder();
        /*
         *Go through each of the groups of app server files (aslib, mqlib, etc.)
         */
        for (ContentOrigin origin : origins.values()) {
            /*
             *For this group, go through all the files.
             */
            for (Content c : origin.pathToContent.values()) {
                if (c instanceof StaticContent) {
                    StaticContent sc = (StaticContent) c;
                    appserverJarElements.append(sc.asJNLPJarElement());
                }
            }
        }
        return appserverJarElements;
    }
    
    /**
     *Represents either an application config bean or an app client config bean.
     *<p>
     *This class encapsulates the differences between an application config bean 
     *and an app client config bean, as well as whether the information about
     *the app or app client config comes from the current environment's config context 
     *or from the config context supplied during a deployment event.
     */
    private abstract class EventConfig {

        /**
         *Creates an Applications config bean using the specified context.
         *<p>
         *If the specified context is null then the Applications config bean is
         *created using the current config context from either the appsManager or
         *appClientModulesManager, depending on whether an ApplicationConfig or
         *an AppClientConfig is being instantiated.
         *@param configContext the configuration the Applications bean should use
         *@return the Applications object based on the specified config
         */
        protected Applications getAppsConfig(ConfigContext configContext) throws ConfigException {
            if (configContext == null) {
                /*
                 *The config context from the event is null, which means this is a
                 *server restart.  In that case the config present in the appsManager
                 *or appClientModulesManager is current, so use that for the config.
                 */
                configContext = chooseManager().getConfigContext();
            }
            Applications apps = (Applications) ConfigBeansFactory.getConfigBeanByXPath(configContext, 
                            ServerXPathHelper.XPATH_APPLICATIONS);
            return apps;
        }
     
        /**
         *Returns the location string for the app or app client config.
         *@return the location (directory) for the module
         */
        abstract String getLocation();
        
        /**
         *Returns the location of the generated XML directory for the app or app client.
         *@return the generated XML directory location for the module
         */
        abstract String getGeneratedXMLLocation();
        
        /**
         *Returns a BaseManager, either AppsManager or AppClientModulesManager,
         *depending on which type of config object is being created.
         */
        abstract BaseManager chooseManager();
    }
    
    /**
     *Represents the config for an application.
     */
    private class ApplicationConfig extends EventConfig {
        
        private J2eeApplication appBean;
        
        private ApplicationConfig(ConfigContext configContext, String regName) throws ConfigException {
            appBean = getAppsConfig(configContext).getJ2eeApplicationByName(regName);
        }
        
        String getLocation() {
            return appBean.getLocation();
        }

        String getGeneratedXMLLocation() {
            ApplicationEnvironment env = instEnv.getApplicationEnvironment(appBean.getName());
            return env.getAppGeneratedXMLPath();
        }
        
        BaseManager chooseManager() {
            return appsManager;
        }
    }
    
    /**
     *Represents the config for an app client.
     */
    private class AppClientConfig extends EventConfig {
        
        private AppclientModule appClientBean;
        
        private AppClientConfig(ConfigContext configContext, String regName) throws ConfigException {
            appClientBean = getAppsConfig(configContext).getAppclientModuleByName(regName);
        }
        
        String getLocation() {
            return appClientBean.getLocation();
        }

        String getGeneratedXMLLocation() {
            ModuleEnvironment menv = instEnv.getModuleEnvironment(appClientBean.getName(),
                                                         DeployableObjectType.CAR); 
            return menv.getModuleGeneratedXMLPath();
        }
        
        BaseManager chooseManager() {
            return appclientModulesManager;
        }
    }
}
