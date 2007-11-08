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
package com.sun.enterprise.security;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.Security;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextHandler;

import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.ServerLifecycleImpl;
import com.sun.enterprise.J2EESecurityManager;
import com.sun.enterprise.security.PolicyLoader;
import com.sun.enterprise.security.RealmConfig;
import com.sun.enterprise.security.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.audit.AuditManagerFactory;
import com.sun.enterprise.security.jmac.config.GFAuthConfigFactory;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;

/**
 * This class extends default implementation of ServerLifecycle interface.
 * It provides security initialization and setup for the server.
 * @author  Shing Wai Chan
 */
public class SecurityLifecycle extends ServerLifecycleImpl {
    private static final Logger _logger = LogDomains.getLogger(LogDomains.SECURITY_LOGGER);

    public SecurityLifecycle() {
	try {
            // security manager is set here so that it can be accessed from
            // other lifecycles, like PEWebContainer

            SecurityManager secMgr = System.getSecurityManager();
            if (secMgr != null &&
                    !(J2EESecurityManager.class.equals(secMgr.getClass()))) {
                J2EESecurityManager mgr = new J2EESecurityManager();
                System.setSecurityManager(mgr);
            }
            
            if (_logger.isLoggable(Level.INFO)) {
                if (secMgr != null) {
                    _logger.info("security.secmgron");
                } else {
                    _logger.info("security.secmgroff");
                }
            }
	} catch(Exception ex) {
            _logger.log(Level.SEVERE, "java_security.init_securitylifecycle_fail", ex);
            throw new RuntimeException(ex.toString(), ex);
	}
    }   

    // override default
    public void onInitialization(ServerContext sc)
            throws ServerLifecycleException {

        try {
            // init SSL store
            // need this for jaxr https for PE
            // need this for webcore, etc for SE
            SSLUtils.initStoresAtStartup();

            //jmac
            initializeJMAC();

            // jacc
            registerPolicyHandlers();
            PolicyLoader policyLoader = PolicyLoader.getInstance();
            policyLoader.loadPolicy();

            // create realms rather than creating RemoteObject RealmManager
            // which will init ORB prematurely
            RealmConfig.createRealms();

            // start the audit mechanism
            AuditManagerFactory amf = AuditManagerFactory.getInstance();
            amf.getAuditManagerInstance().loadAuditModules();

            // initRoleMapperFactory is in J2EEServer.java and not moved to here
            // this is because a DummyRoleMapperFactory is register due
            // to invocation of ConnectorRuntime.createActiveResourceAdapter
            // initRoleMapperFactory is called after it
        } catch(Exception ex) {
            throw new ServerLifecycleException(ex);
        }
    }

    private void initializeJMAC() throws IOException {

	// define default factory if it is not already defined
	// factory will be constructed on first getFactory call.

	String defaultFactory = Security.getProperty
	    (AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY);
	if (defaultFactory == null) {
	    Security.setProperty
		(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY,
		 GFAuthConfigFactory.class.getName());
 	}
    }

    private void registerPolicyHandlers()
            throws javax.security.jacc.PolicyContextException {
        PolicyContextHandler pch = PolicyContextHandlerImpl.getInstance();
        PolicyContext.registerHandler(PolicyContextHandlerImpl.ENTERPRISE_BEAN,
            pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.SUBJECT, pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.EJB_ARGUMENTS,
            pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.SOAP_MESSAGE,
            pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.HTTP_SERVLET_REQUEST,
            pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.REUSE, pch, true);
    }
}
