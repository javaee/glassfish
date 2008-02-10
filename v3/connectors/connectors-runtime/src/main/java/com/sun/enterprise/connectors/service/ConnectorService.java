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

package com.sun.enterprise.connectors;

import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.logging.LogDomains;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.*;

import com.sun.enterprise.*;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.config.*;
import com.sun.enterprise.connectors.util.*;
import com.sun.enterprise.connectors.work.monitor.ConnectorWorkMonitoringLevelListener;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.resource.*;
import com.sun.enterprise.server.*;
import com.sun.enterprise.util.*;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.xml.sax.SAXParseException;


/**
 * This is the base class for all the connector services. It defines the 
 * enviroment of execution (client or server), and holds the reference to
 * connector runtime for inter service method invocations.
 * @author    Srikanth P 
 */


public class ConnectorServiceImpl implements ConnectorConstants {
    static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);     
     
    protected static final ConnectorRegistry _registry = 
                         ConnectorRegistry.getInstance();

    private boolean debug=true;
    protected static int environment = CLIENT;

    /** 
     * Default Constructor 
     */

    public ConnectorServiceImpl() {
    }

    /** Initializes the execution environment. If the execution environment
     *  is appserv runtime it is set to ConnectorConstants.SERVER else
     *  it is set ConnectorConstants.CLIENT
     *  @param environment set to ConnectorConstants.SERVER if execution
     *              environment is appserv runtime else set to
     *              ConnectorConstants.CLIENT
     */

    public static void initialize(int environ) {
        environment = environ;
    }

    /**
     * Returns the execution environment.
     * @return ConnectorConstants.SERVER if execution environment is
     *         appserv runtime
     *         else it returns ConnectorConstants.CLIENT
     */

    public static int getEnviron() {
        return environment;
    }

    /** 
     * Returns the generated default poolName of JMS resources.
     * @jndiName jndi of the resources for which pool is to be created,
     * @return generated poolname
     */

    public String getDefaultPoolName(String jndiName) {
        //This is called by the JMS Deployers alone
        return jndiName;
    }

    /** 
     * Returns the generated default connection poolName for a 
     * connection definition.
     * @moduleName rar module name
     * @connectionDefName connection definition name 
     * @return generated connection poolname
     */

    public String getDefaultPoolName(String moduleName, 
                       String connectionDefName) {
        return moduleName+POOLNAME_APPENDER+connectionDefName;
    }

    /** 
     * Returns the generated default connector resource for a 
     * connection definition.
     * @moduleName rar module name
     * @connectionDefName connection definition name 
     * @return generated default connector resource name
     */
    public String getDefaultResourceName(String moduleName, 
                       String connectionDefName) {
        //Construct the default resource name as
        // <JNDIName_of_RA>#<connectionDefnName>
        String resourceJNDIName = ConnectorAdminServiceUtils.
                    getReservePrefixedJNDINameForResource(moduleName);
        return resourceJNDIName + RESOURCENAME_APPENDER + connectionDefName;
    }

    /** These two methods are NOP. They will be used for future
     * enhancements. 
     */

    public void create() throws ConnectorRuntimeException {

    }

    public void destroy() throws ConnectorRuntimeException {

    }

    /** Checks whether the executing environment is application server
     *  @return true if execution environment is server
     *          false if it is client
     */

    public static boolean isServer() {
        if(getEnviron() == SERVER) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Internal API to lazily load Connector/JDBC/JMS and other resources on
     * first lookup. All dependent resources and related infrastructure are also 
     * loaded.
     */
    public boolean checkAndLoadResource(String resname) {
        //Resolve actual JNDI name. Connector and JDBC connection Pools are 
        //internally reserve prefixed while binding in JNDI. 
        //The Connector backend OTOH uses the actual JNDI name for all checks. 
        //So unmap the transformation here before proceeding to load the resource 
        //and its dependents
        resname = ConnectorAdminServiceUtils.getOriginalResourceName(resname); 
        _logger.fine("ConnectorServiceImpl :: checkAndLoadResource resolved to " +
                "load " + resname);

        ResourcesUtil resutil = ResourcesUtil.createInstance(); 
        DeferredResourceConfig defResConfig = resutil.getDeferredResourceConfig(
                                        resname); 
        return loadResourcesAndItsRar(defResConfig);
    }

    public boolean checkAndLoadPoolResource(String poolName) {

       ResourcesUtil resutil = ResourcesUtil.createInstance(); 
       DeferredResourceConfig defResConfig = resutil.getDeferredPoolConfig(
                                poolName); 
       return loadResourcesAndItsRar(defResConfig);
    }

    public boolean loadResourcesAndItsRar(DeferredResourceConfig defResConfig) {
       if(defResConfig != null) {
           try {
               loadDeferredResources(defResConfig.getResourceAdapterConfig());
               String rarName = defResConfig.getRarName();
               loadDeferredResourceAdapter(rarName);
               final ConfigBean[] resToLoad = defResConfig.getResourcesToLoad();
               AccessController.doPrivileged(new PrivilegedAction() {
                   public Object run() {
                       try {
                           loadDeferredResources(resToLoad);
                       } catch(Exception ex) {
                           _logger.log( Level.SEVERE,
                                "failed to load resources/ResourceAdapter");
                           _logger.log(Level.SEVERE,"" ,ex);
                       }
                       return null;
                   }
               });
           } catch(Exception ex) {
               _logger.log(
                  Level.SEVERE,"failed to load resources/ResourceAdapter");
               _logger.log(Level.SEVERE,"" ,ex);
               return false;
           }
           return true;
       }
       return false;
    }

    
    public void loadDeferredResourceAdapter(String rarName) 
                            throws ConnectorRuntimeException 
    {
       try {
           ManagerFactory.getSAConnectorModulesManager().
           loadOneSystemApp(rarName, true);
       } catch (Exception e) {
           ConnectorRuntimeException ce = 
           new ConnectorRuntimeException(e.getMessage());
           ce.initCause(e);
           throw ce;
       }
    }

    public void loadDeferredResources(ConfigBean[] resourcesToLoad) 
                                     throws Exception 
    {
        if(resourcesToLoad == null || resourcesToLoad.length == 0) {
            return;
        }
        String resourceType = null;
        ResourceDeployer deployer = null;
        ResourcesUtil resourceUtil = ResourcesUtil.createInstance(); 
        for(int i=0;i<resourcesToLoad.length;++i) {
            if(resourcesToLoad[i] == null) {
                continue;
            } else if (resourceUtil.isEnabled(resourcesToLoad[i])) {
                resourceType = resourceUtil.getResourceType(resourcesToLoad[i]);
                ResourceDeployerFactory factory = new ResourceDeployerFactory();
                deployer = factory.getResourceDeployer(resourceType);
                if(deployer != null) {
                    deployer.deployResource(resourcesToLoad[i]);
                }
            }
        }
    }

    public void ifSystemRarLoad(String rarName) 
                           throws ConnectorRuntimeException 
    {
        ResourcesUtil resUtil = ResourcesUtil.createInstance();
        if(resUtil.belongToSystemRar(rarName)){
            loadDeferredResourceAdapter(rarName);
        }
    }

    /**
     * Check whether ClassLoader is permitted to access this resource adapter.
     * If the RAR is deployed and is not a standalone RAR, then only ClassLoader 
     * that loader the archive should be able to access it. Otherwise everybody can
     * access the RAR.
     *
     * @param rarName Resource adapter module name.
     * @param loader <code>ClassLoader</code> to verify.
     */
    public boolean checkAccessibility(String rarName, ClassLoader loader) {
        ActiveResourceAdapter ar = _registry.getActiveResourceAdapter(rarName);
        if (ar != null && loader != null) { // If RA is deployed
	    ClassLoader rarLoader = ar.getClassLoader();
	    //If the RAR is not standalone.
	    if (rarLoader != null && (!(rarLoader instanceof ConnectorClassLoader))) {
		ClassLoader parent = loader;
		while (true) {
		    if (parent.equals(rarLoader)) {
                        return true;
		    }

                    final ClassLoader temp = parent;
                    Object obj = AccessController.doPrivileged( new PrivilegedAction() {
                        public Object run() {
		            return temp.getParent();
                        }
                    });

                    if (obj == null) {
                        break;
                    } else {
                        parent = (ClassLoader) obj;
                    }
                }
		// If no parent matches return false;
                return false;
	    }
	}
	return true;
    }

    /** 
     * Obtains the connector Descriptor pertaining to rar. 
     * If ConnectorDescriptor is present in registry, it is obtained from
     * registry and returned. Else it is explicitly read from directory
     * where rar is exploded.
     * @param rarName Name of the rar
     * @return ConnectorDescriptor pertaining to rar.
     */

    public ConnectorDescriptor getConnectorDescriptor(String rarName) 
                   throws ConnectorRuntimeException 
    {
        
        if(rarName == null) {
            return null;
        }
        ConnectorDescriptor desc = null;
        desc = _registry.getDescriptor(rarName);
        if(desc != null) {
            return desc;
        }
        String moduleDir = null;
        ResourcesUtil resUtil = ResourcesUtil.createInstance();

        //If the RAR is embedded try loading the descriptor directly
        //using the applicationarchivist
        if (rarName.indexOf(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER) != -1){
            try {
                desc = loadConnectorDescriptorForEmbeddedRAR(rarName);
                if (desc != null) return desc;
            } catch (ConnectorRuntimeException e) {
                throw e;
            }
        }
        
        if(resUtil.belongToSystemRar(rarName)){
            ResourceInstaller installer = 
                       Switch.getSwitch().getResourceInstaller();
            moduleDir = installer.getSystemModuleLocation(rarName);
        } else {
            moduleDir = resUtil.getLocation(rarName);
        }
        if (moduleDir != null) {
            desc = ConnectorDDTransformUtils.getConnectorDescriptor(moduleDir);
        } else {
            _logger.log(Level.SEVERE,
                   "rardeployment.no_module_deployed", rarName);
        }
        return desc;
    }
    
    private ConnectorDescriptor loadConnectorDescriptorForEmbeddedRAR(String rarName) throws ConnectorRuntimeException {
        //If the RAR is embedded try loading the descriptor directly
        //using the applicationarchivist
        try {
            String appName = rarName.substring(0, rarName.indexOf(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER));
            String actualRarName = rarName.substring(rarName.indexOf(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER) + 1);
            String appDeployLocation = ResourcesUtil.createInstance().getApplicationDeployLocation(appName);
            
            FileArchive in = new FileArchive();
            in.open(appDeployLocation);
            ApplicationArchivist archivist = new ApplicationArchivist();
            Application application = (Application) archivist.open(in);
            //get all RAR descriptors and try searching for this embedded RAR
            Set s = application.getRarDescriptors();
            for (Iterator iter = s.iterator(); iter.hasNext();) {
                ConnectorDescriptor element = (ConnectorDescriptor) iter.next();
                //Strip ".rar" from deployname before using it.
                String rardescname = element.getDeployName().substring(
                                0, element.getDeployName().indexOf(".rar"));
                if(rardescname.equals(actualRarName)){
                    return element;
                }
            }
        } catch (SAXParseException e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException("" +
                    "SAXParseException while trying to load connector descriptor for embedded RAR");
            crex.initCause(e);
            throw crex;
        } catch (IOException e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException("" +
            "SAXParseException while trying to load connector descriptor for embedded RAR");
            crex.initCause(e);
            throw crex;
        }
        return null;
        
    }

    /**
     * Matching will be switched off in the pool, by default. This will be 
     * switched on if the connections with different resource principals reach the pool.
     * 
     * @param poolName Name of the pool to switchOn matching.
     * @param rarName Name of the resource adater.
     */
    public void switchOnMatching(String rarName, String poolName) {
        // At present it is applicable to only JDBC resource adapters
        // Later other resource adapters also become applicable.
        if (rarName.equals(ConnectorRuntime.JDBCDATASOURCE_RA_NAME) ||
            rarName.equals(ConnectorRuntime.JDBCCONNECTIONPOOLDATASOURCE_RA_NAME) ||
            rarName.equals(ConnectorRuntime.JDBCXA_RA_NAME)) {
           
             PoolManager poolMgr = Switch.getSwitch().getPoolManager();
             boolean result = poolMgr.switchOnMatching(poolName);            
             if (result == false) {
                 try {
                     getRuntime().switchOnMatchingInJndi(poolName);                    
                 } catch (ConnectorRuntimeException cre) {
                     // This will never happen.
                 }
             }
        } 
             
    }

    /**
     * Initialize the monitoring listeners for connection pools, work management
     * and message end point factory related stats
     */
    public void initializeConnectorMonitoring() {
        Switch.getSwitch().getPoolManager().initializeMonitoring();
        initializeWorkMgmtAndEndPointMonitoring();
    }

    /**
     * Initialize the monitoring listeners for connection pools, work management
     * and message end point factory related stats
     */
    private void initializeWorkMgmtAndEndPointMonitoring() {
        try {
            final ConnectorWorkMonitoringLevelListener cwmll 
                        = new ConnectorWorkMonitoringLevelListener();
            //@todo Do for Connector EPF Monitoring as well
        
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    ServerContext ctxt = ApplicationServer.getServerContext();
                    if (ctxt != null) {
                        MonitoringRegistry monitoringRegistry_ 
                            = ctxt.getMonitoringRegistry();
                        //@todo: for EFT level listeners 
                        monitoringRegistry_.registerMonitoringLevelListener(cwmll, 
                                        MonitoredObjectType.CONNECTOR_WORKMGMT);
                                        
/*                        monitoringRegistry_.registerMonitoringLevelListener(
                                        ccpPoolMonitoringLevelListener_,
                                        MonitoredObjectType.CONNECTOR_CONN_POOL );
*/
                }
                return null;
            }    
            });
    
            _logger.log( Level.FINE, "poolmon.init_monitoring_registry");
        } catch(Exception e) {
            _logger.log( Level.INFO, "poolmon.error_registering_listener", 
            e);
        }
    }
    
    protected ConnectorRuntime getRuntime(){
        return ConnectorRuntime.getRuntime();
    }
}
