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

package com.sun.web.security;

import java.util.HashMap;
import java.util.Map;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import java.util.logging.*; 
import com.sun.logging.LogDomains;
import java.util.List;
import java.util.ArrayList;

/** @author JeanFrancois Arcand
 *  @author Harpreet Singh
 */
public class WebSecurityManagerFactory {
    private static Logger logger = 
	Logger.getLogger(LogDomains.SECURITY_LOGGER);
    
    private Map securityManagerPool = new HashMap();
    // stores the context ids to appnames for standalone web apps
    private Map CONTEXT_ID = new HashMap();
    private static WebSecurityManagerFactory factory = null;
        
    private WebSecurityManagerFactory() {
    }
    // returns the singleton instance of WebSecurityManagerFactory
    public static synchronized WebSecurityManagerFactory getInstance(){
        if (factory == null){
	    factory = new WebSecurityManagerFactory();
        }   
        return  factory;
    }
    // generates a webSecurityManager
    public WebSecurityManager newWebSecurityManager(WebBundleDescriptor wbd){
        String contextId = WebSecurityManager.getContextID(wbd);
        String appname = wbd.getApplication().getRegistrationName();

	synchronized (CONTEXT_ID) {
	    List lst = (List)CONTEXT_ID.get(appname);
	    if(lst == null){
		lst = new ArrayList();
		CONTEXT_ID.put(appname, lst);
	    }
	    if (!lst.contains(contextId)) {
		lst.add(contextId);
	    }
	}

        if(logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE, "[Web-Security] Web Security:Creating WebSecurityManager for contextId = "+contextId);
        }

        WebSecurityManager wsManager = getWebSecurityManager(contextId);
	if (wsManager == null) {

	    // we should see if it is safe to do the security manager 
	    // construction within the synchronize block.
	    // for the time being, we will just make sure that we 
	    // synchronize access to the pool.
	    try{
		wsManager = new WebSecurityManager(wbd);
	    } catch (javax.security.jacc.PolicyContextException e){
		logger.log(Level.FINE, "[Web-Security] FATAl Exception. Unable to create WebSecurityManager: " + e.getMessage() );
		throw new RuntimeException(e);
	    }

	    synchronized (securityManagerPool) {
		WebSecurityManager other = 
		    (WebSecurityManager)securityManagerPool.get(contextId);
		if (other == null) {
		    securityManagerPool.put(contextId, wsManager);
		} else {
		    wsManager = other;
		}
	    }
	}
	return wsManager;
    }
        
    public WebSecurityManager getWebSecurityManager(String contextId){
	synchronized (securityManagerPool) {
	    return (WebSecurityManager)securityManagerPool.get(contextId);
	}
    }
    
    public void removeWebSecurityManager(String contextId){
        synchronized (securityManagerPool){
	    securityManagerPool.remove(contextId);
        }
    }

    /**
     * valid for standalone web apps
     */
    public String[] getContextIdsOfApp(String appName){
	synchronized(CONTEXT_ID) {
	    List contextId = (List) CONTEXT_ID.get(appName);
	    if(contextId == null)
		return null;
	    String[] arrayContext = new String[contextId.size()];
	    arrayContext = (String[])contextId.toArray(arrayContext);
	    return arrayContext;
	}
    }

    /**
     * valid for standalone web apps
     */
    public String[] getAndRemoveContextIdForWebAppName(String appName){
	synchronized(CONTEXT_ID) {
	    String [] rvalue = getContextIdsOfApp(appName);
	    CONTEXT_ID.remove(appName);
	    return rvalue;
	}
    }
        
}
