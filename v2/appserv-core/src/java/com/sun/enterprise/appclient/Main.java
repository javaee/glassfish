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
package com.sun.enterprise.appclient;

import com.sun.appserv.naming.S1ASCtxFactory;
import com.sun.enterprise.appclient.AppContainer;
import com.sun.enterprise.appclient.HttpAuthenticator;
import com.sun.enterprise.appclient.jws.TemplateCache;
import com.sun.enterprise.appclient.jws.Util;
import com.sun.enterprise.config.clientbeans.CertDb;
import com.sun.enterprise.config.clientbeans.ClientBeansFactory;
import com.sun.enterprise.config.clientbeans.ClientContainer;
import com.sun.enterprise.config.clientbeans.ClientCredential;
import com.sun.enterprise.config.clientbeans.ElementProperty;
import com.sun.enterprise.config.clientbeans.Security;
import com.sun.enterprise.config.clientbeans.Ssl;
import com.sun.enterprise.config.clientbeans.TargetServer;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.connectors.ActiveResourceAdapter;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.backend.ClientJarMakerThread;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.InjectionManager;
import com.sun.enterprise.J2EESecurityManager;
import com.sun.enterprise.naming.ProviderManager;
import com.sun.enterprise.security.GUIErrorDialog;
import com.sun.enterprise.security.SSLUtils;
import com.sun.enterprise.server.logging.ACCLogManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.JarClassLoader;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.util.shared.ArchivistUtils;
import com.sun.enterprise.util.Utility;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;
import com.sun.web.server.HttpsURLStreamHandlerFactory;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.Properties;
import java.util.Vector;

import javax.security.auth.message.config.AuthConfigFactory;
import com.sun.enterprise.security.jmac.config.GFAuthConfigFactory;

import com.sun.enterprise.security.UsernamePasswordStore;

/**
 * This is the main that gets invoked first. It initializes the application
 * client container for an application client component
 * and other related items and then invokes the real main written by the
 * application developer.
 */
public class Main
{
    private static final String CLIENT = "-client";
    private static final String NAME = "-name";
    private static final String MAIN_CLASS = "-mainclass";
    private static final String TEXT_AUTH = "-textauth";
    private static final String XML_PATH = "-xml";
    private static final String ACC_CONFIG_XML = "-configxml";    
    private static final String DEFAULT_CLIENT_CONTAINER_XML = "sun-acc.xml";
    // duplicated in com.sun.enterprise.jauth.ConfigXMLParser
    private static final String SUNACC_XML_URL = "sun-acc.xml.url";
    private static final String NO_APP_INVOKE = "-noappinvoke";
    //Added for allow user to pass user name and password through command line.
    private static final String USER = "-user";
    private static final String PASSWORD = "-password";
    private static final String PASSWORD_FILE = "-passwordfile";
    private static final String LOGIN_NAME = "j2eelogin.name";
    private static final String LOGIN_PASSWORD = "j2eelogin.password";
    private static final String DASH = "-";

    private static final String lineSep = System.getProperty("line.separator");
    
    /**
     * Property names used on the server to send these values to a Java Web Start client
     * and by the ACC when running under Java Web Start to retrieve them
     */
    public static final String APPCLIENT_IIOP_DEFAULTHOST_PROPERTYNAME = "com.sun.aas.jws.iiop.defaultHost";
    public static final String APPCLIENT_IIOP_DEFAULTPORT_PROPERTYNAME = "com.sun.aas.jws.iiop.defaultPort";
    public static final String APPCLIENT_IIOP_FAILOVER_ENDPOINTS_PROPERTYNAME = "com.sun.aas.jws.iiop.failover.endpoints";
    public static final String APPCLIENT_PROBE_CLASSNAME_PROPERTYNAME = "com.sun.aas.jws.probeClassName";
    
    /** Prop name for keeping temporary files */
    public static final String APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME = "com.sun.aas.jws.retainTempFiles";
    
    /** property name used to indicate that Java Web Start is active */
    public static final String APPCLIENT_ISJWS_PROPERTYNAME = "com.sun.aas.jws.isJWS";
    
    /** Prop used when running under Java Web Start to point to a temporarily-created default file.  
     *This property appears in the template for the default sun-acc.xml content.  Logic below
     *assigns a value to it and then uses it to substitute in the template to create the
     *actual content.  (This is not a property set in the environment and then retrieved by Main.)
    */
    public static final String SUN_ACC_SECURITY_CONFIG_PROPERTY = "security.config.file";
    
    /** Used for constructing the name of the temp file that will hold the login conf. content */
    private static final String LOGIN_CONF_FILE_PREFIX = "login";
    private static final String LOGIN_CONF_FILE_SUFFIX = ".conf";
    
    /** The system property to be set that is later read by jaas */
    private static final String LOGIN_CONF_PROPERTY_NAME = "java.security.auth.login.config";
    
    /** Names of templates for default config for Java Web Start */
    private static final String DEFAULT_TEMPLATE_PREFIX = "jws/templates/";
    private static final String SUN_ACC_DEFAULT_TEMPLATE = DEFAULT_TEMPLATE_PREFIX + "default-sun-accTemplate.xml";
    private static final String WSS_CLIENT_CONFIG_TEMPLATE = DEFAULT_TEMPLATE_PREFIX + "default-wss-client-configTemplate.xml";
    private static final String LOGIN_CONF_TEMPLATE = DEFAULT_TEMPLATE_PREFIX + "appclientlogin.conf"; 
    
    /** Naming for temporary files created under Java Web Start */
    private static final String WSS_CLIENT_CONFIG_PREFIX = "wsscc";
    private static final String WSS_CLIENT_CONFIG_SUFFIX = ".xml";
    private static final String SUN_ACC_PREFIX = "sunacc";
    private static final String SUN_ACC_SUFFIX = ".xml";
    
    private static Logger _logger;

    private static final boolean debug = false;
    private static StringManager localStrings = 
                            StringManager.getManager(Main.class);
    private static boolean guiAuth;
    private static boolean runClient=true;

    private static String host;

    private static String port;

    /** accumulates info to be logged before the logger is initialized */
    private static StringBuilder pendingLogInfo = new StringBuilder();
    private static StringBuilder pendingLogFine = new StringBuilder();
            
    /** Saved arguments so they are accessible from the AWT thread if needed */
    private static String [] args;

    /** Records whether ACC is currently running under Java Web Start */
    private static boolean isJWS;
    
    /** Records whether temp config files created while running under Java Web Start should be retained */
    private static boolean retainTempFiles = false;
    
    private static final String SUPPORT_MODULE_FORMAT = "support.module.format";
    private static final String SUPPORT_MODULE_FORMAT_DEFAULT_VALUE = "true";
    private static final String supportModuleFormatValue = System.getProperty(SUPPORT_MODULE_FORMAT, SUPPORT_MODULE_FORMAT_DEFAULT_VALUE);
    private static final boolean supportModuleFormat = Boolean.parseBoolean(supportModuleFormatValue);

    private static boolean lb_enabled = false;
    
    public static void main(String[] args) {
        if (supportModuleFormat) {
            new MainWithModuleSupport(args);
        } else {
            new Main(args);
        }
    }

    public Main(String[] args) {

        String arg = null;
        String clientJar = null;
        String displayName = null;
        String mainClass = null;
        String xmlPath = null;
        String accConfigXml = null;
        String jwsACCConfigXml = null;
        Vector<String> appArgs = new Vector<String>();
        int i = 0;

        isJWS = Boolean.getBoolean(APPCLIENT_ISJWS_PROPERTYNAME);
        retainTempFiles = Boolean.getBoolean(APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME);
        
        guiAuth = Boolean.valueOf
            (System.getProperty("auth.gui", "true")).booleanValue();
        // Parse command line arguments.
        if(args.length < 1) {
            usage();
        } else {
            while(i < args.length) {
                arg = args[i++];
                if(arg.equals(CLIENT)) {
                    if(i < args.length && !args[i].startsWith(DASH)) {
                        clientJar = args[i++];
                    } else {
                        usage();
                    }
                } else if(arg.equals(NAME) && !args[i].startsWith(DASH)) {
                    //only one option can be used [-mainclass|-name]
                    if(i < args.length && mainClass == null) {
                        displayName = args[i++];
                    } else {
                        usage();
                    }
                } else if(arg.equals(MAIN_CLASS) && !args[i].startsWith(DASH)) {
                    //only one option can be used [-mainclass|-name]
                    if(i < args.length && displayName == null) {
                        mainClass = args[i++];
                    } else {
                        usage();
                    }
                } else if(arg.equals(XML_PATH) ) {
                    if(i < args.length && xmlPath == null) {
                        xmlPath = args[i++];
                    } else {
                        usage();
                    }
                } else if(arg.equals(ACC_CONFIG_XML) ) {
                    if(i < args.length && accConfigXml == null) {
                        accConfigXml = args[i++];
                    } else {
                        usage();
                    }
                } else if(arg.equals(TEXT_AUTH)) {
                    // Overrides legacy auth.gui setting.
                    guiAuth = false;
                } else if(arg.equals(NO_APP_INVOKE)) {
                    runClient = false;
                } else if(arg.equals(USER)) {
                    if(i < args.length) {
                        System.setProperty(LOGIN_NAME, args[i++]);
                    } else {
                        usage();
                    }
                } else if(arg.equals(PASSWORD)) {
                    if (i < args.length) {
                        System.setProperty(LOGIN_PASSWORD, args[i++]);
                    } else {
                        usage();
                    }
                } else if (arg.equals(PASSWORD_FILE)) {
                    if (i < args.length) {
                        try {
                            System.setProperty(LOGIN_PASSWORD,
                                loadPasswordFromFile(args[i++]));
                        } catch(IOException ex) {
                            throw new IllegalArgumentException(ex.getMessage());
                        }
                    } else {
                        usage();
                    }
                } else {
                    appArgs.add(arg);
                }
            }
        }
        
        String uname = System.getProperty(LOGIN_NAME);
        String upass = System.getProperty(LOGIN_PASSWORD);
        if( uname != null || upass != null ) {
            UsernamePasswordStore.set(uname, upass);
        }

        String className=null; 
        
        if(clientJar == null && ! isJWS) {
            // maybe the user has specified the appclient class as the first 
            // parameter using the local directory as the classpath.
            if (appArgs.size()==0 && mainClass==null) {
                usage();
            }             
            // ok, if the first parameter may be the appclient class, let's check 
            // for its existence.
            String value;
            if (mainClass==null) {
                value = appArgs.elementAt(0);
            } else {
                value = mainClass;
            }
            if (value.endsWith(".class")) {
                className = value.substring(0, value.length()-".class".length());
            } else {
                className = value;                
            }
            
            String path = className.replace('.', File.separatorChar) + ".class";
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(System.getProperty("user.dir"),  path);
            }
            /*
             *If the user omitted the client jar from the command line and 
             *we cannot find the first argument as a class in the user's 
             *home directory and this is not a JWS launch, then the user has
             *not entered a valid command.
             */
            if (!file.exists() && ! isJWS) {
                // no clue what the user is trying to do
                usage();
            }
        }
        
        /* validate xmlPath */
        if (xmlPath != null) {
            validateXMLFile(xmlPath);
        } else if (accConfigXml != null ) {
            validateXMLFile(accConfigXml);
            xmlPath = accConfigXml; //use AS_ACC_CONFIG
        } else if (isJWS) {
            /*
             *Neither -xml nor -configxml were present and this is a Java 
             *Web Start invocation.  Use
             *the alternate mechanism to create the default config.
             */
            try {
                jwsACCConfigXml = prepareJWSConfig();
                if (jwsACCConfigXml != null) {
                    validateXMLFile(jwsACCConfigXml);
                    xmlPath = jwsACCConfigXml;
                }
            } catch (Throwable thr) {
                System.err.println("Error preparing configuration");
                thr.printStackTrace(System.err);
                System.exit(1);
            }
        }


        // make sure the default logger for ACCLogManager is set
        _logger = LogDomains.getLogger(LogDomains.ACC_LOGGER);

        LogManager logMgr = LogManager.getLogManager();
        if (logMgr instanceof ACCLogManager) {
            ((ACCLogManager) logMgr).init(xmlPath);
        }

        /*
         *Flush any pending log output.
         */
        if (pendingLogInfo.length() > 0) {
            _logger.info(pendingLogInfo.toString());
            
            if (pendingLogFine.length() > 0) {
                _logger.fine(pendingLogFine.toString());
            }
        }
        
        /*
         *If this is a Java Web Start invocation, prepare the user-specified
         *or default login configuration.
         */
        if (isJWS) {
            try {
                prepareJWSLoginConfig();
            } catch (Throwable thr) {
                _logger.log(Level.SEVERE, "Error preparing default login configuration", thr);
                System.exit(1);
            }
        }
        
        
        Utility.checkJVMVersion();
        
        /* security init */
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null &&
                !(J2EESecurityManager.class.equals(secMgr.getClass()))) {
            J2EESecurityManager mgr = new J2EESecurityManager();
            System.setSecurityManager(mgr);
        }
        if (_logger.isLoggable(Level.INFO)) {
            if (secMgr != null) {
                _logger.info("acc.secmgron");
            } else {
                _logger.info("acc.secmgroff");
            }
        }

        try{
            /* setup keystores.
             * This is required, for appclients that want to use SSL, especially
             * HttpsURLConnection to perform Https.
             */
            SSLUtils.initStoresAtStartup();
        } catch (Exception e){
             /* This is not necessarily an error. This will arise
              * if the user has not specified keystore/truststore properties.
              * and does not want to use SSL.
              */
            if(_logger.isLoggable(Level.FINER)){
                // show the exact stack trace
                _logger.log(Level.FINER, "main.ssl_keystore_init_failed", e);
            } else{
                // just log it as a warning.
                _logger.log(Level.WARNING, "main.ssl_keystore_init_failed");
            }
        }

	try {
	    /* setup jsr 196 factory
	     * define default factory if it is not already defined
	     */
	    String defaultFactory = java.security.Security.getProperty
		(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY);
	    if (defaultFactory == null) {
		java.security.Security.setProperty
		    (AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY,
		     GFAuthConfigFactory.class.getName());
	    }

	} catch (Exception e) {
	    //  XXX put string in catablog
	    _logger.log(Level.WARNING, "main.jmac_default_factory");
	}

	try {
	    Switch.getSwitch().setProviderManager(ProviderManager.getProviderManager());
	    // added for ClientContainer.xml initialization
	    setTargetServerProperties(xmlPath);
	    
	    int exitCode = 0; // 0 for success
	    AppContainer container = null;
	    
	    // Ensure cleanup is performed, even if
	    // application client calls System.exit().
	    Cleanup cleanup = new Cleanup();
	    Runtime runtime = Runtime.getRuntime();
	    runtime.addShutdownHook(cleanup);
	    
            // Set the HTTPS URL stream handler.
            java.security.AccessController.doPrivileged(new
					   java.security.PrivilegedAction() {
		    public Object run() {
			URL.setURLStreamHandlerFactory(new
					   HttpsURLStreamHandlerFactory());
			return null;
		    }
		});
	    
            File appClientFile;
            /*
             *For Java Web Start launches, locate the jar file implicitly.
             *Otherwise, if the user omitted the clientjar argument (and the
             *code has gotten this far) then the user must have used the
             *first argument as the name of the class in ${user.dir} to run.  If
             *the user actually specified the clientjar argument, then use that
             *value as the file spec for the client jar.
             */
            if (isJWS) {
                /*
                 *Java Web Start case.
                 */
                appClientFile = findAppClientFileForJWSLaunch();
            } else if (clientJar==null) {
                /*
                 *First-argument-as-class-name case
                 */
                File userDir = new File(System.getProperty("user.dir"));
                File appClientClass = new File(userDir, className);
                appClientFile = appClientClass.getParentFile();
            } else {
                /*
                 *Normal case - clientjar argument specified.
                 */
                appClientFile = new File(clientJar);
            }
	    
            // class loader
            URL[] urls = new URL[1];
            urls[0] = appClientFile.toURI().toURL();
            /*
             *Set the parent of the new class loader to the current loader.  
             *The Java Web Start-managed class path is implemented in the 
             *current loader, so it must remain on the loader stack.
             */
            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            ClassLoader jcl = new URLClassLoader(urls, currentCL);
            Thread.currentThread().setContextClassLoader(jcl);
            ApplicationClientDescriptor appDesc = null;
            
            // create the application container and call preInvoke.
            
            /*
             *Note that if the client jar argument is missing it can mean one of 
             *two things: Either the user used the first argument to specify 
             *the class to execute or
             *this is a Java Web Start launch.  
             */
            if((clientJar!=null || isJWS ) && FileUtil.isEARFile(appClientFile)) {


                // loads application with only the clients
                Application app = null;
                try {
                    ApplicationArchivist arch = new ApplicationArchivist();
                    arch.setAnnotationProcessingRequested(true);

                    // Set class loader here before opening archive 
                    // to enable validation. 
                    arch.setClassLoader(jcl);
                    app = (Application) arch.open(appClientFile);

                } catch (Throwable t) {
                   _logger.log(Level.WARNING, "acc.failed_load_client_desc",
                        clientJar);
                    throw t;
                }
                app.setClassLoader(jcl);
                appDesc = null;

		int appclientCount = 0;
		for (Iterator itr =
                    app.getApplicationClientDescriptors().iterator();
                    itr.hasNext();) {
		    ApplicationClientDescriptor next =
		      (ApplicationClientDescriptor) itr.next();
		    appclientCount++;
		}

                for (Iterator itr =
                    app.getApplicationClientDescriptors().iterator();
                    itr.hasNext();) {

                    ApplicationClientDescriptor next =
                        (ApplicationClientDescriptor) itr.next();
		    if (appclientCount == 1) {		      
			//for -mainclass <class name> option
			if (mainClass != null) {
			    if (!next.getMainClassName().equals(mainClass)) {
			        next.setMainClassName(mainClass);
			    }
			}
			appDesc = next;
			break;						
		    } else {//app contains multiple app client jars
		        if (mainClass != null) {
			    if (next.getMainClassName().equals(mainClass)) {
			        appDesc = next;
				break;
			    }
			} else {
			    if (displayName == null) {
			        _logger.log(Level.SEVERE,"acc.no_mainclass_or_displayname");
				System.exit(1);
			    } else if (displayName != null && next.getName().equals(displayName)) {
			        if(appDesc == null) {
				    appDesc = next;			    
				} else {
				    //multiple app duplicated display name
				    _logger.log(Level.WARNING, "acc.duplicate_display_name");
				    System.exit(1);
				}
			    }			
			}
		    }
                    
                }
                //construct AppContainer using appDesc
                if (appDesc != null) {
                    container = new AppContainer(appDesc, guiAuth);
                    // the archive uri must have absolute path
                    //f = new File (f, appDesc.getModuleDescriptor().getArchiveUri());
                }
            } else {

                // we are dealing with a class file or a client jar
        
                // reads std & iAS application xml

                try {
                    // Set classloader before opening archive to enable
                    // validation.
                    AppClientArchivist arch = new AppClientArchivist();
                    arch.setAnnotationProcessingRequested(true);
                    arch.setClassLoader(jcl);

                    // for class case, get default bundle
                    if (className!=null) {
                        appDesc = (ApplicationClientDescriptor) arch.getDefaultBundleDescriptor();
                    } else {
                        // for client jar case, do not process annotations.
                        // use AppClientArchivist.open(String) instead of 
                        // AppClientArchivist.open(AbstractArchive) since the 
                        // open(String) method calls validate.
                        appDesc = (ApplicationClientDescriptor) arch.open(appClientFile.getAbsolutePath());
                    }

                    if (className!=null) {
                        // post masssaging
                        AbstractArchive archive;
                        if (appClientFile.isDirectory()) {
                            archive = new FileArchive();
                            ((FileArchive) archive).open(appClientFile.getAbsolutePath());
                        } else {
                            archive = new InputJarArchive();
                            ((InputJarArchive) archive).open(appClientFile.getAbsolutePath());
                        }

                        if (appDesc.getMainClassName()==null || appDesc.getMainClassName().length()==0) {
                            appDesc.setMainClassName(className);
                            arch.processAnnotations(appDesc, archive);
                            
                            // let's remove our appArgs first element since it was the app client class name
                            //...but only if this is not a Java Web Start launch.
                            if (mainClass==null && ! isJWS) {
                                appArgs.removeElementAt(0);
                            }
                        }
                    }
                    
                } catch (Throwable t) {
                    _logger.log(Level.WARNING,
                            "main.appclient_descriptors_failed", (displayName == null) ? mainClass : displayName);
                    throw t;
                }
                container = new AppContainer(appDesc, guiAuth);
            }
            if(container == null) {
                _logger.log(Level.WARNING, "acc.no_client_desc",
                            (displayName == null) ? mainClass : displayName);

                System.exit(1);
            }
            // Set the authenticator which is called back when a
            // protected web resource is requested and authentication data is
            // needed.
            Authenticator.setDefault(new HttpAuthenticator(container));

            // log a machine name, port number per Jagadesh's request
	    _logger.log(Level.INFO, "acc.orb_host_name", host);
            _logger.log(Level.INFO, "acc.orb_port_number", port);
                      
	    Properties props = new Properties();
	    props.put("org.omg.CORBA.ORBInitialHost", host);
	    props.put("org.omg.CORBA.ORBInitialPort", port);

            String appMainClass = container.preInvoke(props);
            cleanup.setAppContainer(container);

            // load and invoke the real main of the application.
            Class cl = null;
            try {
                cl = jcl.loadClass(appMainClass);
            } catch (java.lang.ClassNotFoundException cnf) {
                String errorMessage = localStrings.getString
                    ("appclient.mainclass.not.found", appMainClass);
                _logger.log(Level.WARNING, errorMessage);
                throw cnf;
            }

            _logger.log(Level.INFO, "acc.load_app_class", appMainClass);           

            String[] applicationArgs = new String[appArgs.size()];
            for(int sz = 0; sz < applicationArgs.length; sz++) {
                applicationArgs[sz] = (String) appArgs.elementAt(sz);
            }

            // check if we are dealing with an application client containing
            // service references... if this is the case, I need to explode
            // the appclient jar file to be able to access its wsdl files 
            // with a URL (so that imports can work)
            if (appDesc.hasWebServiceClients()) {
                File moduleFile;
                if (appDesc.getApplication()==null 
                      || appDesc.getApplication().isVirtual()) {
                    // this is a standalone module, I can do Wsdl file 
                    // resolution directly on it.
                    moduleFile = appClientFile;
                } else {
                    InputJarArchive earFile = new InputJarArchive();
                    earFile.open(appClientFile.getAbsolutePath());
                    String moduleName = appDesc.getModuleDescriptor().getArchiveUri();
                    InputStream is = earFile.getEntry(moduleName);
                    moduleFile = File.createTempFile("appclient", ".jar");
                    moduleFile.deleteOnExit();
                    OutputStream os = new FileOutputStream(moduleFile); 
                    ArchivistUtils.copy(new BufferedInputStream(is), new BufferedOutputStream(os));
                    earFile.close();
                }
                // now perform wsdl file resolution
                for (Iterator itr = appDesc.getServiceReferenceDescriptors().iterator();
                    itr.hasNext();) {
                        
                    ServiceReferenceDescriptor serviceRef = (ServiceReferenceDescriptor) itr.next();
                    if (serviceRef.getWsdlFileUri()!=null) {
                        // In case WebServiceRef does not specify wsdlLocation, we get wsdlLocation from @WebClient
                        // in wsimport generated source; If wsimport was given a local WSDL file, then WsdlURI will
                        // be an absolute path - in that case it should not be prefixed with modileFileDir
                        File wsdlFile = new File(serviceRef.getWsdlFileUri());
                        if(wsdlFile.isAbsolute()) {
                            serviceRef.setWsdlFileUrl(wsdlFile.toURI().toURL());
                        } else {
                            // This is the case where WsdlFileUri is a relative path (hence relative to the root of
                            // this module or wsimport was executed with WSDL in HTTP URL form
                            serviceRef.setWsdlFileUrl(FileUtil.getEntryAsUrl(moduleFile, serviceRef.getWsdlFileUri()));
                        }
                    }
                }
            }
            

            // Inject the application client's injectable resources.  This
            // must be done after java:comp/env is initialized but before
            // the application client's main class is invoked.
            InjectionManager injMgr = Switch.getSwitch().getInjectionManager();
            injMgr.injectClass(cl, appDesc);            
                            
            if(runClient) {
                Utility.invokeApplicationMain(cl, applicationArgs);
                _logger.info("Application main() finished normally");
            }


            // inject the pre-destroy methods before shutting down
            injMgr.invokeClassPreDestroy(cl, appDesc);            

	    // Let's shutdown all the system resource adapters that are  
	    // active in the container.
	    shutDownSystemAdapters();

            // System.exit is not called if application main returned
            // without error.  Registered shutdown hook will perform
            // container cleanup
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable tt = ite.getTargetException();
            _logger.log(Level.WARNING, "acc.app_exception", tt);
	    shutDownSystemAdapters();
            System.exit(1);
        } catch (Throwable t) {
            if (t instanceof javax.security.auth.login.FailedLoginException){

               _logger.info("acc.login_error");
                boolean isGui =
                    Boolean.valueOf
                        (System.getProperty ("auth.gui","true")).booleanValue();
                String errorMessage =
                    localStrings.getString
                        ("main.exception.loginError",
                         "Incorrect login and/or password");

                if (isGui) {
                    GUIErrorDialog ged = new GUIErrorDialog (errorMessage);
                    ged.show ();
                }
            }

            _logger.log(Level.WARNING, "acc.app_exception", t);

            if (t instanceof javax.naming.NamingException) {
                _logger.log(Level.WARNING, "acc.naming_exception_received");
            }
	 
	    shutDownSystemAdapters();
	    
            System.exit(1);
        }      
    }

    private static void setTargetServerProperties(String clientXmlLocation) 
	throws ConfigException {
        //FIXME: may need to set the context in switch or generic context. but later
        try {
            if(clientXmlLocation == null || clientXmlLocation.equals("")) {
                clientXmlLocation = DEFAULT_CLIENT_CONTAINER_XML;
            }

	    // set for com.sun.enterprise.security.jauth.ConfigXMLParser
	    System.setProperty(SUNACC_XML_URL, clientXmlLocation);
             _logger.log(Level.INFO, "acc.using_xml_location", clientXmlLocation);
	       
            ConfigContext ctx = ConfigFactory.createConfigContext(
		clientXmlLocation, true,
		false, false,
		ClientContainer.class, 
		new ACCEntityResolver());

            ClientContainer cc = ClientBeansFactory.getClientBean(ctx);
        
	    host = cc.getTargetServer(0).getAddress();
	    port = cc.getTargetServer(0).getPort();

	    //check for targetServerEndpoints 
	    TargetServer[] tServer = cc.getTargetServer();
	    String targetServerEndpoints = null;
	    for (int i = 0; i < tServer.length; i++) {
	        if (targetServerEndpoints == null) {
		    targetServerEndpoints = tServer[i].getAddress() + 
		      ":" + tServer[i].getPort();
		} else {
		  // if we come here, that means we have more than 1 target-server elements. 
		  // in that case FOLB should be enabled
		    lb_enabled = true;
		    targetServerEndpoints = targetServerEndpoints + "," + 
		      tServer[i].getAddress() + 
		      ":" + tServer[i].getPort();
		}
	    }
		
            setSSLData(cc);

            //FIXME: what do we do about realm
            ClientCredential cCrd = cc.getClientCredential();
            if(cCrd != null) {
                String uname = null;
                String upass = null;

                // if user entered user/password from command line,
                // it take percedence over the xml file. - y.l. 05/15/02
                if (System.getProperty(LOGIN_NAME) == null) {
                    _logger.config("using login name from client container xml...");
                    //System.setProperty(LOGIN_NAME, cCrd.getUserName());
                    uname = cCrd.getUserName();
                }
                if (System.getProperty(LOGIN_PASSWORD) == null) {
                    _logger.config("using password from client container xml...");
                    // System.setProperty(LOGIN_PASSWORD, cCrd.getPassword());
                    upass = cCrd.getPassword();
                }
                if( uname != null || upass != null ) {
                    UsernamePasswordStore.set(uname, upass);
                }
            }
		String endpoints_property = null;
	    // Check if client requires SSL to be used
	    ElementProperty[] props = cc.getElementProperty();
	    for ( int i=0; i<props.length; i++ ) {
		if ( props[i].getName().equals("ssl") ) {
		    if ( props[i].getValue().equals("required") ) {
			(ORBManager.getCSIv2Props()).put(ORBManager.ORB_SSL_CLIENT_REQUIRED,
				       "true");
		    }
		}
		if ( props[i].getName().equals(S1ASCtxFactory.LOAD_BALANCING_PROPERTY) ) {
		    System.setProperty(props[i].getName(),props[i].getValue());	
		    lb_enabled = true;
		}	
		if ( props[i].getName().equals(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY) ) {
		    endpoints_property = props[i].getValue().trim();
		    lb_enabled = true;
		}
	    }    
            
            /*
             *If the endpoints property was not set in the XML file's property
             *settings, try to set it from the server's assignment in the JNLP document.
             */
            String jwsEndpointsProperty = System.getProperty(Main.APPCLIENT_IIOP_FAILOVER_ENDPOINTS_PROPERTYNAME);
	    if (jwsEndpointsProperty != null) {
	        targetServerEndpoints = jwsEndpointsProperty;
		lb_enabled = true;
	    } else {
	        /*
		 *Suppress the warning if the endpoints_property was set
		 *from the JNLP document, since that is in fact the preferred
		 *way to set the endpoints.
		 */
	        _logger.warning("acc.targetserver.endpoints.warning");
	    }
            
	    _logger.fine("targetServerEndpoints = " + targetServerEndpoints + 
			 "endpoints_property = " + 
			 endpoints_property);	

	    if (lb_enabled == true && endpoints_property == null) {
	        System.setProperty(
				 S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY, 
				   targetServerEndpoints.trim());
	    } else if (endpoints_property != null) {
		System.setProperty(
				   S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY, 
				   targetServerEndpoints.trim() + "," + 
				   endpoints_property);

	    }
	} catch (ConfigException t) {
	    _logger.log(Level.WARNING,"acc.acc_xml_file_error" ,
			new Object[] {clientXmlLocation, t.getMessage()}); 
	    _logger.log(Level.FINE, "exception : " + t.toString(), t);
	    throw t;
	}
    }    

    private static void setSSLData(ClientContainer cc) {
        try {
            // Set the SSL related properties for ORB
            TargetServer tServer = cc.getTargetServer(0);
            // TargetServer is required.
	    //temp solution to target-server+ change in DTD
            // assuming that multiple servers can be specified but only 1st
	    // first one will be used.
	    Security security = tServer.getSecurity();
	    if (security == null) {
		_logger.fine("No Security input set in ClientContainer.xml");
		// do nothing
		return;
	    }
	    Ssl ssl = security.getSsl();
	    if (ssl == null) {
		_logger.fine("No SSL input set in ClientContainer.xml");
		// do nothing
		return;
		
	    }
	    //XXX do not use NSS in this release
	    //CertDb   certDB  = security.getCertDb();
	    SSLUtils.setAppclientSsl(ssl);	
	} catch (Exception ex) {

        }
    }

    
    private static void validateXMLFile(String xmlFullName) 
    {
        //<Bug # 4689278-Start>
        if(xmlFullName == null || 
           xmlFullName.startsWith("-")){ // If no file name is given after -xml argument
            usage();
        }
        try {
            File f = new File(xmlFullName);
            if((f != null) && f.exists() && f.isFile() && f.canRead()){
                return;
            }else{// If given file does not exists
                xmlMessage(xmlFullName);
                usage();
            }
        } catch (Exception ex) {
            xmlMessage(xmlFullName);
            usage();
        }
        //</Bug # 4689278-End>
    }

    // Shut down system resource adapters. Currently it is
    // only JMS.
    private void shutDownSystemAdapters() {
       try {
	    com.sun.enterprise.PoolManager poolmgr = 
	        Switch.getSwitch().getPoolManager();
	    if ( poolmgr != null ) {	
	        Switch.getSwitch().getPoolManager().killFreeConnectionsInPools();
	    }	
	} catch( Exception e ) {
	    //ignore
	}
        
	try {
            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            ActiveResourceAdapter activeRar = registry.getActiveResourceAdapter
                                         (ConnectorRuntime.DEFAULT_JMS_ADAPTER);
            if (activeRar != null) {
                activeRar.destroy();
            }
        } catch (Exception e) {
            // Some thing has gone wrong. No problem
            _logger.fine("Exception caught while shutting down system adapter:"+e.getMessage());
        }
    }

    private static void usage() {
        System.out.println(localStrings.getString("main.usage",
            "appclient [ -client <appjar> ] [-mainclass <appClass-name>|-name <display-name>] [-xml <xml>] [-textauth] [-user <username>] [-password <password>|-passwordfile <password-file>] [app-args]"));
	System.exit(1);
    }
    
    private static void xmlMessage(String xmlFullName)
    {
        System.out.println(localStrings.getString("main.cannot_read_clientContainer_xml", xmlFullName,
             "Client Container xml: " + xmlFullName + " not found or unable to read.\nYou may want to use the -xml option to locate your configuration xml."));
       
    }

    private String loadPasswordFromFile(String fileName)
            throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(fileName));
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty("PASSWORD");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static class Cleanup extends Thread {
        private AppContainer appContainer = null;
        private boolean cleanedUp = false;

        public Cleanup() {
        }

        public void setAppContainer(AppContainer container) {
            appContainer = container;
        }

        public void run() {
            cleanUp();
        }

        public void cleanUp() {
            if( !cleanedUp ) {
                try {
                    if( appContainer != null ) {
                        appContainer.postInvoke();
                    }
                }
                catch(Throwable t) {
                }
                finally {
                    cleanedUp = true;
                }
            } // End if -- cleanup required
        }
    }
    /**
     *Sets up the user-provided or default sun-acc.xml and 
     *wss-client-config.xml configurations.
     *@return the file name of the sun-acc.xml file
     */
    private String prepareJWSConfig() throws IOException, FileNotFoundException {
        return prepareJWSDefaultConfig();
    }
    
    /**
     *Creates temporary files for use as default sun-acc.xml and 
     *wss-client-config.xml configurations.
     *@return the file name of the temporary sun-acc.xml file
     */
    private String prepareJWSDefaultConfig() throws IOException, FileNotFoundException {
        String result = null;
        
        /*
         *Retrieve the sun-acc and wss-client-config templates.
         */
        String sunACCTemplate = Util.loadResource(this.getClass(), SUN_ACC_DEFAULT_TEMPLATE);
        String wssClientConfigTemplate = Util.loadResource(this.getClass(), WSS_CLIENT_CONFIG_TEMPLATE);
        
        /*
         *Prepare the property names and values for substitution in the templates.  Some
         *of the properties are specified in the environment already, so use those
         *as defaults and just add the extra ones.
         */
        Properties tokenValues = new Properties(System.getProperties());
        
        /**
         *Create the wss client config defaults, then write them to a temporary file.
         */
        String wssClientConfig = Util.replaceTokens(wssClientConfigTemplate, tokenValues);
        File wssClientConfigFile = Util.writeTextToTempFile(wssClientConfig, WSS_CLIENT_CONFIG_PREFIX, WSS_CLIENT_CONFIG_SUFFIX, retainTempFiles);
        pendingLogFine.append("Temporary wss-client-config.xml file: " + wssClientConfigFile.getAbsolutePath() + lineSep);
        
        /*
         *Now that the wss temp file is created, insert its name into the default
         *sun-acc text and write that to another temp file.
         *
         *On Windows, the backslashes in the path will be consumed by the replaceTokens method which will
         *interpret them as quoting the following character.  So replace each \ with \\ first.  All the slashes
         *have to do with quoting a slash to the Java compiler, then quoting it again to the regex
         *processor.
         */
        String quotedConfigFileSpec = wssClientConfigFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
        tokenValues.setProperty(SUN_ACC_SECURITY_CONFIG_PROPERTY, quotedConfigFileSpec);
        
        String sunaccContent = Util.replaceTokens(sunACCTemplate, tokenValues);
        File sunaccFile = Util.writeTextToTempFile(sunaccContent, SUN_ACC_PREFIX, SUN_ACC_SUFFIX, retainTempFiles);
        pendingLogFine.append("Temporary sun-acc.xml file: " + sunaccFile.getAbsolutePath());
        
        return sunaccFile.getAbsolutePath();
    }

    /**
     *Prepares the JAAS login configuration for a Java Web Start invocation.
     *
     */
    private void prepareJWSLoginConfig() throws IOException, FileNotFoundException {
        prepareJWSDefaultLoginConfig();
    }
    
    /**
     *Extracts the default login.conf file into a temporary file and assigns the
     *java.security.auth.login.config property accordingly.
     */
    private void prepareJWSDefaultLoginConfig() throws IOException, FileNotFoundException {
        String configContent = Util.loadResource(this.getClass(), LOGIN_CONF_TEMPLATE);
        File configFile = Util.writeTextToTempFile(configContent, LOGIN_CONF_FILE_PREFIX, LOGIN_CONF_FILE_SUFFIX, retainTempFiles);
        String configFilePath = configFile.getAbsolutePath();
        pendingLogFine.append("Temporary appclientlogin.conf file: " + configFilePath);
        System.setProperty(LOGIN_CONF_PROPERTY_NAME, configFilePath);
    }
    
    /**
     *Locate the app client jar file during a Java Web Start launch.
     *@return File object for the client jar file
     */
    private File findAppClientFileForJWSLaunch() throws ClassNotFoundException, URISyntaxException {
        /*
         *Locate the file by using the name of the "probe" class, passed from the
         *server, to load the class and then find the location of the jar that
         *contains that class.
         */
        String probeClassName = System.getProperty(APPCLIENT_PROBE_CLASSNAME_PROPERTYNAME);
        _logger.fine("Probing class " + probeClassName);
        Class probeClass = Class.forName(probeClassName);
        URL workingURL = probeClass.getProtectionDomain().getCodeSource().getLocation();
        _logger.fine("Location of appclient jar file: " + workingURL.toString());

        /*
         *workingURL.toURI() on Windows gives (for example) file:c:/<rest of URI>.
         *This cannot be used directly in new File(workingURI) because File complains
         *that the URI is not hierarchical - it is missing the / that would 
         *normally precede the device.  So, get the scheme-specific part and 
         *use that in the File constructor, which gives us the result we need.
         */
        URI workingURI = workingURL.toURI();
        String ssp = workingURI.getSchemeSpecificPart();
        return new File(ssp);
    }
}
