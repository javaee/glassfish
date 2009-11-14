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

package org.glassfish.ejb.startup;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.security.PolicyLoader;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.logging.LogDomains;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.ejb.spi.CMPDeployer;
import org.glassfish.ejb.spi.CMPService;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import com.sun.ejb.codegen.StaticRmiStubGenerator;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import java.util.*;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import org.glassfish.api.invocation.RegisteredComponentInvocationHandler;
import org.glassfish.ejb.security.application.EJBSecurityManager;
import org.glassfish.ejb.security.factory.EJBSecurityManagerFactory;

/**
 * Ejb module deployer.
 *
 */
@Service
public class EjbDeployer
    extends JavaEEDeployer<EjbContainerStarter, EjbApplication> {

    @Inject
    protected ServerContext sc;

    @Inject
    protected Domain domain;

    @Inject
    protected ServerEnvironmentImpl env;

    @Inject
    protected Habitat habitat;
    
    @Inject
    protected PolicyLoader policyLoader;
    
    @Inject
    protected EJBSecurityManagerFactory ejbSecManagerFactory;

    @Inject
    private ComponentEnvManager compEnvManager;
    
    private Object lock = new Object();
    private volatile CMPDeployer cmpDeployer = null;

    // Property used to persist unique id across server restart.
    private static final String APP_UNIQUE_ID_PROP = "org.glassfish.ejb.container.application_unique_id";

    private AtomicLong uniqueIdCounter;
    
    private static final Logger _logger =
                LogDomains.getLogger(EjbDeployer.class, LogDomains.EJB_LOGGER);

    /**
     * Constructor
     */
    public EjbDeployer() {

        // Seed a counter used for ejb application unique id generation.
        uniqueIdCounter = new AtomicLong(System.currentTimeMillis());

    }


    protected String getModuleType () {
        return "ejb";
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(false,
                new Class[] {EjbBundleDescriptor.class}, new Class[] {Application.class});
    }

    @Override
    public EjbApplication load(EjbContainerStarter containerStarter, DeploymentContext dc) {
        super.load(containerStarter, dc);


        //Register the EjbSecurityComponentInvocationHandler

        RegisteredComponentInvocationHandler handler = habitat.getComponent(RegisteredComponentInvocationHandler.class,"ejbSecurityCIH");
        handler.register();

        EjbBundleDescriptor ejbBundle = dc.getModuleMetaData(EjbBundleDescriptor.class);
        
        if( ejbBundle == null ) {
            throw new RuntimeException("Unable to load EJB module.  DeploymentContext does not contain any EJB " +
                    " Check archive to ensure correct packaging for " + dc.getSourceDir());
        }

        ejbBundle.setClassLoader(dc.getClassLoader());

        // Get application-level properties (*not* module-level)
        Properties appProps = dc.getAppProps();

        long uniqueAppId;

        if( !appProps.containsKey(APP_UNIQUE_ID_PROP)) {

            // This is the first time load is being called for any ejb module in an
            // application, so generate the unique id.

            uniqueAppId = getNextEjbAppUniqueId();
            appProps.setProperty(APP_UNIQUE_ID_PROP, uniqueAppId + "");
        } else {
            uniqueAppId = Long.parseLong(appProps.getProperty(APP_UNIQUE_ID_PROP));         
        }

        Application app = ejbBundle.getApplication();

        if( !app.isUniqueIdSet() ) {
            // This will set the unique id for all EJB components in the application.
            // If there are multiple ejb modules in the app, we'll only call it once
            // for the first ejb module load().  All the old
            // .xml processing for unique-id in the sun-* descriptors is removed so
            // this is the only place where Application.setUniqueId() should be called.
            app.setUniqueId(uniqueAppId);
        }

        if (ejbBundle.containsCMPEntity()) {
            CMPService cmpService = habitat.getByContract(CMPService.class);
            if (cmpService == null) {
                throw new RuntimeException("CMP Module is not available");
            } else if (!cmpService.isReady()) {
                throw new RuntimeException("CMP Module is not initialized");
            }
        }


        EjbApplication ejbApp = new EjbApplication(ejbBundle, dc, dc.getClassLoader(), habitat,
                                                   ejbSecManagerFactory);

        try {
            compEnvManager.bindToComponentNamespace(ejbBundle);

            // If within .war, also bind dependencies declared by web application.  There is
            // a single naming environment for the entire .war module.  Yhis is necessary
            // in order for eagerly initialized ejb components to have visibility to all the
            // dependencies b/c the web container does not bind to the component namespace until
            // its start phase, which comes after the ejb start phase.
            Object rootDesc = ejbBundle.getModuleDescriptor().getDescriptor();
            if( (rootDesc != ejbBundle) && (rootDesc instanceof WebBundleDescriptor ) ) {
                WebBundleDescriptor webBundle = (WebBundleDescriptor) rootDesc;
                compEnvManager.bindToComponentNamespace(webBundle);
            }

        } catch(Exception e) {
            throw new RuntimeException("Exception registering ejb bundle level resources", e);
        }

        ejbApp.loadContainers(dc);

        return ejbApp;
    }

    public void unload(EjbApplication ejbApplication, DeploymentContext dc) {

        EjbBundleDescriptor ejbBundle = ejbApplication.getEjbBundleDescriptor();

        try {
            compEnvManager.unbindFromComponentNamespace(ejbBundle);          
        } catch(Exception e) {
             _logger.log( Level.WARNING, "Error unbinding ejb bundle " +
                     ejbBundle.getModuleName() + " dependency namespace", e);
        }

        if (ejbBundle.containsCMPEntity()) {
            initCMPDeployer();
            if (cmpDeployer != null) {
                cmpDeployer.unload(ejbBundle.getClassLoader());
            }
        }

        // All the other work is done in EjbApplication. 

    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param dc deployment context
     */
    public void clean(DeploymentContext dc) {
        
        // Both undeploy and shutdown scenarios are
        // handled directly in EjbApplication.shutdown.

        // But CMP drop tables should be handled here.

        OpsParams params = dc.getCommandParameters(OpsParams.class);
        if (params.origin == OpsParams.Origin.undeploy || 
            params.origin == OpsParams.Origin.deploy) {

            // If CMP beans are present, cmpDeployer should've been initialized in unload()
            if (cmpDeployer != null) {
                cmpDeployer.clean(dc);
            }

            //Removing EjbSecurityManager for undeploy case
            String appName = params.name();
            String[] contextIds =
                    ejbSecManagerFactory.getContextsForApp(appName, false);
            if (contextIds != null) {
                for (String contextId : contextIds) {
                    try {
                        SecurityUtil.removePolicy(contextId);
                    } catch (IASSecurityException ex) {
                        _logger.log(Level.WARNING, "Error removing the policy file " +
                                "for application " + appName + " " + ex);
                    }

                    ArrayList<EJBSecurityManager> managers =
                            ejbSecManagerFactory.getManagers(contextId, false);
                    if (managers != null) {
                        for (EJBSecurityManager m : managers) {
                            m.destroy();
                        }
                    }
                }
            }
            //Removing the RoleMapper
            SecurityUtil.removeRoleMapper(dc);
        }


    }

    /**
     * Use this method to generate any ejb-related artifacts for the module
     */
    @Override
    protected void generateArtifacts(DeploymentContext dc)
            throws DeploymentException {

        OpsParams params = dc.getCommandParameters(OpsParams.class);
        if (params.origin != OpsParams.Origin.deploy) {
            return;
        }
        
        EjbBundleDescriptor bundle = dc.getModuleMetaData(EjbBundleDescriptor.class);
        policyLoader.loadPolicy();
        if (bundle != null) {
            for (EjbDescriptor desc : bundle.getEjbs()) {
                 /* create and register security manager for each EJB
                  * WHEN THIS CODE EXECUTED ONLY ON DAS, REGISTRATION WAS NOT DONE
                  */
                this.ejbSecManagerFactory.createManager(desc, true);
            }
        }

        DeployCommandParameters dcp =
                dc.getCommandParameters(DeployCommandParameters.class);
        boolean generateRmicStubs = dcp.generatermistubs;
        dc.addTransientAppMetaData(CMPDeployer.MODULE_CLASSPATH, getModuleClassPath(dc));
        if( generateRmicStubs ) {
            StaticRmiStubGenerator staticStubGenerator = new StaticRmiStubGenerator(habitat);
            try {
                staticStubGenerator.ejbc(dc);
            } catch(Exception e) {
                throw new DeploymentException("Static RMI-IIOP Stub Generation exception for " +
                        dc.getSourceDir(), e);
            }
        }

        if (bundle == null || !bundle.containsCMPEntity()) {
            // bundle WAS null in a war file where we do not support CMPs
            return;
        }

        initCMPDeployer();
        if (cmpDeployer == null) {
            throw new DeploymentException("No CMP Deployer is available to deploy this module");
        }
        cmpDeployer.deploy(dc);   


    }

    private long getNextEjbAppUniqueId() {
        long next = uniqueIdCounter.incrementAndGet();

        // This number represents the base unique id for each ejb application.
        // It is used to generate an id for each ejb component that is
        // guaranteed to be unique across all the applications deployed to a
        // particular stand-alone server instance or DAS.
        //
        // The unique id is 64 bits, with the low-order 16 bits zeroed out.
        // Component ids are selected from these low-order bits, allowing a
        // maximum of 2^16 EJB components per application.
        //
        // The initial number is seeded from System.currentTimeMillis() the
        // first time this class is instantiated after a server start.
        // Since this epoch value is relative to 1970, even after left-shifting
        // 16 bits, the number of remaining milliseconds won't run out until
        // the year 10889.   This scheme also assumes that for the lifetime
        // of the JVM for a given server, there aren't more individual
        // ejb application deployments than elapsed milliseconds, since the
        // next time the server starts it will simply seed from
        // currentTimeMillis() again rather than remembering the largest unique
        // id that was used the last time the server ran.  

        return next << 16;
    }

    private void initCMPDeployer() {
        if (cmpDeployer == null) {
            synchronized(lock) {
                cmpDeployer = habitat.getByContract(CMPDeployer.class);
            }
        }
    }
    
}
