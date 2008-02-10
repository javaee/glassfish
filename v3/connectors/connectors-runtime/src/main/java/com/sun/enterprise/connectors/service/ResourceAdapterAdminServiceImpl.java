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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.*;
import javax.naming.*;
import com.sun.enterprise.connectors.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.*;
import com.sun.enterprise.server.*;
import com.sun.enterprise.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.*;
import com.sun.enterprise.util.ConnectorClassLoader;


/**
 * This is resource adapter admin service. It creates, deletes Resource adapter
 * and also the resource adapter configuration updation.
 * 
 * @author Binod P.G, Srikanth P and Aditya Gore
 */

public class ResourceAdapterAdminServiceImpl
    extends ConnectorServiceImpl
    implements ConnectorAdminService {

    private JmsRaMapping ramap= null;

    private int resource_adapter_shutdown_timeout = 0;

    //Resource-Adapter-Name vs Boolean(shutdownStatus)
    private final Hashtable raShutdownStatusTable = new Hashtable();
    
    /**
	 * Default constructor
	 */

    public ResourceAdapterAdminServiceImpl() {
        super();
        ramap= new JmsRaMapping();
    }


    /**
	 * Destroys/deletes the Active resource adapter object from the connector
	 * container. Active resource adapter abstracts the rar deployed. It checks
	 * whether any resources (pools and connector resources) are still present.
	 * If they are present the deletion fails and all the objects and
	 * datastructures pertaining to to the resource adapter are left untouched.
	 * 
	 * @param moduleName
	 *                     Name of the rarModule to destroy/delete
	 * @throws ConnectorRuntimeException
	 *                      if the deletion fails
	 */

    public void destroyActiveResourceAdapter(String moduleName)
        throws ConnectorRuntimeException {
        destroyActiveResourceAdapter(moduleName, false);

    }

    /**
	 * Destroys/deletes the Active resource adapter object from the connector
	 * container. Active resource adapter abstracts the rar deployed. It checks
	 * whether any resources (pools and connector resources) are still present.
	 * If they are present and cascade option is false the deletion fails and
	 * all the objects and datastructures pertaining to the resource adapter
	 * are left untouched. If cascade option is true, even if resources are
	 * still present, they are also destroyed with the active resource adapter
	 * 
	 * @param moduleName
	 *                     Name of the rarModule to destroy/delete
	 * @param cascade
	 *                     If true all the resources belonging to the rar are destroyed
	 *                     recursively. If false, and if resources pertaining to
	 *                     resource adapter /rar are present deletetion is failed. Then
	 *                     cascade should be set to true or all the resources have to
	 *                     deleted explicitly before destroying the rar/Active resource
	 *                     adapter.
	 * @throws ConnectorRuntimeException
	 *                      if the deletion fails
	 */

    public void destroyActiveResourceAdapter(
        String moduleName,
        boolean cascade)
        throws ConnectorRuntimeException {

        ResourcesUtil resutil= ResourcesUtil.createInstance();
        if (resutil == null) {
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Failed to get ResourcesUtil object");
            _logger.log(Level.SEVERE, "resourcesutil_get_failure", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
        Object[][] resources= null;
        try {
            resources= resutil.getAllConnectorResourcesForRar(moduleName);
        } catch (ConfigException ce) {
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Failed to get Resources from domain.xml");
            ce.initCause(ce);
            _logger.log(
                Level.SEVERE,
                "rardeployment.resources_list_error",
                cre);
            throw cre;
        }

        boolean errrorOccured= false;

        com.sun.enterprise.config.serverbeans.ConnectorConnectionPool ccp= null;
        if (cascade == true && resources != null) {
            for (int i= 0;
                resources[0] != null && i < resources[0].length;
                ++i) {
                ccp=
                    (com
                        .sun
                        .enterprise
                        .config
                        .serverbeans
                        .ConnectorConnectionPool) resources[0][i];
                try {
                    getRuntime().deleteConnectorConnectionPool(
                        ccp.getName(),
                        cascade);
                } catch (ConnectorRuntimeException cre) {
                    errrorOccured= true;
                }
            }
            for (int i= 0;
                resources[2] != null && i < resources[2].length;
                ++i) {
                try {
                    AdminObjectResource aor=
                        (AdminObjectResource) resources[2][i];
                    getRuntime().deleteAdminObject(aor.getJndiName());
                } catch (ConnectorRuntimeException cre) {
                    errrorOccured= true;
                }
            }
        } else if (
            (resources[0] != null && resources[0].length != 0)
                || (resources[1] != null && resources[1].length != 0)
                || (resources[2] != null && resources[2].length != 0)) {
            _logger.log(
                Level.SEVERE,
                "rardeployment.pools_and_resources_exist",
                moduleName);
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Error: Connector Connection Pools/resources exist.");
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }

        if (!stopAndRemoveActiveResourceAdapter(moduleName)) {
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Failed to remove Active Resource Adapter");
            _logger.log(
                Level.SEVERE,
                "rardeployment.ra_removal_registry_failure",
                moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }

        //Embedded RARs are handled by the application classloader
        if (!(ResourcesUtil.createInstance().belongToEmbeddedRar(moduleName))) {
			ConnectorClassLoader.getInstance().removeResourceAdapter(moduleName);
        }

        
        if (errrorOccured == true) {
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Failed to remove all connector resources/pools");
            _logger.log(
                Level.SEVERE,
                "rardeployment.ra_resource_removal",
                moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
    }

    /**
	 * Creates Active resource Adapter which abstracts the rar module. During
	 * the creation of ActiveResourceAdapter, default pools and resources also
	 * are created.
	 * 
	 * @param connectorDescriptor
	 *                     object which abstracts the connector deployment descriptor
	 *                     i.e rar.xml and sun-ra.xml.
	 * @param moduleName
	 *                     Name of the module
	 * @param moduleDir
	 *                     Directory where rar module is exploded.
	 * @param writeSunDescriptor
	 *                     If true write the sun-ra.xml props to domain.xml and if false
	 *                     it doesnot write to domain.xml
	 * @throws ConnectorRuntimeException
	 *                      if creation fails.
	 */

    public synchronized void createActiveResourceAdapter(
        ConnectorDescriptor connectorDescriptor,
        String moduleName,
        String moduleDir)
        throws ConnectorRuntimeException {
        _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA " 
                        + moduleName + " at " + moduleDir  );

        ActiveResourceAdapter activeResourceAdapter=
            _registry.getActiveResourceAdapter(moduleName);
        if (activeResourceAdapter != null) {
            _logger.log(
                Level.FINE,
                "rardeployment.resourceadapter.already.started",
                moduleName);
            return;
        }

        ClassLoader loader= null;
        try {
            loader= connectorDescriptor.getClassLoader();
        } catch (Exception ex) {
            _logger.log(
                Level.FINE,
                "No classloader available with connector descriptor");
            loader= null;
        }
        ModuleDescriptor moduleDescriptor= null;
        Application application = null;
        _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA " 
                        + moduleName + " at " + moduleDir  + " loader :: " + loader);
        if (loader == null) {
            if (environment == SERVER) {
                ConnectorClassLoader.getInstance().addResourceAdapter(moduleName, moduleDir);
                loader = ConnectorClassLoader.getInstance();
                if (loader == null) {
                    ConnectorRuntimeException cre=
                        new ConnectorRuntimeException("Failed to obtain the class loader");
                    _logger.log(
                        Level.SEVERE,
                        "rardeployment.failed_toget_classloader");
                    _logger.log(Level.SEVERE, "", cre);
                    throw cre;
                }
            }
        } else {
            connectorDescriptor.setClassLoader(null);
            moduleDescriptor= connectorDescriptor.getModuleDescriptor();
            application = connectorDescriptor.getApplication();
            connectorDescriptor.setModuleDescriptor(null);
            connectorDescriptor.setApplication(null);
        }
        try {
            activeResourceAdapter=
                ActiveRAFactory.createActiveResourceAdapter(
                    connectorDescriptor,
                    moduleName,
                    loader);
            _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA " + 
                            moduleName + " at " + moduleDir  + 
                            " ADDING to registry " + activeResourceAdapter);
            _registry.addActiveResourceAdapter(
                moduleName,
                activeResourceAdapter);
            _logger.fine("ResourceAdapterAdminServiceImpl:: createActiveRA " + 
                            moduleName + " at " + moduleDir  
                            + " env =server ? " + (environment==SERVER));
            
            if (environment == SERVER) {
                activeResourceAdapter.setup();
                String descriptorJNDIName = ConnectorAdminServiceUtils.
                            getReservePrefixedJNDINameForDescriptor(moduleName);
                _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA " 
                                + moduleName + " at " + moduleDir  
                                + " publishing descriptor " +  descriptorJNDIName);
                //Update RAConfig in Connector Descriptor and bind in JNDI
                //so that ACC clients could use RAConfig
                updateRAConfigInDescriptor(connectorDescriptor, moduleName);
                Switch.getSwitch().getNamingManager().publishObject(
                    descriptorJNDIName, connectorDescriptor, true);
                String securityWarningMessage=
                    getRuntime().getSecurityPermissionSpec(moduleName);
                // To i18N.
                if (securityWarningMessage != null) {
                    _logger.log(Level.WARNING, securityWarningMessage);
                }
            }

        } catch (NullPointerException npEx) {
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Error in creating active RAR");
            cre.initCause(npEx);
            _logger.log(
                Level.SEVERE,
                "rardeployment.nullPointerException",
                moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        } catch (NamingException ne) {
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Error in creating active RAR");
            cre.initCause(ne);
            _logger.log(Level.SEVERE, "rardeployment.jndi_publish_failure");
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        } finally {
            if (moduleDescriptor != null) {
                connectorDescriptor.setModuleDescriptor(moduleDescriptor);
                connectorDescriptor.setApplication(application);
                connectorDescriptor.setClassLoader(loader);
            }
        }
    }

    /*
     * Updates the connector descriptor of the connector module, with the 
     * contents of a resource adapter config if specified.
     * 
     * This modified ConnectorDescriptor is then bound to JNDI so that ACC 
     * clients while configuring a non-system RAR could get the correct merged
     * configuration. Any updates to resource-adapter config while an ACC client
     * is in use is not transmitted to the client dynamically. All such changes 
     * would be visible on ACC client restart. 
     */
    private void updateRAConfigInDescriptor(ConnectorDescriptor connectorDescriptor, 
                    String moduleName) {

        ResourceAdapterConfig raConfig = 
            ConnectorRegistry.getInstance().getResourceAdapterConfig(moduleName);

        ElementProperty[] raConfigProps = null;
        if(raConfig != null) {
            raConfigProps = raConfig.getElementProperty();
        }

        _logger.fine("current RAConfig In Descriptor " + connectorDescriptor.getConfigProperties());

        if(raConfigProps != null) {
            Set mergedProps = ConnectorDDTransformUtils.mergeProps(
                    raConfigProps, connectorDescriptor.getConfigProperties());
            Set actualProps = connectorDescriptor.getConfigProperties();
            actualProps.clear();
            actualProps.addAll(mergedProps);
            _logger.fine("updated RAConfig In Descriptor " + connectorDescriptor.getConfigProperties());
        } 

    }


    /**
	 * Creates Active resource Adapter which abstracts the rar module. During
	 * the creation of ActiveResourceAdapter, default pools and resources also
	 * are created.
	 * 
	 * @param moduleDir
	 *                     Directory where rar module is exploded.
	 * @param moduleName
	 *                     Name of the module
	 * @param writeSunDescriptor
	 *                     If true write the sun-ra.xml props to domain.xml and if false
	 *                     it doesnot write to domain.xml
	 * @throws ConnectorRuntimeException
	 *                      if creation fails.
	 */

    public synchronized void createActiveResourceAdapter(
        String moduleDir,
        String moduleName)
        throws ConnectorRuntimeException {

        ActiveResourceAdapter activeResourceAdapter=
            _registry.getActiveResourceAdapter(moduleName);
        if (activeResourceAdapter != null) {
            _logger.log(
                Level.FINE,
                "rardeployment.resourceadapter.already.started",
                moduleName);
            return;
        }

        if (ResourcesUtil.createInstance().belongToSystemRar(moduleName)) {
            moduleDir = Switch.getSwitch().getResourceInstaller().
                        getSystemModuleLocation(moduleName);
        }

        ConnectorDescriptor connectorDescriptor= null;
        connectorDescriptor=
            ConnectorDDTransformUtils.getConnectorDescriptor(moduleDir);
        if (connectorDescriptor == null) {
            ConnectorRuntimeException cre=
                new ConnectorRuntimeException("Failed to obtain the connectorDescriptor");
            _logger.log(
                Level.SEVERE,
                "rardeployment.connector_descriptor_notfound",
                moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
        createActiveResourceAdapter(
            connectorDescriptor,
            moduleName,
            moduleDir);
    }

    /**
	 * The ActiveResourceAdapter object which abstract the rar module is
	 * recreated in the connector container/registry. All the pools and
	 * resources are killed. But the infrastructure to create the pools and and
	 * resources is untouched. Only the actual pool is killed.
	 * 
	 * @param moduleName
	 *                     rar module Name.
	 * @throws ConnectorRuntimeException
	 *                      if recreation fails.
	 */

    public void reCreateActiveResourceAdapter(String moduleName)
        throws ConnectorRuntimeException {
        String moduleDir= null;
        if (isRarDeployed(moduleName)) {
            getRuntime().killAllPools(moduleName);
            stopAndRemoveActiveResourceAdapter(moduleName);
            moduleDir= ResourcesUtil.createInstance().getLocation(moduleName);
            createActiveResourceAdapter(moduleDir, moduleName);
        } else {
            moduleDir= ResourcesUtil.createInstance().getLocation(moduleName);
            if (moduleDir != null) {
                createActiveResourceAdapter(moduleDir, moduleName);
            }
        }
    }

    /**
	 * Stops the resourceAdapter and removes it from connector container/
	 * registry.
	 * 
	 * @param moduleName
	 *                     Rarmodule name.
	 * @return true it is successful stop and removal of ActiveResourceAdapter
	 *                false it stop and removal fails.
	 */

    public boolean stopAndRemoveActiveResourceAdapter(String moduleName) {

        ActiveResourceAdapter acr= null;
        if (moduleName != null) {
            acr= _registry.getActiveResourceAdapter(moduleName);
        }
        if (acr != null) {
            acr.destroy();
            boolean ret= _registry.removeActiveResourceAdapter(moduleName);
            return ret;
        }
        return false;
    }

    public void addResourceAdapterConfig(
        String rarName,
        ResourceAdapterConfig raConfig)
        throws ConnectorRuntimeException {
        if (rarName != null && raConfig != null) {
            _registry.addResourceAdapterConfig(rarName, raConfig);
            reCreateActiveResourceAdapter(rarName);
        }
    }

    /**
	 * Delete the resource adapter configuration to the connector registry
	 * 
	 * @param rarName
	 *                     rarmodule
	 */

    public void deleteResourceAdapterConfig(String rarName) {
        if (rarName != null) {
            _registry.removeResourceAdapterConfig(rarName);
        }
    }

    public static boolean isJmsRa() {
        //return System.getProperty("jms.ra").equals("true");
        return true;
    }

    public JmsRaMapping getJmsRaMapping() {
        return ramap;
    }

    /**
	 * Checks if the rar module is already reployed.
	 * 
	 * @param moduleName
	 *                     Rarmodule name
	 * @return true if it is already deployed. false if it is not deployed.
	 */

    public boolean isRarDeployed(String moduleName) {

        ActiveResourceAdapter activeResourceAdapter=
            _registry.getActiveResourceAdapter(moduleName);
        if (activeResourceAdapter != null) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Calls the stop method for all J2EE Connector 1.5 spec compliant RARs
     */
    public void stopAllActiveResourceAdapters() {
        try {
            resource_adapter_shutdown_timeout = 
                ResourcesUtil.createInstance().getShutdownTimeout();
        } catch (ConnectorRuntimeException e) {
            _logger.log(Level.WARNING, "error_reading_connectorservice_elt", e);
            //Going ahead with the default timeout value 
            resource_adapter_shutdown_timeout 
            = ConnectorConstants.DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT;
        }
        ActiveResourceAdapter[] resourceAdapters = 
            ConnectorRegistry.getInstance().getAllActiveResourceAdapters();

        //find all Connector 1.5 Spec Compliant resource adapters
        ArrayList resourceAdaptersToStop = new ArrayList();
        for (int i = 0; i < resourceAdapters.length; i++) {
            if (resourceAdapters[i] instanceof ActiveInboundResourceAdapter){
                _logger.log(Level.FINE, "Connector 1.5 spec compliant RA", 
                        resourceAdapters[i].getModuleName());
                raShutdownStatusTable.put(
                      resourceAdapters[i].getModuleName(), new Boolean(false));
                resourceAdaptersToStop.add(resourceAdapters[i]);
            } else {
                _logger.log(Level.FINE, "Connector 1.0 spec compliant RA", 
                        resourceAdapters[i].getModuleName());
            }
        }
        ActiveResourceAdapter[] raToStop = (ActiveResourceAdapter[])
            resourceAdaptersToStop.toArray(new ActiveResourceAdapter[]{});
        sendStopToAllResourceAdapters(raToStop);
    }

    
    
    /**
     * Calls the stop method for all J2EE Connector 1.5 spec compliant RARs
     * @param resourceAdapters
     */
    private void sendStopToAllResourceAdapters(ActiveResourceAdapter[] 
                                                       resourceAdaptersToStop) {
        //Only J2EE Connector 1.5 compliant RAs needs to be stopped
        int numberOfResourceAdaptersToStop = 0;

        numberOfResourceAdaptersToStop = resourceAdaptersToStop.length;
       
        Thread[] raShutDownThreads = new Thread[numberOfResourceAdaptersToStop];
        Thread[] joinerThreads = new Thread[numberOfResourceAdaptersToStop];

        //Start ResourceAdapter shutdownthreads and then 
        //Start Threads to join the resource adapter shutdownThreads
        for (int i = 0; i < numberOfResourceAdaptersToStop; i++) {
            
            _logger.log(Level.FINE, "Starting RA shutdown thread for " 
                    + ( (ActiveInboundResourceAdapter)resourceAdaptersToStop[i] )
                            .getModuleName() );
            Thread rast = new RAShutdownThread(
                  (ActiveInboundResourceAdapter)resourceAdaptersToStop[i]);
            raShutDownThreads[i] = rast;
            rast.start();
            
            _logger.log(Level.FINE, "Starting Thread to join time-out " +
                    "shutdown of " + 
                    ( ( ActiveInboundResourceAdapter )resourceAdaptersToStop[i]).getModuleName() );
            Thread joiner = new JoinerThread(raShutDownThreads[i]);
            joinerThreads[i] = joiner;
            joiner.start();
        }
        
        //Join all the Joiner threads
        for (int i = 0; i < joinerThreads.length; i++) {
            try {
                _logger.log(Level.FINE, "Joining joiner thread of " 
                       + ((ActiveResourceAdapter)resourceAdaptersToStop[i])
                                .getModuleName() );
                joinerThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        _logger.log(Level.FINE, "stop() Complete for all " +
                "active 1.5 compliant RARs");

        //Log the status of the stop() call
        if (resourceAdaptersToStop.length != 0) {
            _logger.log(Level.FINE, "resource_adapter_stop_status");
        }
        
        for (Iterator iter = raShutdownStatusTable.keySet().iterator(); 
                                            iter.hasNext();) {
            String raName = (String) iter.next();
            if(((Boolean)raShutdownStatusTable.get(raName)).booleanValue()) {
                _logger.log(Level.INFO, "ra.stop-successful", raName);
            } else {
                _logger.log(Level.WARNING, "ra.stop-unsuccessful", raName);
            }
        }
    }

    private class JoinerThread extends Thread {
        private Thread threadToJoin;
        
        public JoinerThread(Thread threadToJoin) {
            this.threadToJoin = threadToJoin;
        }
        
        public void run() {
            try {
                this.threadToJoin.join(ResourceAdapterAdminServiceImpl.this.
                        resource_adapter_shutdown_timeout * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class RAShutdownThread extends Thread {
        private ActiveInboundResourceAdapter ra;

        public RAShutdownThread(ActiveInboundResourceAdapter ratoBeShutDown){
            super();
            this.ra = ratoBeShutDown;
            //This thread is a daemon thread
            this.setDaemon(true);
        }

        public void run() {
            _logger.log(Level.FINE, "Calling" + ra.getModuleName() 
                    + " shutdown ");
            this.ra.destroy();
            ResourceAdapterAdminServiceImpl.this.raShutdownStatusTable.put(
                    ra.getModuleName(), new Boolean(true));
        }
    }    

}
