/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.core;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.*;

import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;

import org.apache.catalina.Auditor;// IASRI 4823322
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
//HERCULES:add
import org.apache.catalina.Pipeline;
//end HERCULES:add
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.FilterMaps;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.MessageDestination;
import org.apache.catalina.deploy.MessageDestinationRef;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceParams;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.deploy.ServletMap;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.TldConfig;
import org.apache.catalina.util.CharsetMapper;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.catalina.util.ExtensionValidator;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.URLEncoder;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;
import org.apache.naming.ContextBindings;
import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.apache.naming.resources.WARDirContext;

/**
 * Standard implementation of the <b>Context</b> interface.  Each
 * child container must be a Wrapper implementation to process the
 * requests directed to a particular servlet.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.48 $ $Date: 2007/07/25 00:52:04 $
 */

public class StandardContext
    extends ContainerBase
    implements Context, Serializable
{
    private static transient Logger log = Logger.getLogger(
        StandardContext.class.getName());

    private static final ClassLoader standardContextClassLoader =
        StandardContext.class.getClassLoader();


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardContext component with the default basic Valve.
     */
    public StandardContext() {
        super();
        pipeline.setBasic(new StandardContextValve());
        namingResources.setContainer(this);
        broadcaster = new NotificationBroadcasterSupport();
    }
    
    /**
     * set a new pipeline (restricted to use by EE code)
     * HERCULES:add
     */    
    public void restrictedSetPipeline(Pipeline pl) {
        pl.setBasic(new StandardContextValve());
        pipeline = pl;
    }    


    // ----------------------------------------------------- Instance Variables


    /**
     * The alternate deployment descriptor name.
     */
    private String altDDName = null;


    /**
     * Associated host name.
     */
    private String hostName;


    /**
     * The set of application listener class names configured for this
     * application, in the order they were encountered in the web.xml file.
     */
    private String applicationListeners[] = new String[0];


    /**
     * The set of instantiated application event listener objects</code>.
     */
    private transient Object applicationEventListenersObjects[] = 
        new Object[0];


    /**
     * The set of instantiated application lifecycle listener objects</code>.
     */
    private transient Object applicationLifecycleListenersObjects[] = 
        new Object[0];


    /**
     * The set of application parameters defined for this application.
     */
    private ApplicationParameter applicationParameters[] =
        new ApplicationParameter[0];


    /**
     * The application available flag for this Context.
     */
    private boolean available = false;
    
    /**
     * The broadcaster that sends j2ee notifications. 
     */
    private NotificationBroadcasterSupport broadcaster = null;
    
    /**
     * The Locale to character set mapper for this application.
     */
    private transient CharsetMapper charsetMapper = null;


    /**
     * The Java class name of the CharsetMapper class to be created.
     */
    private String charsetMapperClass =
      "org.apache.catalina.util.CharsetMapper";


    /**
     * The path to a file to save this Context information.
     */
    private String configFile = null;


    /**
     * The "correctly configured" flag for this Context.
     */
    private boolean configured = false;


    /**
     * The security constraints for this web application.
     */
    private SecurityConstraint constraints[] = new SecurityConstraint[0];


    /**
     * The ServletContext implementation associated with this Context.
     */
    // START RIMOD 4894300
    /*
    private transient ApplicationContext context = null;
    */
    protected transient ApplicationContext context = null;
    // END RIMOD 4894300


    /**
     * Compiler classpath to use.
     */
    private String compilerClasspath = null;


    /**
     * Should we attempt to use cookies for session id communication?
     */
    private boolean cookies = true;


    /**
     * Should we allow the <code>ServletContext.getContext()</code> method
     * to access the context of other web applications in this server?
     */
    private boolean crossContext = false;


    /**
     * The "follow standard delegation model" flag that will be used to
     * configure our ClassLoader.
     */
    private boolean delegate = false;


     /**
     * The display name of this web application.
     */
    private String displayName = null;


    /** 
     * Override the default web xml location. ContextConfig is not configurable
     * so the setter is not used.
     */
    private String defaultWebXml;


    /**
     * The distributable flag for this web application.
     */
    private boolean distributable = false;


    /**
     * The document root for this web application.
     */
    private String docBase = null;


    /**
     * The exception pages for this web application, keyed by fully qualified
     * class name of the Java exception.
     */
    private HashMap exceptionPages = new HashMap();


    /**
     * The set of filter configurations (and associated filter instances) we
     * have initialized, keyed by filter name.
     */
    private HashMap filterConfigs = new HashMap();


    /**
     * The set of filter definitions for this application, keyed by
     * filter name.
     */
    private HashMap filterDefs = new HashMap();


    /**
     * The set of filter mappings for this application, in the order
     * they were defined in the deployment descriptor.
     */
    private FilterMap filterMaps[] = new FilterMap[0];


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardContext/1.0";


    /**
     * The set of classnames of InstanceListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String instanceListeners[] = new String[0];

    /**
     * The set of already instantiated InstanceListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private ArrayList instanceListenerInstances = new ArrayList();


    /**
     * The login configuration descriptor for this web application.
     */
    private LoginConfig loginConfig = null;


    /**
     * The mapper associated with this context.
     */
    private transient com.sun.grizzly.util.http.mapper.Mapper mapper = 
        new com.sun.grizzly.util.http.mapper.Mapper();


    /**
     * The naming context listener for this web application.
     */
    private transient NamingContextListener namingContextListener = null;


    /**
     * The naming resources for this web application.
     */
    private NamingResources namingResources = new NamingResources();


    /**
     * The message destinations for this web application.
     */
    private HashMap messageDestinations = new HashMap();


    /**
     * The MIME mappings for this web application, keyed by extension.
     */
    private HashMap<String,String> mimeMappings = new HashMap<String,String>();


    /**
     * The context initialization parameters for this web application,
     * keyed by name.
     */
    private HashMap parameters = new HashMap();


    /**
     * The request processing pause flag (while reloading occurs)
     */
    private boolean paused = false;


    /**
     * The public identifier of the DTD for the web application deployment
     * descriptor version we are currently parsing.  This is used to support
     * relaxed validation rules when processing version 2.2 web.xml files.
     */
    private String publicId = null;


    /**
     * The reloadable flag for this web application.
     */
    private boolean reloadable = false;


    /**
     * Unpack WAR property.
     */
    private boolean unpackWAR = true;


    /**
     * The DefaultContext override flag for this web application.
     */
    private boolean override = false;


    /**
     * The privileged flag for this web application.
     */
    private boolean privileged = false;


    /**
     * Should the next call to <code>addWelcomeFile()</code> cause replacement
     * of any existing welcome files?  This will be set before processing the
     * web application's deployment descriptor, so that application specified
     * choices <strong>replace</strong>, rather than append to, those defined
     * in the global descriptor.
     */
    private boolean replaceWelcomeFiles = false;


    /**
     * With proxy caching disabled, setting this flag to true adds 
     * Pragma and Cache-Control headers with "No-cache" as value. 
     * Setting this flag to false does not add any Pragma header,
     * but sets the Cache-Control header to "private".
     */
    private boolean securePagesWithPragma = true;


    /**
     * The security role mappings for this application, keyed by role
     * name (as used within the application).
     */
    private HashMap roleMappings = new HashMap();


    /**
     * The security roles for this application, keyed by role name.
     */
    private String securityRoles[] = new String[0];


    /**
     * The servlet mappings for this web application, keyed by
     * matching pattern.
     */
    private HashMap servletMappings = new HashMap();


    /**
     * The session timeout (in minutes) for this web application.
     */
    private int sessionTimeout = 30;
    
    /**
     * Has the session timeout (in minutes) for this web application
     * been over-ridden by web-xml
     * HERCULES:add
     */
    private boolean sessionTimeoutOveridden = false;    

    /**
     * The notification sequence number.
     */
    private long sequenceNumber = 0;
    
    /**
     * The status code error pages for this web application, keyed by
     * HTTP status code (as an Integer).
     */
    private HashMap statusPages = new HashMap();


    /**
     * The JSP tag libraries for this web application, keyed by URI
     */
    private HashMap taglibs = new HashMap();


    /**
     * The welcome files for this application.
     */
    private String welcomeFiles[] = new String[0];


    /**
     * The set of classnames of LifecycleListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String wrapperLifecycles[] = new String[0];


    /**
     * The set of classnames of ContainerListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String wrapperListeners[] = new String[0];


    /**
     * The pathname to the work directory for this context (relative to
     * the server's home if not absolute).
     */
    private String workDir = null;


    /**
     * Java class name of the Wrapper class implementation we use.
     */
    private String wrapperClassName = StandardWrapper.class.getName();
    private Class wrapperClass = null;

    /**
     * JNDI use flag.
     */
    private boolean useNaming = true;


    /**
     * Filesystem based flag.
     */
    private boolean filesystemBased = false;


    /**
     * Name of the associated naming context.
     */
    private String namingContextName = null;


    /**
     * Frequency of the session expiration, and related manager operations.
     * Manager operations will be done once for the specified amount of
     * backgrondProcess calls (ie, the lower the amount, the most often the
     * checks will occur).
     */
    private int managerChecksFrequency = 6;


    /**
     * Iteration count for background processing.
     */
    private int count = 0;


    /**
     * Caching allowed flag.
     */
    private boolean cachingAllowed = true;


    /**
     * Case sensitivity.
     */
    protected boolean caseSensitive = true;


    /**
     * Allow linking.
     */
    protected boolean allowLinking = false;


    /**
     * Cache max size in KB.
     */
    protected int cacheMaxSize = 10240; // 10 MB


    /**
     * Cache TTL in ms.
     */
    protected int cacheTTL = 5000;


    private boolean lazy=true;


    /**
     * Non proxied resources.
     */
    private transient DirContext webappResources = null;

    /*
     * Time (in milliseconds) it took to start this context
     */
    private long startupTime;

    /*
     * Time (in milliseconds since January 1, 1970, 00:00:00) when this
     * context was started
     */
    private long startTimeMillis;

    private long tldScanTime;

    // START SJSWS 6324431
    // Should the filter and security mapping be done
    // in a case sensitive manner
    protected boolean caseSensitiveMapping = true; 
    // END SJSWS 6324431

    // START S1AS8PE 4817642
    /**
     * The flag that specifies whether to reuse the session id (if any) from
     * the request for newly created sessions
     */
    private boolean reuseSessionID = false;
    // END S1AS8PE 4817642

    // START RIMOD 4642650
    /**
     * The flag that specifies whether this context allows sendRedirect() to
     * redirect to a relative URL.
     */
    private boolean allowRelativeRedirect = false;


    // END RIMOD 4642650

    /** Name of the engine. If null, the domain is used.
     */ 
    private String engineName = null;
    /* SJSAS 6340499
    private String j2EEApplication="none";
     */ 
    // START SJSAS 6340499
    private String j2EEApplication="null";
    // END SJSAS 6340499
    private String j2EEServer="none";

    // START IASRI 4823322
    /**
     * List of configured Auditors for this context.
     */
    private transient Auditor[] auditors = null;
    // END IASRI 4823322

    // START RIMOD 4868393
    /**
     * used to create unique id for each app instance.
     */
    private static int instanceIDCounter = 1;
    // END RIMOD 4868393

    /**
     * Attribute value used to turn on/off XML validation
     */
    private boolean webXmlValidation = false;

    private String jvmRoute;

    /**
     * Attribute value used to turn on/off XML namespace validation
     */
     private boolean webXmlNamespaceAware = false;


    /**
     * Attribute value used to turn on/off XML validation
     */
     private boolean tldValidation = false;


    /**
     * Attribute value used to turn on/off TLD XML namespace validation
     */
     private boolean tldNamespaceAware = false;
     
     
     /**
      * Is the context contains the JSF servlet.
      */
     protected boolean isJsfApplication = false;

     
     /**
      * Should we save the configuration.
      */
     private boolean saveConfig = true;
  
    /**
     * Array containing the safe characters set.
     */
    protected static final URLEncoder urlEncoder;

    // START S1AS8PE 4965017
    private boolean isReload = false;
    // END S1AS8PE 4965017

    /**
     * Alternate doc base resources
     */
    private ArrayList<AlternateDocBase> alternateDocBases = null;

    private boolean useMyFaces;

    /**
     * GMT timezone - all HTTP dates are on GMT
     */
    static {
        urlEncoder = new URLEncoder();
        urlEncoder.addSafeCharacter('~');
        urlEncoder.addSafeCharacter('-');
        urlEncoder.addSafeCharacter('_');
        urlEncoder.addSafeCharacter('.');
        urlEncoder.addSafeCharacter('*');
        urlEncoder.addSafeCharacter('/');
    }
  
    /**
     * Encoded path.
     */
    private String encodedPath = null;


    // ----------------------------------------------------- Context Properties

    public String getEncodedPath() {
        return encodedPath;
    }


    public void setName( String name ) {
        super.setName( name );
        encodedPath = urlEncoder.encode(name);
    }


    /**
     * Save config ?
     */
    public boolean isSaveConfig() {
        return saveConfig;
    }


    /**
     * Set save config flag.
     */
    public void setSaveConfig(boolean saveConfig) {
        this.saveConfig = saveConfig;
    }


    /**
     * Is caching allowed ?
     */
    public boolean isCachingAllowed() {
        return cachingAllowed;
    }


    /**
     * Set caching allowed flag.
     */
    public void setCachingAllowed(boolean cachingAllowed) {
        this.cachingAllowed = cachingAllowed;
    }


    /**
     * Set case sensitivity.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


    /**
     * Is case sensitive ?
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }


    // START SJSWS 6324431
    /**
     * Set case sensitivity for filter and security constraint mappings.
     */
    public void setCaseSensitiveMapping(boolean caseSensitiveMap) {
        caseSensitiveMapping = caseSensitiveMap;
    }

    /** 
     * Are filters and security constraints mapped in a case sensitive manner?
     */
    public boolean isCaseSensitiveMapping() {
        return caseSensitiveMapping;
    }
    // END SJSWS 6324431


    /**
     * Set allow linking.
     */
    public void setAllowLinking(boolean allowLinking) {
        this.allowLinking = allowLinking;
    }


    /**
     * Is linking allowed.
     */
    public boolean isAllowLinking() {
        return allowLinking;
    }


    /**
     * Set cache TTL.
     */
    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }


    /**
     * Get cache TTL.
     */
    public int getCacheTTL() {
        return cacheTTL;
    }


    /**
     * Return the maximum size of the cache in KB.
     */
    public int getCacheMaxSize() {
        return cacheMaxSize;
    }


    /**
     * Set the maximum size of the cache in KB.
     */
    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }


    /**
     * Return the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     */
    public boolean getDelegate() {

        return (this.delegate);

    }


    /**
     * Set the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     *
     * @param delegate The new flag
     */
    public void setDelegate(boolean delegate) {

        boolean oldDelegate = this.delegate;
        this.delegate = delegate;
        support.firePropertyChange("delegate", Boolean.valueOf(oldDelegate),
                                   Boolean.valueOf(this.delegate));

    }


    /**
     * Returns true if the internal naming support is used.
     */
    public boolean isUseNaming() {

        return (useNaming);

    }


    /**
     * Enables or disables naming.
     */
    public void setUseNaming(boolean useNaming) {
        this.useNaming = useNaming;
    }


    /**
     * Returns true if the resources associated with this context are
     * filesystem based.
     */
    public boolean isFilesystemBased() {

        return (filesystemBased);

    }


    /**
     * Return the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationEventListeners() {
        return (applicationEventListenersObjects);
    }


    /**
     * Store the set of initialized application event listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationEventListeners(Object listeners[]) {
        applicationEventListenersObjects = listeners;
    }


    /**
     * Return the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @exception IllegalStateException if this method is called before
     *  this application has started, or after it has been stopped
     */
    public Object[] getApplicationLifecycleListeners() {
        return (applicationLifecycleListenersObjects);
    }


    /**
     * Store the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    public void setApplicationLifecycleListeners(Object listeners[]) {
        applicationLifecycleListenersObjects = listeners;
    }


    /**
     * Return the application available flag for this Context.
     */
    public boolean getAvailable() {

        return (this.available);

    }


    /**
     * Set the application available flag for this Context.
     *
     * @param available The new application available flag
     */
    public void setAvailable(boolean available) {

        boolean oldAvailable = this.available;
        this.available = available;
        support.firePropertyChange("available",
                                   Boolean.valueOf(oldAvailable),
                                   Boolean.valueOf(this.available));

    }


    /**
     * Return the Locale to character set mapper for this Context.
     */
    public CharsetMapper getCharsetMapper() {

        // Create a mapper the first time it is requested
        if (this.charsetMapper == null) {
            try {
                Class clazz = Class.forName(charsetMapperClass);
                this.charsetMapper =
                  (CharsetMapper) clazz.newInstance();
            } catch (Throwable t) {
                this.charsetMapper = new CharsetMapper();
            }
        }

        return (this.charsetMapper);

    }


    /**
     * Set the Locale to character set mapper for this Context.
     *
     * @param mapper The new mapper
     */
    public void setCharsetMapper(CharsetMapper mapper) {

        CharsetMapper oldCharsetMapper = this.charsetMapper;
        this.charsetMapper = mapper;
        if( mapper != null )
            this.charsetMapperClass= mapper.getClass().getName();
        support.firePropertyChange("charsetMapper", oldCharsetMapper,
                                   this.charsetMapper);

    }

    /**
     * Return the path to a file to save this Context information.
     */
    public String getConfigFile() {

        return (this.configFile);

    }


    /**
     * Set the path to a file to save this Context information.
     *
     * @param configFile The path to a file to save this Context information.
     */
    public void setConfigFile(String configFile) {

        this.configFile = configFile;
    }


    /**
     * Return the "correctly configured" flag for this Context.
     */
    public boolean getConfigured() {

        return (this.configured);

    }


    /**
     * Set the "correctly configured" flag for this Context.  This can be
     * set to false by startup listeners that detect a fatal configuration
     * error to avoid the application from being made available.
     *
     * @param configured The new correctly configured flag
     */
    public void setConfigured(boolean configured) {

        boolean oldConfigured = this.configured;
        this.configured = configured;
        support.firePropertyChange("configured",
                                   Boolean.valueOf(oldConfigured),
                                   Boolean.valueOf(this.configured));

    }


    /**
     * Return the "use cookies for session ids" flag.
     */
    public boolean getCookies() {

        return (this.cookies);

    }


    /**
     * Set the "use cookies for session ids" flag.
     *
     * @param cookies The new flag
     */
    public void setCookies(boolean cookies) {

        boolean oldCookies = this.cookies;
        this.cookies = cookies;
        support.firePropertyChange("cookies",
                                   Boolean.valueOf(oldCookies),
                                   Boolean.valueOf(this.cookies));

    }


    /**
     * Return the "allow crossing servlet contexts" flag.
     */
    public boolean getCrossContext() {

        return (this.crossContext);

    }


    /**
     * Set the "allow crossing servlet contexts" flag.
     *
     * @param crossContext The new cross contexts flag
     */
    public void setCrossContext(boolean crossContext) {

        boolean oldCrossContext = this.crossContext;
        this.crossContext = crossContext;
        support.firePropertyChange("crossContext",
                                   Boolean.valueOf(oldCrossContext),
                                   Boolean.valueOf(this.crossContext));

    }

    public String getDefaultWebXml() {
        return defaultWebXml;
    }

    /** Set the location of the default web xml that will be used.
     * If not absolute, it'll be made relative to the engine's base dir
     * ( which defaults to catalina.base system property ).
     *
     * XXX  If a file is not found - we can attempt a getResource()
     *
     * @param defaultWebXml
     */
    public void setDefaultWebXml(String defaultWebXml) {
        this.defaultWebXml = defaultWebXml;
    }

    /**
     * Gets the time (in milliseconds) it took to start this context.
     *
     * @return Time (in milliseconds) it took to start this context.
     */
    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    public long getTldScanTime() {
        return tldScanTime;
    }

    public void setTldScanTime(long tldScanTime) {
        this.tldScanTime = tldScanTime;
    }

    /**
     * Return the display name of this web application.
     */
    public String getDisplayName() {

        return (this.displayName);

    }


    /**
     * Return the alternate Deployment Descriptor name.
     */
    public String getAltDDName(){
        return altDDName;
    }


    /**
     * Set an alternate Deployment Descriptor name.
     */
    public void setAltDDName(String altDDName) {
        this.altDDName = altDDName;
        if (context != null) {
            context.setAttribute(Globals.ALT_DD_ATTR,altDDName);
            context.setAttributeReadOnly(Globals.ALT_DD_ATTR);
        }
    }


    /**
     * Return the compiler classpath.
     */
    public String getCompilerClasspath(){
        return compilerClasspath;
    }


    /**
     * Set the compiler classpath.
     */
    public void setCompilerClasspath(String compilerClasspath) {
        this.compilerClasspath = compilerClasspath;
    }


    /**
     * Set the display name of this web application.
     *
     * @param displayName The new display name
     */
    public void setDisplayName(String displayName) {

        String oldDisplayName = this.displayName;
        this.displayName = displayName;
        support.firePropertyChange("displayName", oldDisplayName,
                                   this.displayName);
    }


    /**
     * Return the distributable flag for this web application.
     */
    public boolean getDistributable() {

        return (this.distributable);

    }

    /**
     * Set the distributable flag for this web application.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable) {
        boolean oldDistributable = this.distributable;
        this.distributable = distributable;
        support.firePropertyChange("distributable",
                                   Boolean.valueOf(oldDistributable),
                                   Boolean.valueOf(this.distributable));
        
        // Bugzilla 32866
        if(getManager() != null) {
            if(log.isLoggable(Level.FINE)) {
                log.fine("Propagating distributable=" + distributable
                         + " to manager");
            }
            getManager().setDistributable(distributable);
        }
    }


    /**
     * Return the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getDocBase() {
        return (this.docBase);
    }


    /**
     * Set the document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param docBase The new document root
     */
    public void setDocBase(String docBase) {

        this.docBase = docBase;

    }


    /**
     * Configures this context's alternate doc base mappings.
     *
     * @param alternateDocBasesMap HashMap containing this context's 
     * mappings from url patterns to alternate doc base paths
     */
    public void addAlternateDocBase(String urlPattern, String docBase) {

        if (urlPattern == null || docBase == null) {
            throw new IllegalArgumentException(
                sm.getString(
                    "standardContext.alternateDocBase.missingPathOrUrlPattern"));
        }
  
        AlternateDocBase alternateDocBase = new AlternateDocBase();
        alternateDocBase.setUrlPattern(urlPattern);
        alternateDocBase.setDocBase(docBase);
        alternateDocBase.setBasePath(getBasePath(docBase));

        if (alternateDocBases == null) {
            alternateDocBases = new ArrayList<AlternateDocBase>();
        }
        alternateDocBases.add(alternateDocBase);
    }


    /**
     * Gets this context's configured alternate doc bases.
     *
     * @return This context's configured alternate doc bases
     */
    public ArrayList<AlternateDocBase> getAlternateDocBases() {
        return alternateDocBases;
    }


    // experimental
    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }


    /**
     * Return the frequency of manager checks.
     */
    public int getManagerChecksFrequency() {

        return (this.managerChecksFrequency);

    }


    /**
     * Set the manager checks frequency.
     *
     * @param managerChecksFrequency the new manager checks frequency
     */
    public void setManagerChecksFrequency(int managerChecksFrequency) {

        if (managerChecksFrequency <= 0) {
            return;
        }

        int oldManagerChecksFrequency = this.managerChecksFrequency;
        this.managerChecksFrequency = managerChecksFrequency;
        support.firePropertyChange("managerChecksFrequency",
                                   Integer.valueOf(oldManagerChecksFrequency),
                                   Integer.valueOf(this.managerChecksFrequency));

    }


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }

    public void setJvmRoute(String jvmRoute) {
        this.jvmRoute = jvmRoute;
    }

    public String getJvmRoute() {
        return jvmRoute;
    }

    public String getEngineName() {
        if( engineName != null ) return engineName;
        return domain;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getJ2EEApplication() {
        return j2EEApplication;
    }

    public void setJ2EEApplication(String j2EEApplication) {
        this.j2EEApplication = j2EEApplication;
    }

    public String getJ2EEServer() {
        return j2EEServer;
    }

    public void setJ2EEServer(String j2EEServer) {
        this.j2EEServer = j2EEServer;
    }


    /**
     * Return the login configuration descriptor for this web application.
     */
    public LoginConfig getLoginConfig() {

        return (this.loginConfig);

    }


    /**
     * Set the login configuration descriptor for this web application.
     *
     * @param config The new login configuration
     */
    public void setLoginConfig(LoginConfig config) {

        // Validate the incoming property value
        if (config == null)
            throw new IllegalArgumentException
                (sm.getString("standardContext.loginConfig.required"));
        String loginPage = config.getLoginPage();
        if ((loginPage != null) && !loginPage.startsWith("/")) {
            if (isServlet22()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(sm.getString(
                        "standardContext.loginConfig.loginWarning",
                        loginPage));
                }
                config.setLoginPage("/" + loginPage);
            } else {
                throw new IllegalArgumentException
                    (sm.getString("standardContext.loginConfig.loginPage",
                                  loginPage));
            }
        }
        String errorPage = config.getErrorPage();
        if ((errorPage != null) && !errorPage.startsWith("/")) {
            if (isServlet22()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(sm.getString(
                        "standardContext.loginConfig.errorWarning",
                        errorPage));
                }
                config.setErrorPage("/" + errorPage);
            } else {
                throw new IllegalArgumentException
                    (sm.getString("standardContext.loginConfig.errorPage",
                                  errorPage));
            }
        }

        // Process the property setting change
        LoginConfig oldLoginConfig = this.loginConfig;
        this.loginConfig = config;
        support.firePropertyChange("loginConfig",
                                   oldLoginConfig, this.loginConfig);

    }


    /**
     * Get the mapper associated with the context.
     */
    public com.sun.grizzly.util.http.mapper.Mapper getMapper() {
        return (mapper);
    }


    /**
     * Return the naming resources associated with this web application.
     */
    public NamingResources getNamingResources() {

        return (this.namingResources);

    }


    /**
     * Set the naming resources for this web application.
     *
     * @param namingResources The new naming resources
     */
    public void setNamingResources(NamingResources namingResources) {

        // Process the property setting change
        NamingResources oldNamingResources = this.namingResources;
        this.namingResources = namingResources;
        support.firePropertyChange("namingResources",
                                   oldNamingResources, this.namingResources);

    }


    /**
     * Return the context path for this Context.
     */
    public String getPath() {

        return (getName());

    }

    
    /**
     * Set the context path for this Context.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The context path is used as the "name" of
     * a Context, because it must be unique.
     *
     * @param path The new context path
     */
    public void setPath(String path) {
        // XXX  Use host in name
        /* GlassFish Issue 2339
        setName(RequestUtil.URLDecode(path));
         */
        // START GlassFish Issue 2339
        setName(RequestUtil.URLDecode(path, "UTF-8"));
        // END GlassFish Issue 2339
    }


    /**
     * Return the public identifier of the deployment descriptor DTD that is
     * currently being parsed.
     */
    public String getPublicId() {

        return (this.publicId);

    }


    /**
     * Set the public identifier of the deployment descriptor DTD that is
     * currently being parsed.
     *
     * @param publicId The public identifier
     */
    public void setPublicId(String publicId) {

        if (log.isLoggable(Level.FINEST))
            log.finest("Setting deployment descriptor public ID to '" +
                       publicId + "'");

        String oldPublicId = this.publicId;
        this.publicId = publicId;
        support.firePropertyChange("publicId", oldPublicId, publicId);

    }


    /**
     * Return the reloadable flag for this web application.
     */
    public boolean getReloadable() {

        return (this.reloadable);

    }


    /**
     * Return the DefaultContext override flag for this web application.
     */
    public boolean getOverride() {

        return (this.override);

    }


    /**
     * Return the privileged flag for this web application.
     */
    public boolean getPrivileged() {

        return (this.privileged);

    }


    /**
     * Set the privileged flag for this web application.
     *
     * @param privileged The new privileged flag
     */
    public void setPrivileged(boolean privileged) {

        boolean oldPrivileged = this.privileged;
        this.privileged = privileged;
        support.firePropertyChange("privileged",
                                   Boolean.valueOf(oldPrivileged),
                                   Boolean.valueOf(this.privileged));

    }


    /**
     * Set the reloadable flag for this web application.
     *
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable) {

        boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;
        support.firePropertyChange("reloadable",
                                   Boolean.valueOf(oldReloadable),
                                   Boolean.valueOf(this.reloadable));

    }


    /**
     * Set the DefaultContext override flag for this web application.
     *
     * @param override The new override flag
     */
    public void setOverride(boolean override) {

        boolean oldOverride = this.override;
        this.override = override;
        support.firePropertyChange("override",
                                   Boolean.valueOf(oldOverride),
                                   Boolean.valueOf(this.override));

    }

    // START SJSAS 8.1 5049111   
    /**
     * Scan the parent when searching for TLD listeners.
     */
    public boolean isJsfApplication(){
        return isJsfApplication;
    }
    // END SJSAS 8.1 5049111


    // START SJSAS 6253524
    /**
     * Indicates whether this web module contains any ad-hoc paths.
     *
     * An ad-hoc path is a servlet path that is mapped to a servlet 
     * not declared in the web module's deployment descriptor.
     *
     * A web module all of whose mappings are for ad-hoc paths is called an
     * ad-hoc web module.
     *
     * @return true if this web module contains any ad-hoc paths, false
     * otherwise
     */
    public boolean hasAdHocPaths() {
        return false;
    }

    /**
     * Returns the name of the ad-hoc servlet responsible for servicing the
     * given path.
     *
     * @param path The path to service
     *
     * @return The name of the ad-hoc servlet responsible for servicing the
     * given path, or null if the given path is not an ad-hoc path
     */
    public String getAdHocServletName(String path) {
        return null;
    }
    // END SJSAS 6253524


    /**
     * Return the "replace welcome files" property.
     */
    public boolean isReplaceWelcomeFiles() {

        return (this.replaceWelcomeFiles);

    }


    /**
     * Set the "replace welcome files" property.
     *
     * @param replaceWelcomeFiles The new property value
     */
    public void setReplaceWelcomeFiles(boolean replaceWelcomeFiles) {

        boolean oldReplaceWelcomeFiles = this.replaceWelcomeFiles;
        this.replaceWelcomeFiles = replaceWelcomeFiles;
        support.firePropertyChange("replaceWelcomeFiles",
                                   Boolean.valueOf(oldReplaceWelcomeFiles),
                                   Boolean.valueOf(this.replaceWelcomeFiles));

    }


    /**
     * Returns the value of the securePagesWithPragma property.
     */
    public boolean isSecurePagesWithPragma() {

        return (this.securePagesWithPragma);
    }


    /**
     * Sets the securePagesWithPragma property of this Context.
     *
     * Setting this property to true will result in Pragma and Cache-Control
     * headers with a value of "No-cache" if proxy caching has been disabled.
     *
     * Setting this property to false will not add any Pragma header,
     * but will set the Cache-Control header to "private".
     *
     * @param securePagesWithPragma true if Pragma and Cache-Control headers
     * are to be set to "No-cache" if proxy caching has been disabled, false
     * otherwise
     */
    public void setSecurePagesWithPragma(boolean securePagesWithPragma) {

        boolean oldSecurePagesWithPragma = this.securePagesWithPragma;
        this.securePagesWithPragma = securePagesWithPragma;
        support.firePropertyChange("securePagesWithPragma",
                                   Boolean.valueOf(oldSecurePagesWithPragma),
                                   Boolean.valueOf(this.securePagesWithPragma));
    }


    public void setUseMyFaces(boolean useMyFaces) {
        this.useMyFaces = useMyFaces;
    }

    public boolean isUseMyFaces() {
        return useMyFaces;
    }


    /**
     * Return the servlet context for which this Context is a facade.
     */
    public ServletContext getServletContext() {

        if (context == null) {
            context = new ApplicationContext(getBasePath(getDocBase()),
                                             getAlternateDocBases(),
                                             this);
            if (altDDName != null 
                    && context.getAttribute(Globals.ALT_DD_ATTR) == null){
                context.setAttribute(Globals.ALT_DD_ATTR,altDDName);
                context.setAttributeReadOnly(Globals.ALT_DD_ATTR);
            }
        }

        return (context.getFacade());

    }


    /**
     * Return the default session timeout (in minutes) for this
     * web application.
     */
    public int getSessionTimeout() {

        return (this.sessionTimeout);

    }
    
    /**
     * Is the session timeout (in minutes) for this
     * web application over-ridden from the default
     * HERCULES:add
     */
    public boolean isSessionTimeoutOveridden() {

        return (this.sessionTimeoutOveridden);

    }    


    /**
     * Set the default session timeout (in minutes) for this
     * web application.
     *
     * @param timeout The new default session timeout
     */
    public void setSessionTimeout(int timeout) {

        int oldSessionTimeout = this.sessionTimeout;
    /*
     * SRV.13.4 ("Deployment Descriptor"):
     * If the timeout is 0 or less, the container ensures the default
     * behaviour of sessions is never to time out.
     */
        this.sessionTimeout = (timeout == 0) ? -1 : timeout;
        support.firePropertyChange("sessionTimeout",
                                   Integer.valueOf(oldSessionTimeout),
                                   Integer.valueOf(this.sessionTimeout));
        //HERCULES:add
        sessionTimeoutOveridden = true;
        //end HERCULES:add        

    }


    /**
     * Unpack WAR flag accessor.
     */
    public boolean getUnpackWAR() {

        return (unpackWAR);

    }


    /**
     * Unpack WAR flag mutator.
     */
    public void setUnpackWAR(boolean unpackWAR) {

        this.unpackWAR = unpackWAR;

    }

    /**
     * Return the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     */
    public String getWrapperClass() {

        return (this.wrapperClassName);

    }


    /**
     * Set the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     *
     * @param wrapperClassName The new wrapper class name
     *
     * @throws IllegalArgumentException if the specified wrapper class
     * cannot be found or is not a subclass of StandardWrapper
     */
    public void setWrapperClass(String wrapperClassName) {

        this.wrapperClassName = wrapperClassName;

        try {
            wrapperClass = Class.forName(wrapperClassName);         
            if (!StandardWrapper.class.isAssignableFrom(wrapperClass)) {
                throw new IllegalArgumentException(
                    sm.getString("standardContext.invalidWrapperClass",
                                 wrapperClassName));
            }
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException(cnfe);
        }
    }


    /**
     * Set the resources DirContext object with which this Container is
     * associated.
     *
     * @param resources The newly associated DirContext
     */
    public synchronized void setResources(DirContext resources) {

        if (started) {
            throw new IllegalStateException
                (sm.getString("standardContext.resources.started"));
        }

        DirContext oldResources = this.webappResources;
        if (oldResources == resources)
            return;

        if (resources instanceof BaseDirContext) {
            ((BaseDirContext) resources).setCached(isCachingAllowed());
            ((BaseDirContext) resources).setCacheTTL(getCacheTTL());
            ((BaseDirContext) resources).setCacheMaxSize(getCacheMaxSize());
        }
        if (resources instanceof FileDirContext) {
            filesystemBased = true;
            ((FileDirContext) resources).setCaseSensitive(isCaseSensitive());
            ((FileDirContext) resources).setAllowLinking(isAllowLinking());
        }
        this.webappResources = resources;

        // The proxied resources will be refreshed on start
        this.resources = null;

        support.firePropertyChange("resources", oldResources,
                                   this.webappResources);

    }

    private synchronized void setAlternateResources(
                            AlternateDocBase alternateDocBase,
                            DirContext resources) {

        if (started) {
            throw new IllegalStateException
                (sm.getString("standardContext.resources.started"));
        }

        DirContext oldResources = alternateDocBase.getWebappResources();
        if (oldResources == resources)
            return;

        if (resources instanceof BaseDirContext) {
            ((BaseDirContext) resources).setCached(isCachingAllowed());
            ((BaseDirContext) resources).setCacheTTL(getCacheTTL());
            ((BaseDirContext) resources).setCacheMaxSize(getCacheMaxSize());
        }
        if (resources instanceof FileDirContext) {
            filesystemBased = true;
            ((FileDirContext) resources).setCaseSensitive(isCaseSensitive());
            ((FileDirContext) resources).setAllowLinking(isAllowLinking());
        }
        alternateDocBase.setWebappResources(resources);
        // The proxied resources will be refreshed on start
        alternateDocBase.setResources(null);
    }

    // START S1AS8PE 4817642
    /**
     * Return the "reuse session IDs when creating sessions" flag
     */
    public boolean getReuseSessionID() {
        return reuseSessionID;
    }

    /**
     * Set the "reuse session IDs when creating sessions" flag
     *
     * @param reuse The new value for the flag
     */
    public void setReuseSessionID(boolean reuse) {
        reuseSessionID = reuse;
    }
    // END S1AS8PE 4817642



    // START RIMOD 4642650
    /**
     * Return whether this context allows sendRedirect() to redirect
     * to a relative URL.
     *
     * The default value for this property is 'false'.
     */
    public boolean getAllowRelativeRedirect() {

        return allowRelativeRedirect;

    }

    
    /**
     * Set whether this context allows sendRedirect() to redirect
     * to a relative URL.
     *
     * @param allowRelativeURLs The new value for this property.
     *                          The default value for this property is
     *                          'false'.
     */
    public void setAllowRelativeRedirect(boolean allowRelativeURLs) {

        allowRelativeRedirect = allowRelativeURLs;

    }


    // END RIMOD 4642650

    // START IASRI 4823322
    /**
     * Get Auditors associated with this context, if any.
     *
     * @return array of Auditor objects, or null
     *
     */
    public Auditor[] getAuditors() {
        return auditors;
    }


    /**
     * Set the Auditors associated with this context.
     *
     * @param auditor array of Auditor objects
     *
     */
    public void setAuditors(Auditor[] auditor) {
        this.auditors=auditor;
    }
    // END IASRI 4823322


    // START S1AS8PE 4965017
    public void setReload(boolean isReload) {
        this.isReload = isReload;
    }

    public boolean isReload() {
        return isReload;
    }
    // END S1AS8PE 4965017


    // ------------------------------------------------------ Public Properties


    /**
     * Return the Locale to character set mapper class for this Context.
     */
    public String getCharsetMapperClass() {

        return (this.charsetMapperClass);

    }


    /**
     * Set the Locale to character set mapper class for this Context.
     *
     * @param mapper The new mapper class
     */
    public void setCharsetMapperClass(String mapper) {

        String oldCharsetMapperClass = this.charsetMapperClass;
        this.charsetMapperClass = mapper;
        support.firePropertyChange("charsetMapperClass",
                                   oldCharsetMapperClass,
                                   this.charsetMapperClass);

    }


    /** Get the absolute path to the work dir.
     *  To avoid duplication.
     * 
     * @return
     */ 
    public String getWorkPath() {
        if (getWorkDir() == null) {
            return null;
        }
        File workDir = new File(getWorkDir());
        if (!workDir.isAbsolute()) {
            File catalinaHome = engineBase();
            String catalinaHomePath = null;
            try {
                catalinaHomePath = catalinaHome.getCanonicalPath();
                workDir = new File(catalinaHomePath,
                        getWorkDir());
            } catch (IOException e) {
            }
        }
        return workDir.getAbsolutePath();
    }
    
    /**
     * Return the work directory for this Context.
     */
    public String getWorkDir() {

        return (this.workDir);

    }


    /**
     * Set the work directory for this Context.
     *
     * @param workDir The new work directory
     */
    public void setWorkDir(String workDir) {

        this.workDir = workDir;

        if (started)
            postWorkDirectory();

    }


    // -------------------------------------------------------- Context Methods


    /**
     * Add a new Listener class name to the set of Listeners
     * configured for this application.
     *
     * @param listener Java class name of a listener class
     */
    public void addApplicationListener(String listener) {

        synchronized (applicationListeners) {
            String results[] = new String[applicationListeners.length + 1];
            if ("com.sun.faces.config.ConfigureListener".equals(listener)) {
                // Always add the JSF listener as the first element, 
                // see GlassFish Issue 2563 for details
                for (int i = 0; i < applicationListeners.length; i++) {
                    if (listener.equals(applicationListeners[i])) {
                        if (log.isLoggable(Level.INFO)) {
                            log.info(sm.getString(
                                    "standardContext.duplicateListener", listener));
                        }
                        return;
                    }
                    results[i+1] = applicationListeners[i];
                }
                results[0] = listener;
            } else {
                for (int i = 0; i < applicationListeners.length; i++) {
                    if (listener.equals(applicationListeners[i])) {
                        if (log.isLoggable(Level.INFO)) {
                            log.info(sm.getString(
                                    "standardContext.duplicateListener", listener));
                        }
                        return;
                    }
                    results[i] = applicationListeners[i];
                }
                results[applicationListeners.length] = listener;
            }
            applicationListeners = results;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addApplicationListener", listener);
        }

        // FIXME - add instance if already started?

    }


    /**
     * Add a new application parameter for this application.
     *
     * @param parameter The new application parameter
     */
    public void addApplicationParameter(ApplicationParameter parameter) {

        synchronized (applicationParameters) {
            String newName = parameter.getName();
            for (int i = 0; i < applicationParameters.length; i++) {
                if (newName.equals(applicationParameters[i].getName()) &&
                    !applicationParameters[i].getOverride())
                    return;
            }
            ApplicationParameter results[] =
                new ApplicationParameter[applicationParameters.length + 1];
            System.arraycopy(applicationParameters, 0, results, 0,
                             applicationParameters.length);
            results[applicationParameters.length] = parameter;
            applicationParameters = results;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addApplicationParameter", parameter);
        }
    }


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Wrapper.
     *
     * @param child Child container to be added
     *
     * @exception IllegalArgumentException if the proposed container is
     *  not an implementation of Wrapper
     */
    public void addChild(Container child) {

        // Global JspServlet
        Wrapper oldJspServlet = null;

        if (!(child instanceof Wrapper)) {
            throw new IllegalArgumentException
                (sm.getString("standardContext.notWrapper"));
        }

        Wrapper wrapper = (Wrapper) child;
        boolean isJspServlet = "jsp".equals(child.getName());

        // Allow webapp to override JspServlet inherited from global web.xml.
        if (isJspServlet) {
            oldJspServlet = (Wrapper) findChild("jsp");
            if (oldJspServlet != null) {
                removeChild(oldJspServlet);
            }
        }

        String jspFile = wrapper.getJspFile();
        if ((jspFile != null) && !jspFile.startsWith("/")) {
            if (isServlet22()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(sm.getString("standardContext.wrapper.warning", 
                                          jspFile));
                }
                wrapper.setJspFile("/" + jspFile);
            } else {
                throw new IllegalArgumentException
                    (sm.getString("standardContext.wrapper.error", jspFile));
            }
        }

        super.addChild(child);

        // START SJSAS 6342808
        /* SJSWS 6362207
        if (started) {
        */
        // START SJSWS 6362207
        if (getAvailable()) {
        // END SJSWS 6362207
            /*
             * If this StandardContext has already been started, we need to
             * register the newly added child with JMX. Any children that were
             * added before this StandardContext was started have already been
             * registered with JMX (as part of StandardContext.start()).
             */
            if (wrapper instanceof StandardWrapper) {
                ((StandardWrapper) wrapper).registerJMX( this );
            }
        }
        // END SJSAS 6342808

        if (isJspServlet && oldJspServlet != null) {
            /*
             * The webapp-specific JspServlet inherits all the mappings
             * specified in the global web.xml, and may add additional ones.
             */
            String[] jspMappings = oldJspServlet.findMappings();
            for (int i=0; jspMappings!=null && i<jspMappings.length; i++) {
                addServletMapping(jspMappings[i], child.getName());
            }
        }
    }


    /**
     * Add a security constraint to the set for this web application.
     */
    public void addConstraint(SecurityConstraint constraint) {

        // Validate the proposed constraint
        SecurityCollection collections[] = constraint.findCollections();
        for (int i = 0; i < collections.length; i++) {
            String patterns[] = collections[i].findPatterns();
            for (int j = 0; j < patterns.length; j++) {
                patterns[j] = adjustURLPattern(patterns[j]);
                if (!validateURLPattern(patterns[j]))
                    throw new IllegalArgumentException
                        (sm.getString
                         ("standardContext.securityConstraint.pattern",
                          patterns[j]));
            }
        }

        // Add this constraint to the set for our web application
        synchronized (constraints) {
            SecurityConstraint results[] =
                new SecurityConstraint[constraints.length + 1];
            for (int i = 0; i < constraints.length; i++)
                results[i] = constraints[i];
            results[constraints.length] = constraint;
            constraints = results;
        }

    }



    /**
     * Add an EJB resource reference for this web application.
     *
     * @param ejb New EJB resource reference
     */
    public void addEjb(ContextEjb ejb) {

        namingResources.addEjb(ejb);

        if (notifyContainerListeners) {
           fireContainerEvent("addEjb", ejb.getName());
        }
    }


    /**
     * Add an environment entry for this web application.
     *
     * @param environment New environment entry
     */
    public void addEnvironment(ContextEnvironment environment) {

        ContextEnvironment env = findEnvironment(environment.getName());
        if ((env != null) && !env.getOverride())
            return;
        namingResources.addEnvironment(environment);

        if (notifyContainerListeners) {
            fireContainerEvent("addEnvironment", environment.getName());
        }
    }


    /**
     * Add resource parameters for this web application.
     *
     * @param resourceParameters New resource parameters
     */
    public void addResourceParams(ResourceParams resourceParameters) {

        namingResources.addResourceParams(resourceParameters);

        if (notifyContainerListeners) {
            fireContainerEvent("addResourceParams",
                               resourceParameters.getName());
        }
    }


    /**
     * Add an error page for the specified error or Java exception.
     *
     * @param errorPage The error page definition to be added
     */
    public void addErrorPage(ErrorPage errorPage) {
        // Validate the input parameters
        if (errorPage == null)
            throw new IllegalArgumentException
                (sm.getString("standardContext.errorPage.required"));
        String location = errorPage.getLocation();
        if ((location != null) && !location.startsWith("/")) {
            if (isServlet22()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(sm.getString("standardContext.errorPage.warning",
                                          location));
                }
                errorPage.setLocation("/" + location);
            } else {
                throw new IllegalArgumentException
                    (sm.getString("standardContext.errorPage.error",
                                  location));
            }
        }

        // Add the specified error page to our internal collections
        String exceptionType = errorPage.getExceptionType();
        if (exceptionType != null) {
            synchronized (exceptionPages) {
                exceptionPages.put(exceptionType, errorPage);
            }
        } else {
            synchronized (statusPages) {
                int errorCode = errorPage.getErrorCode();
                if ((errorCode >= 400) && (errorCode < 600)) {
                    statusPages.put(Integer.valueOf(errorCode), errorPage);
                } else {
                    log.severe(sm.getString(
                        "standardContext.invalidErrorPageCode", errorCode));
                }
            }
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addErrorPage", errorPage);
        }
    }


    /**
     * Add a filter definition to this Context.
     *
     * @param filterDef The filter definition to be added
     */
    public void addFilterDef(FilterDef filterDef) {

        synchronized (filterDefs) {
            filterDefs.put(filterDef.getFilterName(), filterDef);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addFilterDef", filterDef);
        }
    }


    /**
     * Add multiple filter mappings to this Context.
     *
     * @param filterMaps The filter mappings to be added
     *
     * @exception IllegalArgumentException if the specified filter name
     *  does not match an existing filter definition, or the filter mapping
     *  is malformed
     */
    public void addFilterMaps(FilterMaps filterMaps) {
        int dispatcherMapping = filterMaps.getDispatcherMapping();
        String filterName = filterMaps.getFilterName();
        String[] servletNames = filterMaps.getServletNames();
        String[] urlPatterns = filterMaps.getURLPatterns();
        for (int i = 0; i < servletNames.length; i++) {
            FilterMap fmap = new FilterMap();
            fmap.setFilterName(filterName);
            fmap.setServletName(servletNames[i]);
            fmap.setDispatcherMapping(dispatcherMapping);
            addFilterMap(fmap);
        }
        for (int i = 0; i < urlPatterns.length; i++) {
            FilterMap fmap = new FilterMap();
            fmap.setFilterName(filterName);
            fmap.setURLPattern(urlPatterns[i]);
            fmap.setDispatcherMapping(dispatcherMapping);
            addFilterMap(fmap);
        }
    }


    /**
     * Add a filter mapping to this Context.
     *
     * @param filterMap The filter mapping to be added
     *
     * @exception IllegalArgumentException if the specified filter name
     *  does not match an existing filter definition, or the filter mapping
     *  is malformed
     */
    public void addFilterMap(FilterMap filterMap) {

        // Validate the proposed filter mapping
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getURLPattern();
        if (findFilterDef(filterName) == null)
            throw new IllegalArgumentException
                (sm.getString("standardContext.filterMap.name", filterName));
        if ((servletName == null) && (urlPattern == null))
            throw new IllegalArgumentException
                (sm.getString("standardContext.filterMap.either"));
        if ((servletName != null) && (urlPattern != null))
            throw new IllegalArgumentException
                (sm.getString("standardContext.filterMap.either"));
        // Because filter-pattern is new in 2.3, no need to adjust
        // for 2.2 backwards compatibility
        if ((urlPattern != null) && !validateURLPattern(urlPattern))
            throw new IllegalArgumentException
                (sm.getString("standardContext.filterMap.pattern",
                              urlPattern));

        // Add this filter mapping to our registered set
        synchronized (filterMaps) {
            FilterMap results[] =new FilterMap[filterMaps.length + 1];
            System.arraycopy(filterMaps, 0, results, 0, filterMaps.length);
            results[filterMaps.length] = filterMap;
            filterMaps = results;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addFilterMap", filterMap);
        }
    }


    /**
     * Adds the filter with the given name, description, and class name to
     * this servlet context.
     */
    public void addFilter(String filterName,
                          String description,
                          String className,
                          Map<String, String> initParameters) {

        FilterDef filterDef = new FilterDef();

        filterDef.setFilterName(filterName);
        filterDef.setDescription(description);
        filterDef.setFilterClass(className);

        if (initParameters != null) {
            for (Map.Entry<String, String> e : initParameters.entrySet()) {
                filterDef.addInitParameter(e.getKey(), e.getValue());
            }
        }

        addFilterDef(filterDef);
    }


    /**
     * Add the classname of an InstanceListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of an InstanceListener class
     */
    public void addInstanceListener(String listener) {

        synchronized (instanceListeners) {
            String results[] =new String[instanceListeners.length + 1];
            for (int i = 0; i < instanceListeners.length; i++)
                results[i] = instanceListeners[i];
            results[instanceListeners.length] = listener;
            instanceListeners = results;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addInstanceListener", listener);
        }
    }

    public void addInstanceListener(InstanceListener listener) {
        synchronized (instanceListenerInstances) {
            instanceListenerInstances.add(listener);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addInstanceListener", listener);
        }
    }

    /**
     * Add the given URL pattern as a jsp-property-group.  This maps
     * resources that match the given pattern so they will be passed
     * to the JSP container.  Though there are other elements in the
     * property group, we only care about the URL pattern here.  The
     * JSP container will parse the rest.
     *
     * @param pattern URL pattern to be mapped
     */
    public void addJspMapping(String pattern) {
        String servletName = findServletMapping("*.jsp");
        if (servletName == null) {
            servletName = "jsp";
        }

        if( findChild(servletName) != null) {
            addServletMapping(pattern, servletName, true);
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Skipping " + pattern + " , no servlet "
                         + servletName);
            }
        }
    }


    /**
     * Add a Locale Encoding Mapping (see Sec 5.4 of Servlet spec 2.4)
     *
     * @param locale locale to map an encoding for
     * @param encoding encoding to be used for a give locale
     */
    public void addLocaleEncodingMappingParameter(String locale, String encoding){
        getCharsetMapper().addCharsetMappingFromDeploymentDescriptor(locale, encoding);
    }


    /**
     * Add a local EJB resource reference for this web application.
     *
     * @param ejb New EJB resource reference
     */
    public void addLocalEjb(ContextLocalEjb ejb) {

        namingResources.addLocalEjb(ejb);
        
        if (notifyContainerListeners) {
            fireContainerEvent("addLocalEjb", ejb.getName());
        }
    }


    /**
     * Add a message destination for this web application.
     *
     * @param md New message destination
     */
    public void addMessageDestination(MessageDestination md) {

        synchronized (messageDestinations) {
            messageDestinations.put(md.getName(), md);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addMessageDestination", md.getName());
        }
    }


    /**
     * Add a message destination reference for this web application.
     *
     * @param mdr New message destination reference
     */
    public void addMessageDestinationRef
        (MessageDestinationRef mdr) {

        namingResources.addMessageDestinationRef(mdr);

        if (notifyContainerListeners) {
            fireContainerEvent("addMessageDestinationRef", mdr.getName());
        }
    }


    /**
     * Add a new MIME mapping, replacing any existing mapping for
     * the specified extension.
     *
     * @param extension Filename extension being mapped
     * @param mimeType Corresponding MIME type
     */
    public void addMimeMapping(String extension, String mimeType) {

        synchronized (mimeMappings) {
            mimeMappings.put(extension, mimeType);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addMimeMapping", extension);
        }
    }


    /**
     * Add a new context initialization parameter.
     *
     * @param name Name of the new parameter
     * @param value Value of the new  parameter
     *
     * @exception IllegalArgumentException if the name or value is missing,
     *  or if this context initialization parameter has already been
     *  registered
     */
    public void addParameter(String name, String value) {
        // Validate the proposed context initialization parameter
        if ((name == null) || (value == null))
            throw new IllegalArgumentException
                (sm.getString("standardContext.parameter.required"));
        if (parameters.get(name) != null)
            throw new IllegalArgumentException
                (sm.getString("standardContext.parameter.duplicate", name));

        // Add this parameter to our defined set
        synchronized (parameters) {
            parameters.put(name, value);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addParameter", name);
        }
    }


    /**
     * Add a resource reference for this web application.
     *
     * @param resource New resource reference
     */
    public void addResource(ContextResource resource) {

        namingResources.addResource(resource);

        if (notifyContainerListeners) {
            fireContainerEvent("addResource", resource.getName());
        }
    }


    /**
     * Add a resource environment reference for this web application.
     *
     * @param name The resource environment reference name
     * @param type The resource environment reference type
     */
    public void addResourceEnvRef(String name, String type) {

        namingResources.addResourceEnvRef(name, type);

        if (notifyContainerListeners) {
            fireContainerEvent("addResourceEnvRef", name);
        }
    }


    /**
     * Add a resource link for this web application.
     *
     * @param resourceLink New resource link
     */
    public void addResourceLink(ContextResourceLink resourceLink) {

        namingResources.addResourceLink(resourceLink);

        if (notifyContainerListeners) {
            fireContainerEvent("addResourceLink", resourceLink.getName());
        }
    }


    /**
     * Add a security role reference for this web application.
     *
     * @param role Security role used in the application
     * @param link Actual security role to check for
     */
    public void addRoleMapping(String role, String link) {

        synchronized (roleMappings) {
            roleMappings.put(role, link);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addRoleMapping", role);
        }
    }


    /**
     * Add a new security role for this web application.
     *
     * @param role New security role
     */
    public void addSecurityRole(String role) {

        synchronized (securityRoles) {
            String results[] =new String[securityRoles.length + 1];
            for (int i = 0; i < securityRoles.length; i++)
                results[i] = securityRoles[i];
            results[securityRoles.length] = role;
            securityRoles = results;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addSecurityRole", role);
        }
    }


    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the multiple patterns.
     *
     * @param servletMap ServletMap containing the servletname and patterns
     *
     * @exception IllegalArgumentException if the specified servlet name
     *  is not known to this Context
     */
     public void addServletMapping(ServletMap servletMap) {
         String[] patterns = servletMap.getUrlPatterns();
         String name = servletMap.getServletName();
         for (int i = 0; i < patterns.length; i++) {
             addServletMapping(patterns[i], name, false);
         }
     }


    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     *
     * @exception IllegalArgumentException if the specified servlet name
     *  is not known to this Context
     */
    public void addServletMapping(String pattern, String name) {
        addServletMapping(pattern, name, false);
    }


    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     * @param jspWildCard true if name identifies the JspServlet
     * and pattern contains a wildcard; false otherwise
     *
     * @exception IllegalArgumentException if the specified servlet name
     *  is not known to this Context
     */
    public void addServletMapping(String pattern, String name,
                                  boolean jspWildCard) {
        // Validate the proposed mapping
        if (findChild(name) == null)
            throw new IllegalArgumentException
                (sm.getString("standardContext.servletMap.name", name));
        pattern = adjustURLPattern(RequestUtil.URLDecode(pattern));
        if (!validateURLPattern(pattern))
            throw new IllegalArgumentException
                (sm.getString("standardContext.servletMap.pattern", pattern));

        // Add this mapping to our registered set
        synchronized (servletMappings) {
            String name2 = (String) servletMappings.get(pattern);
            if (name2 != null) {
                // Don't allow more than one servlet on the same pattern
                Wrapper wrapper = (Wrapper) findChild(name2);
                wrapper.removeMapping(pattern);
                mapper.removeWrapper(pattern);
            }
            servletMappings.put(pattern, name);
        }
        Wrapper wrapper = (Wrapper) findChild(name);
        wrapper.addMapping(pattern);

        // Update context mapper
        mapper.addWrapper(pattern, wrapper, jspWildCard);

        if (notifyContainerListeners) {
            fireContainerEvent("addServletMapping", pattern);
        }
    }


    /**
     * Adds servlet mappings from the given url patterns to the servlet
     * with the given servlet name to this servlet context.
     */
    public void addServletMapping(String servletName,
                                  String[] urlPatterns) {
        if (urlPatterns != null) {
            for (String urlPattern : urlPatterns) {
                addServletMapping(servletName, urlPattern);
            }
        }
    }


    /*
     * Adds the servlet with the given name, description, class name,
     * init parameters, and loadOnStartup, to this servlet context.
     */
    public void addServlet(String servletName,
                           String description,
                           String className,
                           Map<String, String> initParameters,
                           int loadOnStartup) {

        Wrapper wrapper = createWrapper();

        wrapper.setName(servletName);
        wrapper.setDescription(description);
        wrapper.setServletClass(className);

        if (initParameters != null) {
            for (Map.Entry<String, String> e : initParameters.entrySet()) {
                wrapper.addInitParameter(e.getKey(), e.getValue());
            }
        }

        wrapper.setLoadOnStartup(loadOnStartup);

        addChild(wrapper);
    }


    /**
     * Add a JSP tag library for the specified URI.
     *
     * @param uri URI, relative to the web.xml file, of this tag library
     * @param location Location of the tag library descriptor
     */
    public void addTaglib(String uri, String location) {

        synchronized (taglibs) {
            taglibs.put(uri, location);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addTaglib", uri);
        }
    }


    /**
     * Add a new welcome file to the set recognized by this Context.
     *
     * @param name New welcome file name
     */
    public void addWelcomeFile(String name) {

        synchronized (welcomeFiles) {
            // Welcome files from the application deployment descriptor
            // completely replace those from the default conf/web.xml file
            if (replaceWelcomeFiles) {
                welcomeFiles = new String[0];
                setReplaceWelcomeFiles(false);
            }
            String results[] =new String[welcomeFiles.length + 1];
            for (int i = 0; i < welcomeFiles.length; i++)
                results[i] = welcomeFiles[i];
            results[welcomeFiles.length] = name;
            welcomeFiles = results;
        }
        postWelcomeFiles();

        if (notifyContainerListeners) {
            fireContainerEvent("addWelcomeFile", name);
        }
    }


    /**
     * Add the classname of a LifecycleListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a LifecycleListener class
     */
    public void addWrapperLifecycle(String listener) {

        synchronized (wrapperLifecycles) {
            String results[] =new String[wrapperLifecycles.length + 1];
            for (int i = 0; i < wrapperLifecycles.length; i++)
                results[i] = wrapperLifecycles[i];
            results[wrapperLifecycles.length] = listener;
            wrapperLifecycles = results;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addWrapperLifecycle", listener);
        }
    }


    /**
     * Add the classname of a ContainerListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a ContainerListener class
     */
    public void addWrapperListener(String listener) {

        synchronized (wrapperListeners) {
            String results[] =new String[wrapperListeners.length + 1];
            for (int i = 0; i < wrapperListeners.length; i++)
                results[i] = wrapperListeners[i];
            results[wrapperListeners.length] = listener;
            wrapperListeners = results;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addWrapperListener", listener);
        }
    }


    /**
     * Factory method to create and return a new Wrapper instance, of
     * the Java implementation class appropriate for this Context
     * implementation.  The constructor of the instantiated Wrapper
     * will have been called, but no properties will have been set.
     */
    public Wrapper createWrapper() {
     
        Wrapper wrapper = null;
        if (wrapperClass != null) {
            try {
                wrapper = (Wrapper) wrapperClass.newInstance();
            } catch (Throwable t) {
                log.log(Level.SEVERE,
                        sm.getString("standardContext.createWrapperInstance",
                                     wrapperClassName),
                        t);
                return (null);
            }
        } else {
            wrapper = new StandardWrapper();
        }

        synchronized (instanceListeners) {
            for (int i = 0; i < instanceListeners.length; i++) {
                try {
                    Class clazz = Class.forName(instanceListeners[i]);
                    InstanceListener listener =
                      (InstanceListener) clazz.newInstance();
                    wrapper.addInstanceListener(listener);
                } catch (Throwable t) {
                    log.log(Level.SEVERE,
                        sm.getString("standardContext.instanceListener",
                                     instanceListeners[i]),
                        t);
                    return (null);
                }
            }
        }

        synchronized (instanceListenerInstances) {
            for (Iterator<InstanceListener> iter =
                    instanceListenerInstances.iterator();
                        iter.hasNext();) {
                wrapper.addInstanceListener(iter.next());
            }
        }

        synchronized (wrapperLifecycles) {
            for (int i = 0; i < wrapperLifecycles.length; i++) {
                try {
                    Class clazz = Class.forName(wrapperLifecycles[i]);
                    LifecycleListener listener =
                      (LifecycleListener) clazz.newInstance();
                    if (wrapper instanceof Lifecycle)
                        ((Lifecycle) wrapper).addLifecycleListener(listener);
                } catch (Throwable t) {
                    log.log(Level.SEVERE,
                        sm.getString("standardContext.lifecycleListener",
                                     wrapperLifecycles[i]),
                        t);
                    return (null);
                }
            }
        }

        synchronized (wrapperListeners) {
            for (int i = 0; i < wrapperListeners.length; i++) {
                try {
                    Class clazz = Class.forName(wrapperListeners[i]);
                    ContainerListener listener =
                      (ContainerListener) clazz.newInstance();
                    wrapper.addContainerListener(listener);
                } catch (Throwable t) {
                    log.log(Level.SEVERE,
                            sm.getString("standardContext.containerListener",
                                         wrapperListeners[i]),
                            t);
                    return (null);
                }
            }
        }

        return (wrapper);

    }


    /**
     * Return the set of application listener class names configured
     * for this application.
     */
    public String[] findApplicationListeners() {

        return (applicationListeners);

    }


    /**
     * Return the set of application parameters for this application.
     */
    public ApplicationParameter[] findApplicationParameters() {

        return (applicationParameters);

    }


    /**
     * Return the security constraints for this web application.
     * If there are none, a zero-length array is returned.
     */
    public SecurityConstraint[] findConstraints() {

        return (constraints);

    }


    /**
     * Return the EJB resource reference with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired EJB resource reference
     */
    public ContextEjb findEjb(String name) {

        return namingResources.findEjb(name);

    }


    /**
     * Return the defined EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextEjb[] findEjbs() {

        return namingResources.findEjbs();

    }


    /**
     * Return the environment entry with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired environment entry
     */
    public ContextEnvironment findEnvironment(String name) {

        return namingResources.findEnvironment(name);

    }


    /**
     * Return the set of defined environment entries for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public ContextEnvironment[] findEnvironments() {

        return namingResources.findEnvironments();

    }


    /**
     * Return the error page entry for the specified HTTP error code,
     * if any; otherwise return <code>null</code>.
     *
     * @param errorCode Error code to look up
     */
    public ErrorPage findErrorPage(int errorCode) {
        if ((errorCode >= 400) && (errorCode < 600)) {
            return ((ErrorPage) statusPages.get(Integer.valueOf(errorCode)));
        }

        return null;
    }


    /**
     * Return the error page entry for the specified Java exception type,
     * if any; otherwise return <code>null</code>.
     *
     * @param exceptionType Exception type to look up
     */
    public ErrorPage findErrorPage(String exceptionType) {

        synchronized (exceptionPages) {
            return ((ErrorPage) exceptionPages.get(exceptionType));
        }

    }


    /**
     * Return the set of defined error pages for all specified error codes
     * and exception types.
     */
    public ErrorPage[] findErrorPages() {

        synchronized(exceptionPages) {
            synchronized(statusPages) {
                ErrorPage results1[] = new ErrorPage[exceptionPages.size()];
                results1 =
                    (ErrorPage[]) exceptionPages.values().toArray(results1);
                ErrorPage results2[] = new ErrorPage[statusPages.size()];
                results2 =
                    (ErrorPage[]) statusPages.values().toArray(results2);
                ErrorPage results[] =
                    new ErrorPage[results1.length + results2.length];
                for (int i = 0; i < results1.length; i++)
                    results[i] = results1[i];
                for (int i = results1.length; i < results.length; i++)
                    results[i] = results2[i - results1.length];
                return (results);
            }
        }

    }


    /**
     * Return the filter definition for the specified filter name, if any;
     * otherwise return <code>null</code>.
     *
     * @param filterName Filter name to look up
     */
    public FilterDef findFilterDef(String filterName) {

        synchronized (filterDefs) {
            return ((FilterDef) filterDefs.get(filterName));
        }

    }


    /**
     * Return the set of defined filters for this Context.
     */
    public FilterDef[] findFilterDefs() {

        synchronized (filterDefs) {
            FilterDef results[] = new FilterDef[filterDefs.size()];
            return ((FilterDef[]) filterDefs.values().toArray(results));
        }

    }


    /**
     * Return the set of filter mappings for this Context.
     */
    public FilterMap[] findFilterMaps() {

        return (filterMaps);

    }


    /**
     * Return the set of InstanceListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findInstanceListeners() {

        return (instanceListeners);

    }


    /**
     * Return the local EJB resource reference with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired EJB resource reference
     */
    public ContextLocalEjb findLocalEjb(String name) {

        return namingResources.findLocalEjb(name);

    }


    /**
     * Return the defined local EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextLocalEjb[] findLocalEjbs() {

        return namingResources.findLocalEjbs();

    }


    /**
     * FIXME: Fooling introspection ...
     */
    public Context findMappingObject() {
        return (Context) getMappingObject();
    }
    
    
    /**
     * Return the message destination with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired message destination
     */
    public MessageDestination findMessageDestination(String name) {

        synchronized (messageDestinations) {
            return ((MessageDestination) messageDestinations.get(name));
        }

    }


    /**
     * Return the set of defined message destinations for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public MessageDestination[] findMessageDestinations() {

        synchronized (messageDestinations) {
            MessageDestination results[] =
                new MessageDestination[messageDestinations.size()];
            return ((MessageDestination[])
                    messageDestinations.values().toArray(results));
        }

    }


    /**
     * Return the message destination ref with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired message destination ref
     */
    public MessageDestinationRef
        findMessageDestinationRef(String name) {

        return namingResources.findMessageDestinationRef(name);

    }


    /**
     * Return the set of defined message destination refs for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public MessageDestinationRef[]
        findMessageDestinationRefs() {

        return namingResources.findMessageDestinationRefs();

    }


    /**
     * Return the MIME type to which the specified extension is mapped,
     * if any; otherwise return <code>null</code>.
     *
     * @param extension Extension to map to a MIME type
     */
    public String findMimeMapping(String extension) {

        String mimeType = mimeMappings.get(extension);
        if (mimeType == null) {
            // No mapping found, try case-insensitive match
            synchronized (mimeMappings) {
                Iterator<String> extensions = mimeMappings.keySet().iterator();
                while (extensions.hasNext()) {
                    String ext = extensions.next();
                    if (ext.equalsIgnoreCase(extension)) {
                        // Case-insensitive extension match found
                        mimeType = mimeMappings.get(ext);
                        // Add given extension to the map, in order to make
                        // subsequent lookups faster
                        mimeMappings.put(extension, mimeType);
                        break;
                    }
                }                
            }        
        }

        return mimeType;
    }


    /**
     * Return the extensions for which MIME mappings are defined.  If there
     * are none, a zero-length array is returned.
     */
    public String[] findMimeMappings() {

        synchronized (mimeMappings) {
            String results[] = new String[mimeMappings.size()];
            return
                ((String[]) mimeMappings.keySet().toArray(results));
        }

    }


    /**
     * Return the value for the specified context initialization
     * parameter name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the parameter to return
     */
    public String findParameter(String name) {

        synchronized (parameters) {
            return ((String) parameters.get(name));
        }

    }


    /**
     * Return the names of all defined context initialization parameters
     * for this Context.  If no parameters are defined, a zero-length
     * array is returned.
     */
    public String[] findParameters() {

        synchronized (parameters) {
            String results[] = new String[parameters.size()];
            return ((String[]) parameters.keySet().toArray(results));
        }

    }


    /**
     * Return the resource reference with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource reference
     */
    public ContextResource findResource(String name) {

        return namingResources.findResource(name);

    }


    /**
     * Return the resource environment reference type for the specified
     * name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource environment reference
     */
    public String findResourceEnvRef(String name) {

        return namingResources.findResourceEnvRef(name);

    }


    /**
     * Return the set of resource environment reference names for this
     * web application.  If none have been specified, a zero-length
     * array is returned.
     */
    public String[] findResourceEnvRefs() {

        return namingResources.findResourceEnvRefs();

    }


    /**
     * Return the resource link with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource link
     */
    public ContextResourceLink findResourceLink(String name) {

        return namingResources.findResourceLink(name);

    }


    /**
     * Return the defined resource links for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResourceLink[] findResourceLinks() {

        return namingResources.findResourceLinks();

    }


    /**
     * Return the defined resource references for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResource[] findResources() {

        return namingResources.findResources();

    }


    /**
     * For the given security role (as used by an application), return the
     * corresponding role name (as defined by the underlying Realm) if there
     * is one.  Otherwise, return the specified role unchanged.
     *
     * @param role Security role to map
     */
    public String findRoleMapping(String role) {

        String realRole = null;
        synchronized (roleMappings) {
            realRole = (String) roleMappings.get(role);
        }
        if (realRole != null)
            return (realRole);
        else
            return (role);

    }


    /**
     * Return <code>true</code> if the specified security role is defined
     * for this application; otherwise return <code>false</code>.
     *
     * @param role Security role to verify
     */
    public boolean findSecurityRole(String role) {

        synchronized (securityRoles) {
            for (int i = 0; i < securityRoles.length; i++) {
                if (role.equals(securityRoles[i]))
                    return (true);
            }
        }
        return (false);

    }


    /**
     * Return the security roles defined for this application.  If none
     * have been defined, a zero-length array is returned.
     */
    public String[] findSecurityRoles() {

        return (securityRoles);

    }


    /**
     * Return the servlet name mapped by the specified pattern (if any);
     * otherwise return <code>null</code>.
     *
     * @param pattern Pattern for which a mapping is requested
     */
    public String findServletMapping(String pattern) {

        synchronized (servletMappings) {
            return ((String) servletMappings.get(pattern));
        }

    }


    /**
     * Return the patterns of all defined servlet mappings for this
     * Context.  If no mappings are defined, a zero-length array is returned.
     */
    public String[] findServletMappings() {

        synchronized (servletMappings) {
            String results[] = new String[servletMappings.size()];
            return
               ((String[]) servletMappings.keySet().toArray(results));
        }

    }


    /**
     * Return the context-relative URI of the error page for the specified
     * HTTP status code, if any; otherwise return <code>null</code>.
     *
     * @param status HTTP status code to look up
     */
    public String findStatusPage(int status) {

        return ((String) statusPages.get(Integer.valueOf(status)));

    }


    /**
     * Return the set of HTTP status codes for which error pages have
     * been specified.  If none are specified, a zero-length array
     * is returned.
     */
    public int[] findStatusPages() {

        synchronized (statusPages) {
            int results[] = new int[statusPages.size()];
            Iterator elements = statusPages.keySet().iterator();
            int i = 0;
            while (elements.hasNext())
                results[i++] = ((Integer) elements.next()).intValue();
            return (results);
        }

    }


    /**
     * Return the tag library descriptor location for the specified taglib
     * URI, if any; otherwise, return <code>null</code>.
     *
     * @param uri URI, relative to the web.xml file
     */
    public String findTaglib(String uri) {

        synchronized (taglibs) {
            return ((String) taglibs.get(uri));
        }

    }


    /**
     * Return the URIs of all tag libraries for which a tag library
     * descriptor location has been specified.  If none are specified,
     * a zero-length array is returned.
     */
    public String[] findTaglibs() {

        synchronized (taglibs) {
            String results[] = new String[taglibs.size()];
            return ((String[]) taglibs.keySet().toArray(results));
        }

    }


    /**
     * Return <code>true</code> if the specified welcome file is defined
     * for this Context; otherwise return <code>false</code>.
     *
     * @param name Welcome file to verify
     */
    public boolean findWelcomeFile(String name) {

        synchronized (welcomeFiles) {
            for (int i = 0; i < welcomeFiles.length; i++) {
                if (name.equals(welcomeFiles[i]))
                    return (true);
            }
        }
        return (false);

    }


    /**
     * Return the set of welcome files defined for this Context.  If none are
     * defined, a zero-length array is returned.
     */
    public String[] findWelcomeFiles() {

        return (welcomeFiles);

    }


    /**
     * Return the set of LifecycleListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperLifecycles() {

        return (wrapperLifecycles);

    }


    /**
     * Return the set of ContainerListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperListeners() {

        return (wrapperListeners);

    }


    /**
     * Reload this web application, if reloading is supported.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  This method is designed to deal with
     * reloads required by changes to classes in the underlying repositories
     * of our class loader.  It does not handle changes to the web application
     * deployment descriptor.  If that has occurred, you should stop this
     * Context and create (and start) a new Context instance instead.
     *
     * @exception IllegalStateException if the <code>reloadable</code>
     *  property is set to <code>false</code>.
     */
    public synchronized void reload() {

        // Validate our current component state
        if (!started)
            throw new IllegalStateException
                (sm.getString("containerBase.notStarted", logName()));

        // Make sure reloading is enabled
        //      if (!reloadable)
        //          throw new IllegalStateException
        //              (sm.getString("standardContext.notReloadable"));
        log.info(sm.getString("standardContext.reloadingStarted"));

        // Stop accepting requests temporarily
        setPaused(true);

        try {
            stop();
        } catch (LifecycleException e) {
            log.log(Level.SEVERE,
                    sm.getString("standardContext.stoppingContext", this),
                    e);
        }

        try {
            start();
        } catch (LifecycleException e) {
            log.log(Level.SEVERE,
                    sm.getString("standardContext.startingContext", this),
                    e);
        }

        setPaused(false);

    }


    /**
     * Remove the specified application listener class from the set of
     * listeners for this application.
     *
     * @param listener Java class name of the listener to be removed
     */
    public void removeApplicationListener(String listener) {

        synchronized (applicationListeners) {

            // Make sure this welcome file is currently present
            int n = -1;
            for (int i = 0; i < applicationListeners.length; i++) {
                if (applicationListeners[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified constraint
            int j = 0;
            String results[] = new String[applicationListeners.length - 1];
            for (int i = 0; i < applicationListeners.length; i++) {
                if (i != n)
                    results[j++] = applicationListeners[i];
            }
            applicationListeners = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeApplicationListener", listener);
        }

        // FIXME - behavior if already started?

    }


    /**
     * Remove the application parameter with the specified name from
     * the set for this application.
     *
     * @param name Name of the application parameter to remove
     */
    public void removeApplicationParameter(String name) {

        synchronized (applicationParameters) {

            // Make sure this parameter is currently present
            int n = -1;
            for (int i = 0; i < applicationParameters.length; i++) {
                if (name.equals(applicationParameters[i].getName())) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified parameter
            int j = 0;
            ApplicationParameter results[] =
                new ApplicationParameter[applicationParameters.length - 1];
            for (int i = 0; i < applicationParameters.length; i++) {
                if (i != n)
                    results[j++] = applicationParameters[i];
            }
            applicationParameters = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeApplicationParameter", name);
        }
    }


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Wrapper.
     *
     * @param child Child container to be added
     *
     * @exception IllegalArgumentException if the proposed container is
     *  not an implementation of Wrapper
     */
    public void removeChild(Container child) {

        if (!(child instanceof Wrapper))
            throw new IllegalArgumentException
                (sm.getString("standardContext.notWrapper"));

        super.removeChild(child);
    }


    /**
     * Remove the specified security constraint from this web application.
     *
     * @param constraint Constraint to be removed
     */
    public void removeConstraint(SecurityConstraint constraint) {

        synchronized (constraints) {

            // Make sure this constraint is currently present
            int n = -1;
            for (int i = 0; i < constraints.length; i++) {
                if (constraints[i].equals(constraint)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified constraint
            int j = 0;
            SecurityConstraint results[] =
                new SecurityConstraint[constraints.length - 1];
            for (int i = 0; i < constraints.length; i++) {
                if (i != n)
                    results[j++] = constraints[i];
            }
            constraints = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeConstraint", constraint);
        }
    }


    /**
     * Remove any EJB resource reference with the specified name.
     *
     * @param name Name of the EJB resource reference to remove
     */
    public void removeEjb(String name) {

        namingResources.removeEjb(name);
     
        if (notifyContainerListeners) {
            fireContainerEvent("removeEjb", name);
        }
    }


    /**
     * Remove any environment entry with the specified name.
     *
     * @param name Name of the environment entry to remove
     */
    public void removeEnvironment(String name) {
        if (namingResources == null) {
            return;
        }
        ContextEnvironment env = namingResources.findEnvironment(name);
        if (env == null) {
            throw new IllegalArgumentException
                ("Invalid environment name '" + name + "'");
        }

        namingResources.removeEnvironment(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeEnvironment", name);
        }
    }


    /**
     * Remove the error page for the specified error code or
     * Java language exception, if it exists; otherwise, no action is taken.
     *
     * @param errorPage The error page definition to be removed
     */
    public void removeErrorPage(ErrorPage errorPage) {

        String exceptionType = errorPage.getExceptionType();
        if (exceptionType != null) {
            synchronized (exceptionPages) {
                exceptionPages.remove(exceptionType);
            }
        } else {
            synchronized (statusPages) {
                statusPages.remove(Integer.valueOf(errorPage.getErrorCode()));
            }
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeErrorPage", errorPage);
        }
    }


    /**
     * Remove the specified filter definition from this Context, if it exists;
     * otherwise, no action is taken.
     *
     * @param filterDef Filter definition to be removed
     */
    public void removeFilterDef(FilterDef filterDef) {

        synchronized (filterDefs) {
            filterDefs.remove(filterDef.getFilterName());
        }
 
        if (notifyContainerListeners) {
            fireContainerEvent("removeFilterDef", filterDef);
        }
    }


    /**
     * Remove a filter mapping from this Context.
     *
     * @param filterMap The filter mapping to be removed
     */
    public void removeFilterMap(FilterMap filterMap) {

        synchronized (filterMaps) {

            // Make sure this filter mapping is currently present
            int n = -1;
            for (int i = 0; i < filterMaps.length; i++) {
                if (filterMaps[i] == filterMap) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified filter mapping
            FilterMap results[] = new FilterMap[filterMaps.length - 1];
            System.arraycopy(filterMaps, 0, results, 0, n);
            System.arraycopy(filterMaps, n + 1, results, n,
                             (filterMaps.length - 1) - n);
            filterMaps = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeFilterMap", filterMap);
        }
    }


    /**
     * Remove a class name from the set of InstanceListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of an InstanceListener class to be removed
     */
    public void removeInstanceListener(String listener) {

        synchronized (instanceListeners) {

            // Make sure this welcome file is currently present
            int n = -1;
            for (int i = 0; i < instanceListeners.length; i++) {
                if (instanceListeners[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified constraint
            int j = 0;
            String results[] = new String[instanceListeners.length - 1];
            for (int i = 0; i < instanceListeners.length; i++) {
                if (i != n)
                    results[j++] = instanceListeners[i];
            }
            instanceListeners = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeInstanceListener", listener);
        }
    }


    /**
     * Remove any local EJB resource reference with the specified name.
     *
     * @param name Name of the EJB resource reference to remove
     */
    public void removeLocalEjb(String name) {

        namingResources.removeLocalEjb(name);
        
        if (notifyContainerListeners) {
            fireContainerEvent("removeLocalEjb", name);
        }
    }


    /**
     * Remove any message destination with the specified name.
     *
     * @param name Name of the message destination to remove
     */
    public void removeMessageDestination(String name) {

        synchronized (messageDestinations) {
            messageDestinations.remove(name);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeMessageDestination", name);
        }
    }


    /**
     * Remove any message destination ref with the specified name.
     *
     * @param name Name of the message destination ref to remove
     */
    public void removeMessageDestinationRef(String name) {

        namingResources.removeMessageDestinationRef(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeMessageDestinationRef", name);
        }
    }


    /**
     * Remove the MIME mapping for the specified extension, if it exists;
     * otherwise, no action is taken.
     *
     * @param extension Extension to remove the mapping for
     */
    public void removeMimeMapping(String extension) {

        synchronized (mimeMappings) {
            mimeMappings.remove(extension);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeMimeMapping", extension);
        }
    }


    /**
     * Remove the context initialization parameter with the specified
     * name, if it exists; otherwise, no action is taken.
     *
     * @param name Name of the parameter to remove
     */
    public void removeParameter(String name) {

        synchronized (parameters) {
            parameters.remove(name);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeParameter", name);
        }
    }


    /**
     * Remove any resource reference with the specified name.
     *
     * @param name Name of the resource reference to remove
     */
    public void removeResource(String name) {
        name = URLDecoder.decode(name);
        if (namingResources == null) {
            return;
        }
        ContextResource resource = namingResources.findResource(name);
        if (resource == null) {
            throw new IllegalArgumentException
                ("Invalid resource name '" + name + "'");
        }

        namingResources.removeResource(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeResource", name);
        }
    }


    /**
     * Remove any resource environment reference with the specified name.
     *
     * @param name Name of the resource environment reference to remove
     */
    public void removeResourceEnvRef(String name) {

        namingResources.removeResourceEnvRef(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeResourceEnvRef", name);
        }
    }


    /**
     * Remove any resource link with the specified name.
     *
     * @param name Name of the resource link to remove
     */
    public void removeResourceLink(String name) {
        name = URLDecoder.decode(name);
        if (namingResources == null) {
            return;
        }
        ContextResourceLink resource = namingResources.findResourceLink(name);
        if (resource == null) {
            throw new IllegalArgumentException
                ("Invalid resource name '" + name + "'");
        }

        namingResources.removeResourceLink(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeResourceLink", name);
        }
    }


    /**
     * Remove any security role reference for the specified name
     *
     * @param role Security role (as used in the application) to remove
     */
    public void removeRoleMapping(String role) {

        synchronized (roleMappings) {
            roleMappings.remove(role);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeRoleMapping", role);
        }
    }


    /**
     * Remove any security role with the specified name.
     *
     * @param role Security role to remove
     */
    public void removeSecurityRole(String role) {

        synchronized (securityRoles) {

            // Make sure this security role is currently present
            int n = -1;
            for (int i = 0; i < securityRoles.length; i++) {
                if (role.equals(securityRoles[i])) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified security role
            int j = 0;
            String results[] = new String[securityRoles.length - 1];
            for (int i = 0; i < securityRoles.length; i++) {
                if (i != n)
                    results[j++] = securityRoles[i];
            }
            securityRoles = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeSecurityRole", role);
        }
    }


    /**
     * Remove any servlet mapping for the specified pattern, if it exists;
     * otherwise, no action is taken.
     *
     * @param pattern URL pattern of the mapping to remove
     */
    public void removeServletMapping(String pattern) {

        String name = null;
        synchronized (servletMappings) {
            name = (String) servletMappings.remove(pattern);
        }
        Wrapper wrapper = (Wrapper) findChild(name);
        if( wrapper != null ) {
            wrapper.removeMapping(pattern);
        }
        mapper.removeWrapper(pattern);

        if (notifyContainerListeners) {
            fireContainerEvent("removeServletMapping", pattern);
        }
    }


    /**
     * Remove the tag library location forthe specified tag library URI.
     *
     * @param uri URI, relative to the web.xml file
     */
    public void removeTaglib(String uri) {

        synchronized (taglibs) {
            taglibs.remove(uri);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeTaglib", uri);
        }
    }


    /**
     * Remove the specified welcome file name from the list recognized
     * by this Context.
     *
     * @param name Name of the welcome file to be removed
     */
    public void removeWelcomeFile(String name) {

        synchronized (welcomeFiles) {

            // Make sure this welcome file is currently present
            int n = -1;
            for (int i = 0; i < welcomeFiles.length; i++) {
                if (welcomeFiles[i].equals(name)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified constraint
            int j = 0;
            String results[] = new String[welcomeFiles.length - 1];
            for (int i = 0; i < welcomeFiles.length; i++) {
                if (i != n)
                    results[j++] = welcomeFiles[i];
            }
            welcomeFiles = results;

        }

        // Inform interested listeners
        postWelcomeFiles();

        if (notifyContainerListeners) {
            fireContainerEvent("removeWelcomeFile", name);
        }
    }


    /**
     * Remove a class name from the set of LifecycleListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a LifecycleListener class to be removed
     */
    public void removeWrapperLifecycle(String listener) {


        synchronized (wrapperLifecycles) {

            // Make sure this welcome file is currently present
            int n = -1;
            for (int i = 0; i < wrapperLifecycles.length; i++) {
                if (wrapperLifecycles[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified constraint
            int j = 0;
            String results[] = new String[wrapperLifecycles.length - 1];
            for (int i = 0; i < wrapperLifecycles.length; i++) {
                if (i != n)
                    results[j++] = wrapperLifecycles[i];
            }
            wrapperLifecycles = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeWrapperLifecycle", listener);
        }
    }


    /**
     * Remove a class name from the set of ContainerListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a ContainerListener class to be removed
     */
    public void removeWrapperListener(String listener) {


        synchronized (wrapperListeners) {

            // Make sure this welcome file is currently present
            int n = -1;
            for (int i = 0; i < wrapperListeners.length; i++) {
                if (wrapperListeners[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified constraint
            int j = 0;
            String results[] = new String[wrapperListeners.length - 1];
            for (int i = 0; i < wrapperListeners.length; i++) {
                if (i != n)
                    results[j++] = wrapperListeners[i];
            }
            wrapperListeners = results;

        }

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeWrapperListener", listener);
        }
    }


    /**
     * Gets the cumulative processing times of all servlets in this
     * StandardContext.
     *
     * @return Cumulative processing times of all servlets in this
     * StandardContext
     */
    public long getProcessingTimeMillis() {
        
        long result = 0;

        Container[] children = findChildren();
        if (children != null) {
            for( int i=0; i< children.length; i++ ) {
                result += ((StandardWrapper)children[i]).getProcessingTimeMillis();
            }
        }

        return result;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * SailFin extension.
     *
     * Allows context to implement additional tasks at the beginning of its
     * pipeline invocation.
     */
    public void beginPipelineInvoke(Session s) {
        // Deliberate noop
    }

    /**
     * SailFin extension.
     *
     * Allows context to implement additional tasks at the end of its
     * pipeline invocation.
     */
    public void endPipelineInvoke() {
        // Deliberate noop
    }

    /**
     * Configure and initialize the set of filters for this Context.
     * Return <code>true</code> if all filter initialization completed
     * successfully, or <code>false</code> otherwise.
     */
    public boolean filterStart() {

        if (log.isLoggable(Level.FINE))
            log.fine("Starting filters");
        // Instantiate and record a FilterConfig for each defined filter
        boolean ok = true;
        synchronized (filterConfigs) {
            filterConfigs.clear();
            Iterator names = filterDefs.keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                if (log.isLoggable(Level.FINE))
                    log.fine(" Starting filter '" + name + "'");
                ApplicationFilterConfig filterConfig = null;
                try {
                    filterConfig = new ApplicationFilterConfig
                      (this, (FilterDef) filterDefs.get(name));
                    filterConfigs.put(name, filterConfig);
                } catch (Throwable t) {
                    getServletContext().log
                        (sm.getString("standardContext.filterStart", name), t);
                    ok = false;
                }
            }
        }

        return (ok);

    }


    /**
     * Finalize and release the set of filters for this Context.
     * Return <code>true</code> if all filter finalization completed
     * successfully, or <code>false</code> otherwise.
     */
    public boolean filterStop() {

        if (log.isLoggable(Level.FINE))
            log.fine("Stopping filters");

        // Release all Filter and FilterConfig instances
        synchronized (filterConfigs) {
            Iterator names = filterConfigs.keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                if (log.isLoggable(Level.FINE))
                    log.fine(" Stopping filter '" + name + "'");
                ApplicationFilterConfig filterConfig =
                  (ApplicationFilterConfig) filterConfigs.get(name);
                filterConfig.release();
            }
            filterConfigs.clear();
        }
        return (true);

    }


    /**
     * Find and return the initialized <code>FilterConfig</code> for the
     * specified filter name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the desired filter
     */
    public FilterConfig findFilterConfig(String name) {

        return ((FilterConfig) filterConfigs.get(name));

    }


    /**
     * Configure the set of instantiated application event listeners
     * for this Context.  Return <code>true</code> if all listeners wre
     * initialized successfully, or <code>false</code> otherwise.
     */
    public boolean listenerStart() {

        if (log.isLoggable(Level.FINE))
            log.fine("Configuring application event listeners");

        // Instantiate the required listeners
        ClassLoader loader = getLoader().getClassLoader();
        String listeners[] = findApplicationListeners();
        Object results[] = new Object[listeners.length];
        boolean ok = true;
        for (int i = 0; i < results.length; i++) {
            try {
                results[i] = loadListener(loader, listeners[i]);
            } catch (Throwable t) {
                getServletContext().log
                    (sm.getString("standardContext.applicationListener",
                                  listeners[i]), t);
                ok = false;
            }
        }
        if (!ok) {
            log.severe(sm.getString("standardContext.applicationSkipped"));
            return (false);
        }

        // Sort listeners in two arrays
        ArrayList eventListeners = new ArrayList();
        ArrayList lifecycleListeners = new ArrayList();
        for (int i = 0; i < results.length; i++) {
            if ((results[i] instanceof ServletContextAttributeListener)
                || (results[i] instanceof ServletRequestAttributeListener)
                || (results[i] instanceof ServletRequestListener)
                || (results[i] instanceof HttpSessionAttributeListener)) {
                eventListeners.add(results[i]);
            }
            if ((results[i] instanceof ServletContextListener)
                || (results[i] instanceof HttpSessionListener)) {
                lifecycleListeners.add(results[i]);
            }
        }

        setApplicationEventListeners(eventListeners.toArray());
        setApplicationLifecycleListeners(lifecycleListeners.toArray());

        // Send application start events

        if (log.isLoggable(Level.FINE))
            log.fine("Sending application start events");

        Object instances[] = getApplicationLifecycleListeners();
        if (instances == null)
            return (ok);
        ServletContextEvent event =
          new ServletContextEvent(getServletContext());
        for (int i = 0; i < instances.length; i++) {
            if (instances[i] == null)
                continue;
            if (!(instances[i] instanceof ServletContextListener))
                continue;
            ServletContextListener listener =
                (ServletContextListener) instances[i];
            try {
                fireContainerEvent(ContainerEvent.BEFORE_CONTEXT_INITIALIZED,
                                   listener);
                listener.contextInitialized(event);
                fireContainerEvent(ContainerEvent.AFTER_CONTEXT_INITIALIZED,
                                   listener);
            } catch (Throwable t) {
                fireContainerEvent(ContainerEvent.AFTER_CONTEXT_INITIALIZED,
                                   listener);
                getServletContext().log
                    (sm.getString("standardContext.listenerStart",
                                  instances[i].getClass().getName()), t);
                ok = false;
            }
        }
        return (ok);

    }


    /**
     * Instantiates and returns the listener with the specified classname
     *
     * @param loader the classloader to use
     * @param listenerClassName the fully qualified classname to instantiate
     *
     * @return the instantiated listener
     *
     * @throws Exception if the specified classname fails to be loaded or
     * instantiated
     */
    protected Object loadListener(ClassLoader loader, String listenerClassName)
            throws Exception {
        if (log.isLoggable(Level.FINE)) {
            log.fine(" Configuring event listener class '" +
                     listenerClassName + "'");
        }
        Class clazz = loader.loadClass(listenerClassName);
        Object listener = clazz.newInstance();
            // START PWC 1.2 6310695
        fireContainerEvent(ContainerEvent.AFTER_LISTENER_INSTANTIATED,
                           listener);
        // END PWC 1.2 6310695
        return listener;
    }


    /**
     * Send an application stop event to all interested listeners.
     * Return <code>true</code> if all events were sent successfully,
     * or <code>false</code> otherwise.
     */
    public boolean listenerStop() {

        if (log.isLoggable(Level.FINE))
            log.fine("Sending application stop events");

        boolean ok = true;
        Object[] listeners = getApplicationLifecycleListeners();
        Object[] eventListeners = getApplicationEventListeners();
        if (listeners == null && eventListeners == null) {
            return (ok);
        }
        ServletContextEvent event =
            new ServletContextEvent(getServletContext());
        for (int i=0; i<eventListeners.length; i++) {
            if (eventListeners[i] != null) {
                fireContainerEvent(ContainerEvent.PRE_DESTROY, 
                                   eventListeners[i]);
            }
        }
        for (int i = 0; i < listeners.length; i++) {
            int j = (listeners.length - 1) - i;
            if (listeners[j] == null) {
                continue;
            }
            if (!(listeners[j] instanceof ServletContextListener)) {
                fireContainerEvent(ContainerEvent.PRE_DESTROY, listeners[j]);
                continue;
            }
            ServletContextListener listener =
                (ServletContextListener) listeners[j];
            try {
                fireContainerEvent(ContainerEvent.BEFORE_CONTEXT_DESTROYED,
                                   listener);
                listener.contextDestroyed(event);
                fireContainerEvent(ContainerEvent.AFTER_CONTEXT_DESTROYED,
                                   listener);
            } catch (Throwable t) {
                fireContainerEvent(ContainerEvent.AFTER_CONTEXT_DESTROYED,
                                   listener);
                getServletContext().log
                    (sm.getString("standardContext.listenerStop",
                                  listeners[j].getClass().getName()), t);
                ok = false;
            }
        }

        setApplicationEventListeners(null);
        setApplicationLifecycleListeners(null);

        return (ok);
    }


    /**
     * Allocate resources, including proxy.
     * Return <code>true</code> if initialization was successfull,
     * or <code>false</code> otherwise.
     */
    public boolean resourcesStart() {

        boolean ok = true;

        Hashtable env = new Hashtable();
        if (getParent() != null)
            env.put(ProxyDirContext.HOST, getParent().getName());
        env.put(ProxyDirContext.CONTEXT, getName());

        try {
            ProxyDirContext proxyDirContext =
                new ProxyDirContext(env, webappResources);
            if (webappResources instanceof BaseDirContext) {
                ((BaseDirContext) webappResources).setDocBase(getBasePath(getDocBase()));
                ((BaseDirContext) webappResources).allocate();
            }
            // Register the cache in JMX
            if (isCachingAllowed()) {
                ObjectName resourcesName = new ObjectName(
                    this.getDomain() + ":type=Cache,host=" 
                    + getHostname() + ",path=" 
                    + (("".equals(encodedPath)) ? "/" : encodedPath));
                Registry.getRegistry(null, null).registerComponent
                    (proxyDirContext.getCache(), resourcesName, null);
            }
            this.resources = proxyDirContext;
        } catch (Throwable t) {
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.SEVERE,
                        sm.getString("standardContext.resourcesStart",
                                     getName()), 
                        t);
            } else {
                log.log(Level.SEVERE,
                        sm.getString("standardContext.resourcesStart",
                                     getName()) +
                        ": " + t.getMessage());
            }
            ok = false;
        }

        return (ok);
    }


    /**
     * Starts this context's alternate doc base resources.
     */
    public boolean alternateResourcesStart() {

        boolean ok = true;

        if (alternateDocBases == null || alternateDocBases.size() == 0) {
            return ok;
        }

        Hashtable env = new Hashtable();
        if (getParent() != null)
            env.put(ProxyDirContext.HOST, getParent().getName());
        env.put(ProxyDirContext.CONTEXT, getName());

        for (int i=0; i<alternateDocBases.size(); i++) {

            AlternateDocBase alternateDocBase = alternateDocBases.get(i);
            String basePath = alternateDocBase.getBasePath();
            DirContext alternateWebappResources =
                alternateDocBase.getWebappResources();
            try {
                ProxyDirContext proxyDirContext =
                    new ProxyDirContext(env, alternateWebappResources);
                if (alternateWebappResources instanceof BaseDirContext) {
                    ((BaseDirContext) alternateWebappResources).setDocBase(
                        basePath);
                    ((BaseDirContext) alternateWebappResources).allocate();
                }
                alternateDocBase.setResources(proxyDirContext);
            } catch (Throwable t) {
                if(log.isLoggable(Level.FINE)) {
                    log.log(Level.SEVERE,
                            sm.getString("standardContext.resourcesStart",
                                         getName()), 
                            t);
                } else {
                    log.log(Level.SEVERE,
                            sm.getString("standardContext.resourcesStart",
                                         getName()) +
                            ": " + t.getMessage());
                }
                ok = false;
            }
        }

        return ok;
    }


    /**
     * Deallocate resources and destroy proxy.
     */
    public boolean resourcesStop() {

        boolean ok = true;

        try {
            if (resources != null) {
                if (resources instanceof Lifecycle) {
                    ((Lifecycle) resources).stop();
                }
                if (webappResources instanceof BaseDirContext) {
                    ((BaseDirContext) webappResources).release();
                }
                // Unregister the cache in JMX
                if (isCachingAllowed()) {
                    ObjectName resourcesName = 
                        new ObjectName(this.getDomain()
                                       + ":type=Cache,host=" 
                                       + getHostname() + ",path=" 
                                       + (("".equals(getPath()))?"/"
                                          :getPath()));
                    Registry.getRegistry(null, null).unregisterComponent(resourcesName);
                }
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE,
                    sm.getString("standardContext.resourcesStop"), t);
            ok = false;
        }

        this.resources = null;

        return (ok);

    }


    /**
     * Stops this context's alternate doc base resources.
     */
    public boolean alternateResourcesStop() {

        boolean ok = true;

        if (alternateDocBases == null || alternateDocBases.size() == 0) {
            return ok;
        }

        for (int i=0; i<alternateDocBases.size(); i++) {

            AlternateDocBase alternateDocBase = alternateDocBases.get(i);
            DirContext alternateResources =
                alternateDocBase.getResources();
            if (alternateResources instanceof Lifecycle) {
                try {
                    ((Lifecycle) alternateResources).stop();
                } catch (Throwable t) {
                    log.log(Level.SEVERE,
                            sm.getString("standardContext.resourcesStop"),
                            t);
                    ok = false;
                }
            }

            DirContext alternateWebappResources =
                alternateDocBase.getWebappResources();
            if (alternateWebappResources instanceof BaseDirContext) {
                try {
                    ((BaseDirContext) alternateWebappResources).release();
                } catch (Throwable t) {
                    log.log(Level.SEVERE,
                            sm.getString("standardContext.resourcesStop"),
                            t);
                    ok = false;
                }
            }
        }

        this.alternateDocBases = null;

        return (ok);

    }


    /**
     * Load and initialize all servlets marked "load on startup" in the
     * web application deployment descriptor.
     *
     * @param children Array of wrappers for all currently defined
     *  servlets (including those not declared load on startup)
     */
    /* SJSAS 6377790
    public void loadOnStartup(Container children[]){
    */
    // START SJSAS 6377790
    public void loadOnStartup(Container children[]) throws LifecycleException {
    // END SJSAS 6377790
        // Collect "load on startup" servlets that need to be initialized
        TreeMap map = new TreeMap();
        for (int i = 0; i < children.length; i++) {
            Wrapper wrapper = (Wrapper) children[i];
            int loadOnStartup = wrapper.getLoadOnStartup();
            if (loadOnStartup < 0)
                continue;
            if (loadOnStartup == 0)     // Arbitrarily put them last
                loadOnStartup = Integer.MAX_VALUE;
            Integer key = Integer.valueOf(loadOnStartup);
            ArrayList list = (ArrayList) map.get(key);
            if (list == null) {
                list = new ArrayList();
                map.put(key, list);
            }
            list.add(wrapper);
        }

        // Load the collected "load on startup" servlets
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            Integer key = (Integer) keys.next();
            ArrayList list = (ArrayList) map.get(key);
            Iterator wrappers = list.iterator();
            while (wrappers.hasNext()) {
                Wrapper wrapper = (Wrapper) wrappers.next();
                try {
                    wrapper.load();
                } catch (ServletException e) {
                    getServletContext().log
                        (sm.getString("standardWrapper.loadException",
                                      getName()),
                        StandardWrapper.getRootCause(e));
                    // NOTE: load errors (including a servlet that throws
                    // UnavailableException from tht init() method) are NOT
                    // fatal to application startup
                    // START SJSAS 6377790
                    throw new LifecycleException(
                        StandardWrapper.getRootCause(e));
                    // END SJSAS 6377790
                }
            }
        }

    }


    /**
     * Start this Context component.
     *
     * @exception LifecycleException if a startup error occurs
     */
    public synchronized void start() throws LifecycleException {

        //if (lazy ) return;
        if (started) {
            if (log.isLoggable(Level.INFO)) {
                log.info(sm.getString("containerBase.alreadyStarted",
                                      logName()));
            }
            return;
        }

        long startupTimeStart = System.currentTimeMillis();

        if( !initialized ) { 
            try {
                init();
            } catch( Exception ex ) {
                throw new LifecycleException("Error initializaing ", ex);
            }
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Starting " + ("".equals(getName()) ? "ROOT" : getName()));
        }

        // Set JMX object name for proper pipeline registration
        preRegisterJMX();

        if ((oname != null) && 
            (Registry.getRegistry().getMBeanServer().isRegistered(oname))) {
            // As things depend on the JMX registration, the context
            // must be reregistered again once properly initialized
            Registry.getRegistry(null, null).unregisterComponent(oname);
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        setAvailable(false);
        setConfigured(false);
        boolean ok = true;

        // Set config file name
        File configBase = getConfigBase();
        if (configBase != null && saveConfig) {
            if (getConfigFile() == null) {
                File file = new File(configBase, getDefaultConfigFile());
                setConfigFile(file.getPath());
                // If the docbase is outside the appBase, we should save our
                // config
                try {
                    File appBaseFile = new File(getAppBase());
                    if (!appBaseFile.isAbsolute()) {
                        appBaseFile = new File(engineBase(), getAppBase());
                    }
                    String appBase = appBaseFile.getCanonicalPath();
                    String basePath = 
                        (new File(getBasePath(getDocBase()))).getCanonicalPath();
                    if (!basePath.startsWith(appBase)) {
                        Server server = ServerFactory.getServer();
                        ((StandardServer) server).storeContext(this);
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error storing config file", e);
                }
            } else {
                try {
                    String canConfigFile = 
                        (new File(getConfigFile())).getCanonicalPath();
                    if (!canConfigFile.startsWith
                        (configBase.getCanonicalPath())) {
                        File file = 
                            new File(configBase, getDefaultConfigFile());
                        if (copy(new File(canConfigFile), file)) {
                            setConfigFile(file.getPath());
                        }
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error setting config file", e);
                }
            }
        }

        // Install DefaultContext configuration
        if (!getOverride()) {
            Container host = getParent();
            if (host instanceof StandardHost) {
                ((StandardHost)host).installDefaultContext(this);
                Container engine = host.getParent();
                if( engine instanceof StandardEngine ) {
                    ((StandardEngine)engine).installDefaultContext(this);
                }
            }
        }

        // Add missing components as necessary
        if (webappResources == null) {   // (1) Required by Loader
            if (log.isLoggable(Level.FINE))
                log.fine("Configuring default Resources");
            try {
                if ((docBase != null) && (docBase.endsWith(".war")) && 
                        (!(new File(docBase).isDirectory()))) 
                    setResources(new WARDirContext());
                else
                    setResources(new FileDirContext());
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE,
                        sm.getString("standardContext.resourcesInit"), e);
                ok = false;
            }
        }
        if (ok) {
            if (!resourcesStart()) {
                ok = false;
            }
        }

        // Add alternate resources
        if (alternateDocBases != null && alternateDocBases.size() > 0) {

            for (int i=0; i<alternateDocBases.size(); i++) {

                AlternateDocBase alternateDocBase = alternateDocBases.get(i);
                String docBase = alternateDocBase.getDocBase();

                if (log.isLoggable(Level.FINE)) {
                    log.fine("Configuring alternate resources");
                }
                try {
                    if (docBase != null && docBase.endsWith(".war") &&
                            (!(new File(docBase).isDirectory()))) {
                        setAlternateResources(alternateDocBase,
                                              new WARDirContext());
                    } else {
                        setAlternateResources(alternateDocBase,
                                              new FileDirContext());
                    }
                } catch (IllegalArgumentException e) {
                    log.log(Level.SEVERE,
                            sm.getString("standardContext.resourcesInit"),
                            e);
                    ok = false;
                }
            }
            if (ok) {
                if (!alternateResourcesStart()) {
                    ok = false;
                }
            }
        }

        // Look for a realm - that may have been configured earlier. 
        // If the realm is added after context - it'll set itself.
        if( realm == null ) {
            ObjectName realmName=null;
            try {
                realmName=new ObjectName( getEngineName() + ":type=Realm,host=" 
                                        + getHostname() + ",path=" + getPath());
                if( mserver.isRegistered(realmName ) ) {
                    mserver.invoke(realmName, "init", 
                            new Object[] {},
                            new String[] {}
                    );            
                }
            } catch( Throwable t ) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("No realm for this host " + realmName);
                }
            }
        }
        
        if (getLoader() == null) {
            createLoader();
        }

        // Initialize character set mapper
        getCharsetMapper();

        // Post work directory
        postWorkDirectory();

        // Validate required extensions
        boolean dependencyCheck = true;
        try {
            dependencyCheck = ExtensionValidator.validateApplication
                (getResources(), this);
        } catch (IOException ioe) {
            log.log(Level.SEVERE,
                    sm.getString("standardContext.dependencyCheck", this),
                    ioe);
            dependencyCheck = false;
        }

        if (!dependencyCheck) {
            // do not make application available if depency check fails
            ok = false;
        }

        // Reading the "catalina.useNaming" environment variable
        String useNamingProperty = System.getProperty("catalina.useNaming");
        if ((useNamingProperty != null)
            && (useNamingProperty.equals("false"))) {
            useNaming = false;
        }

        if (ok && isUseNaming()) {
            if (namingContextListener == null) {
                namingContextListener = new NamingContextListener();
                namingContextListener.setDebug(getDebug());
                namingContextListener.setName(getNamingContextName());
                addLifecycleListener(namingContextListener);
            }
        }

        // Binding thread
        // START OF SJSAS 8.1 6174179
        //ClassLoader oldCCL = bindThread();
        ClassLoader oldCCL = null;
        // END OF SJSAS 8.1 6174179

        // Standard container startup
        if (log.isLoggable(Level.FINEST))
            log.finest("Processing standard container startup");

        boolean mainOk = false;
        try {
            if (ok) {

                started = true;

                // Start our subordinate components, if any
                if ((loader != null) && (loader instanceof Lifecycle))
                    ((Lifecycle) loader).start();
                if ((logger != null) && (logger instanceof Lifecycle))
                    ((Lifecycle) logger).start();

                // Unbinding thread
                // START OF SJSAS 8.1 6174179
                //unbindThread(oldCCL);
                // END OF SJSAS 8.1 6174179
                
                // Binding thread
                oldCCL = bindThread();

                if ((realm != null) && (realm instanceof Lifecycle))
                    ((Lifecycle) realm).start();
                if ((resources != null) && (resources instanceof Lifecycle))
                    ((Lifecycle) resources).start();

                // Start our child containers, if any
                Container children[] = findChildren();
                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof Lifecycle)
                        ((Lifecycle) children[i]).start();
                }

                // Start the Valves in our pipeline (including the basic),
                // if any
                if (pipeline instanceof Lifecycle)
                    ((Lifecycle) pipeline).start();
                
                // START SJSAS 8.1 5049111 
                // Notify our interested LifecycleListeners
                lifecycle.fireLifecycleEvent(START_EVENT, null);  
                
                if (TldConfig.getScanParentTldListener() == false)
                    isJsfApplication = isJsfServletDefined();
                // END SJSAS 8.1 5049111 
               
                // Read tldListeners. XXX  Option to disable                
                TldConfig tldConfig = new TldConfig();
                tldConfig.setContext(this);

                // (1)  check if the attribute has been defined
                //      on the context element.
                tldConfig.setTldValidation(tldValidation);
                tldConfig.setTldNamespaceAware(tldNamespaceAware);

                // (2) if the attribute wasn't defined on the context
                //     try the host.
                if (!tldValidation){
                    tldConfig.setTldValidation
                        (((StandardHost) getParent()).getXmlValidation());
                }

                try {
                    tldConfig.execute();
                } catch (Exception ex) {
                    log.log(Level.SEVERE,
                            sm.getString("standardContext.tldConfig"), ex);
                    //ok=false;
                }
                
                 // START SJSAS 8.1 5049111 
                // Notify our interested LifecycleListeners
                // lifecycle.fireLifecycleEvent(START_EVENT, null);  
                // END SJSAS 8.1 504911
                
                mainOk = true;
            }
        } finally {
            // Unbinding thread
            unbindThread(oldCCL);
            if (!mainOk) {
                // An exception occurred
                // Register with JMX anyway, to allow management
                registerJMX();
            }
        }

        if (!getConfigured()) {
            ok = false;
        }

        // We put the resources into the servlet context
        if (ok) {
            getServletContext().setAttribute
                (Globals.RESOURCES_ATTR, getResources());
            context.setAttributeReadOnly(Globals.RESOURCES_ATTR);
            getServletContext().setAttribute
                (Globals.ALTERNATE_RESOURCES_ATTR, getAlternateDocBases());
            context.setAttributeReadOnly(Globals.ALTERNATE_RESOURCES_ATTR);
        }
        
        // Initialize associated mapper
        mapper.setContext(getPath(), welcomeFiles, resources);

        // Binding thread
        oldCCL = bindThread();

        try{
            // Create context attributes that will be required
            if (ok) {
                if (log.isLoggable(Level.FINE))
                    log.fine("Posting standard context attributes");
                postWelcomeFiles();
            }

            if (ok) {
                // Notify our interested LifecycleListeners
                lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
            }


            // Configure and call application event listeners
            if (ok) {
                if (!listenerStart()) {
                    ok = false;
                }
            }

            try {
                // Start manager
                if ((manager != null) && (manager instanceof Lifecycle)) {
                    ((Lifecycle) getManager()).start();
                }

                // Start ContainerBackgroundProcessor thread
                super.threadStart();
            } catch(Exception e) {
                log.log(Level.SEVERE, sm.getString("standardContext.startManager.error"), e);
                ok = false;
            }

            // Configure and call application filters
            if (ok) {
                if (!filterStart()) {
                    ok = false;
                }
            }

            // Load and initialize all "load on startup" servlets
            if (ok) {
                loadOnStartup(findChildren());
            }
        } finally {
            // Unbinding thread
            unbindThread(oldCCL);
        }

        // Set available status depending upon startup success
        if (ok) {
            if (log.isLoggable(Level.FINEST))
                log.finest("Starting completed");
            setAvailable(true);
        } else {
            log.severe(sm.getString("standardContext.startFailed", getName()));
            try {
                stop();
            } catch (Throwable t) {
                log.log(Level.SEVERE,
                        sm.getString("standardContext.startCleanup"), t);
            }
            setAvailable(false);
        }

        // JMX registration
        registerJMX();

        startTimeMillis = System.currentTimeMillis();
        startupTime = startTimeMillis - startupTimeStart;

        // Send j2ee.state.running notification 
        if (ok && (this.getObjectName() != null)) {
            Notification notification = 
                new Notification("j2ee.state.running", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        }

        // Close all JARs right away to avoid always opening a peak number 
        // of files on startup
        if (getLoader() instanceof WebappLoader) {
            ((WebappLoader) getLoader()).closeJARs(true);
        }

        // Reinitializing if something went wrong
        if (!ok && started) {
            stop();
        }

        //cacheContext();
    }


    /**
     * Creates a classloader for this context.
     */
    public void createLoader() {
        ClassLoader parent = null;
        if (getPrivileged()) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Configuring privileged default Loader");
            }
            parent = this.getClass().getClassLoader();
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Configuring non-privileged default Loader");
            }
            parent = getParentClassLoader();
        }
        WebappLoader webappLoader = new WebappLoader(parent);
        webappLoader.setDelegate(getDelegate());
        webappLoader.setUseMyFaces(useMyFaces);
        setLoader(webappLoader);
    }

    
    private void cacheContext() {
        try {
            File workDir=new File( getWorkPath() );
            
            File ctxSer=new File( workDir, "_tomcat_context.ser");
            FileOutputStream fos=new FileOutputStream( ctxSer );
            ObjectOutputStream oos=new ObjectOutputStream( fos );
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch( Throwable t ) {
            log.log(Level.INFO, "Error saving context.ser ", t);
        }
    }


    /**
     * Stop this Context component.
     *
     * @exception LifecycleException if a shutdown error occurs
     */
    public synchronized void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started) {
            if(log.isLoggable(Level.INFO))
                log.info(sm.getString("containerBase.notStarted", logName()));
            return;
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);
        
        // Send j2ee.state.stopping notification 
        if (this.getObjectName() != null) {
            Notification notification = 
                new Notification("j2ee.state.stopping", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        }
        
        // Mark this application as unavailable while we shut down
        setAvailable(false);

        // Binding thread
        ClassLoader oldCCL = bindThread();

        try{
            // Stop our filters
            filterStop();
            
            // Stop ContainerBackgroundProcessor thread
            super.threadStop();

            if ((manager != null) && (manager instanceof Lifecycle)) {
                ((Lifecycle) manager).stop();
            }
            
            // Finalize our character set mapper
            setCharsetMapper(null);

            // Normal container shutdown processing
            if (log.isLoggable(Level.FINE))
                log.fine("Processing standard container shutdown");
            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(STOP_EVENT, null);
            started = false;

            // Stop the Valves in our pipeline (including the basic), if any
            if (pipeline instanceof Lifecycle) {
                ((Lifecycle) pipeline).stop();
            }

            // Stop our child containers, if any
            Container[] children = findChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof Lifecycle)
                    ((Lifecycle) children[i]).stop();
            }
        
            // Clear all application-originated servlet context attributes
            if (context != null)
                context.clearAttributes();
            
            // Stop our application listeners
            listenerStop();

            // Stop resources
            resourcesStop();
            alternateResourcesStop();

            if ((realm != null) && (realm instanceof Lifecycle)) {
                ((Lifecycle) realm).stop();
            }
            if ((logger != null) && (logger instanceof Lifecycle)) {
                ((Lifecycle) logger).stop();
            }
            /* SJSAS 6347606 
            if ((loader != null) && (loader instanceof Lifecycle)) {
                ((Lifecycle) loader).stop();
            }
            */
        } finally {

            // Unbinding thread
            unbindThread(oldCCL);

            // START SJSAS 6347606 
            /*
             * Delay the stopping of the webapp classloader until this point,
             * because unbindThread() calls the security-checked
             * Thread.setContextClassLoader(), which may ask the current thread
             * context classloader (i.e., the webapp classloader) to load
             * Principal classes specified in the security policy file
             */
            if ((loader != null) && (loader instanceof Lifecycle)) {
                ((Lifecycle) loader).stop();
            }
            // END SJSAS 6347606 
        }

        // Send j2ee.state.stopped notification 
        if (this.getObjectName() != null) {
            Notification notification = 
                new Notification("j2ee.state.stopped", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        }
        
        // Reset application context
        context = null;

        // This object will no longer be visible or used. 
        try {
            resetContext();
        } catch( Exception ex ) {
            log.log(Level.SEVERE, sm.getString("standardContext.reset", this),
                    ex);
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);

        if (log.isLoggable(Level.FINE))
            log.fine("Stopping complete");

    }

    /** Destroy needs to clean up the context completely.
     * 
     * The problem is that undoing all the config in start() and restoring 
     * a 'fresh' state is impossible. After stop()/destroy()/init()/start()
     * we should have the same state as if a fresh start was done - i.e
     * read modified web.xml, etc. This can only be done by completely 
     * removing the context object and remapping a new one, or by cleaning
     * up everything.
     * 
     * XXX  Should this be done in stop() ?
     * 
     */ 
    public void destroy() throws Exception {
        if( oname != null ) { 
            // Send j2ee.object.deleted notification 
            Notification notification = 
                new Notification("j2ee.object.deleted", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        } 
        super.destroy();
        
        // START SJASAS 6359401
        // super.destroy() will stop session manager and cause it to unload
        // all its active sessions into a file. Delete this file, because this
        // context is being destroyed and must not leave any traces.
        if (getManager() instanceof ManagerBase) {
            ((ManagerBase)getManager()).release();
        }
        // END SJSAS 6359401

        instanceListeners = new String[0];
        instanceListenerInstances.clear();
    }
    
    private void resetContext() throws Exception, MBeanRegistrationException {
        // Restore the original state ( pre reading web.xml in start )
        // If you extend this - override this method and make sure to clean up
        children=new HashMap();
        startupTime = 0;
        startTimeMillis = 0;
        tldScanTime = 0;

        // Bugzilla 32867
        distributable = false;

        applicationListeners = new String[0];
        applicationEventListenersObjects = new Object[0];
        applicationLifecycleListenersObjects = new Object[0];

        if (log.isLoggable(Level.FINE)) {
            log.fine("resetContext " + oname + " " + mserver);
        }
    }

    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        if (getParent() != null) {
            sb.append(getParent().toString());
            sb.append(".");
        }
        sb.append("StandardContext[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess() {

        if (!started)
            return;

        count = (count + 1) % managerChecksFrequency;

        if ((getManager() != null) && (count == 0)) {
            if (getManager() instanceof StandardManager) {
                ((StandardManager) getManager()).processExpires();
            } else if (getManager() instanceof PersistentManagerBase) {
                PersistentManagerBase pManager = 
                    (PersistentManagerBase) getManager();
                pManager.backgroundProcess();
            }
        }

        // START S1AS8PE 4965017
        if (isReload()) {
            if (getLoader() != null) {
                if (reloadable && (getLoader().modified())) {
                    try {
                        Thread.currentThread().setContextClassLoader
                            (standardContextClassLoader);
                        reload();
                    } finally {
                        if (getLoader() != null) {
                            Thread.currentThread().setContextClassLoader
                                (getLoader().getClassLoader());
                        }
                    }
                }
                if (getLoader() instanceof WebappLoader) {
                    ((WebappLoader) getLoader()).closeJARs(false);
                }
            }
        }
        // END S1AS8PE 4965017
    }


    // ------------------------------------------------------ Protected Methods

    // START SJSAS 8.1 5049111
    /**
     * Check if we need to scan the classloader parent for Tld listeners. 
     * Tlds will be scanned for listeners only if the web app uses 
     * Java Server Faces.
     */
    private boolean isJsfServletDefined(){
        Container[] wrappers = (Container[])findChildren();     
        String servletName;
        for(int i=0; i < wrappers.length; i++){
            servletName = ((Wrapper)wrappers[i]).getServletClass();
            if (servletName == null){
                continue;
            } 
            // REVISIT Should we make the list configurable?
            if ( servletName.equals("javax.faces.webapp.FacesServlet") ){
                return true;
            }
        }
        
        return false;
        
    }
    // END SJSAS 8.1 5049111
    
    

    /**
     * Adjust the URL pattern to begin with a leading slash, if appropriate
     * (i.e. we are running a servlet 2.2 application).  Otherwise, return
     * the specified URL pattern unchanged.
     *
     * @param urlPattern The URL pattern to be adjusted (if needed)
     *  and returned
     */
    protected String adjustURLPattern(String urlPattern) {

        if (urlPattern == null)
            return (urlPattern);
        if (urlPattern.startsWith("/") || urlPattern.startsWith("*."))
            return (urlPattern);
        if (!isServlet22())
            return (urlPattern);
        if (log.isLoggable(Level.FINE)) {
            log.fine(sm.getString("standardContext.urlPattern.patternWarning",
                                  urlPattern));
        }
        return ("/" + urlPattern);

    }


    /**
     * Are we processing a version 2.2 deployment descriptor?
     */
    protected boolean isServlet22() {

        if (this.publicId == null)
            return (false);
        if (this.publicId.equals
            (org.apache.catalina.startup.Constants.WebDtdPublicId_22))
            return (true);
        else
            return (false);

    }


    /**
     * Return a File object representing the base directory for the
     * entire servlet container (i.e. the Engine container if present).
     */
    protected File engineBase() {
        String base=System.getProperty("catalina.base");
        if( base == null ) {
            StandardEngine eng=(StandardEngine)this.getParent().getParent();
            base=eng.getBaseDir();
        }
        return (new File(base));
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Bind current thread, both for CL purposes and for JNDI ENC support
     * during : startup, shutdown and realoading of the context.
     *
     * @return the previous context class loader
     */
    private ClassLoader bindThread() {

        ClassLoader oldContextClassLoader =
            Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader
            (getLoader().getClassLoader());

        if (isUseNaming()) {
            try {
                ContextBindings.bindThread(this, this);
            } catch (Throwable e) {
                e.printStackTrace();
                // Silent catch, as this is a normal case during the early
                // startup stages
            }
        }

        return oldContextClassLoader;

    }


    /**
     * Unbind thread.
     */
    private void unbindThread(ClassLoader oldContextClassLoader) {

        Thread.currentThread().setContextClassLoader(oldContextClassLoader);

        if (isUseNaming()) {
            ContextBindings.unbindThread(this, this);
        }
    }



    /**
     * Get base path.
     */
    private String getBasePath(String docBase) {
        String basePath = null;
        Container container = this;
        while (container != null) {
            if (container instanceof Host)
                break;
            container = container.getParent();
        }
        File file = new File(docBase);
        if (!file.isAbsolute()) {
            if (container == null) {
                basePath = (new File(engineBase(), docBase)).getPath();
            } else {
                // Use the "appBase" property of this container
                String appBase = ((Host) container).getAppBase();
                file = new File(appBase);
                if (!file.isAbsolute())
                    file = new File(engineBase(), appBase);
                basePath = (new File(file, docBase)).getPath();
            }
        } else {
            basePath = file.getPath();
        }
        return basePath;
    }


    /**
     * Get app base.
     */
    private String getAppBase() {
        String appBase = null;
        Container container = this;
        while (container != null) {
            if (container instanceof Host)
                break;
            container = container.getParent();
        }
        if (container != null) {
            appBase = ((Host) container).getAppBase();
        }
        return appBase;
    }


    /**
     * Get config base.
     */
    private File getConfigBase() {
        File configBase = 
            new File(System.getProperty("catalina.base"), "conf");
        if (!configBase.exists()) {
            return null;
        }
        Container container = this;
        Container host = null;
        Container engine = null;
        while (container != null) {
            if (container instanceof Host)
                host = container;
            if (container instanceof Engine)
                engine = container;
            container = container.getParent();
        }
        if (engine != null) {
            configBase = new File(configBase, engine.getName());
        }
        if (host != null) {
            configBase = new File(configBase, host.getName());
        }
        configBase.mkdirs();
        return configBase;
    }


    /**
     * Given a context path, get the config file name.
     */
    protected String getDefaultConfigFile() {
        String basename = null;
        String path = getPath();
        if (path.equals("")) {
            basename = "ROOT";
        } else {
            basename = path.substring(1).replace('/', '_');
        }
        return (basename + ".xml");
    }


    /**
     * Copy a file.
     */
    private boolean copy(File src, File dest) {
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);
            byte[] buf = new byte[4096];
            while (true) {
                int len = is.read(buf);
                if (len < 0)
                    break;
                os.write(buf, 0, len);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                // Ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return true;
    }


    /**
     * Get naming context full name.
     */
    // START RIMOD 6195820
    /*
    private String getNamingContextName() {
    */
    public String getNamingContextName() {
    // END RIMOD 6195820
        if (namingContextName == null) {
            Container parent = getParent();
            if (parent == null) {
                namingContextName = getName();
            } else {
                Stack stk = new Stack();
                StringBuffer buff = new StringBuffer();
                while (parent != null) {
                    stk.push(parent.getName());
                    parent = parent.getParent();
                }
                while (!stk.empty()) {
                    buff.append("/" + stk.pop());
                }
                buff.append(getName());
                namingContextName = buff.toString();
            }
            // START RIMOD 4868393
            // append an id to make the name unique to the instance.
            namingContextName += instanceIDCounter++;
            // END RIMOD 4868393
        }
        return namingContextName;
    }


    /**
     * Return the request processing paused flag for this Context.
     */
    public boolean getPaused() {

        return (this.paused);

    }


    /**
     * Post a copy of our web application resources as a servlet context
     * attribute.
     */
    private void postResources() {

        getServletContext().setAttribute
            (Globals.RESOURCES_ATTR, getResources());
        context.setAttributeReadOnly(Globals.RESOURCES_ATTR);

    }


    /**
     * Post a copy of our current list of welcome files as a servlet context
     * attribute, so that the default servlet can find them.
     */
    private void postWelcomeFiles() {

        getServletContext().setAttribute("org.apache.catalina.WELCOME_FILES",
                                         welcomeFiles);
        context.setAttributeReadOnly("org.apache.catalina.WELCOME_FILES");

    }

    public String getHostname() {
        Container parentHost = getParent();
        if (parentHost != null) {
            hostName = parentHost.getName();
        }
        if ((hostName == null) || (hostName.length() < 1))
            hostName = "_";
        return hostName;
    }

    /**
     * Set the appropriate context attribute for our work directory.
     */
    private void postWorkDirectory() {

        // Acquire (or calculate) the work directory path
        String workDir = getWorkDir();
        if (workDir == null || workDir.length() == 0) {

            // Retrieve our parent (normally a host) name
            String hostName = null;
            String engineName = null;
            String hostWorkDir = null;
            Container parentHost = getParent();
            if (parentHost != null) {
                hostName = parentHost.getName();
                if (parentHost instanceof StandardHost) {
                    hostWorkDir = ((StandardHost)parentHost).getWorkDir();
                }
                Container parentEngine = parentHost.getParent();
                if (parentEngine != null) {
                   engineName = parentEngine.getName();
                }
            }
            if ((hostName == null) || (hostName.length() < 1))
                hostName = "_";
            if ((engineName == null) || (engineName.length() < 1))
                engineName = "_";

            String temp = getPath();
            if (temp.startsWith("/"))
                temp = temp.substring(1);
            temp = temp.replace('/', '_');
            temp = temp.replace('\\', '_');
            if (temp.length() < 1)
                temp = "_";
            if (hostWorkDir != null ) {
                workDir = hostWorkDir + File.separator + temp;
            } else {
                workDir = "work" + File.separator + engineName +
                    File.separator + hostName + File.separator + temp;
            }
            setWorkDir(workDir);
        }

        // Create this directory if necessary
        File dir = new File(workDir);
        if (!dir.isAbsolute()) {
            File catalinaHome = engineBase();
            String catalinaHomePath = null;
            try {
                catalinaHomePath = catalinaHome.getCanonicalPath();
                dir = new File(catalinaHomePath, workDir);
            } catch (IOException e) {
            }
        }
        dir.mkdirs();

        // Set the appropriate servlet context attribute
        getServletContext().setAttribute(Globals.WORK_DIR_ATTR, dir);
        context.setAttributeReadOnly(Globals.WORK_DIR_ATTR);

    }


    /**
     * Set the request processing paused flag for this Context.
     *
     * @param paused The new request processing paused flag
     */
    private void setPaused(boolean paused) {

        this.paused = paused;

    }


    /**
     * Validate the syntax of a proposed <code>&lt;url-pattern&gt;</code>
     * for conformance with specification requirements.
     *
     * @param urlPattern URL pattern to be validated
     */
    private boolean validateURLPattern(String urlPattern) {

        if (urlPattern == null)
            return (false);
        if (urlPattern.indexOf('\n') >= 0 || urlPattern.indexOf('\r') >= 0) {
            log.warning(sm.getString("standardContext.crlfinurl", urlPattern));
            return false;
        }
        if (urlPattern.startsWith("*.")) {
            if (urlPattern.indexOf('/') < 0) {
                checkUnusualURLPattern(urlPattern);
                return (true);
            } else
                return (false);
        }
        if ( (urlPattern.startsWith("/")) &&
                (urlPattern.indexOf("*.") < 0)) {
            checkUnusualURLPattern(urlPattern);
            return (true);
        } else
            return (false);

    }


    /**
     * Check for unusual but valid <code>&lt;url-pattern&gt;</code>s.
     * See Bugzilla 34805, 43079 &amp; 43080
     */
    private void checkUnusualURLPattern(String urlPattern) {
        if (log.isLoggable(Level.INFO)) {
            if(urlPattern.endsWith("*") && (urlPattern.length() < 2 ||
                    urlPattern.charAt(urlPattern.length()-2) != '/')) {
                log.info("Suspicious url pattern: \"" + urlPattern + "\"" +
                        " in context [" + getName() + "] - see" +
                        " section SRV.11.2 of the Servlet specification" );
            }
        }
    }


    // -------------------- JMX methods  --------------------

    /**
     * Return the MBean Names of the set of defined environment entries for
     * this web application
     */
    public String[] getEnvironments() {
        ContextEnvironment[] envs = getNamingResources().findEnvironments();
        ArrayList results = new ArrayList();
        for (int i = 0; i < envs.length; i++) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName(this.getEngineName(), envs[i]);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                IllegalArgumentException iae = new IllegalArgumentException
                    ("Cannot create object name for environment " + envs[i]);
                iae.initCause(e);
                throw iae;
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    /**
     * Return the MBean Names of all the defined resource references for this
     * application.
     */
    public String[] getResourceNames() {

        ContextResource[] resources = getNamingResources().findResources();
        ArrayList results = new ArrayList();
        for (int i = 0; i < resources.length; i++) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName(this.getEngineName(), resources[i]);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                IllegalArgumentException iae = new IllegalArgumentException
                    ("Cannot create object name for resource " + resources[i]);
                iae.initCause(e);
                throw iae;
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    /**
     * Return the MBean Names of all the defined resource links for this
     * application
     */
    public String[] getResourceLinks() {

        ContextResourceLink[] links = getNamingResources().findResourceLinks();
        ArrayList results = new ArrayList();
        for (int i = 0; i < links.length; i++) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName(this.getEngineName(), links[i]);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                IllegalArgumentException iae = new IllegalArgumentException
                    ("Cannot create object name for resource " + links[i]);
                iae.initCause(e);
                throw iae;
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }

    // ------------------------------------------------------------- Operations


    /**
     * Add an environment entry for this web application.
     *
     * @param envName New environment entry name
     */
    public String addEnvironment(String envName, String type)
        throws MalformedObjectNameException {

        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }
        ContextEnvironment env = nresources.findEnvironment(envName);
        if (env != null) {
            throw new IllegalArgumentException
                ("Invalid environment name - already exists '" + envName + "'");
        }
        env = new ContextEnvironment();
        env.setName(envName);
        env.setType(type);
        nresources.addEnvironment(env);

        // Return the corresponding MBean name
        ManagedBean managed = Registry.getRegistry(null, null).findManagedBean("ContextEnvironment");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), env);
        return (oname.toString());

    }


    /**
     * Add a resource reference for this web application.
     *
     * @param resourceName New resource reference name
     */
    public String addResource(String resourceName, String type)
        throws MalformedObjectNameException {

        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }
        ContextResource resource = nresources.findResource(resourceName);
        if (resource != null) {
            throw new IllegalArgumentException
                ("Invalid resource name - already exists'" + resourceName + "'");
        }
        resource = new ContextResource();
        resource.setName(resourceName);
        resource.setType(type);
        nresources.addResource(resource);

        // Return the corresponding MBean name
        ManagedBean managed = Registry.getRegistry(null, null).findManagedBean("ContextResource");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), resource);
        return (oname.toString());
    }


    /**
     * Add a resource link for this web application.
     *
     * @param resourceLinkName New resource link name
     */
    public String addResourceLink(String resourceLinkName, String global,
                String name, String type) throws MalformedObjectNameException {

        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }
        ContextResourceLink resourceLink =
                                nresources.findResourceLink(resourceLinkName);
        if (resourceLink != null) {
            throw new IllegalArgumentException
                ("Invalid resource link name - already exists'" +
                                                        resourceLinkName + "'");
        }
        resourceLink = new ContextResourceLink();
        resourceLink.setGlobal(global);
        resourceLink.setName(resourceLinkName);
        resourceLink.setType(type);
        nresources.addResourceLink(resourceLink);

        // Return the corresponding MBean name
        ManagedBean managed = Registry.getRegistry(null, null).findManagedBean("ContextResourceLink");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), resourceLink);
        return (oname.toString());
    }
    
    
    /** JSR77 deploymentDescriptor attribute
     *
     * @return string deployment descriptor 
     */
    public String getDeploymentDescriptor() {
    
        InputStream stream = null;
        ServletContext servletContext = getServletContext();
        if (servletContext != null) {
            stream = servletContext.getResourceAsStream(
                org.apache.catalina.startup.Constants.ApplicationWebXml);
        }
        if (stream == null) {
            return "";
        }
        BufferedReader br = new BufferedReader(
                                new InputStreamReader(stream));
        StringBuffer sb = new StringBuffer();
        String strRead = "";
        try {
            while (strRead != null) {
                sb.append(strRead);
                strRead = br.readLine();
            }
        } catch (IOException e) {
            return "";
        }

        return sb.toString(); 
    
    }
    
    
    /** JSR77 servlets attribute
     *
     * @return list of all servlets ( we know about )
     */
    public String[] getServlets() {

        String[] result = null;

        Container[] children = findChildren();
        if (children != null) {
            result = new String[children.length];
            for( int i=0; i< children.length; i++ ) {
                result[i] = ((StandardWrapper)children[i]).getObjectName();
            }
        }

        return result;
    }
    

    public ObjectName createObjectName(String hostDomain, ObjectName parentName)
            throws MalformedObjectNameException
    {
        String onameStr;
        StandardHost hst=(StandardHost)getParent();
        
        String hostName=getParent().getName();
        String name= "//" + ((hostName==null)? "DEFAULT" : hostName) +
                (("".equals(encodedPath)) ? "/" : encodedPath);

        String suffix=",J2EEApplication=" +
                getJ2EEApplication() + ",J2EEServer=" +
                getJ2EEServer();

        onameStr="j2eeType=WebModule,name=" + name + suffix;
        if( log.isLoggable(Level.FINE))
            log.fine("Registering " + onameStr + " for " + oname);
        
        // default case - no domain explictely set.
        if( getDomain() == null ) domain=hst.getDomain();

        ObjectName oname=new ObjectName(getDomain() + ":" + onameStr);
        return oname;        
    }    
    
    private void preRegisterJMX() {
        try {
            StandardHost host = (StandardHost) getParent();
            if ((oname == null) 
                || (oname.getKeyProperty("j2eeType") == null)) {
                oname = createObjectName(host.getDomain(), host.getJmxName());
                controller = oname;
            }
        } catch(Exception ex) {
            log.log(Level.INFO,
                    "Error registering ctx with jmx " + this + " " +
                    oname + " " + ex.toString(),
                    ex );
        }
    }

    private void registerJMX() {
        try {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Checking for " + oname );
            }
            if(! Registry.getRegistry(null, null).getMBeanServer().isRegistered(oname)) {
                controller = oname;
                Registry.getRegistry(null, null).registerComponent(this, oname, null);

                // Send j2ee.object.created notification 
                if (this.getObjectName() != null) {
                    Notification notification = new Notification(
                                                        "j2ee.object.created", 
                                                        this.getObjectName(), 
                                                        sequenceNumber++);
                    broadcaster.sendNotification(notification);
                }
            }
            Container children[] = findChildren();
            for (int i=0; children!=null && i<children.length; i++) {
                ((StandardWrapper)children[i]).registerJMX( this );
            }
        } catch (Exception ex) {
            log.log(Level.INFO,
                    "Error registering wrapper with jmx " + this + " " +
                    oname + " " + ex.toString(),
                    ex );
        }
    }

    /** There are 2 cases:
     *   1.The context is created and registered by internal APIS
     *   2. The context is created by JMX, and it'll self-register.
     *
     * @param server
     * @param name
     * @return
     * @throws Exception
     */

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name)
            throws Exception
    {
        if( oname != null ) {
            //log.info( "Already registered " + oname + " " + name);
            // Temporary - /admin uses the old names
            return name;
        }
        ObjectName result=super.preRegister(server,name);
        return name;
    }

    public void preDeregister() throws Exception {
        if( started ) {
            try {
                stop();
            } catch( Exception ex ) {
                log.log(Level.SEVERE,
                        sm.getString("standardContext.stoppingContext", this),
                        ex);
            }
        }
    }

    public void init() throws Exception {

        if( this.getParent() == null ) {
            ObjectName parentName=getParentName();
            
            if( ! mserver.isRegistered(parentName)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("No host, creating one " + parentName);
                }
                StandardHost host=new StandardHost();
                host.setName(hostName);
                Registry.getRegistry(null, null).registerComponent(host, parentName, null);
                mserver.invoke(parentName, "init", new Object[] {}, new String[] {} );
            }
            ContextConfig config = new ContextConfig();
            this.addLifecycleListener(config);
      
            if (log.isLoggable(Level.FINE)) {
                log.fine( "AddChild " + parentName + " " + this);
            }
            try {
                mserver.invoke(parentName, "addChild", new Object[] { this },
                               new String[] {"org.apache.catalina.Container"});
            } catch (Exception e) {
                destroy();
                throw e;
            }
        }
        
        // It's possible that addChild may have started us
        if( initialized ) {
            return;
        }
        
        super.init();

        // START GlassFish 2439
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(INIT_EVENT, null);
        // END GlassFish 2439
        
        // Send j2ee.state.starting notification 
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.starting", 
                                                        this.getObjectName(), 
                                                        sequenceNumber++);
            broadcaster.sendNotification(notification);
        }
        
    }

    public ObjectName getParentName() throws MalformedObjectNameException {
        // "Life" update
        String path=oname.getKeyProperty("name");
        if( path == null ) {
            log.severe(sm.getString(
                "standardContext.missingNameAttributeInName", getName()));
            return null;
        }
        if( ! path.startsWith( "//")) {
            log.severe(sm.getString("standardContext.malformedName", getName()));
        }
        path=path.substring(2);
        int delim=path.indexOf( "/" );
        hostName="localhost"; // Should be default...
        if( delim > 0 ) {
            hostName=path.substring(0, delim);
            path = path.substring(delim);
            if (path.equals("/")) {
                this.setName("");
            } else {
                this.setName(path);
            }
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Setting path " +  path );
            }
            this.setName( path );
        }
        // XXX  The service and domain should be the same.
        String parentDomain=getEngineName();
        if( parentDomain == null ) parentDomain=domain;
        ObjectName parentName=new ObjectName( parentDomain + ":" +
                "type=Host,host=" + hostName);
        return parentName;
    }
    
    public void create() throws Exception{
        init();
    }

    // ------------------------------------------------------------- Attributes


    /**
     * Return the naming resources associated with this web application.
     */
    public javax.naming.directory.DirContext getStaticResources() {

        return getResources();

    }


    /**
     * Return the naming resources associated with this web application.
     * FIXME: Fooling introspection ... 
     */
    public javax.naming.directory.DirContext findStaticResources() {

        return getResources();

    }


    /**
     * Return the naming resources associated with this web application.
     */
    public String[] getWelcomeFiles() {

        return findWelcomeFiles();

    }



    /** Support for "stateManageable" JSR77 
     * 
     */
    public boolean isStateManageable() {
        return true;
    }
    
        /**
     * Set the validation feature of the XML parser used when
     * parsing xml instances.
     * @param xmlValidation true to enable xml instance validation
     */
    public void setXmlValidation(boolean webXmlValidation){
        
        this.webXmlValidation = webXmlValidation;

    }

    /**
     * Get the server.xml <context> attribute's xmlValidation.
     * @return true if validation is enabled.
     *
     */
    public boolean getXmlValidation(){
        return webXmlValidation;
    }


    /**
     * Get the server.xml <context> attribute's xmlNamespaceAware.
     * @return true if namespace awarenes is enabled.
     */
    public boolean getXmlNamespaceAware(){
        return webXmlNamespaceAware;
    }


    /**
     * Set the namespace aware feature of the XML parser used when
     * parsing xml instances.
     * @param xmlNamespaceAware true to enable namespace awareness
     */
    public void setXmlNamespaceAware(boolean webXmlNamespaceAware){
        this.webXmlNamespaceAware= webXmlNamespaceAware;
    }    


    /**
     * Set the validation feature of the XML parser used when
     * parsing tlds files. 
     * @param tldXmlValidation true to enable xml instance validation
     */
    public void setTldValidation(boolean tldValidation){
        
        this.tldValidation = tldValidation;

    }

    /**
     * Get the server.xml <context> attribute's webXmlValidation.
     * @return true if validation is enabled.
     *
     */
    public boolean getTldValidation(){
        return tldValidation;
    }


    /**
     * Get the server.xml <host> attribute's xmlNamespaceAware.
     * @return true if namespace awarenes is enabled.
     */
    public boolean getTldNamespaceAware(){
        return tldNamespaceAware;
    }


    /**
     * Set the namespace aware feature of the XML parser used when
     * parsing xml instances.
     * @param xmlNamespaceAware true to enable namespace awareness
     */
    public void setTldNamespaceAware(boolean tldNamespaceAware){
        this.tldNamespaceAware= tldNamespaceAware;
    }    

    
    public void startRecursive() throws LifecycleException {
        // nothing to start recursive, the servlets will be started by load-on-startup
        start();
    }
    
    public int getState() {
        if( started ) {
            return 1; // RUNNING
        }
        if( initialized ) {
            return 0; // starting ? 
        }
        if( ! available ) { 
            return 4; //FAILED
        }
        // 2 - STOPPING
        return 3; // STOPPED
    }
    
    /**
     * The J2EE Server ObjectName this module is deployed on.
     */     
    private String server = null;
    
    /**
     * The Java virtual machines on which this module is running.
     */       
    private String[] javaVMs = null;
    
    public String getServer() {
        return server;
    }
        
    public String setServer(String server) {
        return this.server=server;
    }
        
    public String[] getJavaVMs() {
        return javaVMs;
    }
        
    public String[] setJavaVMs(String[] javaVMs) {
        return this.javaVMs = javaVMs;
    }
    

    /**
     * Creates an ObjectInputStream that provides special deserialization
     * logic for classes that are normally not serializable (such as
     * javax.naming.Context).
     */
    public ObjectInputStream createObjectInputStream(InputStream is)
            throws IOException {

        ObjectInputStream ois = null;

        Loader loader = getLoader();
        if (loader != null) {
            ClassLoader classLoader = loader.getClassLoader();
            if (classLoader != null) {
                try {
                    ois = new CustomObjectInputStream(is, classLoader);
                } catch (IOException ioe) {
                    log.log(Level.SEVERE,
                            "Unable to create custom ObjectInputStream",
                            ioe);
                }
            }
        }

        if (ois == null) {
            ois = new ObjectInputStream(is);
        }

        return ois;
    }


    /**
     * Creates an ObjectOutputStream that provides special serialization
     * logic for classes that are normally not serializable (such as
     * javax.naming.Context).
     */
    public ObjectOutputStream createObjectOutputStream(OutputStream os)
            throws IOException {

        return new ObjectOutputStream(os); 
    }


    /**
     * Gets the time this context was started.
     *
     * @return Time (in milliseconds since January 1, 1970, 00:00:00) when this
     * context was started 
     */
    public long getStartTimeMillis() {
        return startTimeMillis;
    }
    
    public boolean isEventProvider() {
        return false;
    }
    
    public boolean isStatisticsProvider() {
        return false;
    }


    // HTTP session related monitoring events

    public void sessionCreatedEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionDestroyedEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionRejectedEvent(int maxSessions) {
        // Deliberate noop
    }

    public void sessionExpiredEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPersistedStartEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPersistedEndEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionActivatedStartEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionActivatedEndEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPassivatedStartEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPassivatedEndEvent(HttpSession session) {
        // Deliberate noop
    }

}
