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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.Security;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextHandler;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

import com.sun.enterprise.J2EESecurityManager;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.auth.LoginContextDriver;
import com.sun.enterprise.security.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.jmac.config.GFAuthConfigFactory;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import org.glassfish.api.Startup;
import org.glassfish.api.Startup.Lifecycle;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * This class extends default implementation of ServerLifecycle interface.
 * It provides security initialization and setup for the server.
 * @author  Shing Wai Chan
 */
@Service
@Scoped(Singleton.class)
public class SecurityLifecycle implements  Startup, PostConstruct, PreDestroy {
    
    @Inject
    private ServerContext sc;
    
    @Inject 
    private RealmConfig realmConfig;
    
    @Inject 
    private PolicyLoader policyLoader;
    
    @Inject 
    private AuditManager auditManager;
    
    @Inject 
    private SecurityServicesUtil secServUtil;
    
    private static final LocalStringManagerImpl _localStrings =
	new LocalStringManagerImpl(SecurityLifecycle.class);
 
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
    public void onInitialization() {

        try {
             if (_logger.isLoggable(Level.INFO)) {
                 _logger.log(Level.INFO, "Security startup service called");
             }
             
            //TODO:V3 LoginContextDriver has a static variable dependency on AuditManager
            //And since LoginContextDriver has too many static methods that use AuditManager
            //we have to make this workaround here.
            LoginContextDriver.AUDIT_MANAGER = auditManager;
            
            secServUtil.initSecureSeed();
            // init SSL store
            // need this for jaxr https for PE
            // need this for webcore, etc for SE
            //TODO:V3 try creating a Service and injecting it here.
            SSLUtils.initStoresAtStartup();

            //jmac
            initializeJMAC();

            // jacc
            registerPolicyHandlers();
            //V3:Commented PolicyLoader policyLoader = PolicyLoader.getInstance();
            //TODO:V3 check if the above singleton was a better way
            assert(policyLoader != null);
            policyLoader.loadPolicy();
            // create realms rather than creating RemoteObject RealmManager
            // which will init ORB prematurely
            assert(realmConfig != null);
            realmConfig.createRealms();
            // start the audit mechanism
            auditManager.loadAuditModules();
            

            // initRoleMapperFactory is in J2EEServer.java and not moved to here
            // this is because a DummyRoleMapperFactory is register due
            // to invocation of ConnectorRuntime.createActiveResourceAdapter
            // initRoleMapperFactory is called after it
           // TODO:V3 i have moved it here : instantiate and register the server-side RoleMapperFactory
           initRoleMapperFactory();
           
           if (_logger.isLoggable(Level.INFO)) {
                 _logger.log(Level.INFO, "Security service(s) started successfully....");
             }

        } catch(Exception ex) {
            throw new SecurityLifecycleException(ex);
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

    public Lifecycle getLifecycle() {
        return Lifecycle.START;
    }

    public void postConstruct() {
        onInitialization();
    }

    public void preDestroy() {
        //DO Nothing ?
        //TODO:V3 need to see if something needs cleanup
    }
    
    /** register the RoleMapperFactory that should be used on the server side
     */
    private void initRoleMapperFactory() throws Exception
    {    
        Object o = null;
        Class c=null;
        // this should never fail.
        try {
            c = Class.forName("com.sun.enterprise.security.acl.RoleMapperFactory");
            if (c!=null) {
                o = c.newInstance();  
                if (o!=null && o instanceof SecurityRoleMapperFactory) {
                    SecurityRoleMapperFactoryMgr.registerFactory((SecurityRoleMapperFactory) o);
                }
            }
            if (o==null) {
                _logger.log(Level.SEVERE,_localStrings.getLocalString("j2ee.norolemapper", 
								     "Cannot instantiate the SecurityRoleMapperFactory"));
            }
        } catch(Exception cnfe) {
            _logger.log(Level.SEVERE,
			_localStrings.getLocalString("j2ee.norolemapper", "Cannot instantiate the SecurityRoleMapperFactory"), 
			cnfe);
            throw  cnfe;
        } 
    }
    
}
