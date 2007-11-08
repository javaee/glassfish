/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.security.auth.message.config;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import java.security.AccessController;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.*;
import javax.security.auth.message.config.*;
import javax.security.auth.message.module.*;
import javax.security.auth.Subject;

/**
 * This interface is implemented by objects that can be used to obtain
 * authentication context configuration objects, that is, 
 * <code>ClientAuthConfig</code> or <code>ServerAuthConfig</code> objects.
 *
 * <p> Authentication context configuration objects serve as sources of 
 * the authentication context objects, that is, <code>ClientAuthContext</code> or
 * <code>ServerAuthContext</code> objects, for a specific message layer
 * and messaging context.
 * 
 * <p> Authentication context objects encapsulate the initialization, 
 * configuration, and invocation of authentication modules, that is,
 * <code>ClientAuthModule</code> or <code>ServerAuthModule</code> objects, for
 * a specific message exchange within a specific message layer and 
 * messaging context.
 * 
 * <p> Callers do not directly operate on authentication modules.
 * Instead, they rely on a ClientAuthContext or ServerAuthContext
 * to manage the invocation of modules. A caller obtains an instance
 * of ClientAuthContext or ServerAuthContext by calling the respective
 * <code>getAuthContext</code> method on a <code>ClientAuthConfig</code>
 * or <code>ServerAuthConfig</code> object obtained from an 
 * AuthConfigProvider.
 *
 * <p> The following represents a typical sequence of calls for obtaining
 * a client authentication context object, and then using it to secure 
 * a request.
 * <ol>
 * <li>AuthConfigProvider provider;
 * <li>ClientAuthConfig config = provider.getClientAuthConfig(layer,appID,cbh);
 * <li>String authContextID = config.getAuthContextID(messageInfo);
 * <li>ClientAuthContext context = config.getAuthContext(authContextID,subject,properties);
 * <li>context.secureRequest(messageInfo,subject);
 * </ol>
 *
* <p> Every implementation of this interface must offer a public,
 * two argument constructor with the following signature:
 * <pre>
 * <code>
 * public AuthConfigProviderImpl(Map properties, AuthConfigFactory factory);
 * </code>
 *</pre>
 * where the properties argument may be null, and where all values and 
 * keys occurring in a non-null properties argument must be of type String.
 * When the factory argument is not null, it indicates that the
 * provider is to self-register at the factory by calling the following
 * method on the factory:
 * <pre>
 * <code>
 * public String 
 * registerConfigProvider(AuthConfigProvider provider, String layer, 
 *                        String appContext, String description);
 * </code>
 * </pre>
 * @version %I%, %G%
 *
 * @see ClientAuthContext
 * @see ServerAuthContext
 * @see AuthConfigFactory
 */
public class ServletAuthConfigProvider implements AuthConfigProvider {

    private static ReentrantReadWriteLock rwLock = 
        new ReentrantReadWriteLock();
    private static Lock rLock = rwLock.readLock();;
    private static Lock wLock = rwLock.writeLock();

    private static HashMap authConfigMap = new HashMap();
    private static HashMap defaultAuthConfigMap;

    private static String HTTP_SERVLET_LAYER = "HttpServlet";
    private static String MANDATORY_KEY = 
        "javax.security.auth.message.MessagePolicy.isMandatory";

    private static String MANDATORY_CONTEXT_ID = "mandatory";
    private static String OPTIONAL_CONTEXT_ID = "optional";

    private static String CONTEXTS_KEY = "AppContextIDs";
    private static String MODULE_KEY = "ServerAuthModule";
    
    private static String defaultModule = null;
    private static Map defaultModuleOptions = null;

    /**
     * initialization properties
     *
     * ServerAuthModule=ClassName
     * AppContextIDs=x,y,z
     *
     */
    public ServletAuthConfigProvider
        (Map properties, AuthConfigFactory factory) throws AuthException {

	if (properties == null) {
	    throw new AuthException("properties required for construction");
	}

	String module = (String) properties.get(MODULE_KEY);
	if (module == null) {
	    throw new AuthException("ServerAuthModule property is required");
	}

	HashMap options = new HashMap(properties);
	options.remove(MODULE_KEY);

	String[] contextID = parseStringValue
	    ((String) properties.get(CONTEXTS_KEY));
	 
	options.remove(CONTEXTS_KEY);

	System.err.println("constructing ServletAuthConfigProvider: " +
			   module);

	if (contextID != null) {
		
	    for (String appContext : contextID) {

		System.err.println("constructing ServletServerAuthConfig: " +
			   appContext);

		if (appContext != null && appContext.length() > 0) {

		    ServerAuthConfig sAC = new ServletServerAuthConfig
			(appContext,module,options);
		    
		    try {
			wLock.lock();
			authConfigMap.put(appContext,sAC);
		    } finally {
			wLock.unlock();
		    }

		    if (factory != null) {
			factory.registerConfigProvider
			    (this,HTTP_SERVLET_LAYER,appContext,module);
		    }
		}
	    }
	} else {
	    // record defaults to handle registration for all appcontexts
	    try {
		wLock.lock();
		defaultModule = module;
		defaultModuleOptions = options;
		defaultAuthConfigMap = new HashMap();
	    } finally {
		wLock.unlock();
	    }
	}
    }

    private static String[] parseStringValue(String value) {
	String[] rvalue = null;
	if (value != null) {
            
	    // removed blank
            String delim = new String(":,;");
	    StringTokenizer tokenizer = new StringTokenizer(value,delim);
	    int count = tokenizer.countTokens();
	    if (count > 0) {
		rvalue = new String[count];
		for (int i = 0; i < count; i++) {
		    rvalue[i] = tokenizer.nextToken();
		}
	    }
	}
	return rvalue;
    }

    public ClientAuthConfig getClientAuthConfig
    (String layer, String appContext, CallbackHandler handler) 
	throws AuthException {
	    throw new AuthException("Not implemented");
    }

    public ServerAuthConfig getServerAuthConfig	
	(String layer, String appContext, CallbackHandler handler) 
	throws AuthException {

	if (!HTTP_SERVLET_LAYER.equals(layer)) {
	    throw new AuthException("Layer Not implemented");
	}
		
	if (handler == null) {
	    throw new AuthException("default handler Not implemented");
	}

	// reuse config for a given layer and appcontext, handler will be 
	// set on first access.

	ServletServerAuthConfig sSAC = null;

	try {
	    rLock.lock();
	    sSAC = (ServletServerAuthConfig) 
		authConfigMap.get(appContext);
	    if (sSAC == null) {
		if (defaultAuthConfigMap != null) {
		    sSAC = (ServletServerAuthConfig) 
			defaultAuthConfigMap.get(appContext);
		}
	    }
	    if (sSAC != null) {
		sSAC.setHandlerIfNotSet(handler);
	    }
	} finally {
	    rLock.unlock();
	}

	if (sSAC == null) {
	    try {
		wLock.lock();
		if (defaultAuthConfigMap != null) {
		    sSAC = (ServletServerAuthConfig) 
			defaultAuthConfigMap.get(appContext);
		}
		if (sSAC == null) {
		    sSAC = new ServletServerAuthConfig
			(appContext,defaultModule,defaultModuleOptions);
		    defaultAuthConfigMap.put(appContext,sSAC);
		}
	    } finally {
		rLock.unlock();
	    }
	    if (sSAC != null) {
		sSAC.setHandlerIfNotSet(handler);
	    }
	}

	if (sSAC == null) {
	    throw new AuthException("context: " + appContext + 
				    " not configured");
	}

	return sSAC;
    }

    public void refresh() {
    }

    static class ServletServerAuthConfig implements ServerAuthConfig {

	static final Class[] PARAMS = { };
	static final Object[] ARGS = { };

	Lock rLockConfig;
	Lock wLockConfig;

	String appContext;
	CallbackHandler cbh;

	ServerAuthModule modules[] = null;

	ServerAuthContext mandatoryContext;
	ServerAuthContext optionalContext;

	Map options;

	static MessagePolicy mandatoryPolicy = new MessagePolicy
	 ( new MessagePolicy.TargetPolicy[] 
	   { new MessagePolicy.TargetPolicy
		 ( (MessagePolicy.Target[]) null, 
		   new ServletProtectionPolicy()) } , true);

	static MessagePolicy optionalPolicy = new MessagePolicy
	 ( new MessagePolicy.TargetPolicy[] 
	   { new MessagePolicy.TargetPolicy
		 ((MessagePolicy.Target[]) null, 
		  new ServletProtectionPolicy()) }, false);
 
	ServletServerAuthConfig (String appContext,
	    final String clazz, Map options) throws AuthException {

	    ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	    rLockConfig = rwLock.readLock();;
	    wLockConfig = rwLock.writeLock();

	    this.appContext = appContext;
	    this.options = options;

	    try {

		modules  = (ServerAuthModule[]) AccessController.doPrivileged

		(new java.security.PrivilegedExceptionAction() {
		    
		    public Object run() throws 

			java.lang.ClassNotFoundException,
			java.lang.NoSuchMethodException,
			java.lang.InstantiationException,
			java.lang.IllegalAccessException,
			java.lang.reflect.InvocationTargetException {

			ClassLoader loader = 
			    Thread.currentThread().getContextClassLoader();

			Class c = Class.forName(clazz, true, loader);

			java.lang.reflect.Constructor constructor =
			    c.getConstructor(PARAMS);

			return new ServerAuthModule[] 
			    { (ServerAuthModule) constructor.newInstance(ARGS),
			      (ServerAuthModule) constructor.newInstance(ARGS)
			    };
		    }
		    
		});
		
	    } catch (java.security.PrivilegedActionException pae) {
		AuthException ae = new AuthException();
		ae.initCause(pae.getCause());
		throw ae;
	    } 

	    System.out.println("created ServletServerAuthConfig: " + 
			       appContext + " " + clazz);

	    mandatoryContext = null;
	    optionalContext = null;
	}

	public ServerAuthContext 
	getAuthContext(String authContextID, Subject serviceSubject, 
		       Map properties) throws AuthException {

	    boolean mandatory = false;
	    if (MANDATORY_CONTEXT_ID.equals(authContextID)) {
		mandatory = true;
	    } else if (!OPTIONAL_CONTEXT_ID.equals(authContextID)) {
		throw new AuthException("invalid AuthContext ID");
	    }

	    ServerAuthContext rvalue = null;

	    try {
		rLockConfig.lock();
		if (mandatory) {
		    rvalue = mandatoryContext;
		} else {
		    rvalue = optionalContext;
		}
	    } finally {
		rLockConfig.unlock();
	    }
	    if (rvalue == null) {
		try {
		    wLockConfig.lock();
		    if (options != null && properties != null) {
			properties = new HashMap(properties);
			properties.putAll(options);
		    }
		    
		    if (mandatory) {
			mandatoryContext = new ServletServerAuthContext
			    (modules[1],mandatoryPolicy,cbh,properties);
			rvalue = mandatoryContext;
		    } else {
			optionalContext = new ServletServerAuthContext
			    (modules[0],optionalPolicy,cbh,properties);
			rvalue = optionalContext;
		    }
		} finally {
		    wLockConfig.unlock();
		}
	    } 
	    return rvalue;
	}

	public String getMessageLayer() {
	    return HTTP_SERVLET_LAYER;
	}

	public String getAppContext() {
	    return this.appContext;
	}
	
	public String getAuthContextID(MessageInfo messageInfo) {
	    if (messageInfo.getMap().containsKey(MANDATORY_KEY)) {
		return MANDATORY_CONTEXT_ID;
	    } else {
		return OPTIONAL_CONTEXT_ID;
	    }
	}

	public void refresh() {
	}

	public boolean isProtected() {
	    return true;
	}

	boolean setHandlerIfNotSet(CallbackHandler handler) {
	    try {
		wLockConfig.lock();
		if (this.cbh == null && handler != null) {
		    this.cbh = handler;
		    return true;
		} else {
		    return false;
		}
	    } finally {
		wLockConfig.unlock();
	    }
	}

	static class ServletProtectionPolicy implements 
	MessagePolicy.ProtectionPolicy {
	
	    ServletProtectionPolicy() {
	    }
	
	    public String getID() {
		return MessagePolicy.ProtectionPolicy.AUTHENTICATE_SENDER;
	    }
	}
    }

    static class ServletServerAuthContext implements ServerAuthContext {

	ServerAuthModule module;

	ServletServerAuthContext (ServerAuthModule module,
				  MessagePolicy requestPolicy,  
				  CallbackHandler cbh,
				  Map options) throws AuthException {

	    module.initialize(requestPolicy,null,cbh,options);
	    this.module = module;
	}

	public AuthStatus validateRequest
	    (MessageInfo messageInfo, Subject clientSubject, 
	     Subject serviceSubject) throws AuthException {
		 return module.validateRequest
		     (messageInfo,clientSubject,serviceSubject);
	}

	public AuthStatus secureResponse
	     (MessageInfo messageInfo, Subject serviceSubject)
	    throws AuthException {
		return module.secureResponse(messageInfo,serviceSubject);
	}

	public void cleanSubject(MessageInfo messageInfo, Subject subject)
	    throws AuthException {
	        module.cleanSubject(messageInfo,subject);
	}
	
    }

}






