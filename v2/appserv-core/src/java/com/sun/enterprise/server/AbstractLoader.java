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

/*
 * @(#) AbstractLoader.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.enterprise.deploy.shared.ModuleType;
import javax.naming.NameAlreadyBoundException;

import com.sun.ejb.Container;
import com.sun.ejb.ContainerFactory;

import com.sun.enterprise.Switch;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.backend.DeploymentUtils;
import com.sun.enterprise.SecurityManager;
import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.loader.InstrumentableClassLoader;
import com.sun.logging.LogDomains;
import com.sun.enterprise.security.factory.FactoryForSecurityManagerFactoryImpl;
import com.sun.enterprise.security.factory.SecurityManagerFactory;
import com.sun.enterprise.security.factory.FactoryForSecurityManagerFactory;

import com.sun.enterprise.security.SecurityUtil;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.J2eeApplication;

import com.sun.enterprise.instance.BaseManager;

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.Utility;

import com.sun.enterprise.connectors.*;
import com.sun.enterprise.server.ondemand.entry.*;

//for jsr77
import com.sun.enterprise.deployment.Descriptor;
import javax.management.MBeanException;
import javax.persistence.EntityManagerFactory;

import com.sun.enterprise.security.application.EJBSecurityManager;
import com.sun.enterprise.server.event.ApplicationEvent;
import com.sun.enterprise.server.event.EjbContainerEvent;
import com.sun.enterprise.server.event.ApplicationLoaderEventNotifier;

import com.sun.enterprise.admin.monitor.WSMonitorLifeCycleFactory;

/**
 * Base loader to load and unload applicaton and stand alone module. 
 *
 * @author  Mahesh  Kannan
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
abstract class AbstractLoader implements EntryPoint {
 
    /** the registration name for this application */
    protected String id = null;

    /** parent class loader of ejb class loader */
    ClassLoader parentClassLoader = null;

    /** ejb class loader used by this application */
    ClassLoader ejbClassLoader = null;

    /** deployment descriptor object for this application */
    Application       application = null;

    protected boolean robFlag = false;

    /** application registry */ 
    protected ApplicationRegistry registry = null;

    /** encapsulates application related information */
    protected BaseManager configManager = null;

    /** logger to log loader messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.LOADER_LOGGER);

    /** factory for SecurityManagerFactory */
    private FactoryForSecurityManagerFactory ffsmf = null;

    /*** This variable tells whether to load/unload pools and resources recursively. **/

    protected boolean cascade=false;

    /** Possible values for this:
     *  0. LOAD_UNSET is before initialization
     *
     *  For loading:
     *  1. LOAD_ALL is for loading regular application
     *  2. LOAD_RAR is for loading the rar part of the embedded rar 
     *  3. LOAD_REST is for loading the rest part of the embedded rar
     *  Embedded rar is loaded in two steps so we can create connector
     *  resources in between.
     *
     *  For unloading:
     *  4. UNLOAD_ALL is for unloading regular application
     *  5. UNLOAD_RAR is for unloading the rar part of the embedded rar 
     *  6. UNLOAD_REST is for unloading the rest part of the embedded rar
     *  Embedded rar is unloaded in two steps so we can delete connector
     *  resources in between.
     */
    protected int loadUnloadAction;

    private ApplicationLoaderEventNotifier loaderEventNotifier;

    private ConfigContext dynamicConfigContext;

    /**
     * Constructor.  ApplicationLoader loads an application or 
     * stand alone ejb module.
     *
     * @param id                 the registration name of this application 
     * @param parentClassLoader  the parent class loader
     * @param configMgr          the config manager for this VS
     */
    AbstractLoader(String id, ClassLoader parentClassLoader, 
            BaseManager configMgr) { 

        this.id                     = id;
        this.parentClassLoader      = parentClassLoader;
        this.registry               = ApplicationRegistry.getInstance();
        this.configManager          = configMgr;

	this.loaderEventNotifier =
	    ApplicationLoaderEventNotifier.getInstance();
    }

    /**
     * Initializes this loader by creating the ejb class loader.
     *
     * @param    classPaths    class paths for the ejb class loader
     */
    protected void initializeLoader(String[] classPaths, String moduleRoot, ModuleType moduleType) {
        this.ejbClassLoader = EJBClassPathUtils.createEJBClassLoader(
                        classPaths, moduleRoot, this.id, this.parentClassLoader, moduleType);
    }

    /**
     * Initializes this loader to the given classloader
     */
    protected void initializeLoader(ClassLoader clazzloader) {
        this.ejbClassLoader = clazzloader;
    }

    void setConfigContext(ConfigContext dynamicConfigContext) {
	this.dynamicConfigContext = dynamicConfigContext;
    }

    ConfigContext getConfigContext() {
        return dynamicConfigContext;
    }
    
    private boolean verifyModuleDirectory() {
        String moduleDirectory;
        try {
            moduleDirectory = configManager.getLocation(id);
        } catch (ConfigException ce) {
            _logger.log(Level.WARNING, "loader.configexception", ce);
            return false;
        }
        boolean result = FileUtils.safeIsDirectory(moduleDirectory);
        if ( ! result) {
            /*
             *Log a warning message without a stack trace.
             */
            _logger.log(Level.WARNING, "loader.module_dir_error", new Object[] {id, moduleDirectory});
        }
        return result;
    }
    
    /**
     * Loads a deployed application or stand alone module.
     * Verifies that the module's directory exists and is valid before
     * trying to load the module, so as to display a useful warning in the
     * log without displaying a stack trace from later failures (such as 
     * failed attempts to load classes from the module).
     *
     * If jsr77 is true then corresponding jsr77 mBeans will be created
     * during load time.
     *
     * @param    jsr77  create jsr77 mbeans if true
     */
    boolean load(boolean jsr77) {
        return (verifyModuleDirectory() && doLoad(jsr77));
    }
    
    /**
     * Loads a deployed application or stand alone module.
     * If jsr77 is true then corresponding jsr77 mBeans will be created
     * during load time.
     *
     * @param    jsr77  create jsr77 mbeans if true
     */
    abstract boolean doLoad(boolean jsr77);

    /**
     * Unloads a deployed application or stand alone module.
     * If jsr77 is true then corresponding jsr77 mBeans will be deleted
     * during unload time.
     *
     * @param    jsr77  delete jsr77 mbeans if true
     */
    abstract boolean unload(boolean jsr77);

    /**
     * Create jsr77 root mBean
     * The root mBean corresponds to either an application
     * or stand-alone module depending on j2ee type
     */
    abstract void createRootMBean () throws MBeanException;

    /**
     * Delete jsr77 root mBean
     * The root mBean corresponds to either an application
     * or stand-alone module depending on j2ee type
     */
    abstract void deleteRootMBean () throws MBeanException;

    /**
     * Create jsr77 mBeans for components within root mBean
     * The leaf mBeans correspond to the child objects of rootMBean.
     * For an ejb stand-alone module it will be ejbs
     * and for a web stand-alone module it will be servlets
     * and for a connector stand-alone module it will be resource adapters
     */
    abstract void createLeafMBeans () throws MBeanException;

    /**
     * Delete jsr77 mBeans for components within root mBean
     * The leaf mBeans correspond to the child objects of rootMBean.
     * For an ejb stand-alone module it will be ejbs
     * and for a web stand-alone module it will be servlets
     * and for a connector stand-alone module it will be resource adapters
     */
    abstract void deleteLeafMBeans () throws MBeanException;

    /**
     * Create jsr77 mBean for the leaf object
     * The leaf mBean correspond to the child object of rootMBean.
     * For an ejb stand-alone module it will be ejb
     * and for a web stand-alone module it will be servlet
     * and for a connector stand-alone module it will be resource adapter
     *
     * @param    descriptor  descriptor object for the leaf element
     */
    abstract void createLeafMBean (Descriptor descriptor) throws MBeanException;

    /**
     * Delete jsr77 mBean for the leaf object
     * The leaf mBean correspond to the child object of rootMBean.
     * For an ejb stand-alone module it will be ejb
     * and for a web stand-alone module it will be servlet
     * and for a connector stand-alone module it will be resource adapter
     */
    abstract void deleteLeafMBean (Descriptor descriptor) throws MBeanException;

    /**
     * Delete jsr77 mBeans for the root and its' components
     */
    abstract void deleteLeafAndRootMBeans () throws MBeanException;

    /**
     * Set state for jsr77 root mBean
     * The state can be set for a root mBean and it could be
     * one of STARTING, RUNNING, STOPPING, STOPPED, or FAILED
     *
     * @param    state  state of the module or application
     */
    abstract void setState(int state) throws MBeanException;

    /**
     * Returns the ejb class loader used by this loader. 
     *
     * @return    the ejb class loader
     */
    ClassLoader getClassLoader() {
        return this.ejbClassLoader;
    }

    /**
     * Returns the deployment descriptor object used by this loader.
     * 
     * @return    the deployment descriptor object
     */
    Application getApplication() {
        return this.application;
    }
    
    /**
     * Helps garbage collector by assigning member variables to null.
     * This is called from unload.
     * 
     * @see #unload
     */
    protected void done() {

        // releases resources (file handles, etc) in the class loader
        if (this.ejbClassLoader instanceof EJBClassLoader) {
            ((EJBClassLoader) ejbClassLoader).done();
        }
        
        // set connector descriptor classloaders to null
        Set rars = this.application.getRarDescriptors();
        for (Iterator itr = rars.iterator(); itr.hasNext();) {
            ConnectorDescriptor cd = (ConnectorDescriptor) itr.next();
            cd.setClassLoader(null);
        }
        
        this.id                 = null;
        this.parentClassLoader  = null;
        this.application        = null;
        this.ejbClassLoader     = null;
        this.registry           = null;
        this.configManager      = null;
    }

    /**
     * Unloads all the ejb bean containers.
     * If jsr77 is true then corresponding jsr77 mBeans will be deleted
     * during unload time.
     *
     * @param    jsr77  delete jsr77 mBeans if true
     * @return   true   if unloaded successfully
     */
    protected boolean unloadEjbs(boolean jsr77) {

        boolean result = true;

        if (this.application == null) {
            result = false;
        } else {

            // undeploy ejb module
            Vector beanDescriptors  = this.application.getEjbDescriptors();
            Enumeration e           = beanDescriptors.elements(); 

            while ( e.hasMoreElements() ) {
                EjbDescriptor nextDescriptor = (EjbDescriptor) e.nextElement();

		notifyEjbEvent(EjbContainerEvent.BEFORE_EJB_CONTAINER_UNLOAD,
		    nextDescriptor);

                // removes the unique id from the registry
                this.registry.removeUniqueId( nextDescriptor.getUniqueId() );

                try {
                    // removes the bean container from the application registry
                    Container container = (Container) this.registry.
                            removeDescriptor2Container(nextDescriptor);
                    if (container != null) {
                        container.undeploy();
                    }

                } catch (Exception beanEx) {
                    result = false;
                    _logger.log(Level.WARNING,
                        "loader.unexpected_error_while_unloading_ejb_container",
                        beanEx);
                }

		notifyEjbEvent(EjbContainerEvent.AFTER_EJB_CONTAINER_UNLOAD,
		    nextDescriptor);

                if (jsr77) {
                          try {
                            deleteLeafMBean(nextDescriptor);
                          } catch (MBeanException mbe) {
                              _logger.log(Level.WARNING, 
                              "loader.delete_ejb_mbean_exception", mbe);
                          }
                }
            }

            if (!application.isVirtual()) {
                // removes the class loader from the application registry
                this.registry.removeAppId2ClassLoader(this.id);
            } else {
                this.registry.removeModuleId2ClassLoader(this.id);
            }

            // removes the descriptor object from the application registry
            this.registry.removeClassLoader2Application(this.ejbClassLoader);

        }

        _logger.log(Level.FINE,"[AbstractLoader] Unload EJB(s) Status: "
                    + result);

        return result;
    }

    /**
     * Creates the bean containers for all ejbs.
     * If jsr77 is true then corresponding jsr77 mBeans will be created
     * during load time.
     *
     * @return    true if all modules deployed successfully
     */
    protected boolean loadEjbs(boolean jsr77) {

        // application.getApplicationArchivist().getClassLoader();
        final ClassLoader loader = this.ejbClassLoader;

        if (this.application == null) {
            return false;
        }
        
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, 
                        "[AbstractLoader] Id: " + this.id +
                        " Setting thread context class loader to: " + loader);
        }

        /*
         *5003242 - Save current context class loader as it is changed.  Restored in finally block of next try.
        */
        ClassLoader savedContextClassLoader = Utility.setContextClassLoader(loader);

      try {
        if (!application.isVirtual()) {
            // adds the ejb class loader to the repository
            this.registry.addAppId2ClassLoader(this.id, this.ejbClassLoader);
        } else {
            this.registry.addModuleId2ClassLoader(this.id, this.ejbClassLoader);
        }

        // adds the deployment descriptor obj to the repository
        this.registry.addClassLoader2Application(this.ejbClassLoader, 
                                                 this.application);

        HashSet myContainers = new HashSet();

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "[AbstractLoader] Id: " + this.id 
                        + " Unique Id: " + this.application.getUniqueId());
        }
       
        Vector beanDescriptors = application.getEjbDescriptors();

        // create EJB containers for each EJB
        Enumeration e = beanDescriptors.elements(); 
        ContainerFactory cf = Switch.getSwitch().getContainerFactory();

        while (e.hasMoreElements()) {            
            EjbDescriptor nextDescriptor = (EjbDescriptor) e.nextElement();

            // detects any unique id collisions
            if ( !this.registry.isUnique( nextDescriptor.getUniqueId() ) ) {

                _logger.log(Level.SEVERE, "loader.duplicate_unique_id",
                            new Object[] {
                              String.valueOf(nextDescriptor.getUniqueId()),
                              nextDescriptor.getName()
                            });

                // unload all the ejbs
                unloadEjbs(jsr77);

                // abort loading
                return false;
            }

            try {
		if(ffsmf == null){
		    ffsmf = FactoryForSecurityManagerFactoryImpl.getInstance();
		}
		SecurityManagerFactory smf = ffsmf.getSecurityManagerFactory("ejb");
		SecurityManager sm = smf.createSecurityManager(nextDescriptor);
                // create the bean container
                Container container = null;
                try {
		    notifyEjbEvent(EjbContainerEvent.BEFORE_EJB_CONTAINER_LOAD,
			nextDescriptor);

                    container = cf.createContainer(nextDescriptor, loader,
					sm, dynamicConfigContext);

		    notifyEjbEvent(EjbContainerEvent.AFTER_EJB_CONTAINER_LOAD,
			nextDescriptor);
                } catch (NameAlreadyBoundException jndiEx) {

                    // logs a message
                    _logger.log(Level.SEVERE, "loader.jndi_name_conflict",
                                new Object[] {
                                    this.id, 
                                    nextDescriptor.getJndiName(),
                                    nextDescriptor.getName()
                                });

                    // logs the actual exception
                    _logger.log(Level.SEVERE,
                        "loader.naming_exception_in_createcontainer", jndiEx);

                    // remove all loaded ejbs
                    unloadEjbs(jsr77);

                    // abort loading
                    return false;
                }

                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, 
                                "Created container with uinque id: " 
                                + nextDescriptor.getUniqueId());
                }

                // adds the container to the repository
                this.registry.addDescriptor2Container(nextDescriptor,container);

                // adds the container to the temp local table
                myContainers.add(container);

            } catch (Exception beanEx) {
                _logger.log(Level.SEVERE,
                    "loader.unexpected_error_while_creating_ejb_container",
                    beanEx);

                // remove all loaded ejbs
                unloadEjbs(jsr77);

                return false;
            }

            if (jsr77) {
              try {
                createLeafMBean(nextDescriptor);
              } catch (MBeanException mbe) {
                _logger.log(Level.WARNING, "loader.create_ejb_mbean_exception", mbe);
              }
            }
        }

	try {
            Enumeration en = beanDescriptors.elements();
            // generate policy files for applications with ejbs not otherwise
            while (en.hasMoreElements()) {            
                EjbDescriptor nextDescriptor = (EjbDescriptor)en.nextElement();
                SecurityUtil.generatePolicyFile(
                    EJBSecurityManager.getContextID(nextDescriptor));
            }
	} catch (Exception ge) {
            _logger.log(Level.SEVERE, 
			"code.could_not_generate/load_the_policy_files_for_system_app",
			ge);

	    // remove all loaded ejbs
	    unloadEjbs(jsr77);

	    return false;
	}
 
        // notify ejb containers that application deployment succeeded.
        Iterator iter = myContainers.iterator();
        while ( iter.hasNext() ) {
            Container c = (Container) iter.next();
            c.doAfterApplicationDeploy();
            //IASRI 4717059 BEGIN
            /*
            if (robFlag) {
                if ( c instanceof EntityContainer) {
                    ( (EntityContainer) c).setROBNotifier(robNotifier);
                }
            }
            */
            //IASRI 4717059 END
        }

        // diagnostics of all the roles and acls 
        com.sun.enterprise.security.Audit.showACL(this.application);

        // log that ejbs were loaded
        if (this.application.getEjbComponentCount() > 0) {
            _logger.log(Level.INFO, "loader.ejbs_loaded", this.id);
        }

        _logger.log(Level.FINE,"[AbstractLoader] loadEjbs completed");

        return true;
      } finally {
            /*
             *5003242 - Restore saved context class loader.
             */
            Utility.setContextClassLoader(savedContextClassLoader);
      }
            
    }    

    // ---- START OF IASRI 4666595 ---------------------------------------
    
    /**
     * Unloads all the rar.
     * If jsr77 is true then corresponding jsr77 mBeans will be deleted
     * during unload time.
     *
     * @param    jsr77  delete jsr77 mBeans if true
     * @return   true if unloaded successfully
     */
    protected boolean unloadRars(boolean jsr77) {
        boolean result = true;
        // undeploy rar module
        try {
            Set rarsDescriptors  = this.application.getRarDescriptors();
            ConnectorRuntime connectorRuntime = ConnectorRuntime.getRuntime();
        
            for (Iterator itr = rarsDescriptors.iterator(); itr.hasNext();) {
                ConnectorDescriptor cd = (ConnectorDescriptor) itr.next();
                String rarName = cd.getDeployName();
                String jndiName = this.id+
                         ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER+
                         FileUtils.makeFriendlyFilenameNoExtension(rarName);
                connectorRuntime.destroyActiveResourceAdapter(jndiName,cascade);
                if (jsr77) {
                     deleteLeafMBean(cd);
                }
            }
        } catch (Exception rarEx) {
            result = false;
            _logger.log(Level.WARNING,
                        "loader.application_loader_exception", rarEx);
        } 

        _logger.log(Level.FINE,"[AbstractLoader] Unload RAR(s) Status: " 
                    + result);

        return result;
    }

    
    /**
     * Load the rars of that application
     * If jsr77 is true then corresponding jsr77 mBeans will be created
     * during load time.
     *
     * @param    jsr77  delete jsr77 mBeans if true
     * @return    true if all modules deployed successfully
     */
    protected boolean loadRars(boolean jsr77) {
        
        try {

            String appLocation = this.configManager.getLocation(this.id);
            Set rars = application.getRarDescriptors();
            ConnectorRuntime connectorRuntime = ConnectorRuntime.getRuntime();

            for (Iterator itr = rars.iterator(); itr.hasNext();) {
                ConnectorDescriptor cd = (ConnectorDescriptor) itr.next();
                String rarName = cd.getDeployName();
                String location = DeploymentUtils.getEmbeddedModulePath(
                    appLocation, rarName);
                String jndiName = this.id+
                       ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER+
                       FileUtils.makeFriendlyFilenameNoExtension(rarName);
                connectorRuntime.createActiveResourceAdapter(
                              cd,jndiName,location);
                if (jsr77) {
                    createLeafMBean(cd);
                }
            }
            // now load resources associated with these embedded RARs
            try {
                ConnectorResourcesLoader crl = new ConnectorResourcesLoader();
                crl.loadEmbeddedRarResources(id,getApplication());
            } catch (ConfigException cex) {
                _logger.log(Level.SEVERE, "loader.connector_resource_initialization_error", cex);   
            }
        } catch (Exception rarEx) {
            _logger.log(Level.WARNING,
                        "loader.application_loader_exception", rarEx);
        }
        _logger.log(Level.FINE,"[AbstractLoader] loadRars completed");
        return true;
    }
 
    protected void setCascade(boolean cascade) {
        this.cascade=cascade;
    }

    protected boolean getCascade() {
        return this.cascade;
    }

    protected int getLoadUnloadAction(){
        return this.loadUnloadAction;
    }

    protected void setLoadUnloadAction(int loadUnloadAction) {
        this.loadUnloadAction = loadUnloadAction;
    }

    // ---- END OF IASRI 4666595 ----------------------------------------

    /**
     * Loads all the Web Service Management MBeans.
     * If jsr77 is true then corresponding jsr77 mBeans will be created
     * during load time.
     *
     * @param    jsr77  create jsr77 mBeans if true
     * @return   true   if unloaded successfully
     */
    protected boolean loadWebserviceEndpoints(boolean jsr77) {

        boolean result = true;

        if (this.application == null) {
            result = false;
        } else {

            Set bundleSet = new HashSet();
            bundleSet.addAll(this.application.getEjbBundleDescriptors());
            bundleSet.addAll(this.application.getWebBundleDescriptors());

            // unload web service endpoint
            Iterator itr  = bundleSet.iterator();

            while ( itr.hasNext() ) {
                BundleDescriptor nextDescriptor = (BundleDescriptor) 
                        itr.next();

                try {
                    WSMonitorLifeCycleFactory.getInstance().
                    getWSMonitorLifeCycleProvider().
                    registerWebServiceEndpoints(
                    this.application.getRegistrationName(), nextDescriptor);
                } catch (Exception e) {
                  _logger.log(Level.WARNING, 
                  "loader.register_ws_endpoint_error", 
                  this.application.getRegistrationName());
                  _logger.log(Level.WARNING, 
                  "loader.register_ws_endpoint_exception", e);
                   result = false;
                }

                /*
                if (jsr77) {
                      try {
                            createLeafMBeans(nextDescriptor);
                          } catch (MBeanException mbe) {
                                mbe.printStackTrace();
                              _logger.log(Level.WARNING, 
                              "loader.create_ejb_mbean_exception", mbe);
                          }
                }
                */

                _logger.log(Level.FINE,
                 "[AbstractLoader] Unload Web Service Endpoint(s) Status: " + 
                 result);
            }
        }

        return result;
    }

    /**
     * Unloads all the Web Service Management MBeans.
     * If jsr77 is true then corresponding jsr77 mBeans will be deleted
     * during unload time.
     *
     * @param    jsr77  delete jsr77 mBeans if true
     * @return   true   if unloaded successfully
     */
    protected boolean unloadWebserviceEndpoints(boolean jsr77) {

        boolean result = true;

        if (this.application == null) {
            result = false;
        } else {

            // unload web service endpoint
            Set bundleSet = new HashSet();
            bundleSet.addAll(this.application.getEjbBundleDescriptors());
            bundleSet.addAll(this.application.getWebBundleDescriptors());
            Iterator itr  = bundleSet.iterator();

            while ( itr.hasNext() ) {
                BundleDescriptor nextDescriptor = (BundleDescriptor) itr.next();

                try {
                    WSMonitorLifeCycleFactory.getInstance().
                    getWSMonitorLifeCycleProvider().
                    unregisterWebServiceEndpoints(
                    this.application.getRegistrationName(), nextDescriptor);
                } catch (Exception e) {
                  _logger.log(Level.WARNING, 
                  "loader.unregister_ws_endpoint_error", 
                  this.application.getRegistrationName());
                  _logger.log(Level.WARNING, 
                  "loader.register_ws_endpoint_exception", e);
                   result = false;
                }

                /*
                if (jsr77) {
                    try {
                        deleteLeafMBeans(nextDescriptor);
                    } catch (MBeanException mbe) {
                                mbe.printStackTrace();
                        _logger.log(Level.WARNING, 
                        "loader.delete_ejb_mbean_exception", mbe);
                    }
                }
                */

                _logger.log(Level.FINE,
                "[AbstractLoader] Unload Web Service Endpoint(s) Status: " + 
                result);
            }
        }

        return result;
    }

    public void generateEntryContext(Object context) {
         ServerEntryHelper.generateAppLoaderEntryContext((Descriptor) context);
    }

    protected void notifyAppEvent(int eventType) {
	ApplicationEvent event = new ApplicationEvent(eventType,
		getApplication(), getClassLoader(), dynamicConfigContext);
        generateEntryContext(getApplication());
	loaderEventNotifier.notifyListeners(event);
    }

    protected void notifyEjbEvent(int eventType, EjbDescriptor desc) {
	EjbContainerEvent event = new EjbContainerEvent(eventType,
		desc, getClassLoader());
        generateEntryContext(desc);
	loaderEventNotifier.notifyListeners(event);
    }

    /**
     * Loads all the EJB 3.0 persistence entities bundled in this application.
     */
    protected boolean loadPersistenceUnits() {
        try{
            new PersistenceUnitLoaderImpl().load(new ApplicationInfoImpl());
            return true;
        }catch(Exception ge){
            _logger.log(Level.WARNING, ge.getMessage(), ge);
            return false;
        }
    }

    /**
     * Unloads all the EJB 3.0 persistence entities bundled in this application.
     */
    protected boolean unloadPersistenceUnits() {
        try{
            new PersistenceUnitLoaderImpl().unload(new ApplicationInfoImpl());
            return true;
        }catch(Exception ge){
            _logger.log(Level.WARNING, ge.getMessage(), ge);
            return false;
        }
    }

    private class ApplicationInfoImpl
            implements PersistenceUnitLoader.ApplicationInfo {
        public Application getApplication() {
            return application;
        }

        public InstrumentableClassLoader getClassLoader() {
            return InstrumentableClassLoader.class.cast(ejbClassLoader);
        }

        public String getApplicationLocation() {
            try {
                return configManager.getLocation(id);
            } catch (ConfigException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * This method is used to find out the precise list of PUs that are
         * referenced by the components of this application. The components
         * include EJB and Web components, but not appclients as appclients are
         * loaded seperately by ACC.
         *
         */
        public Collection<? extends PersistenceUnitDescriptor>
                    getReferencedPUs() {
            Collection<PersistenceUnitDescriptor> pus =
                    new HashSet<PersistenceUnitDescriptor>();
            final boolean fineMsgLoggable = _logger.isLoggable(Level.FINE);
            for (BundleDescriptor bundle : getEjbAndWebBundles()) {
                if(fineMsgLoggable) {
                    _logger.fine("Finding PUs referenced by module called " + // NOI18N
                            bundle.getModuleDescriptor().getArchiveUri());
                }
                pus.addAll(bundle.findReferencedPUs());
            }
            if(fineMsgLoggable) {
                _logger.fine("Total number of PUs referenced by this app is : " // NOI18N
                        + pus.size());
            }
            return pus;
        }

        /**
         * It returns the EntityManagerFactories that needs to be closed.
         * It searches in the Application structure to build the list of EMFs.
         * This implementation does not search appclients.
         */
        public Collection<? extends EntityManagerFactory> getEntityManagerFactories() {
            Collection<EntityManagerFactory> emfs =
                    new HashSet<EntityManagerFactory>();
            emfs.addAll(application.getEntityManagerFactories()); // ear level PUs

            for (BundleDescriptor bundle : getEjbAndWebBundles()) {
                emfs.addAll(bundle.getEntityManagerFactories());
            }
            // no need to look for EMFs in appclients as they are used in ACC.
            return emfs;
        }

        /**
         * It returns EJBs & Wars only.
         */
        private Collection<BundleDescriptor> getEjbAndWebBundles() {
            Collection<BundleDescriptor> bundles =
                    new HashSet<BundleDescriptor>();
            bundles.addAll((Collection<BundleDescriptor>)
                    application.getEjbBundleDescriptors());
            bundles.addAll((Collection<BundleDescriptor>)
                    application.getWebBundleDescriptors());
            return bundles;
        }
    }
}
