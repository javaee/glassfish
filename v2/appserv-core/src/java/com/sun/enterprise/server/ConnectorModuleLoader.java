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


package com.sun.enterprise.server;

// START OF IASRI 4666602
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.Switch;
// START OF IASRI 4666602
import javax.resource.spi.ManagedConnectionFactory;
// END OF IASRI 4666602

//START OF  4666169
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
//END OF  4666169

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeException;

//for jsr77
import javax.management.ObjectName;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.deployment.archivist.ConnectorArchivist;
import java.io.IOException;
import org.xml.sax.SAXParseException;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import com.sun.enterprise.deployment.io.ConnectorDeploymentDescriptorFile;
import javax.management.MBeanException;
import com.sun.enterprise.Switch;
import com.sun.enterprise.server.event.ApplicationEvent;


/**
 * Connector loader loads and unloads stand alone connector module.
 *
 * @author  Orit Flint
 * @since   JDK1.4
 */

class ConnectorModuleLoader extends AbstractLoader { 

   //START OF  4666169
   static Logger _logger=LogDomains.getLogger(LogDomains.LOADER_LOGGER);
   //END OF  4666169

     private ConnectorDescriptor connectorDescriptor = null;

    /**
     * ConnectorModuleLoader loads one connector module.
     *
     * @param modID              the name of the connector module
     * @param parentClassLoader  the parent class loader
     * @param connModulesManager  the connector module mgr for this VS
     */
    ConnectorModuleLoader(String modID, ClassLoader parentClassLoader,
        ConnectorModulesManager connModulesManager) {

            super(modID,parentClassLoader,connModulesManager);
            // set connector descriptor for subsequent use
            setConnectorDescriptor(modID);
    }
    
    /**
     * Loads stand alone connector module.
     *
     * @return    true if the connector loaded properly
     */
    //START OF IASRI 4686190
    boolean load() {
        try{
           ConnectorRuntime cr = ConnectorRuntime.getRuntime();
           cr.initialize(ConnectorRuntime.SERVER);
           cr.createActiveResourceAdapter(this.configManager.getLocation(this.id),this.id);
           return true;
        }catch(ConfigException e){
            _logger.log(Level.WARNING,"loader.configexception",e);
            return false;
        }
        catch(ConnectorRuntimeException ex) {
            _logger.log(Level.WARNING,"Failed to load the rar",ex);

            return false;

        }
    }

    /**
     * Loads stand alone connector module.
     *
     * @param     jsr77    create jsr77 mBeans if true
     * @return    true if the connector loaded properly
     */
    boolean doLoad(boolean jsr77) {
	//Note: Application.isVirtual will be true for stand-alone module
	notifyAppEvent(ApplicationEvent.BEFORE_APPLICATION_LOAD);

        if (load()) {
            try {
                createLeafMBeans();
            } catch (MBeanException mbe) {
                _logger.log(Level.WARNING,"loader.exception",mbe);
            }
            return true;
        }
        return false;
    }

	
    /**
     * Unloads stand alone connector module.
     *
     * @return    true if removed successful
     */
    boolean unload() {
           ConnectorRuntime connectorRuntime = ConnectorRuntime.getRuntime();
           try {
               connectorRuntime.destroyActiveResourceAdapter(this.id,cascade);
               configManager.unregisterDescriptor(id);
           } 
           catch(ConnectorRuntimeException cre) {
              return false;
           }
           return true;
    }

    /**
     * Unloads stand alone connector module.
     *
     * @param     jsr77    delete jsr77 mBeans if true
     * @return    true     if removed successful
     */
    boolean unload(boolean jsr77) {
        if (unload()) {
            try {
                deleteLeafMBeans();
            } catch (MBeanException mbe) {
                _logger.log(Level.WARNING,"loader.exception",mbe);
            }
            return true;
        }
        return false;
    }

    /**
     * Create connector descriptor and store in local variable
     * for subsequent use by jsr77 and connector code.
     */
    private void setConnectorDescriptor(String modID) {
        try {
            // hack for setting stand-alone attribute
            // this needs to be fixed in connectorArchivist
            // remove this code once it is fixed in connectorArchivist
            // hack-start
	    this.application = configManager.getDescriptor(modID, null, false);	    
            connectorDescriptor = (ConnectorDescriptor) application.getStandaloneBundleDescriptor();
            // hack-end
        } catch(ConfigException ex) {
            _logger.log(Level.WARNING,"Failed to get the module directory ");
        }
    }

    /**
     * Returns connector descriptor for this module
     */
    public ConnectorDescriptor getConnectorDescriptor() {
        return connectorDescriptor;
    }


    /**
     * Create jsr77 root mBean
     */
    void createRootMBean() throws MBeanException {

	try {
            Switch.getSwitch().getManagementObjectManager().createRARModuleMBean(
		connectorDescriptor,
            	this.configManager.getInstanceEnvironment().getName(),
		this.configManager.getLocation(this.id));
	} catch (Exception e) {
	    throw new MBeanException(e);
	}
    }


    /**
     * Delete jsr77 root mBean
     */
    void deleteRootMBean() throws MBeanException {

        Switch.getSwitch().getManagementObjectManager().deleteRARModuleMBean(connectorDescriptor,
            this.configManager.getInstanceEnvironment().getName());
    }


    /**
     * Create jsr77 resource adapter mebans which are contained
     * within this connector module
     */
    void createLeafMBeans() throws MBeanException {
        Switch.getSwitch().getManagementObjectManager().createRARMBeans(connectorDescriptor,
            this.configManager.getInstanceEnvironment().getName());
    }

    /**
     * Create jsr77 resource adapter mebans which are contained
     * within this connector module
     */
    void createLeafMBean(Descriptor descriptor) throws MBeanException {
        ConnectorDescriptor cd = null;
        try {
            cd = (ConnectorDescriptor) descriptor;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
        Switch.getSwitch().getManagementObjectManager().createRARMBean(cd,
            this.configManager.getInstanceEnvironment().getName());
    }

    /**
     * Delete jsr77 resource adapter mebans which are contained
     * within this connector module
     */
    void deleteLeafMBeans() throws MBeanException {
        Switch.getSwitch().getManagementObjectManager().deleteRARMBeans(connectorDescriptor, 
            this.configManager.getInstanceEnvironment().getName());
    }

    /**
     * Delete jsr77 resource adapter mebans which are contained
     * within this connector module
     */
    void deleteLeafMBean(Descriptor descriptor) throws MBeanException {
        ConnectorDescriptor cd = null;
        try {
            cd = (ConnectorDescriptor) descriptor;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
        Switch.getSwitch().getManagementObjectManager().deleteRARMBean(cd,
            this.configManager.getInstanceEnvironment().getName());
    }


    /**
     * Delete jsr77 mBeans for the module and its' components
     */
    void deleteLeafAndRootMBeans() throws MBeanException {
        deleteLeafMBeans();
        deleteRootMBean();
    }


    /**
     * Set the state for the rootMBean
     */
    void setState(int state) throws MBeanException {

        Switch.getSwitch().getManagementObjectManager().setRARModuleState(state, connectorDescriptor,
            this.configManager.getInstanceEnvironment().getName());

    }

}
// END OF IASRI 4666602
