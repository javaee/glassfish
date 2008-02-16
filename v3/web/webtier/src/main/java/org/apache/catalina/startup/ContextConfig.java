

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.startup;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Map;
import javax.servlet.ServletContext;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.SchemaResolver;
import com.sun.org.apache.commons.digester.Digester;
import com.sun.org.apache.commons.digester.RuleSet;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @version $Revision: 1.14 $ $Date: 2007/02/20 20:16:56 $
 */

// START OF SJAS 8.0 BUG 5046959
// public final class ContextConfig
// NOTE: All the methods were originally private and changed to public.
public class ContextConfig
// END OF SJAS 8.0 BUG 5046959
    implements LifecycleListener {

    private static Log log= LogFactory.getLog( ContextConfig.class );

    // ----------------------------------------------------- Instance Variables


    /*
     * Custom mappings of login methods to authenticators
     */
    //START SJSAS 6202703
    //private Map customAuthenticators;
    protected Map customAuthenticators;
    //END SJSAS 6202703


    /**
     * The set of Authenticators that we know how to configure.  The key is
     * the name of the implemented authentication method, and the value is
     * the fully qualified Java class name of the corresponding Valve.
     */
    //START SJSAS 6202703
    //private static Properties authenticators = null;
    
    protected static Properties authenticators = null;
    //END SJSAS 6202703

    /**
     * The Context we are associated with.
     */
    protected Context context = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    // START GlassFish 2439
    /**
     * The default web application's context file location.
     */
    protected String defaultContextXml = null;
    // END GlassFish 2439
    

    /**
     * The default web application's deployment descriptor location.
     */
    // BEGIN OF SJSAS 8.1 6172288  
    // private String defaultWebXml = null;
    protected String defaultWebXml = null;
    
    
    /**
     * Track any fatal errors during startup configuration processing.
     */
    // private boolean ok = false;
    protected boolean ok = false;
    // END OF SJSAS 8.1 6172288 


    // START GlassFish 2439
    /**
     * Any parse error which occurred while parsing XML descriptors.
     */
    protected SAXParseException parseException = null;
    // END GlassFish 2439 


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // START GlassFish 2439
    /**
     * The <code>Digester</code> we will use to process web application
     * context files.
     */
    protected static Digester contextDigester = null;
    // END GlassFish 2439


    /**
     * The <code>Digester</code> we will use to process web application
     * deployment descriptor files.
     */
    // BEGIN OF SJSAS 8.1 6172288  
    // private static Digester webDigester = null;
    protected static Digester webDigester = null;
    // END OF SJSAS 8.1 6172288  
    
    
    /**
     * The <code>Rule</code> used to parse the web.xml
     */
    // BEGIN OF SJSAS 8.1 6172288  
    // private static WebRuleSet webRuleSet = new WebRuleSet();
    protected static final WebRuleSet webRuleSet = new WebRuleSet();
    // END OF SJSAS 8.1 6172288  

    /**
     * Attribute value used to turn on/off XML validation
     */
    private static boolean xmlValidation = false;


    /**
     * Attribute value used to turn on/off XML namespace awarenes.
     */
    private static boolean xmlNamespaceAware = false;

        
    // ------------------------------------------------------------- Properties


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }
    
    
    // START GlassFish 2439
    /**
     * Return the location of the default deployment descriptor
     */
    public String getDefaultWebXml() {
        if( defaultWebXml == null ) defaultWebXml=Constants.DefaultWebXml;
        return (this.defaultWebXml);

    }


    /**
     * Set the location of the default deployment descriptor
     *
     * @param path Absolute/relative path to the default web.xml
     */
    public void setDefaultWebXml(String path) {

        this.defaultWebXml = path;

    }
    // END GlassFish 2439


    /**
     * Return the location of the default context file
     */
    public String getDefaultContextXml() {
        if( defaultContextXml == null ) {
            defaultContextXml=Constants.DEFAULT_CONTEXT_XML;
        }

        return (this.defaultContextXml);

    }


    /**
     * Set the location of the default context file
     *
     * @param path Absolute/relative path to the default context.xml
     */
    public void setDefaultContextXml(String path) {

        this.defaultContextXml = path;

    }


    /**
     * Sets custom mappings of login methods to authenticators.
     *
     * @param customAuthenticators Custom mappings of login methods to
     * authenticators
     */
    public void setCustomAuthenticators(Map customAuthenticators) {
        this.customAuthenticators = customAuthenticators;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the context we are associated with
        try {
            context = (Context) event.getLifecycle();
//             if (context instanceof StandardContext) {
//                 int contextDebug = ((StandardContext) context).getDebug();
//                 if (contextDebug > this.debug)
//                     this.debug = contextDebug;
//             }
        } catch (ClassCastException e) {
            log.error(sm.getString("contextConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Called from ContainerBase.addChild() -> StandardContext.start()
        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) 
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT)) 
            stop();
        // START GlassFish 2439
        else if (event.getType().equals(Lifecycle.INIT_EVENT)) 
            init();
        // END GlassFish 2439

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Process the application configuration file, if it exists.
     */
    protected void applicationConfig() {

        String altDDName = null;

        // Open the application web.xml file, if it exists
        InputStream stream = null;
        ServletContext servletContext = context.getServletContext();
        if (servletContext != null) {
            altDDName = (String)servletContext.getAttribute(
                                                        Globals.ALT_DD_ATTR);
            if (altDDName != null) {
                try {
                    stream = new FileInputStream(altDDName);
                } catch (FileNotFoundException e) {
                    log.error(sm.getString("contextConfig.altDDNotFound",
                                           altDDName));
                }
            }
            else {
                stream = servletContext.getResourceAsStream
                    (Constants.ApplicationWebXml);
            }
        }
        if (stream == null) {
            /* PWC 6296257
            log.info(sm.getString("contextConfig.applicationMissing") + " " + context);
            */
            // START PWC 6296257
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("contextConfig.applicationMissing")
                          + " " + context);
            }
            // END PWC 6296257
            return;
        }
        
        long t1=System.currentTimeMillis();

        if (webDigester == null){
            webDigester = createWebDigester();
        }
        
        URL url=null;
        // Process the application web.xml file
        synchronized (webDigester) {
            try {
                if (altDDName != null) {
                    url = new File(altDDName).toURL();
                } else {
                    url = servletContext.getResource(
                                                Constants.ApplicationWebXml);
                }
                if( url!=null ) {
                    InputSource is = new InputSource(url.toExternalForm());
                    is.setByteStream(stream);
                    webDigester.clear();
                    webDigester.setDebug(getDebug());
                    if (context instanceof StandardContext) {
                        ((StandardContext) context).setReplaceWelcomeFiles(true);
                    }
                    webDigester.setUseContextClassLoader(false);
                    webDigester.push(context);
                    // START PWC 6390776
                    webDigester.setLogger(
                                    new DigesterLogger(context.getName()));
                    // END PWC 6390776
                    webDigester.parse(is);
                } else {
                    log.info("No web.xml, using defaults " + context );
                }
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.applicationParse"), e);
                log.error(sm.getString("contextConfig.applicationPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.applicationParse"), e);
                ok = false;
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.applicationClose"), e);
                }
                webDigester.push(null);
            }
        }
        webRuleSet.recycle();

        long t2=System.currentTimeMillis();
        if (context instanceof StandardContext) {
            ((StandardContext) context).setStartupTime(t2-t1);
        }
    }


    /**
     * Set up a manager.
     */
    protected synchronized void managerConfig() {

        if (context.getManager() == null) {
            if ((context.getCluster() != null) && context.getDistributable()) {
                try {
                    context.setManager(context.getCluster().createManager
                                       (context.getName()));
                } catch (Exception ex) {
                    log.error("contextConfig.clusteringInit", ex);
                    ok = false;
                }
            } else {
                context.setManager(new StandardManager());
            }
        }

    }


    /**
     * Set up an Authenticator automatically if required, and one has not
     * already been configured.
     */
    protected synchronized void authenticatorConfig() {

        // Does this Context require an Authenticator?
        /* START IASRI 4856062
           // This constraints check is relocated to happen after
           // setRealmName(). This allows apps which have no constraints
           // and no authenticator to still have a realm name set in
           // their RealmAdapater. This is only relevant in the case where
           // the core ACLs are doing all access control AND the servlet
           // wishes to call isUserInRole AND the application does have
           // security-role-mapping elements in sun-web.xml. This is probably
           // not an interesting scenario. But might as well allow it to
           // work, maybe it is of some use.
        SecurityConstraint constraints[] = context.findConstraints();
        if ((constraints == null) || (constraints.length == 0))
            return;
        */
        LoginConfig loginConfig = context.getLoginConfig();
        if (loginConfig == null) {
            loginConfig = new LoginConfig("NONE", null, null, null);
            context.setLoginConfig(loginConfig);
        }

        // Has an authenticator been configured already?
        if (context instanceof Authenticator)
            return;
        if (context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                Valve basic = pipeline.getBasic();
                if ((basic != null) && (basic instanceof Authenticator))
                    return;
                Valve valves[] = pipeline.getValves();
                for (int i = 0; i < valves.length; i++) {
                    if (valves[i] instanceof Authenticator)
                        return;
                }
            }
        } else {
            return;     // Cannot install a Valve even if it would be needed
        }

        // Has a Realm been configured for us to authenticate against?
        /* START IASRI 4856062
        if (context.getRealm() == null) {
        */
        // BEGIN IASRI 4856062
        Realm rlm = context.getRealm();
        if (rlm == null) {
        // END IASRI 4856062
            log.error(sm.getString("contextConfig.missingRealm"));
            ok = false;
            return;
        }

        // BEGIN IASRI 4856062
        // If a realm is available set its name in the Realm(Adapter)
        rlm.setRealmName(loginConfig.getRealmName(),
                         loginConfig.getAuthMethod());

        SecurityConstraint constraints[] = context.findConstraints();
        if ((constraints == null) || (constraints.length == 0))
            return;
        // END IASRI 4856062

        /*
         * First check to see if there is a custom mapping for the login
         * method. If so, use it. Otherwise, check if there is a mapping in
         * org/apache/catalina/startup/Authenticators.properties.
         */
        Valve authenticator = null;
        if (customAuthenticators != null) {
            /* PWC 6392537
            authenticator = (Valve)
                customAuthenticators.get(loginConfig.getAuthMethod());
            */
            // START PWC 6392537
            String loginMethod = loginConfig.getAuthMethod();
            if (loginMethod != null
                    && customAuthenticators.containsKey(loginMethod)) {
                authenticator = (Valve) customAuthenticators.get(loginMethod);
                if (authenticator == null) {
                    log.error(
                        sm.getString("contextConfig.authenticatorMissing",
                                     loginMethod));
                    ok = false;
                    return;
                }
            }
            // END PWC 6392537
        }
        if (authenticator == null) {
            // Load our mapping properties if necessary
            if (authenticators == null) {
                try {
                    InputStream is=this.getClass().getClassLoader().getResourceAsStream("org/apache/catalina/startup/Authenticators.properties");
                    if( is!=null ) {
                        authenticators = new Properties();
                        authenticators.load(is);
                    } else {
                        log.error(sm.getString(
                                "contextConfig.authenticatorResources"));
                        ok=false;
                        return;
                    }
                } catch (IOException e) {
                    log.error(sm.getString(
                                "contextConfig.authenticatorResources"), e);
                    ok = false;
                    return;
                }
            }

            // Identify the class name of the Valve we should configure
            String authenticatorName = null;

            // BEGIN RIMOD 4808402
            // If login-config is given but auth-method is null, use NONE
            // so that NonLoginAuthenticator is picked
            String authMethod = loginConfig.getAuthMethod();
            if (authMethod == null) {
                authMethod = "NONE";
            }
            authenticatorName = authenticators.getProperty(authMethod);
            // END RIMOD 4808402
            /* RIMOD 4808402
            authenticatorName =
                    authenticators.getProperty(loginConfig.getAuthMethod());
            */

            if (authenticatorName == null) {
                log.error(sm.getString("contextConfig.authenticatorMissing",
                                 loginConfig.getAuthMethod()));
                ok = false;
                return;
            }

            // Instantiate and install an Authenticator of the requested class
            try {
                Class authenticatorClass = Class.forName(authenticatorName);
                authenticator = (Valve) authenticatorClass.newInstance();
            } catch (Throwable t) {
                log.error(sm.getString(
                                    "contextConfig.authenticatorInstantiate",
                                    authenticatorName),
                          t);
                ok = false;
            }
        }

        if (authenticator != null && context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                ((ContainerBase) context).addValve(authenticator);
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString(
                                    "contextConfig.authenticatorConfigured",
                                    loginConfig.getAuthMethod()));
                }
            }
        }
    }

    /**
     * Create (if necessary) and return a Digester configured to process the
     * web application deployment descriptor (web.xml).
     */
    // BEGIN OF SJSAS 8.1 6172288  
    // private static Digester createWebDigester() {
    public static Digester createWebDigester() {
    // END OF SJSAS 8.1 6172288  
        Digester webDigester =
            createWebXmlDigester(xmlNamespaceAware, xmlValidation);
        return webDigester;
    }


    /*
     * Create (if necessary) and return a Digester configured to process the
     * web application deployment descriptor (web.xml).
     */
    public static Digester createWebXmlDigester(boolean namespaceAware,
                                                boolean validation) {
        
        Digester webDigester =  DigesterFactory.newDigester(xmlValidation,
                                                            xmlNamespaceAware,
                                                            webRuleSet);
        return webDigester;
    }


    // START GlassFish 2439 
    /**
     * Create (if necessary) and return a Digester configured to process the
     * context configuration descriptor for an application.
     */
    protected Digester createContextDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
        RuleSet contextRuleSet = new ContextRuleSet("", false);
        digester.addRuleSet(contextRuleSet);
        RuleSet namingRuleSet = new NamingRuleSet("Context/");
        digester.addRuleSet(namingRuleSet);
        return digester;
    }
    // END GlassFish 2439


    protected String getBaseDir() {
        Container engineC=context.getParent().getParent();
        if( engineC instanceof StandardEngine ) {
            return ((StandardEngine)engineC).getBaseDir();
        }
        return System.getProperty("catalina.base");
    }

    /**
     * Process the default configuration file, if it exists.
     * The default config must be read with the container loader - so
     * container servlets can be loaded
     */
    protected void defaultConfig() {
        long t1=System.currentTimeMillis();

        // Open the default web.xml file, if it exists
        if( defaultWebXml==null && context instanceof StandardContext ) {
            defaultWebXml=((StandardContext)context).getDefaultWebXml();
        }
        // set the default if we don't have any overrides
        if( defaultWebXml==null ) getDefaultWebXml();

        File file = new File(this.defaultWebXml);
        if (!file.isAbsolute()) {
            file = new File(getBaseDir(),
                            this.defaultWebXml);
        }
        
        InputStream stream = null;
        InputSource source = null;

        try {
            if ( ! file.exists() ) {
                // Use getResource and getResourceAsStream
                stream = getClass().getClassLoader()
                    .getResourceAsStream(defaultWebXml);
                if( stream != null ) {
                    source = new InputSource
                            (getClass().getClassLoader()
                            .getResource(defaultWebXml).toString());
                } 

                if( stream== null ) { 
                    // maybe embedded
                    stream = getClass().getClassLoader()
                        .getResourceAsStream("web-embed.xml");
                    if( stream != null ) {
                        source = new InputSource
                        (getClass().getClassLoader()
                                .getResource("web-embed.xml").toString());
                    }                                         
                }

                if( stream== null ) {
                    log.info("No default web.xml");
                    // no default web.xml
                    return;
                }
            } else {
                source =
                    new InputSource("file://" + file.getAbsolutePath());
                stream = new FileInputStream(file);
            }
        } catch (Exception e) {
            log.error(sm.getString("contextConfig.defaultMissing") 
                      + " " + defaultWebXml + " " + file , e);
            return;
        }

        if (webDigester == null){
            webDigester = createWebDigester();
        }
        
        // Process the default web.xml file
        synchronized (webDigester) {
            try {
                source.setByteStream(stream);
                webDigester.setDebug(getDebug());
                
                // JFA
                if (context instanceof StandardContext)
                    ((StandardContext) context).setReplaceWelcomeFiles(true);
                webDigester.clear();
                webDigester.setClassLoader(this.getClass().getClassLoader());
                //log.info( "Using cl: " + webDigester.getClassLoader());
                webDigester.setUseContextClassLoader(false);
                webDigester.push(context);
                webDigester.parse(source);
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                log.error(sm.getString("contextConfig.defaultPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                ok = false;
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.defaultClose"), e);
                }
            }
        }
        webRuleSet.recycle();
        
        long t2=System.currentTimeMillis();
        if( (t2-t1) > 200 )
            log.debug("Processed default web.xml " + file + " "  + ( t2-t1));
    }


    // START GlassFish 2439
    /**
     * Process the default configuration file, if it exists.
     */
    protected void contextConfig() {

        if( defaultContextXml==null ) getDefaultContextXml();

        if (!context.getOverride()) {
            processContextConfig(new File(getBaseDir()), defaultContextXml);
        }

        if (context.getConfigFile() != null)
            processContextConfig(new File(context.getConfigFile()), null);

    }


    /**
     * Process a context.xml.
     */
    protected void processContextConfig(File baseDir, String resourceName) {
        if (log.isDebugEnabled())
            log.debug("Processing context [" + context.getName()
                    + "] configuration file " + baseDir + " " + resourceName);

        InputSource source = null;
        InputStream stream = null;

        File file = baseDir;
        if (resourceName != null) {
            file = new File(baseDir, resourceName);
        }
        try {
            if ( !file.exists() ) {
                if (resourceName != null) {
                    // Use getResource and getResourceAsStream
                    stream = getClass().getClassLoader()
                        .getResourceAsStream(resourceName);
                    if( stream != null ) {
                        source = new InputSource
                            (getClass().getClassLoader()
                            .getResource(resourceName).toString());
                    }
                }
            } else {
                source =
                    new InputSource("file://" + file.getAbsolutePath());
                stream = new FileInputStream(file);
            }
        } catch (Exception e) {
            log.error(sm.getString("contextConfig.defaultMissing")
                      + " " + resourceName + " " + file , e);
        }
        if (source == null)
            return;
        if (contextDigester == null){
            contextDigester = createContextDigester();
        }
        synchronized (contextDigester) {
            try {
                source.setByteStream(stream);
                contextDigester.setClassLoader(this.getClass().getClassLoader());
                contextDigester.setUseContextClassLoader(false);
                contextDigester.push(context.getParent());
                contextDigester.push(context);
                contextDigester.setErrorHandler(new ContextErrorHandler());
                contextDigester.parse(source);
                if (parseException != null) {
                    ok = false;
                }
                if (log.isDebugEnabled())
                    log.debug("Successfully processed context [" + context.getName()
                            + "] configuration file " + baseDir + " " + resourceName);
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                log.error(sm.getString("contextConfig.defaultPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                ok = false;
            } finally {
                //contextDigester.reset();
                parseException = null;
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.defaultClose"), e);
                }
            }
        }

    }
    // END GlassFish 2439


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = null;
        if (context != null)
            logger = context.getLogger();
        if (logger != null)
            logger.log("ContextConfig[" + context.getName() + "]: " + message);
        else
            log.info( message );
    }


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = null;
        if (context != null)
            logger = context.getLogger();
        if (logger != null)
            logger.log("ContextConfig[" + context.getName() + "] "
                       + message, throwable);
        else {
            log.error( message, throwable );
        }

    }


    
    // START GlassFish 2439
    /**
     * Process a "init" event for this Context.
     */
    protected void init() {

        // Called from StandardContext.init()

        if (log.isDebugEnabled())
            log.debug(sm.getString("contextConfig.init"));
        context.setConfigured(false);
        ok = true;

        contextConfig();

    }
    // END GlassFish 2439


    /**
     * Process a "start" event for this Context - in background
     */
    protected synchronized void start() {
        // Called from StandardContext.start()

        if (log.isTraceEnabled())
            log.trace(sm.getString("contextConfig.start"));
        context.setConfigured(false);
        ok = true;

        // Set properties based on DefaultContext
        Container container = context.getParent();
        if( !context.getOverride() ) {
            if( container instanceof Host ) {
                ((Host)container).importDefaultContext(context);
             
                // Reset the value only if the attribute wasn't
                // set on the context.
                xmlValidation = context.getXmlValidation();
                if (!xmlValidation) {
                    xmlValidation = ((Host)container).getXmlValidation();
                }
                
                xmlNamespaceAware = context.getXmlNamespaceAware();
                if (!xmlNamespaceAware){
                    xmlNamespaceAware 
                                = ((Host)container).getXmlNamespaceAware();
                }
                
                container = container.getParent();
            }
            if( container instanceof Engine ) {
                ((Engine)container).importDefaultContext(context);
            }
        }

        // Process the default and application web.xml files
        defaultConfig();
        applicationConfig();
        if (ok) {
            validateSecurityRoles();
        }

        // Configure an authenticator if we need one
        if (ok)
            authenticatorConfig();

        // Configure a manager
        if (ok)
            managerConfig();

        // Dump the contents of this pipeline if requested
        if ((log.isTraceEnabled()) && (context instanceof ContainerBase)) {
            log.trace("Pipline Configuration:");
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            Valve valves[] = null;
            if (pipeline != null)
                valves = pipeline.getValves();
            if (valves != null) {
                for (int i = 0; i < valves.length; i++) {
                    log.trace("  " + valves[i].getInfo());
                }
            }
            log.trace("======================");
        }

        // Make our application available if no problems were encountered
        if (ok)
            context.setConfigured(true);
        else {
            log.error(sm.getString("contextConfig.unavailable"));
            context.setConfigured(false);
        }

    }


    /**
     * Process a "stop" event for this Context.
     */
    protected synchronized void stop() {

        if (log.isTraceEnabled())
            log.trace(sm.getString("contextConfig.stop"));

        int i;

        // Removing children
        Container[] children = context.findChildren();
        for (i = 0; i < children.length; i++) {
            context.removeChild(children[i]);
        }

        // Removing application parameters
/*        ApplicationParameter[] applicationParameters =
            context.findApplicationParameters();
        for (i = 0; i < applicationParameters.length; i++) {
            context.removeApplicationParameter
                (applicationParameters[i].getName());
        }
*/
        // Removing security constraints
        SecurityConstraint[] securityConstraints = context.findConstraints();
        for (i = 0; i < securityConstraints.length; i++) {
            context.removeConstraint(securityConstraints[i]);
        }

        // Removing Ejbs
        /*
        ContextEjb[] contextEjbs = context.findEjbs();
        for (i = 0; i < contextEjbs.length; i++) {
            context.removeEjb(contextEjbs[i].getName());
        }
        */

        // Removing environments
        /*
        ContextEnvironment[] contextEnvironments = context.findEnvironments();
        for (i = 0; i < contextEnvironments.length; i++) {
            context.removeEnvironment(contextEnvironments[i].getName());
        }
        */

        // Removing errors pages
        ErrorPage[] errorPages = context.findErrorPages();
        for (i = 0; i < errorPages.length; i++) {
            context.removeErrorPage(errorPages[i]);
        }

        // Removing filter defs
        FilterDef[] filterDefs = context.findFilterDefs();
        for (i = 0; i < filterDefs.length; i++) {
            context.removeFilterDef(filterDefs[i]);
        }

        // Removing filter maps
        FilterMap[] filterMaps = context.findFilterMaps();
        for (i = 0; i < filterMaps.length; i++) {
            context.removeFilterMap(filterMaps[i]);
        }

        // Removing local ejbs
        /*
        ContextLocalEjb[] contextLocalEjbs = context.findLocalEjbs();
        for (i = 0; i < contextLocalEjbs.length; i++) {
            context.removeLocalEjb(contextLocalEjbs[i].getName());
        }
        */

        // Removing Mime mappings
        String[] mimeMappings = context.findMimeMappings();
        for (i = 0; i < mimeMappings.length; i++) {
            context.removeMimeMapping(mimeMappings[i]);
        }

        // Removing parameters
        String[] parameters = context.findParameters();
        for (i = 0; i < parameters.length; i++) {
            context.removeParameter(parameters[i]);
        }

        // Removing resource env refs
        /*
        String[] resourceEnvRefs = context.findResourceEnvRefs();
        for (i = 0; i < resourceEnvRefs.length; i++) {
            context.removeResourceEnvRef(resourceEnvRefs[i]);
        }
        */

        // Removing resource links
        /*
        ContextResourceLink[] contextResourceLinks =
            context.findResourceLinks();
        for (i = 0; i < contextResourceLinks.length; i++) {
            context.removeResourceLink(contextResourceLinks[i].getName());
        }
        */

        // Removing resources
        /*
        ContextResource[] contextResources = context.findResources();
        for (i = 0; i < contextResources.length; i++) {
            context.removeResource(contextResources[i].getName());
        }
        */

        // Removing sercurity role
        String[] securityRoles = context.findSecurityRoles();
        for (i = 0; i < securityRoles.length; i++) {
            context.removeSecurityRole(securityRoles[i]);
        }

        // Removing servlet mappings
        String[] servletMappings = context.findServletMappings();
        for (i = 0; i < servletMappings.length; i++) {
            context.removeServletMapping(servletMappings[i]);
        }

        // FIXME : Removing status pages

        // Removing taglibs
        String[] taglibs = context.findTaglibs();
        for (i = 0; i < taglibs.length; i++) {
            context.removeTaglib(taglibs[i]);
        }

        // Removing welcome files
        String[] welcomeFiles = context.findWelcomeFiles();
        for (i = 0; i < welcomeFiles.length; i++) {
            context.removeWelcomeFile(welcomeFiles[i]);
        }

        // Removing wrapper lifecycles
        String[] wrapperLifecycles = context.findWrapperLifecycles();
        for (i = 0; i < wrapperLifecycles.length; i++) {
            context.removeWrapperLifecycle(wrapperLifecycles[i]);
        }

        // Removing wrapper listeners
        String[] wrapperListeners = context.findWrapperListeners();
        for (i = 0; i < wrapperListeners.length; i++) {
            context.removeWrapperListener(wrapperListeners[i]);
        }

        ok = true;

    }

    /**
     * Validate the usage of security role names in the web application
     * deployment descriptor.  If any problems are found, issue warning
     * messages (for backwards compatibility) and add the missing roles.
     * (To make these problems fatal instead, simply set the <code>ok</code>
     * instance variable to <code>false</code> as well).
     */
    protected void validateSecurityRoles() {

        // Check role names used in <security-constraint> elements
        SecurityConstraint constraints[] = context.findConstraints();
        for (int i = 0; i < constraints.length; i++) {
            String roles[] = constraints[i].findAuthRoles();
            for (int j = 0; j < roles.length; j++) {
                if (!"*".equals(roles[j]) &&
                    !context.findSecurityRole(roles[j])) {
                    log.info ( sm.getString ("contextConfig.role.auth", 
                                             roles[j],
                                             context.getName()) );
                    context.addSecurityRole(roles[j]);
                }
            }
        }

        // Check role names used in <servlet> elements
        Container wrappers[] = context.findChildren();
        for (int i = 0; i < wrappers.length; i++) {
            Wrapper wrapper = (Wrapper) wrappers[i];
            String runAs = wrapper.getRunAs();
            if ((runAs != null) && !context.findSecurityRole(runAs)) {
                log.info( sm.getString("contextConfig.role.runas", 
                                       runAs,
                                       context.getName()) );
                context.addSecurityRole(runAs);
            }
            String names[] = wrapper.findSecurityReferences();
            for (int j = 0; j < names.length; j++) {
                String link = wrapper.findSecurityReference(names[j]);
                if ((link != null) && !context.findSecurityRole(link)) {
                    log.info( sm.getString("contextConfig.role.link", 
                                           link,
                                           context.getName()) );
                    context.addSecurityRole(link);
                }
            }
        }

    }


    // START GlassFish 2439
    protected class ContextErrorHandler
        implements ErrorHandler {

        public void error(SAXParseException exception) {
            parseException = exception;
        }

        public void fatalError(SAXParseException exception) {
            parseException = exception;
        }

        public void warning(SAXParseException exception) {
            parseException = exception;
        }

    }
    // END GlassFish 2439


} //end of public class

// START PWC 6390776
/**
 * This class is to override log.error method that Digester uses.
 * With log.error call, we don't want the Digester to log the entire stack
 * trace at the default log level. We are interested in only error message. 
 * If log-level is debug, then only stack trace is shown.
 */
class DigesterLogger implements Log {

    public Log log;
    public String contextMsg = null;

    public DigesterLogger(String contextName) {
        log = LogFactory.getLog("com.sun.org.apache.commons.digester.Digester");
        contextMsg = "In context [" + contextName + "]: ";
    }
    
    public void debug(Object message) {
        log.debug(contextMsg + message);
    }

    public void debug(Object message, Throwable t) {
        log.debug(contextMsg + message,t);
    }

    public void error(Object message) {
        log.error(contextMsg + message);
    }

    public void error(Object message, Throwable t) {
        if (log.isDebugEnabled()) {
            log.error(contextMsg + message,t);
        } else {
            log.error(contextMsg + message);
        }
    }

    public void fatal(Object message) {
        log.fatal(contextMsg + message);
    }

    public void fatal(Object message, Throwable t) {
        log.fatal(contextMsg + message,t);
    }

    public void info(Object message) {
        log.info(contextMsg + message);
    }

    public void info(Object message, Throwable t) {
        log.info(contextMsg + message, t);
    }

    public void trace(Object message) {
        log.trace(contextMsg + message);
    }

    public void trace(Object message, Throwable t) {
        log.trace(contextMsg + message, t);
    }

    public void warn(Object message) {
        log.warn(contextMsg + message);
    }

    public void warn(Object message, Throwable t) {
        log.warn(contextMsg + message, t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }
}
// END PWC 6390776
