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

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.auth.realm.RealmConfig;
import com.sun.enterprise.security.auth.realm.RealmStatsProvider;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.security.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.common.Util;
import com.sun.enterprise.security.jmac.config.GFAuthConfigFactory;
import com.sun.enterprise.security.ssl.SSLUtils;
import org.glassfish.internal.api.ServerContext;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.api.Startup.Lifecycle;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.ConfigListener;

/**
 * This class extends default implementation of ServerLifecycle interface.
 * It provides security initialization and setup for the server.
 * @author  Shing Wai Chan
 */
@Service
@Scoped(Singleton.class)
public class SecurityLifecycle implements  PostConstruct, PreDestroy {
    
    @Inject
    private ServerContext sc;
    
    //@Inject 
    //private RealmConfig realmConfig;
    
    @Inject 
    private PolicyLoader policyLoader;
    
    @Inject
    private SecurityServicesUtil secServUtil;
    
    @Inject 
    private Util util;
    
    @Inject
    private SSLUtils sslUtils;
    
    @Inject
    private SecurityConfigListener configListener;
    
    @Inject(name="MessageSecurityConfigListener", optional=true)
    private ConfigListener msgSecurityConfigListener;

    @Inject
    private Habitat habitat;
    
    private static Map statsProviders = new HashMap();
    

    
    private static WebSecurityDeployerStatsProvider webStatsProvider = null;
        
    private static final LocalStringManagerImpl _localStrings =
	new LocalStringManagerImpl(SecurityLifecycle.class);

    private EventListener listener = null;
 
    private static final Logger _logger = LogDomains.getLogger(SecurityLifecycle.class, LogDomains.SECURITY_LOGGER);

    public SecurityLifecycle() {
	try {
            
            if (Util.isEmbeddedServer()) {
                System.setProperty("java.security.auth.login.config", Util.writeConfigFileToTempDir("login.conf").getAbsolutePath());
                System.setProperty("java.security.policy", Util.writeConfigFileToTempDir("server.policy").getAbsolutePath());
            }
            
            // security manager is set here so that it can be accessed from
            // other lifecycles, like PEWebContainer
            java.lang.SecurityManager secMgr = System.getSecurityManager();
            if (secMgr != null &&
                    !(J2EESecurityManager.class.equals(secMgr.getClass()))) {
                J2EESecurityManager mgr = new J2EESecurityManager();
                try {
                    System.setSecurityManager(mgr);
                } catch (SecurityException ex) {
                    _logger.log(Level.WARNING, "security.secmgr.could.not.override");
                }
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
             
             webStatsProvider = new WebSecurityDeployerStatsProvider();
             StatsProviderManager.register("security", PluginPoint.SERVER, "security/web", webStatsProvider);
             

            //TODO:V3 LoginContextDriver has a static variable dependency on AuditManager
            //And since LoginContextDriver has too many static methods that use AuditManager
            //we have to make this workaround here.
             //Commenting this since this is being handles in LoginContextDriver
        //    LoginContextDriver.AUDIT_MANAGER = secServUtil.getAuditManager();

            //replaced with SharedSecureRandom API
            //secServUtil.initSecureSeed();

            //jmac
            initializeJMAC();

            // jacc
            //registerPolicyHandlers();
            // assert(policyLoader != null);
            policyLoader.loadPolicy();
            // create realms rather than creating RemoteObject RealmManager
            // which will init ORB prematurely
            createRealms();
            // start the audit mechanism
            AuditManager auditManager = secServUtil.getAuditManager();
            auditManager.loadAuditModules();

            //Audit the server started event
            auditManager.serverStarted();
            
            // initRoleMapperFactory is in J2EEServer.java and not moved to here
            // this is because a DummyRoleMapperFactory is register due
            // to invocation of ConnectorRuntime.createActiveResourceAdapter
            // initRoleMapperFactory is called after it
            //initRoleMapperFactory();
           
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

    private boolean realmsAlreadyLoaded() {
       RealmsManager mgr = habitat.getComponent(RealmsManager.class);
       if (mgr != null) {
           Enumeration en = mgr.getRealmNames();
           if (en.hasMoreElements()) {
               return true;
           }
       }
       return false;
    }

    private void registerPolicyHandlers()
            throws javax.security.jacc.PolicyContextException {
        PolicyContextHandler pch = PolicyContextHandlerImpl.getInstance();
        PolicyContext.registerHandler(PolicyContextHandlerImpl.ENTERPRISE_BEAN,
            pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.SUBJECT, pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.EJB_ARGUMENTS,
            pch, true);
        /*V3 Commented: PolicyContext.registerHandler(PolicyContextHandlerImpl.SOAP_MESSAGE,
            pch, true);
         */
        PolicyContext.registerHandler(PolicyContextHandlerImpl.HTTP_SERVLET_REQUEST,
            pch, true);
        PolicyContext.registerHandler(PolicyContextHandlerImpl.REUSE, pch, true);
    }

    public Lifecycle getLifecycle() {
        return Lifecycle.START;
    }

    public void postConstruct() {
        onInitialization();
        listener = new AuditServerShutdownListener();
        Events events = habitat.getByContract(Events.class);
        events.register(listener);

    }

    public void preDestroy() {
        //DO Nothing ?
        //TODO:V3 need to see if something needs cleanup
       
    }
    
     /**
     * Load all configured realms from server.xml and initialize each
     * one.  Initialization is done by calling Realm.initialize() with
     * its name, class and properties.  The name of the default realm
     * is also saved in the Realm class for reference during server
     * operation.
     *
     * <P>This method superceeds the RI RealmManager.createRealms() method.
     *
     * */
    public  void createRealms() {   
        //check if realms are already loaded by admin GUI ?
        if (realmsAlreadyLoaded()) {
            return;
        }
        
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Initializing configured realms from SecurityService in Domain.xml....");
            }
            
            SecurityService securityBean = sc.getDefaultHabitat().getComponent(SecurityService.class);
            assert(securityBean != null);

                                // grab default realm name
            String defaultRealm = securityBean.getDefaultRealm();

                                // get set of auth-realms and process each
            List<AuthRealm> realms = securityBean.getAuthRealm();
            assert(realms != null);

            RealmConfig.createRealms(defaultRealm, realms);
            
        } catch (Exception e) {
            _logger.log(Level.SEVERE, "realmconfig.nogood", e);
        }
    }

    //To audit the server shutdown event
    public class AuditServerShutdownListener implements EventListener {
        public void event(Event event) {
            if (EventTypes.SERVER_SHUTDOWN.equals(event.type())) {
                secServUtil.getAuditManager().serverShutdown();
            }
        }
    }
}
