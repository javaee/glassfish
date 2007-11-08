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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import com.sun.enterprise.Switch;
import com.sun.enterprise.NamingManager;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.InjectionManager;
import com.sun.enterprise.util.InjectionManagerImpl;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.J2EETransactionManager;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.naming.NamingManagerImpl;
import com.sun.enterprise.util.InvocationManagerImpl;
import com.sun.enterprise.distributedtx.J2EETransactionManagerImpl;
import com.sun.enterprise.security.auth.LoginContextDriver;
import com.sun.enterprise.iiop.PEORBConfigurator;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import java.util.Properties;
/**
 * This is the application client container. It performs tasks such as
 * user login and other initialization based on the deployment descriptor
 * information.
 * @author Vivek Nagar
 * @author Harpreet Singh
 */
public class AppContainer
{
    public static final int USERNAME_PASSWORD = 1;
    public static final int CERTIFICATE = 2;
    // harry - added for LoginContextDriver access
    public static final int ALL = 3;
    private static final boolean debug = false;

    private ApplicationClientDescriptor descriptor = null;
    private Switch sw = null;
    private NamingManager nm = null;

    // the handler should be defined - it will be used by the J2EEKeyManager
    // to pick up the certificate alias when performing SSL handshake during
    // lazy authentication.
    private static CallbackHandler handler = null;
    private boolean guiAuth = true;

    private static Logger _logger = LogDomains.getLogger(LogDomains.ACC_LOGGER);

    /**
     * The constructor takes the application client descriptor as an argument.
     */
    public AppContainer(ApplicationClientDescriptor desc, boolean useGuiAuth) 
    {
	sw = Switch.getSwitch();
	sw.setContainerType(Switch.APPCLIENT_CONTAINER);
        // Inform the JTS that this is a APPCLIENT_CONTAINER . This is required
        // as JTS can not use Switch interface as it creates a circular dependency
        // Configuration.setAsAppClientConatiner();
	descriptor = desc;
        guiAuth = useGuiAuth;
    }

    /**
     * This returns the CallbackHandler for the AppContainer.
     * @return handler
     */
    public static CallbackHandler getCallbackHandler() {
        return handler;
    }

    /**
     * This is called by main before the actual main of the application
     * is invoked. It initializes the container and performs login for
     * the user.
     * @return the main class of the application.
     */
    public String preInvoke(Properties props) throws Exception {
        return preInvoke(initializeNaming(props), Thread.currentThread().getContextClassLoader());
    }
    
    /**
     * This is called by main before the actual main of the application
     * is invoked. It initializes the container and performs login for
     * the user.
     * @return the main class of the application.
     */
    public String preInvoke(InitialContext ic, ClassLoader loader) throws Exception
    {
	ComponentInvocation ci = new ComponentInvocation(null, this);

	sw.getInvocationManager().preInvoke(ci);
 	
	// Do security setup
	String callbackHandler = descriptor.getCallbackHandler();
        
	_logger.fine("Callback Handler:" + callbackHandler);
        
	initializeCallbackHandler(callbackHandler, loader);
	
        performUserLogin();

	String mainClass = descriptor.getMainClassName();
	sw.setDescriptorFor(this, descriptor);
        
        nm = Switch.getSwitch().getNamingManager();
	nm.bindObjects(descriptor);

	return mainClass;
    }

    /**
     * This is called by the main after the real main of the application
     * is invoked. Performs cleanup.
     */
    public void postInvoke() throws Exception
    {
	nm.unbindObjects(descriptor);
    }

    /*
     *Prepares user authentication.
     */
    public void performUserLogin() {
	/* Login the user. */
	// eager authentication!
	boolean doLogin = 
	    Boolean.valueOf(System.getProperty("startup.login", "false")).booleanValue();
	if(doLogin) {
            _logger.info("acc.init_login");
	    String loginMech = System.getProperty(
		"com.sun.enterprise.loginMech", "password");
	    if(loginMech.equalsIgnoreCase("ssl")) {
		LoginContextDriver.doClientLogin(CERTIFICATE, handler);
	    } else if(loginMech.equalsIgnoreCase("all")) {
		LoginContextDriver.doClientLogin(ALL, handler);
	    } else {
		LoginContextDriver.doClientLogin(USERNAME_PASSWORD, handler);
	    }
	}
    }

    /**
     * Creates the InitialContext, initializes the ORB's transaction service, 
     * and creates and establishes the switch's transaction manager.
     * @param iiopProperties Properties object used in creating the InitialContext
     * @return the InitialContext created
     * @throws NamingException for errors creating the InitialContext
     */
    public static InitialContext initializeNaming(Properties iiopProperties) throws NamingException {
        InitialContext result = new InitialContext(iiopProperties);
        
        Switch sw = Switch.getSwitch();
        
	InvocationManager im = new InvocationManagerImpl();
	sw.setInvocationManager(im);

	// Initialize Transaction service. By now the ORB must have
	// been created during the new InitialContext() call using
	// the ORBManager
	PEORBConfigurator.initTransactionService(null, new Properties() );
	
	/* Create the transaction manager and set it in the switch. */
	// J2EETransactionManager tm = new J2EETransactionManagerImpl();
	J2EETransactionManager tm = 
		J2EETransactionManagerImpl.createTransactionManager();
	sw.setTransactionManager(tm);

        /* Create the naming manager and set it in the switch. */
	sw.setNamingManager(new NamingManagerImpl(result));

        // Create the Injection Manager and set it on the switch.
        InjectionManager injectionMgr = new InjectionManagerImpl();
        sw.setInjectionManager(injectionMgr);               

        return result;
    }
    
    /**
     * Initialize the JAAS login modules.
     * One login module per realm (default and certificate).
     * @param the JAAS callback handler class.
     * @exception LoginException if there was an error.
     */
    private void initializeCallbackHandler(String callbackHandler, ClassLoader loader){
	Class handlerClass = null;
	handler = null;
	
	try {
	    if(callbackHandler != null) {
	        handlerClass = Class.forName(callbackHandler, true, loader);
		handler = (CallbackHandler) handlerClass.newInstance();
	    } else {
	        handler = new com.sun.enterprise.security.auth.login.LoginCallbackHandler(guiAuth);
	    }
	} catch(Exception e) {
            _logger.log(Level.FINE, "Could not instantiate specified " +
                         "callback handler:" + e.getMessage(), e);
            _logger.info("acc.using_default_callback");
	    handler = new com.sun.enterprise.security.auth.login.LoginCallbackHandler(guiAuth);
	} 
    }
}

