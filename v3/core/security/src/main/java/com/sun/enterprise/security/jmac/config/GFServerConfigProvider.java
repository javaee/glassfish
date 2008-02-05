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

package com.sun.enterprise.security.jmac.config;

import java.lang.reflect.Constructor;
import java.security.Principal; 
import java.security.PrivilegedAction; 
import java.security.PrivilegedActionException; 
import java.security.PrivilegedExceptionAction; 

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.config.AuthConfig;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ClientAuthContext;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ClientAuthModule;
import javax.security.auth.message.module.ServerAuthModule;

//V3:Commented import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;

import com.sun.enterprise.security.AppservAccessController;
/*TODO:V3 Commented, Pre-JSR-196 API's, get them into source form and uncomment later
import com.sun.enterprise.security.jauth.AuthParam;
import com.sun.enterprise.security.jauth.AuthPolicy;
import com.sun.enterprise.security.jauth.FailureException;
import com.sun.enterprise.security.jauth.HttpServletAuthParam;
import com.sun.enterprise.security.jauth.PendingException;
import com.sun.enterprise.security.jauth.SOAPAuthParam;*/
import com.sun.enterprise.security.jmac.AuthMessagePolicy;
//TODO:V3 Commented import com.sun.enterprise.security.jmac.PacketMessageInfo;
import com.sun.enterprise.security.jmac.PacketMessageInfo;
import com.sun.logging.LogDomains;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;

/**
 * This class implements the interface AuthConfigProvider.
 * @author  Shing Wai Chan
 * @author  Ronald Monzillo
 */
public class GFServerConfigProvider implements AuthConfigProvider {

    public static final String SOAP = "SOAP";
    public static final String HTTPSERVLET = "HttpServlet";

    protected static final String CLIENT = "client";
    protected static final String SERVER = "server";
    protected static final String MANAGES_SESSIONS_OPTION = "managessessions";

    private static Logger logger = LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
    private static final String DEFAULT_HANDLER_CLASS =
        "com.sun.enterprise.security.jmac.callback.ContainerCallbackHandler";
    private static final String DEFAULT_PARSER_CLASS =
        "com.sun.enterprise.security.jmac.config.ConfigXMLParser";

    // since old api does not have subject in PasswordValdiationCallback,
    // this is for old modules to pass group info back to subject
    private static final ThreadLocal<Subject> subjectLocal = new ThreadLocal<Subject>();

    protected static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected static final Map<String, String> layerDefaultRegisIDMap = new HashMap<String, String>();
    protected static int epoch; 
    protected static String parserClassName = null;
    protected static ConfigParser parser;
    protected static boolean parserInitialized = false;
    protected static AuthConfigFactory slaveFactory = null;
    // keep the slave from visible outside
    protected static AuthConfigProvider slaveProvider = null;

    protected AuthConfigFactory factory = null;

    private GFServerConfigProvider() {
    }

    public GFServerConfigProvider(Map properties, AuthConfigFactory factory) {
        this.factory = factory;
        initializeParser();

        if (factory != null) {
            boolean hasSlaveFactory = false;
            try {
                rwLock.readLock().lock();
                hasSlaveFactory = (slaveFactory != null);
            }  finally {
                rwLock.readLock().unlock();
            }

            if (!hasSlaveFactory) {
                try {
                    rwLock.writeLock().lock();
                    if (slaveFactory == null) {
                        slaveFactory = factory;
                    }
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
        }
           
        boolean hasSlaveProvider = false;
        try {
            rwLock.readLock().lock();
            hasSlaveProvider = (slaveProvider != null);
        }  finally {
            rwLock.readLock().unlock();
        }

        if (!hasSlaveProvider) {
            try {
                rwLock.writeLock().lock();
                if (slaveProvider == null) {
                    slaveProvider = this;
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    private void initializeParser() {
        try {
            rwLock.readLock().lock();
            if (parserInitialized) {
                return;
            }
        } finally {
            rwLock.readLock().unlock();
        }

        try {
            rwLock.writeLock().lock();
            if (!parserInitialized) {
                parserClassName = 
                    System.getProperty("config.parser", DEFAULT_PARSER_CLASS);
                /*TODO:V3 Commented uncomment later: needs rewrite 
                 loadParser(this, factory, null); // null ConfigContext
                 */
                parserInitialized = true;
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Instantiate+initialize module class
     */
    protected static ModuleInfo createModuleInfo(Entry entry,
            CallbackHandler handler, String type, Map properties)
            throws AuthException {
        try {
            // instantiate module using no-arg constructor
            Object newModule = entry.newInstance();

            Map map = properties;
            Map entryOptions = entry.getOptions();

            if (entryOptions != null) {
                if (map == null) {
                    map = new HashMap();
                } else {
                    map = new HashMap(map);
                }
                map.putAll(entryOptions);
            }

            // no doPrivilege at this point, need to revisit
            if (SERVER.equals(type)) {
                if (newModule instanceof ServerAuthModule) {
                    ServerAuthModule sam = (ServerAuthModule)newModule;
                    sam.initialize(entry.getRequestPolicy(),
                        entry.getResponsePolicy(), handler, map);
                } /*TODO:V3 Commented uncomment later
                 else if (newModule instanceof
                        com.sun.enterprise.security.jauth.ServerAuthModule) {
                    com.sun.enterprise.security.jauth.ServerAuthModule sam0 =
                        (com.sun.enterprise.security.jauth.ServerAuthModule)newModule;
                    AuthPolicy requestPolicy =
                            (entry.getRequestPolicy() != null) ?
                            new AuthPolicy(entry.getRequestPolicy()) : null;

                    AuthPolicy responsePolicy =
                            (entry.getResponsePolicy() != null) ?
                            new AuthPolicy(entry.getResponsePolicy()) : null;

                    sam0.initialize(requestPolicy, responsePolicy,
                        handler, map);
                }*/
            } else { // CLIENT
                if (newModule instanceof ClientAuthModule) {
                    ClientAuthModule cam = (ClientAuthModule)newModule;
                    cam.initialize(entry.getRequestPolicy(),
                        entry.getResponsePolicy(), handler, map);
                } /*TODO:V3 Commented uncomment later
                 else if (newModule instanceof
                        com.sun.enterprise.security.jauth.ClientAuthModule) {
                    com.sun.enterprise.security.jauth.ClientAuthModule cam0 =
                        (com.sun.enterprise.security.jauth.ClientAuthModule)newModule;
                    AuthPolicy requestPolicy = 
			new AuthPolicy(entry.getRequestPolicy());

		    AuthPolicy responsePolicy =
			new AuthPolicy(entry.getResponsePolicy());

                    cam0.initialize(requestPolicy,responsePolicy,
                        handler, map);
                }*/
            }

            return new ModuleInfo(newModule, map);
        } catch(Exception e) {
            if (e instanceof AuthException) {
                throw (AuthException)e;
            }
            AuthException ae = new AuthException();
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * Create an object of a given class.
     * @param className
     *
     */
    private static Object createObject(String className) {
        try {
            final String finalClassName = className;

            return AppservAccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    ClassLoader loader =
                        Thread.currentThread().getContextClassLoader();
                    Class c = Class.forName(finalClassName, true, loader);
                    return c.newInstance();
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new RuntimeException(pae.getException());
        }
    }

    protected Entry getEntry(String intercept,
            String id, MessagePolicy requestPolicy,
            MessagePolicy responsePolicy, String type) {

        // get the parsed module config and DD information

        Map configMap;

        try {
            rwLock.readLock().lock();
            configMap = parser.getConfigMap();
        } finally {
            rwLock.readLock().unlock();
        }

        if (configMap == null) {
            return null;
        }
        
        // get the module config info for this intercept

        InterceptEntry intEntry = (InterceptEntry)configMap.get(intercept);
        if (intEntry == null || intEntry.idMap == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("module config has no IDs configured for [" +
                                intercept +
                                "]");
            }
            return null;
        }

        // look up the DD's provider ID in the module config

        IDEntry idEntry = null;
        if (id == null || (idEntry = (IDEntry)intEntry.idMap.get(id)) == null) {

            // either the DD did not specify a provider ID,
            // or the DD-specified provider ID was not found
            // in the module config.
            //
            // in either case, look for a default ID in the module config

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("DD did not specify ID, " +
                                "or DD-specified ID for [" +
                                intercept +
                                "] not found in config -- " +
                                "attempting to look for default ID");
            }

            String defaultID;
            if (CLIENT.equals(type)) {
                defaultID = intEntry.defaultClientID;
            } else {
                defaultID = intEntry.defaultServerID;
            }

            idEntry = (IDEntry)intEntry.idMap.get(defaultID);
            if (idEntry == null) {

                // did not find a default provider ID

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("no default config ID for [" +
                                        intercept +
                                        "]");
                }
                return null;
            }
        }

        // we found the DD provider ID in the module config
        // or we found a default module config

        // check provider-type
        if (idEntry.type.indexOf(type) < 0) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("request type [" +
                                type +
                                "] does not match config type [" +
                                idEntry.type +
                                "]");
            }
            return null;
        }

        // check whether a policy is set
        MessagePolicy reqP =
            (requestPolicy != null || responsePolicy != null) ?
            requestPolicy : 
            idEntry.requestPolicy;  //default;        

        MessagePolicy respP =
            (requestPolicy != null || responsePolicy != null) ?
            responsePolicy : 
            idEntry.responsePolicy;  //default;        

        // optimization: if policy was not set, return null
        if (reqP == null && respP == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("no policy applies");
            }
            return null;
        }

        // return the configured modules with the correct policies

        Entry entry = new Entry(idEntry.moduleClassName,
                reqP, respP, idEntry.options);
            
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getEntry for: " + intercept + " -- " + id + 
                    "\n    module class: " + entry.moduleClassName +
                    "\n    options: " + entry.options +
                    "\n    request policy: " + entry.requestPolicy +
                    "\n    response policy: " + entry.responsePolicy);
        }

        return entry;
    }

    /**
     * Class representing a single AuthModule entry configured
     * for an ID, interception point, and stack.
     *
     * <p> This class also provides a way for a caller to obtain
     * an instance of the module listed in the entry by invoking
     * the <code>newInstance</code> method.
     */
    static class Entry {

        // for loading modules
        private static final Class[] PARAMS = { };
        private static final Object[] ARGS = { };

        private String moduleClassName;
        private MessagePolicy requestPolicy;
        private MessagePolicy responsePolicy;
        private Map options;
        Object module;        // convenience location to store instance -
                        // package private for AuthContext

        /**
         * Construct a ConfigFile entry.
         *
         * <p> An entry encapsulates a single module and its related
         * information.
         *
         * @param moduleClassName the module class name
         * @param requestPolicy the request policy assigned to the module
         *                listed in this entry, which may be null.
         *
         * @param responsePolicy the response policy assigned to the module
         *                listed in this entry, which may be null.
         *
         * @param moduleClass the fully qualified class name of the module.
         *
         * @param options the options configured for this module.
         */
        Entry(String moduleClassName, MessagePolicy requestPolicy,
                MessagePolicy responsePolicy, Map options) {
            this.moduleClassName = moduleClassName;
            this.requestPolicy = requestPolicy;
            this.responsePolicy = responsePolicy;
            this.options = options;
        }

        /**
         * Return the request policy assigned to this module.
         *
         * @return the policy, which may be null.
         */
        MessagePolicy getRequestPolicy() {
            return requestPolicy;
        }

        /**
         * Return the response policy assigned to this module.
         *
         * @return the policy, which may be null.
         */
        MessagePolicy getResponsePolicy() {
            return responsePolicy;
        }

        String getModuleClassName() {
            return moduleClassName;
        }

        Map getOptions() {
            return options;
        }

        /**
         * Return a new instance of the module contained in this entry.
         *
         * <p> The default implementation of this method attempts
         * to invoke the default no-args constructor of the module class.
         * This method may be overridden if a different constructor
         * should be invoked.
         *
         * @return a new instance of the module contained in this entry.
         *
         * @exception AuthException if the instantiation failed.
         */
        Object newInstance() throws AuthException {
            try {
                final ClassLoader finalLoader= getClassLoader();
                Class c = Class.forName(moduleClassName,
                                        true,
                                        finalLoader);
                Constructor constructor = c.getConstructor(PARAMS);
                return constructor.newInstance(ARGS);
            } catch (Exception e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING,
                        "jmac.provider_unable_to_load_authmodule",
                        new String [] { moduleClassName, e.toString() });
                }

                AuthException ae = new AuthException();
                ae.initCause(e);
                throw ae;
            }
        }
    }

    static class InterceptEntry {
        String defaultClientID;
        String defaultServerID;
        HashMap idMap;

        InterceptEntry(String defaultClientID,
                String defaultServerID, HashMap idMap) {
            this.defaultClientID = defaultClientID;
            this.defaultServerID = defaultServerID;
            this.idMap = idMap;
        }
    }

    /**
     * parsed ID entry
     */
    static class IDEntry {
        private String type;  // provider type (client, server, client-server)
        private String moduleClassName;
        private MessagePolicy requestPolicy;
        private MessagePolicy responsePolicy;
        private Map options;

        IDEntry(String type, String moduleClassName,
                MessagePolicy requestPolicy,
                MessagePolicy responsePolicy,
                Map options) {
            this.type = type;
            this.moduleClassName = moduleClassName;
            this.requestPolicy = requestPolicy;
            this.responsePolicy = responsePolicy;
            this.options = options;
        }
    }

    /**
     * A data object contains module object and the corresponding map.
     */
    protected static class ModuleInfo {
        private Object module;
        private Map map;

        ModuleInfo(Object module, Map map) {
            this.module = module;
            this.map = map;
        }

        Object getModule() {
            return module;
        }

        Map getMap() {
            return map;
        }   
    }

    /**
     * Get an instance of ClientAuthConfig from this provider.
     *
     * <p> The implementation of this method returns a ClientAuthConfig
     * instance that describes the configuration of ClientAuthModules
     * at a given message layer, and for use in an identified application
     * context.
     *
     * @param layer a String identifying the message layer
     *                for the returned ClientAuthConfig object. 
     *          This argument must not be null.
     *
     * @param appContext a String that identifies the messaging context 
     *          for the returned ClientAuthConfig object.
     *          This argument must not be null.
     *
     * @param handler a CallbackHandler to be passed to the ClientAuthModules
     *                encapsulated by ClientAuthContext objects derived from the
     *                returned ClientAuthConfig. This argument may be null,
     *                in which case the implementation may assign a default handler
     *                to the configuration. 
     *
     * @return a ClientAuthConfig Object that describes the configuration
     *                of ClientAuthModules at the message layer and messaging context
     *          identified by the layer and appContext arguments.
     *                This method does not return null.
     *
     * @exception AuthException if this provider does not support the 
     *          assignment of a default CallbackHandler to the returned 
     *          ClientAuthConfig.
     *
     * @exception SecurityException if the caller does not have permission
     *                to retrieve the configuration.
     *
     * The CallbackHandler assigned to the configuration must support 
     * the Callback objects required to be supported by the profile of this
     * specification being followed by the messaging runtime. 
     * The CallbackHandler instance must be initialized with any application 
     * context needed to process the required callbacks 
     * on behalf of the corresponding application.
     */
    public ClientAuthConfig getClientAuthConfig
            (String layer, String appContext, CallbackHandler handler) 
            throws AuthException {
        return new GFClientAuthConfig(this, layer, appContext, handler);
    }

    /**
     * Get an instance of ServerAuthConfig from this provider.
     *
     * <p> The implementation of this method returns a ServerAuthConfig
     * instance that describes the configuration of ServerAuthModules
     * at a given message layer, and for a particular application context.
     *
     * @param layer a String identifying the message layer
     *                for the returned ServerAuthConfig object.
     *          This argument must not be null.
     *
     * @param appContext a String that identifies the messaging context 
     *          for the returned ServerAuthConfig object.
     *          This argument must not be null.
     *
     * @param handler a CallbackHandler to be passed to the ServerAuthModules
     *                encapsulated by ServerAuthContext objects derived from the
     *                returned ServerAuthConfig. This argument may be null,
     *                in which case the implementation may assign a default handler
     *                to the configuration.
     *
     * @return a ServerAuthConfig Object that describes the configuration
     *                of ServerAuthModules at a given message layer,
     *                and for a particular application context. 
     *          This method does not return null.
     *
     * @exception AuthException if this provider does not support the 
     *          assignment of a default CallbackHandler to the returned
     *          ServerAuthConfig.
     *
     * @exception SecurityException if the caller does not have permission
     *                to retrieve the configuration.
     * <p>
     * The CallbackHandler assigned to the configuration must support 
     * the Callback objects required to be supported by the profile of this
     * specification being followed by the messaging runtime. 
     * The CallbackHandler instance must be initialized with any application 
     * context needed to process the required callbacks 
     * on behalf of the corresponding application.
     */
    public ServerAuthConfig getServerAuthConfig        
            (String layer, String appContext, CallbackHandler handler)
            throws AuthException {
        return new GFServerAuthConfig(this, layer, appContext, handler);
    }


    /**
     * Causes a dynamic configuration provider to update its internal 
     * state such that any resulting change to its state is reflected in
     * the corresponding authentication context configuration objects 
     * previously created by the provider within the current process context. 
     *
     * @exception AuthException if an error occured during the refresh.
     *
     * @exception SecurityException if the caller does not have permission
     *                to refresh the provider.
     */
    
    public void refresh() {
       /*TODO:V3 Commented uncomment later
        loadParser(this, factory, null); // null ConfigContext
        */
    }
    /*TODO:V3 Commented uncomment later: needs rewrite
    public static void loadConfigContext(ConfigContext configContext) {
        AuthConfigFactory aFactory = slaveFactory;
        if (aFactory == null) {
            aFactory = AuthConfigFactory.getFactory();
        }
        loadParser(slaveProvider, slaveFactory, configContext);
    }

    
    protected static void loadParser(AuthConfigProvider aProvider, 
            AuthConfigFactory aFactory, ConfigContext configContext) {
        try {
            rwLock.writeLock().lock();
            ConfigParser nextParser;
            int next = epoch + 1;
            nextParser = (ConfigParser)createObject(parserClassName);
            nextParser.initialize(configContext);

            if (aFactory != null && aProvider != null) {
                Set<String> layerSet = nextParser.getLayersWithDefault();
                for (String layer : layerDefaultRegisIDMap.keySet()) {
                    if (!layerSet.contains(layer)) {
                        String regisID = layerDefaultRegisIDMap.remove(layer);
                        aFactory.removeRegistration(regisID);
                    }
                }

                for (String layer : layerSet) {
                    if (!layerDefaultRegisIDMap.containsKey(layer)) {
                        String regisID = aFactory.registerConfigProvider
         		       (aProvider, layer, null,
 		           "GFServerConfigProvider: self registration");
                        layerDefaultRegisIDMap.put(layer, regisID);
                    }
                }
            }
            epoch = (next == 0 ? 1 : next);
            parser = nextParser;
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }*/

    protected static ClassLoader getClassLoader() {
        final ClassLoader rvalue = (ClassLoader)AppservAccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });

        return rvalue;
    }

    // for old API
    public static void setValidateRequestSubject(Subject subject) {
        subjectLocal.set(subject);
    }

    class GFAuthConfig implements AuthConfig {
        protected AuthConfigProvider provider = null;
        protected String layer = null;
        protected String appContext = null;
        protected CallbackHandler handler = null;
        protected String type = null;
        protected String providerID = null;
        protected boolean init = false;
        protected boolean onePolicy = false;
        protected boolean newHandler = false;
	protected MessageSecurityBindingDescriptor binding = null;
        protected SunWebApp sunWebApp = null;

        protected GFAuthConfig(AuthConfigProvider provider,
                String layer, String appContext, 
                CallbackHandler handler, String type) {
            this.provider = provider;
            this.layer = layer;
            this.appContext = appContext;
            this.type = type;
            if (handler == null) {
                handler = AuthMessagePolicy.getDefaultCallbackHandler();
		this.newHandler = true;
            }
            this.handler = handler;
        }

        /**
         * Get the message layer name of this authentication context configuration 
         * object.
         *
         * @return the message layer name of this configuration object, or null if 
         * the configuration object pertains to an unspecified message layer.
         */
        public String getMessageLayer() {
            return layer;
        }

        /**
         * Get the application context identifier of this authentication 
         * context configuration object.
         *
         * @return the String identifying the application context of this
         * configuration object or null if the configuration object pertains
         * to an unspecified application context.
         */
        public String getAppContext() {
            return appContext;
        }

	/**
	 * Get the authentication context identifier corresponding to the
	 * request and response objects encapsulated in messageInfo.
	 *
	 * @param messageInfo a contextual Object that encapsulates the
	 *          client request and server response objects.
	 *
	 * @return the authentication context identifier corresponding to the 
	 *          encapsulated request and response objects, or null.
	 *
	 * @throws IllegalArgumentException if the type of the message
	 * objects incorporated in messageInfo are not compatible with
	 * the message types supported by this 
	 * authentication context configuration object.
	 */
        public String getAuthContextID(MessageInfo messageInfo) {
            //XXX temporary, may need to re-structure the code
            if (GFServerConfigProvider.SOAP.equals(layer)) {
                // make this more efficient by operating on packet 
                String rvalue = null;
                if (messageInfo instanceof PacketMessageInfo) {
                    PacketMessageInfo pmi = (PacketMessageInfo) messageInfo;
                    Packet p = (Packet) pmi.getRequestPacket();
                    if (p != null) {
                        Message m = p.getMessage();
                        if (m != null) {
                            WSDLPort port = 
                                (WSDLPort) messageInfo.getMap().get("WSDL_MODEL");
                            if (port != null) {
                                WSDLBoundOperation w = m.getOperation(port);
                                if (w != null) {
                                    QName n = w.getName();
                                    if (n != null) {
                                        rvalue = n.getLocalPart();
                                    }
                                }
                            }
                        }
                    }
                    return rvalue;
                } else {
                    // make this more efficient by operating on packet 
                    return getOpName((SOAPMessage)messageInfo.getRequestMessage());
                }
            } else if (GFServerConfigProvider.HTTPSERVLET.equals(layer)) {
                String isMandatoryStr =
                    (String)messageInfo.getMap().get(HttpServletConstants.IS_MANDATORY);
                return Boolean.valueOf(isMandatoryStr).toString();
            } else {
                return null;
            }
        }


        // we should be able to replace the following with a method on packet
        private String getOpName(SOAPMessage message) {
            if (message == null) {
                return null;
            }

            String rvalue = null;

            // first look for a SOAPAction header. 
            // this is what .net uses to identify the operation

            MimeHeaders headers = message.getMimeHeaders();
            if (headers != null) {
                String[] actions = headers.getHeader("SOAPAction");
                if (actions != null && actions.length > 0) {
                    rvalue = actions[0];
                    if (rvalue != null && rvalue.equals("\"\"")) {
                        rvalue = null;
                    }
                }
            } 

            // if that doesn't work then we default to trying the name
            // of the first child element of the SOAP envelope.

            if (rvalue == null) {
                Name name = getName(message);
                if (name != null) {
                    rvalue = name.getLocalName();
                }
            }
        
            return rvalue;
        }

        private Name getName(SOAPMessage message) {
            Name rvalue = null;
            SOAPPart soap = message.getSOAPPart();
            if (soap != null) {
                try {
                    SOAPEnvelope envelope = soap.getEnvelope(); 
                    if (envelope != null) {
                        SOAPBody body = envelope.getBody();
                        if (body != null) {
                            Iterator it = body.getChildElements();
                            while (it.hasNext()) {
                                Object o = it.next();
                                if (o instanceof SOAPElement) {
                                    rvalue = ((SOAPElement) o).getElementName(); 
                                    break;
                                }
                            }
                        }
                    }
                } catch (SOAPException se) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "WSS: Unable to get SOAP envelope",
                                   se);
                    }
                }
            }
        
            return rvalue;
        }

        /**
         * Causes a dynamic anthentication context configuration object to 
         * update the internal state that it uses to process calls to its
         * <code>getAuthContext</code> method.
         *
         * @exception AuthException if an error occured during the update.
         *
         * @exception SecurityException if the caller does not have permission
         *                to refresh the configuration object.
         */
        public void refresh() {
            /*TODO:V3 Commented uncomment later
            loadParser(provider, factory, null); // null configContext
             */
        }

	/**
	 * Used to determine whether or not the <code>getAuthContext</code> 
	 * method of the authentication context configuration will return null for
	 * all possible values of authentication context identifier.
	 *
	 * @return false when <code>getAuthContext</code> will return null for
	 *        all possible values of authentication context identifier. 
	 *        Otherwise, this method returns true.
	 */
	public boolean isProtected() {
	    // XXX TBD
	    return true;
	}

        /*TODO:V3 Commented uncomment later
        protected AuthParam getAuthParam(MessageInfo info) 
	    throws AuthException {
            if (GFServerConfigProvider.SOAP.equals(layer)) {
		if (info instanceof PacketMessageInfo) {
		    // construct AuthParam with Packets if they are available
		    return ((PacketMessageInfo)info).getSOAPAuthParam();
		} else {
		    return new SOAPAuthParam((SOAPMessage)
					     info.getRequestMessage(),
					     (SOAPMessage)
					     info.getResponseMessage());
		}
            } else if (GFServerConfigProvider.HTTPSERVLET.equals(layer)) {
                return new HttpServletAuthParam(info);
            } else {
                throw new AuthException();
            }
        }*/

        CallbackHandler getCallbackHandler() {
            return handler;
        }

        protected ModuleInfo getModuleInfo(String authContextID, Map properties) 
	    throws AuthException {
            if (!init) {
                initialize(properties);
            }

            MessagePolicy[] policies = null;

	    if (GFServerConfigProvider.SOAP.equals(layer)) {

		policies = AuthMessagePolicy.getSOAPPolicies
		    (binding, authContextID, onePolicy);

	    } else {
		policies = AuthMessagePolicy.getHttpServletPolicies
		    (authContextID);
            
	    }

            MessagePolicy requestPolicy = policies[0];
            MessagePolicy responsePolicy = policies[1];

            Entry entry = getEntry(layer, providerID,
                    requestPolicy, responsePolicy, type);

            return (entry != null)?
                    createModuleInfo(entry, handler, type, properties) : null;
        }

        // lazy initialize this as SunWebApp is not available in
        // RealmAdapter creation
        private void initialize(Map properties) {
            if (!init) {
		if (GFServerConfigProvider.SOAP.equals(layer)) {

		    binding = AuthMessagePolicy.getMessageSecurityBinding
			(layer,properties);

		    providerID = AuthMessagePolicy.getProviderID(binding);
		    onePolicy = AuthMessagePolicy.oneSOAPPolicy(binding);

		} else {
                    sunWebApp = AuthMessagePolicy.getSunWebApp(properties);

                    providerID = AuthMessagePolicy.getProviderID(sunWebApp);
		    onePolicy = true;
		}

                // handlerContext need to be explictly set by caller
                init = true;
            }
        }
    }

    class GFServerAuthConfig extends GFAuthConfig implements ServerAuthConfig {

        protected GFServerAuthConfig(AuthConfigProvider provider,
                String layer, String appContext,
                CallbackHandler handler) {
            super(provider, layer, appContext, handler, SERVER);
        }

        public ServerAuthContext getAuthContext(
                String authContextID, Subject serviceSubject, Map properties) 
                throws AuthException {
            ServerAuthContext serverAuthContext = null;
            ModuleInfo moduleInfo = getModuleInfo(authContextID,properties);

            if (moduleInfo != null && moduleInfo.getModule() != null) {
                Object moduleObj = moduleInfo.getModule();
                Map map = moduleInfo.getMap();
                if (moduleObj instanceof ServerAuthModule) {
                    serverAuthContext = new GFServerAuthContext(this,
                            (ServerAuthModule)moduleObj, map);
                } /*TODO:V3 Commented uncomment laterelse {
                    serverAuthContext = new GFServerAuthContext(this,
                        (com.sun.enterprise.security.jauth.ServerAuthModule)
                        moduleObj, map);
                }*/
            }

            return serverAuthContext;
        }
    }

    class GFClientAuthConfig extends GFAuthConfig implements ClientAuthConfig {

        protected GFClientAuthConfig(AuthConfigProvider provider,
                String layer, String appContext,
                CallbackHandler handler) {
            super(provider, layer, appContext, handler, CLIENT);
        }

        public ClientAuthContext getAuthContext(String authContextID,
                Subject clientSubject, Map properties) 
                throws AuthException {
            ClientAuthContext clientAuthContext = null;
            ModuleInfo moduleInfo = getModuleInfo(authContextID, properties);

            if (moduleInfo != null && moduleInfo.getModule() != null ) {
                Object moduleObj = moduleInfo.getModule();
                Map map = moduleInfo.getMap();
                if (moduleObj instanceof ClientAuthModule) {
                    clientAuthContext = new GFClientAuthContext(this,
                            (ClientAuthModule)moduleObj, map);
                }/*TODO:V3 Commented, uncomment later
                 else {
                    clientAuthContext = new GFClientAuthContext(this,
                        (com.sun.enterprise.security.jauth.ClientAuthModule)
                        moduleObj, map);
                }*/
            }

            return clientAuthContext;
        }
    }

    static protected class GFServerAuthContext implements ServerAuthContext {

        private GFServerAuthConfig config;
        private ServerAuthModule module;
        /*TODO:V3 Commented, uncomment later
        private com.sun.enterprise.security.jauth.ServerAuthModule oldModule;
        */
        private Map map;
        boolean managesSession = false;

        protected GFServerAuthContext(GFServerAuthConfig config, 
                                      ServerAuthModule module, Map map) {
            this.config = config;
            this.module = module;
            //TODO:V3 commented this.oldModule = null;
            this.map = map;
        }
  
        /*TODO:V3 Commented uncomment later
        protected GFServerAuthContext(GFServerAuthConfig config, 
                com.sun.enterprise.security.jauth.ServerAuthModule module,
                Map map) {
            this.config = config;
            this.module = null;
            this.oldModule = module;
            this.map = map;
            if (map != null) {
                String msStr = (String)map.get(
                        GFServerConfigProvider.MANAGES_SESSIONS_OPTION);
                if (msStr != null) {
                    managesSession = Boolean.valueOf(msStr);
                }
            }
        }*/

	// for old modules
	private static void setCallerPrincipals(final Subject s,
                final CallbackHandler handler, final Subject pvcSubject)
                throws AuthException {
            if (handler != null) { // handler should be non-null
                try {
                    AppservAccessController.doPrivileged
                        (new PrivilegedExceptionAction() {
                            public Object run() throws Exception {
                                Set<Principal> ps = s.getPrincipals();
                                Iterator<Principal> it = ps.iterator();
                                if (ps == null || ps.isEmpty()) {
                                    return null ;
                                } 

                                Callback[] callbacks = new Callback[] {
                                    new CallerPrincipalCallback(
                                    s, it.next().getName()) };
                                if (pvcSubject != null) {
                                    s.getPrincipals().addAll(
                                            pvcSubject.getPrincipals());
                                }
                                handler.handle(callbacks);
                                return null;
                            }
                        });
                } catch(PrivilegedActionException pae) {
                    Throwable cause = pae.getCause();
                    AuthException aex = new AuthException();
                    aex.initCause(cause);
                    throw aex;
                }
            }
	}

        public AuthStatus validateRequest(MessageInfo messageInfo,
                Subject clientSubject, Subject serviceSubject) 
                throws AuthException {
            if (module != null) {
                return module.validateRequest
                    (messageInfo, clientSubject, serviceSubject);
            }/*TODO:V3 Commented uncomment later
             else if (oldModule != null) {
                try {
                    subjectLocal.remove();
                    oldModule.validateRequest(config.getAuthParam(messageInfo),
                            clientSubject,
                            messageInfo.getMap());
		    setCallerPrincipals(clientSubject,
                            config.getCallbackHandler(), subjectLocal.get());
                    if (!managesSession &&
                            GFServerConfigProvider.HTTPSERVLET.equals(
                            config.getMessageLayer())) {
                        messageInfo.getMap().put(
                            HttpServletConstants.REGISTER_WITH_AUTHENTICATOR,
                            Boolean.TRUE.toString());
                    }
                    return AuthStatus.SUCCESS;
                } catch(PendingException pe) {
                    return AuthStatus.SEND_CONTINUE;
                } catch(FailureException fe) {
                    return AuthStatus.SEND_FAILURE;
                } finally {
                    subjectLocal.remove();
                }
            }*/ else {
                throw new AuthException();
            }
        }

        public AuthStatus secureResponse(MessageInfo messageInfo,
                Subject serviceSubject) throws AuthException {
            if (module != null) {
                return module.secureResponse(messageInfo, serviceSubject);
            } /*TODO:V3 Commented uncomment later
             else if (oldModule != null) {
                oldModule.secureResponse(config.getAuthParam(messageInfo),
                        serviceSubject,
                        messageInfo.getMap());
                return AuthStatus.SEND_SUCCESS;
            }*/ else {
                throw new AuthException();
            }
        }

        public void cleanSubject(MessageInfo messageInfo, Subject subject)
                throws AuthException {
            if (module != null) {
                module.cleanSubject(messageInfo, subject);
            } /*TODO:V3 Commented uncomment later
             else if (oldModule != null) {
                oldModule.disposeSubject(subject, messageInfo.getMap());
            }*/ else {
                 throw new AuthException();
            }
        }
    }

    static protected class GFClientAuthContext implements ClientAuthContext {

        private GFClientAuthConfig config;
        private ClientAuthModule module;
        /*TODO:V3 Commented, uncomment later 
        private com.sun.enterprise.security.jauth.ClientAuthModule oldModule;
         */
        private Map map;

        protected GFClientAuthContext(GFClientAuthConfig config, 
                                      ClientAuthModule module, Map map) {
            this.config = config;
            this.module = module;
            //TODO:V3 commented this.oldModule = null;
            this.map = map;
        }

        /*TODO:V3 Commented uncomment later
        protected GFClientAuthContext(GFClientAuthConfig config, 
                com.sun.enterprise.security.jauth.ClientAuthModule module,
                Map map) {
            this.config = config;
            this.module = null;
            this.oldModule = module;
            this.map = map;
        }*/

        public AuthStatus secureRequest(MessageInfo messageInfo,
                Subject clientSubject) throws AuthException {
            if (module != null) {
                return module.secureRequest(messageInfo, clientSubject);
            }/*TODO:V3 Commented uncomment later
             else if (oldModule != null) {
                oldModule.secureRequest(config.getAuthParam(messageInfo),
                        clientSubject,
                        messageInfo.getMap());
                return AuthStatus.SEND_SUCCESS;
            }*/ else {
                throw new AuthException();
            }
        }

        public AuthStatus validateResponse(MessageInfo messageInfo,
                Subject clientSubject, Subject serviceSubject)
                throws AuthException {
            if (module != null) {
                return module.validateResponse(messageInfo, clientSubject,
                        serviceSubject);
            } /*TODO:V3 Commented uncomment later
             else if (oldModule != null) {
                oldModule.validateResponse(config.getAuthParam(messageInfo),
                        clientSubject, messageInfo.getMap());
                return AuthStatus.SUCCESS;
            }*/ else {
                throw new AuthException();
            }
        }

        public void cleanSubject(MessageInfo messageInfo, Subject subject)
                throws AuthException {
            if (module != null) {
                module.cleanSubject(messageInfo, subject);
            }/*TODO:V3 Commented uncomment later
              else if (oldModule != null) {
                oldModule.disposeSubject(subject, messageInfo.getMap());
            }*/ else {
                 throw new AuthException();
            }
        }
    }
}
